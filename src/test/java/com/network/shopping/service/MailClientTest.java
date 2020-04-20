package com.network.shopping.service;

import com.icegreen.greenmail.util.GreenMailUtil;
import com.network.shopping.common.property.MailProperties;
import com.network.shopping.config.SmtpServerRule;
import com.network.shopping.dto.MailRequest;
import com.network.shopping.service.impl.MailClient;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Collections;

import static com.network.shopping.config.Constants.ACTIVATION_KEY;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class MailClientTest {

    public static final String RECIPIENT_ADDRESS = "recipient.address@gmail.com";
    @Rule
    public SmtpServerRule smtpServerRule = new SmtpServerRule(2525);
    @Autowired
    private MailClient mailClient;
    @Autowired
    private MailProperties properties;

    @Test
    public void shouldSendActivationMail() throws Exception {
        //when
        final MailRequest request = new MailRequest();
        request.setRecipient(RECIPIENT_ADDRESS);
        final String key = randomAlphabetic(10);
        request.setProps(Collections.singletonMap(ACTIVATION_KEY, key));
        this.mailClient.prepareAndSendActivation(request);
        //then
        this.assertReceivedMessageContains(key);
    }

    private void assertReceivedMessageContains(final String expected) throws IOException, MessagingException {
        final MimeMessage[] receivedMessages = this.smtpServerRule.getMessages();
        assertEquals(1, receivedMessages.length);
        final MimeMessage currentMimeMessage = receivedMessages[0];
        final String content = GreenMailUtil.getBody(currentMimeMessage).replaceAll("=\r?\n", "");
        assertEquals(this.properties.getActivationSubject(), currentMimeMessage.getSubject());
        assertEquals(1, currentMimeMessage.getAllRecipients().length);
        assertEquals(RECIPIENT_ADDRESS, currentMimeMessage.getAllRecipients()[0].toString());
        assertTrue(content.contains(expected));
    }
}
