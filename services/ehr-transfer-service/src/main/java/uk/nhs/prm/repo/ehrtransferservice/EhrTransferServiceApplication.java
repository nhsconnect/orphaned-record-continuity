package uk.nhs.prm.repo.ehrtransferservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EhrTransferServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EhrTransferServiceApplication.class, args);
    }

}