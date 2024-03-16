package ru.MrEntropy.AlkoBot.AlkoTimer.Service;

import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class ContentLevelService {

    private HashMap<Long,Double> contentCountMap = new HashMap<>();

    public void addContentCount(Long chatId){
        contentCountMap.put(chatId,0.0);
    }

    public void contentLevelUp(Long chatId, double d) {
        //if (contentCountMap.containsKey(chatId)){
            contentCountMap.put(chatId,contentCountMap.get(chatId)+d);
        //}
    }
    public void contentLevelDown(Long chatId, double d){
        //if (contentCountMap.containsKey(chatId)){
            contentCountMap.put(chatId,contentCountMap.get(chatId)-d);
            if (contentCountMap.get(chatId)<0||contentCountMap.get(chatId)<0.001){
                contentCountMap.put(chatId,0.0);
            }
        //}
    }
    public Double getContentLevel(Long chatId){
        return contentCountMap.get(chatId);
    }
}

