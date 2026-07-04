package local.wifiadmin.platform;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@ConfigurationProperties(prefix = "wifi-platform")
@Validated
record WifiPlatformProperties(
        @NotBlank
        String url,

        @NotNull
        @DurationMin(millis = 1)
        Duration connectTimeout,

        @NotNull
        @DurationMin(millis = 1)
        Duration readTimeout
) {
}
