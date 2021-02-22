package fmi.unibuc.ro.mycloudapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fmi.unibuc.ro.mycloudapi.properties.FileStorageProperties;
import fmi.unibuc.ro.mycloudapi.properties.JwtProperties;
import fmi.unibuc.ro.mycloudapi.properties.KubernetesProperties;
import fmi.unibuc.ro.mycloudapi.security.jwt.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

@SpringBootApplication
@EnableConfigurationProperties({
        FileStorageProperties.class,
        JwtProperties.class,
        KubernetesProperties.class
})
@Slf4j
public class MyCloudApplication implements CommandLineRunner {

    @Autowired
    private KubernetesProperties kubernetesProperties;

    public static void main(String[] args) {
        SpringApplication.run(MyCloudApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println();
        kubernetesProperties.printAllProperties();
    }
}
