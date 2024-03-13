package ru.MrEntropy.AlkoBot.AlkoTimer.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.bind.DefaultValue;


@Data
@Entity(name = "usersTable")
public class User{

    @Id
    @NotNull
    Long chatId;
    @NotNull
    String userName;
    @NotNull
    Long age;
    @NotNull
    @Enumerated(EnumType.STRING)
    GenderEnum gender;
    @NotNull
    Double weight;
    Double contentLevel;

    public void contentLevelUp(double d){
        this.contentLevel=contentLevel+d;
    }

}
