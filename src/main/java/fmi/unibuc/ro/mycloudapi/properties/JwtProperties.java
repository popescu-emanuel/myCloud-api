package fmi.unibuc.ro.mycloudapi.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "cloud.app.jwt")
public class JwtProperties {
    private String secret;
    private int expirationMs;
}
