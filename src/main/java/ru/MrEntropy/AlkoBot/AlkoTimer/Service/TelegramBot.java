package ru.MrEntropy.AlkoBot.AlkoTimer.Service;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    //Переменная конфигурации

    final BotConfig config;

    @Autowired
    TimerManager timerManager;


    //Методы конфигурации
    @Override
    public String getBotUsername() { return config.getBotName();}

    @Override
    public String getBotToken(){return config.getToken();}
    //Текстовые константы
    final String START_TEXT =EmojiParser.parseToUnicode("Бар АлкоТайм снова открыт!!! :tada: :dancers: :tada:") ;
    final String HELLO_TEXT = EmojiParser.parseToUnicode("""
            Приветствуем вас в баре АлкоТайм. Первый в мире телеграм-бар, где вы можете узнать,
            сколько вам осталось до протрезвления. Это первая тестовая версия нашего бара, так что ждите обновлений""") ;

    final String SORRY_TEXT= EmojiParser.parseToUnicode("Извините, я вас не понял :sweat_smile:. Либо вам уже хватит, либо я просто не распознаю эту команду") ;

    static final String BEER = "BEER";
    static final String WINE = "WINE";
    static final String WHISKY = "WHISKY";
    static final String COCTAILE = "COCTAILE";
    static final String OTHER = "OTHER";

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



            } else {switch (messageText){
                case "/start": prepareAndSendMessage(chatId,START_TEXT);
                    if (!timerManager.checkTask(chatId)) {

                        TimerTask timerTask = new TimerTask() {
                            @Override
                            public void run() {
                                prepareAndSendMessage(chatId,"Поздравляю, вы полностью протрезвели");
                            }
                        };
                        timerManager.addTask(chatId,timerTask);
                        System.out.println("Новый пользователь добавлен");
                        log.info("New user: "+chatId+", in "+ LocalDateTime.now());
                        }else {
                        System.out.println("Пользователь уже есть");
                    }

                    break;

                case "/hello": prepareAndSendMessage(chatId,HELLO_TEXT);
                    break;

                default: prepareAndSendMessage(chatId,SORRY_TEXT);
            }}


        }else if (update.hasCallbackQuery()){
            String callBackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            switch (callBackData){
                case BEER ->prepareAndSendMessage(chatId,
                        "Вы выпили пинту пива (0,6 л). Время выведения из организма: 4 ч.24 мин");
                case WINE -> prepareAndSendMessage(chatId,
                        "Вы выпили бокал вина (250 мл). Время выведения из организма: 5 ч.6 мин");
                case WHISKY -> prepareAndSendMessage(chatId,
                        "Вы выпили стакан виски (30 мл). Время выведения из организма: 2 ч.18 мин");
                case COCTAILE -> prepareAndSendMessage(chatId, "Какой коктейль желаете?");
                case OTHER -> prepareAndSendMessage(chatId, "Какой другой напиток предпочитаете?");
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
        sendMessage(chatId,answer);

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
        coctaile.setCallbackData(COCTAILE);
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








}
