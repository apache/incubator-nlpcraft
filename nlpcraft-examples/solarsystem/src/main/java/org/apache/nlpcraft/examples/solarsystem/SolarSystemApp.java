package org.apache.nlpcraft.examples.solarsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SolarSystemApp {
    public static void main(String[] args) {
        SpringApplication.run(SolarSystemApp.class, args);
    }

    @Bean
    public SolarSystemProbe getSolarSystemProbe() {
        return new SolarSystemProbe();
    }
}
