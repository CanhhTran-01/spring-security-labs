package vn.edu.kma.securityjwtcustom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class SecurityJwtCustomApplication {

    public static void main(String[] args) {

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        SpringApplication.run(SecurityJwtCustomApplication.class, args);
    }
}
