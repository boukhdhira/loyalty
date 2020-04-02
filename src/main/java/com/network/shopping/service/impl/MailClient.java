package com.network.shopping.service.impl;

import com.network.shopping.common.property.MailProperties;
import com.network.shopping.service.dto.MailRequest;
import com.network.shopping.service.utils.MailContentBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.NonNull;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.network.shopping.config.Constants.ACTIVATION_KEY;
import static io.micrometer.core.instrument.util.StringUtils.isBlank;
import static java.util.Objects.isNull;
import static org.springframework.util.CollectionUtils.isEmpty;

@Service
@Slf4j
public class MailClient {

    private final JavaMailSender mailSender;

    private final MailContentBuilder mailContentBuilder;

    private final MailProperties mailProperties;

    private final ResourceLoader resourceLoader;

    @Autowired
    public MailClient(JavaMailSender mailSender, MailContentBuilder mailContentBuilder
            , MailProperties mailProperties, ResourceLoader resourceLoader) {
        this.mailSender = mailSender;
        this.mailContentBuilder = mailContentBuilder;
        this.mailProperties = mailProperties;
        this.resourceLoader = resourceLoader;
    }

    @Async
    public void sendActivation(@NonNull MailRequest mailRequest) throws MessagingException, IOException {

        if (isNull(mailRequest.getRecipient())) {
            log.error("recipient address cannot be empty ");
            throw new IOException("Recipient address is empty");
        }
        String activationKey = (String) mailRequest.getProps().get(ACTIVATION_KEY);
        if (isBlank(activationKey)) {
            log.error("Activation key is not generated for id= {} ", mailRequest.getRecipient());
            throw new IOException("Activation key is not generated");
        }

        String mailContent = this.mailContentBuilder.build(mailRequest, this.mailProperties.getActivationTemplate(),
                this.mailProperties.getBaseUrl());
        this.prepareAndSendWithTemplate(mailRequest.getRecipient()
                , mailRequest.getCc(),
                this.mailProperties.getActivationSubject()
                , mailContent
                , new HashMap<>());
    }

    private void prepareAndSendWithTemplate(String to, List<String> cc, String subject, String content,
                                            Map<String, String> attachments) throws MessagingException {
        MimeMessage message = this.mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name());
        helper.setTo(to);
        if (!isEmpty(cc)) {
            helper.setCc(cc.toArray(new String[20]));
        }
        helper.setText(content, true);
        helper.setSubject(subject);
        helper.setFrom(this.mailProperties.getSender());
        attachments.forEach((key, value) -> {
            try {
                helper.addAttachment(key, new ClassPathResource(value));
            } catch (MessagingException e) {
                log.warn("Resource not found: {}", e.getMessage());
            }
        });
        this.mailSender.send(message);
    }

    /**
     * Add the inline image, referenced from the HTML code as "cid:${imageResourceName}"
     *
     * @param helper      message
     * @param image       image name
     * @param contentType image type. example: MediaType.IMAGE_PNG_VALUE
     * @param contentId   image template identifier into cid tag
     */
    private void addImageToMail(MimeMessageHelper helper, String image, String contentType, String contentId) throws MessagingException {
        helper.addInline(contentId, new ClassPathResource(this.mailProperties.getResourcesPath().concat(image)), contentType);
        //MediaType.IMAGE_PNG_VALUE
    }

    /**
     * Send emails with attachment file
     *
     * @param helper           message
     * @param pathToAttachment file attachment path
     * @throws MessagingException when file it not found or does'nt have access privilege.
     */
    private void addAttachment(MimeMessageHelper helper, String pathToAttachment) throws MessagingException {
        FileSystemResource file
                = new FileSystemResource(new File(pathToAttachment));
        helper.addAttachment(file.getFilename(), file);
    }

}
