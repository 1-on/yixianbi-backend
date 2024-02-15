package com.yixian.springbootinit.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "yixian.jwt")
@Data
public class JwtProperties {
    private String secretKey;
    private long ttl;
    private String tokenName;
}
