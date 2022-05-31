package com.example.demo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "bot")
@ConstructorBinding
public class BotConfig {
    private final String token;
    private final String username;

    public BotConfig(String token,
                     String username) {
        this.token = token;
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }
}
