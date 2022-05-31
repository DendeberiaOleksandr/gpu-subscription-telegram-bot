package com.example.demo;

import com.example.demo.persistance.Subscription;
import com.example.demo.persistance.SubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class Bot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final SubscriptionRepository subscriptionRepository;
    public final String CHAT_ID = "725986733";

    @Autowired
    public Bot(BotConfig botConfig,
               SubscriptionRepository subscriptionRepository) {
        this.botConfig = botConfig;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public String getBotUsername() {
        return this.botConfig.getUsername();
    }

    @Override
    public String getBotToken() {
        return this.botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage()){
            log.info("Get update: " + update);
            Message message = update.getMessage();
            String messageText = message.getText();

            if (messageText.matches("\\/s")){
                List<Subscription> subscriptions = subscriptionRepository.findAll();

                String msg = "";

                for(Subscription subscription: subscriptions){
                    msg += subscription.getId() + ") " + subscription.getName() + "\n";
                }

                SendMessage sendMessage = new SendMessage(CHAT_ID, msg);
                sendMessage(sendMessage);

            } else if (messageText.matches("\\/s \\-a .+")){
                String replaced = messageText.replaceAll("/s -a", "");

                Subscription subscription = new Subscription();
                subscription.setName(replaced);

                subscriptionRepository.save(subscription);
                SendMessage sendMessage = new SendMessage(CHAT_ID, "Запись сохранена!");
                sendMessage(sendMessage);
                log.info("Saved subscription: " + subscription);

            } else if (messageText.matches("\\/s \\-d [0-9]+")){
                String[] split = messageText.split(" ");
                if(split.length == 3){
                    String id = split[2];
                    long idL = Long.parseLong(id);
                    Optional<Subscription> subscription = subscriptionRepository.findById(idL);
                    subscription.ifPresent(s -> {
                        subscriptionRepository.delete(s);
                        SendMessage sendMessage = new SendMessage(CHAT_ID, "Запись удалена!");
                        sendMessage(sendMessage);
                        log.info("Deleted subscription: " + s);
                    });
                }
            }
        }
    }

    public void sendMessage(SendMessage sendMessage){
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Can't execute send message: " + sendMessage, e);
        }
    }
}
