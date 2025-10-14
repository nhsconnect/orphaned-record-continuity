package uk.nhs.prm.repo.re_registration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class ReRegistrationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReRegistrationServiceApplication.class, args);
	}
}
