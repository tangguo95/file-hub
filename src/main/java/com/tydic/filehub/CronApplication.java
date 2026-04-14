package com.tydic.filehub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.tydic.filehub.mapper.uoc")
@ConfigurationPropertiesScan
public class CronApplication {

    public static void main(String[] args) {
        SpringApplication.run(CronApplication.class, args);
    }
}
