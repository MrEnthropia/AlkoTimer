package ru.MrEntropy.AlkoBot.AlkoTimer.Service;

import org.springframework.stereotype.Component;


import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

@Component
public class TimerService{

    private HashMap<Long, Timer> timerMap = new HashMap<>();

    public void addTimer(Long chatId){
        Timer timer = new Timer();
        timerMap.put(chatId,timer);
    }
    public Timer getTimer(Long chatId){
        return timerMap.get(chatId);
    }

    public void removeTimer(Long chatId){
        timerMap.remove(chatId);
    }

    public void reloadTimer(Long chatId){
        timerMap.get(chatId).cancel();
        Timer timer = new Timer();
        timerMap.put(chatId, timer);
    }

    public void startTimer(TimerTask task, Long chatId, Double contentLevel){
        if (contentLevel==0.0 && !timerMap.containsKey(chatId)){
            Timer timer = new Timer();
            timer.schedule(task,30000,30000);
            //TODO: Метод в тестовом режиме
            timerMap.put(chatId,timer);
            System.out.println("Таймер пользователя: "+chatId+" запущен.");
            System.out.println(timerMap);
        }
    }
    public void endTimer(Long chatId){
        if (timerMap.containsKey(chatId)) {
            timerMap.get(chatId).cancel();
            timerMap.remove(chatId);
            System.out.println("Таймер пользователя: " + chatId + " остановлен");
            System.out.println(timerMap);
        }
    }

}
