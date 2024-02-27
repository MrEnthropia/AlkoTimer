package ru.MrEntropy.AlkoBot.AlkoTimer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.Time;
import java.time.LocalDateTime;

@Slf4j
@SpringBootApplication
public class AlkoTimerApplication {

	public static void main(String[] args) {

		SpringApplication.run(AlkoTimerApplication.class, args);
		log.info("Bot is run: "+ LocalDateTime.now());
	}

}
