package ru.MrEntropy.AlkoBot.AlkoTimer.Service;

import jakarta.persistence.MapKey;
import org.glassfish.grizzly.asyncqueue.TaskQueue;
import org.hibernate.mapping.Map;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.SortedMap;
import java.util.Timer;


public class TimerService{

    private HashMap<Long, Timer> timerMap = new HashMap<>();

    public void newTimer(Long chatId, Timer timer){

    }

}
