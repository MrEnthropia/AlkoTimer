package ru.MrEntropy.AlkoBot.AlkoTimer.Service;

import com.vdurmont.emoji.EmojiParser;
import jdk.jfr.Timestamp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.MrEntropy.AlkoBot.AlkoTimer.Config.BotConfig;
import ru.MrEntropy.AlkoBot.AlkoTimer.Model.GenderEnum;
import ru.MrEntropy.AlkoBot.AlkoTimer.Model.User;
import ru.MrEntropy.AlkoBot.AlkoTimer.DAO.UserRepository;

import java.sql.Time;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    //Переменная конфигурации

    final BotConfig config;

    @Autowired
    UserRepository userRepository;



    //Методы конфигурации
    @Override
    public String getBotUsername() { return config.getBotName();}

    @Override
    public String getBotToken(){return config.getToken();}
    //Текстовые константы
    final String START_TEXT =EmojiParser.parseToUnicode("Бар АлкоТайм снова открыт!!! :tada: :dancers: :tada:") ;
    final String HELLO_TEXT = EmojiParser.parseToUnicode("""
            Приветствуем вас в баре АлкоТайм, чат-боте с функцией расчёта и контроля времени выведения алкоголя из
            вашего организма.
            Если вы у нас впервые, нажмите кнопку на клавитуре, и мы вас зарегистрируем нашим гостем. Если вы уже
            записаны у нас, позовите бармена, и он вам нальёт.
            Это на данный момент pre-alfa версия бота, вам пока доступен ограниченный выбор напитков
            и расчёты выведения алкоголя в пределах отдельных порций. Ждите новых, обновлений.""");

    final String SORRY_TEXT= EmojiParser.parseToUnicode(
            "Извините, я вас не понял :sweat_smile:. Либо вам уже хватит, либо я просто не распознаю эту команду");
    final String SIGN_TEXT = """
            Сию минуту. Для регистрации в нашем баре, вам не нужно писать имя, фамилию, или указывать ваши персональные данные.
            Мы гарантируем вам полную конфиденциальность. Что бы стать нашим гостем, вам нужно указать ваш возраст, пол,
            вес, и рост. Для этого вам необходимо написать все эти параметры по следующему шаблону:
            Незабудка;м/ж;22;333
            Незабудка- наш тайный пароль для регистрации. Хотя для вас уже не тайный)
            м/ж- ваш пол(мужской или женский)
            22-ваш возраст. ВНИМАНИЕ: в наш бар допускаются только совершеннолетние посетители. Если вам меньше возраста
            совершеннолетия, мы вынуждены будем заблокировать вам доступ к нашему боту
            333-ваш вес в кг,
            Пример заполнения анкеты: Незабудка;ж;18;70""";


    static final String BEER = "BEER";
    static final String WINE = "WINE";
    static final String WHISKY = "WHISKY";
    static final String COCKTAIL = "COCKTAIL";
    static final String OTHER = "OTHER";

    DecimalFormat decimalFormat = new DecimalFormat("#.##");



    //Конструктор класса
    public TelegramBot(BotConfig config) {
        this.config = config;
        //Лист с командами
        List<BotCommand> commandList = new ArrayList<>();

        //Добавление команд в список
        commandList.add(new BotCommand("/start","Время открытия нашего бара"));
        commandList.add(new BotCommand("/hello","Скажите нам привет"));
        commandList.add(new BotCommand("/help","Чего то не понимаете? Мы всё расскажем!"));
        commandList.add(new BotCommand("/settings","Желаете что то изменить или узнать?"));

        //Добавление списка в бот
        try {
            this.execute(new SetMyCommands(commandList, new BotCommandScopeDefault(),null));
        } catch (TelegramApiException e) {
            log.error("Error occurred: "+e.getMessage());
        }
    }


    //Метод-ответчик
    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()){
            String messageText = update.getMessage().getText().toLowerCase(Locale.ROOT);
            long chatId = update.getMessage().getChatId();

            if (messageText.contains("бармен")){
                barmenAnswer(chatId,update.getMessage().getChat().getFirstName());


            } else if (messageText.contains("налей")){
                chooseDrink(chatId);


            } else if (messageText.contains("запиши")){
                if (!userRepository.existsById(chatId)) {
                    prepareAndSendMessage(chatId, SIGN_TEXT);
                }else {prepareAndSendMessage(chatId,
                        "Вы уже записаны. Позовите бармена, что бы он смог вам налить");}

            } else if (messageText.contains("незабудка")){
                createGuest(update);

            } else {switch (messageText){
                case "/start": prepareAndSendMessage(chatId,START_TEXT);
                    break;

                case "/hello": prepareAndSendMessage(chatId,HELLO_TEXT);
                    break;

                default: prepareAndSendMessage(chatId,SORRY_TEXT);
            }}


        }else if (update.hasCallbackQuery()){
            String callBackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            switch (callBackData){
                case BEER -> alcoCalcuiator(chatId,BEER);
//                        prepareAndSendMessage(chatId,
//                        "Вы выпили пинту пива (0,6 л). Время выведения из организма: 4 ч.24 мин");
                case WINE -> alcoCalcuiator(chatId, WINE);
//                        prepareAndSendMessage(chatId,
//                        "Вы выпили бокал вина (250 мл). Время выведения из организма: 5 ч.6 мин");
                case WHISKY -> alcoCalcuiator(chatId, WHISKY);
//                        prepareAndSendMessage(chatId,
//                        "Вы выпили стакан виски (30 мл). Время выведения из организма: 2 ч.18 мин");
                case COCKTAIL ->
                        prepareAndSendMessage(chatId,
                        "Список коктелей в разработке. Ждите обновлений");
                case OTHER ->
                        prepareAndSendMessage(chatId,
                        "Список других напитков в разработке. Ждите обновлений");
            }
        }

    }

    //Методы обработки сообщений
    //Отправка
    public void executeMessage( SendMessage messageText){
        try {
            execute(messageText);
        } catch (TelegramApiException e) {
            log.error("Error occurred: "+e.getMessage());
        }
    }
    //Подготовка и настройка
    public void prepareAndSendMessage(Long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);
        executeMessage(message);

    }

    //Метод вызова бармена
    public void barmenAnswer(long chatId, String name){

        String answer = EmojiParser.parseToUnicode("Здравствуйте, "+name+"."+":grin:"+"Чем могу быть полезен?");

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(answer);

        if (!userRepository.existsById(chatId)) {
            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
            KeyboardButton sign = new KeyboardButton("Запиши меня гостем, пожалуйста");


            List<KeyboardRow> keyboardRows = new ArrayList<>();
            KeyboardRow row1 = new KeyboardRow();
            row1.add(sign);

            keyboardRows.add(row1);
            keyboardMarkup.setKeyboard(keyboardRows);
            message.setReplyMarkup(keyboardMarkup);

            executeMessage(message);
        } else {
            sendMessage(chatId,answer);

        }
    }

    //Добавление клавиатуры
    public void sendMessage(long chatId, String messageText){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);

        //Конструктор клавиатуры
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        KeyboardButton pour = new KeyboardButton("Налей мне выпить, пожалуйста");
        KeyboardButton status = new KeyboardButton("Проверь моё состояние");
        KeyboardButton reset = new KeyboardButton("Обнули моё состояние");

        //Ряд всей клавиатуры
        List<KeyboardRow> keyboardRows = new ArrayList<>();


        //Первый ряд кнопок
        KeyboardRow row1 = new KeyboardRow();

        row1.add(pour);
        row1.add(status);
        row1.add(reset);

        keyboardRows.add(row1);
        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);

        executeMessage(message);
    }

    public void chooseDrink(Long chatId){

        SendMessage message = new SendMessage();

        message.setText("Сию минуту. Что желаете выпить на это раз?");
        message.setChatId(chatId);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

       List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();


        InlineKeyboardButton beer = new InlineKeyboardButton(
                EmojiParser.parseToUnicode("Кружку пива "+":beer:"));
        beer.setCallbackData(BEER);
        InlineKeyboardButton wine = new InlineKeyboardButton(
                EmojiParser.parseToUnicode("Бокал вина "+":wine_glass:"));
        wine.setCallbackData(WINE);
        InlineKeyboardButton whisky = new InlineKeyboardButton(
                EmojiParser.parseToUnicode("Стакан виски "+":tumbler_glass:"));
        whisky.setCallbackData(WHISKY);
        InlineKeyboardButton coctaile = new InlineKeyboardButton(
                EmojiParser.parseToUnicode("Сделай коктейль "+":cocktail:"));
        coctaile.setCallbackData(COCKTAIL);
        InlineKeyboardButton other = new InlineKeyboardButton(
                EmojiParser.parseToUnicode("Другое "+":sake:"));
        other.setCallbackData(OTHER);

        row1.add(beer);
        row1.add(wine);
        row2.add(whisky);
        row2.add(coctaile);
        row3.add(other);


        keyboardRows.add(row1);
        keyboardRows.add(row2);
        keyboardRows.add(row3);
        inlineKeyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(inlineKeyboardMarkup);
        executeMessage(message);

    }

    private void createGuest(Update update){

        String messageText = update.getMessage().getText().toLowerCase(Locale.ROOT);
        long chatId = update.getMessage().getChatId();
        Chat chat = update.getMessage().getChat();

                String userName = chat.getUserName();

                String[] strings = messageText.split(";");
                if (strings.length<4){
                    prepareAndSendMessage(chatId, "Упс, кажется вы ввели не все ваши данные. Попробуйте ещё раз");
                }else {
                    String gender = strings[1];
                    long age = Integer.parseInt(strings[2]);
                    long weight = Integer.parseInt(strings[3]);
                    if (age < 0 || weight < 50||age>100||weight>200){
                        prepareAndSendMessage(chatId,"Упс, кажется вы ввели некорректные данные. Попробуйте ещё раз");
                    } else{ if (!userRepository.existsById(chatId) && age >= 18) {
                        registerUser(chatId, userName, gender, age, weight);
                    } else if (userRepository.existsById(chatId) && age >= 18) {
                        updateUser(chatId, userName, gender, age, weight);
                    } else if (age < 18) {
                        prepareAndSendMessage(chatId,
                                "Извините, мы не можем записать вас нашим гостем. Ваш возраст меньше возраста совершеннолетия");
                        }
                    }

                }
    }

    //Crud-методы
    private void registerUser(long chatId, String userName, String gender, long age, long weight){
        User user = new User();

        user.setChatId(chatId);
        user.setUserName(userName);
        user.setAge(age);
        user.setWeight((double) weight);

        switch (gender){
            case "м"->user.setGender(GenderEnum.MALE);
            case "ж"->user.setGender(GenderEnum.FEMALE);
            //TODO:Добавить ошибку
        }
        userRepository.save(user);
        log.info("New user was registered: "+user.toString());
        prepareAndSendMessage(chatId,"Всё. Вы записаны. Добро пожаловать в клуб, "+userName);
    }

    private void updateUser(long chatId, String userName, String gender, long age, long weight) {
        User user = new User();


        user.setChatId(chatId);
        user.setUserName(userName);
        user.setAge(age);
        user.setWeight((double) weight);

        switch (gender) {
            case "м" -> user.setGender(GenderEnum.MALE);
            case "ж" -> user.setGender(GenderEnum.FEMALE);
            //TODO:Добавить ошибку
        }
        userRepository.deleteById(chatId);
        userRepository.save(user);
        log.info("Users data was update: "+user.toString());
        prepareAndSendMessage(chatId,"Готово. Ваша анкета была обновлена, "+userName);

    }
    //Метод рассчёта концентрации алкоголя в крови
    private double widmarksFormula(User user, double a){
        double w = user.getWeight();
        double r = 0;
        switch (user.getGender()){
            case MALE -> r=0.7;
            case FEMALE -> r=0.6;
            default -> r=0.65;
        }
        return a/(w*r);

    }
    //Метод рассчёта времени выведения алкоголя из организма
    private void alcoCalcuiator(long chatId, String drink){
        Optional<User> userOptional = userRepository.findById(chatId);
        User user = userOptional.get();
        String drinkName = null;
        double alchogol=0.0;

        switch (drink){
            case BEER -> {
                drinkName="пинту пива (0,6 л)";
                alchogol=37.44;
            }
            case WINE -> {
                drinkName="бокал вина (250 мл)";
                alchogol=44.865;
            }
            case WHISKY -> {
                drinkName="стакан виски (300 мл)";
                alchogol=168;
            }
        }

        double content = widmarksFormula(user,alchogol);
        double doubleTime = content/0.15;

        int hours = (int)doubleTime ; // часы

        double minute = 60*(doubleTime - hours); // минуты

        int minutes = (int)minute; // минуты

        prepareAndSendMessage(chatId,
                "Вы выпили "+ drinkName +". Время выведения из организма: "+hours+" часов "+minutes+" минут.");
    }



}
