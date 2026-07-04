package local.wifiadmin.platform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class WifiPlatformPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PropertiesTestConfiguration.class);

    @Test
    void bindsValidProperties() {
        contextRunner
                .withPropertyValues(
                        "wifi-platform.url=http://localhost:8080/platform",
                        "wifi-platform.connect-timeout=2s",
                        "wifi-platform.read-timeout=5s"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(WifiPlatformProperties.class);
                    WifiPlatformProperties properties = context.getBean(WifiPlatformProperties.class);
                    assertThat(properties.url()).isEqualTo("http://localhost:8080/platform");
                    assertThat(properties.connectTimeout()).hasSeconds(2);
                    assertThat(properties.readTimeout()).hasSeconds(5);
                });
    }

    @Test
    void rejectsBlankUrl() {
        contextRunner
                .withPropertyValues(
                        "wifi-platform.url=",
                        "wifi-platform.connect-timeout=2s",
                        "wifi-platform.read-timeout=5s"
                )
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void rejectsZeroTimeout() {
        contextRunner
                .withPropertyValues(
                        "wifi-platform.url=http://localhost:8080/platform",
                        "wifi-platform.connect-timeout=0ms",
                        "wifi-platform.read-timeout=5s"
                )
                .run(context -> assertThat(context).hasFailed());
    }

    @EnableConfigurationProperties(WifiPlatformProperties.class)
    static class PropertiesTestConfiguration {
    }
}
