package ru.webinnovations.messagesender;

import jakarta.mail.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Properties;

@EnableScheduling
@Configuration
@PropertySource("classpath:application.properties")
@Getter
@RequiredArgsConstructor
public class EmailConfig {

    private final Environment env;

    @Bean
    public Session getSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", env.getProperty("email.server"));
        props.put("mail.smtp.port", env.getProperty("email.port"));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");

        return Session.getInstance(props, new jakarta.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(env.getProperty("email.sender"), env.getProperty("email.password"));
            }
        });
    }
}
