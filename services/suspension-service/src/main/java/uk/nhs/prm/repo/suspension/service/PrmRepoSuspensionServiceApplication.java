package uk.nhs.prm.repo.suspension.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PrmRepoSuspensionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrmRepoSuspensionServiceApplication.class, args);
    }

}
