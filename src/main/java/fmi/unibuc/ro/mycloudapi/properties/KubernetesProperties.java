package fmi.unibuc.ro.mycloudapi.properties;


import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@Slf4j
@ConfigurationProperties(prefix = "kubernetes")
public class KubernetesProperties {
    private String angularService;
    private String host;
    private String uiPort;
    private String apiPort;

    public void printAllProperties(){
        log.info("Angular service address: {}", angularService);
        log.info("Kubernetes host address: {}", host);
        log.info("my-cloud api port: {}", apiPort);
        log.info("my-cloud ui  port: {}", uiPort);
    }

}
