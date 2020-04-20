package com.network.shopping.service.impl;

import com.network.shopping.common.property.MailProperties;
import com.network.shopping.dto.MailRequest;
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
    public MailClient(final JavaMailSender mailSender, final MailContentBuilder mailContentBuilder
            , final MailProperties mailProperties, final ResourceLoader resourceLoader) {
        this.mailSender = mailSender;
        this.mailContentBuilder = mailContentBuilder;
        this.mailProperties = mailProperties;
        this.resourceLoader = resourceLoader;
    }

    @Async
    public void prepareAndSendActivation(@NonNull final MailRequest mailRequest) throws MessagingException, IOException {
        final String recipientAddress = mailRequest.getRecipient();
        if (isNull(recipientAddress)) {
            log.error("recipient address cannot be empty ");
            throw new IOException("Recipient address is empty");
        }
        final String activationKey = (String) mailRequest.getProps().get(ACTIVATION_KEY);
        if (isBlank(activationKey)) {
            log.error("Activation key is not generated for id= {} ", recipientAddress);
            throw new IOException("Activation key is not generated");
        }

        final String mailContent = this.mailContentBuilder.build(mailRequest, this.mailProperties.getActivationTemplate(),
                this.mailProperties.getBaseUrl());
        this.prepareAndSendWithTemplate(recipientAddress
                , mailRequest.getCc(),
                this.mailProperties.getActivationSubject()
                , mailContent
                , new HashMap<>());
        log.debug("Activation mail was sent to {}", recipientAddress);
    }

    @Async
    public void prepareAndSendBonus(@NonNull final MailRequest mailRequest) throws MessagingException, IOException {
        final String recipientAddress = mailRequest.getRecipient();
        if (isNull(recipientAddress)) {
            log.error("recipient address cannot be empty ");
            throw new IOException("Recipient address is empty");
        }

        final String mailContent = this.mailContentBuilder.build(mailRequest, this.mailProperties.getBonusTemplate(),
                this.mailProperties.getBaseUrl());
        this.prepareAndSendWithTemplate(recipientAddress
                , mailRequest.getCc(),
                this.mailProperties.getBonusSubject()
                , mailContent
                , new HashMap<>());
        log.debug("Bonus notification mail was sent to {}", recipientAddress);
    }

    private void prepareAndSendWithTemplate(final String to, final List<String> cc, final String subject, final String content,
                                            final Map<String, String> attachments) throws MessagingException {
        final MimeMessage message = this.mailSender.createMimeMessage();
        final MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
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
            } catch (final MessagingException e) {
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
    private void addImageToMail(final MimeMessageHelper helper, final String image, final String contentType, final String contentId) throws MessagingException {
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
    private void addAttachment(final MimeMessageHelper helper, final String pathToAttachment) throws MessagingException {
        final FileSystemResource file
                = new FileSystemResource(new File(pathToAttachment));
        helper.addAttachment(file.getFilename(), file);
    }
}
