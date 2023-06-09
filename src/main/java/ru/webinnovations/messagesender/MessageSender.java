package ru.webinnovations.messagesender;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@PropertySource("classpath:application.properties")
@RequiredArgsConstructor
@EnableScheduling
@Component
public class MessageSender {

    private final Session session;
    private final Environment env;
    private static final AtomicInteger messageNumber = new AtomicInteger(0);

    @Scheduled(fixedRate = 25000)
    public void sendMessage() {
        try {
            Message message = new MimeMessage(session);

            File folder = new File(Objects.requireNonNull(env.getProperty("folder_path")));
            File[] fileList = folder.listFiles();
            File selectedFile;
            if (fileList != null && fileList.length != 0) {
                selectedFile = fileList[0];
            }
            else {
                log.info("Files to sent are over");
                return;
            }

            message.setFrom(new InternetAddress(Objects.requireNonNull(env.getProperty("email.sender"))));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(Objects.requireNonNull(env.getProperty("email.recipient"))));
            message.setSubject(selectedFile.getName());

            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("80020");

            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.attachFile(selectedFile);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);
            Transport.send(message);
            log.info("The message №{} successfully has been sent {}", messageNumber.addAndGet(1), LocalDateTime.now());
            Files.deleteIfExists(Path.of(selectedFile.getPath()));
            log.trace("The file successfully has been deleted");
        } catch (MessagingException e) {
            log.error("An error occurred while sending the message: {}", e.getMessage());
            throw new MessageProcessException(e.getMessage());
        } catch (IOException e) {
            log.error("File upload failed: {}", e.getMessage());
            throw new FileProcessException(e.getMessage());
        }
    }
}
