package local.wifiadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class WifiAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(WifiAdminApplication.class, args);
    }
}
