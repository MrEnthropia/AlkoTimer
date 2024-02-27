package ru.MrEntropy.AlkoBot.AlkoTimer.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

@Component
@Slf4j
public class TimerManager {


    private Map <Long, TimerTask> timerTaskMap = new HashMap<>();



    public void addTask(Long chatId, TimerTask task){
        timerTaskMap.put(chatId,task);
    }
    public boolean checkTask(long chatId){
        return timerTaskMap.containsKey(chatId);
    }
    public TimerTask getTask(Long chatId){
        return timerTaskMap.get(chatId);
    }

    public void updateTask(Long chatId, TimerTask task){
        if (timerTaskMap.containsKey(chatId)){
            timerTaskMap.remove(chatId);
            timerTaskMap.put(chatId,task);
        }
    }
}
