package com.network.shopping.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtConfigProperties {
    String secret;
    String tokenValidity;
    String rememberTokenValidity;
}
