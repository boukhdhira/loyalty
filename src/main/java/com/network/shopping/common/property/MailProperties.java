package com.network.shopping.common.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.mail")
@Getter
@Setter
public final class MailProperties {
    String sender;
    String activationSubject;
    String activationTemplate;
    String baseUrl;
    String resourcesPath;
    String bonusSubject;
    String bonusTemplate;

    private MailProperties() {
    }
}
