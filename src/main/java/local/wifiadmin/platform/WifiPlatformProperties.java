package local.wifiadmin.platform;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "wifi-platform")
record WifiPlatformProperties(
        String url,
        Duration connectTimeout,
        Duration readTimeout
) {
}
