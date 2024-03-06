package ru.MrEntropy.AlkoBot.AlkoTimer.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Data;



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
    Double bodyMassIndex;
}
