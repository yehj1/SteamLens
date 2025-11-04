package com.steamlens;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.gpr")
public class SteamLensApplication {

    public static void main(String[] args) {
        SpringApplication.run(SteamLensApplication.class, args);
    }
}
