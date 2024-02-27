package ru.MrEntropy.AlkoBot.AlkoTimer.Model;

import lombok.Data;

import java.util.Timer;

@Data
public class UserTimer extends Timer {

    long id;
    public UserTimer(long id){
        this.id = id;
    }
}
