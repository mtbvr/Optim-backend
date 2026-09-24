package com.opt.backend;

import com.opt.backend.common.config.CorsProperties;
import com.opt.backend.common.config.PixelWarsProperties;
import com.opt.backend.common.config.SecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({SecurityProperties.class, CorsProperties.class, PixelWarsProperties.class})
@EnableScheduling
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
