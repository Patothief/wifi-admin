package local.wifiadmin.sync;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class WifiSyncPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PropertiesTestConfiguration.class);

    @Test
    void bindsValidProperties() {
        contextRunner
                .withPropertyValues(
                        "wifi-sync.cron=0 30 2 * * *",
                        "wifi-sync.cpe-count=12"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(WifiSyncProperties.class);
                    WifiSyncProperties properties = context.getBean(WifiSyncProperties.class);
                    assertThat(properties.cron()).isEqualTo("0 30 2 * * *");
                    assertThat(properties.cpeCount()).isEqualTo(12);
                });
    }

    @Test
    void rejectsBlankCron() {
        contextRunner
                .withPropertyValues(
                        "wifi-sync.cron=",
                        "wifi-sync.cpe-count=12"
                )
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void rejectsInvalidCron() {
        contextRunner
                .withPropertyValues(
                        "wifi-sync.cron=not-a-cron",
                        "wifi-sync.cpe-count=12"
                )
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void rejectsZeroCpeCount() {
        contextRunner
                .withPropertyValues(
                        "wifi-sync.cron=0 30 2 * * *",
                        "wifi-sync.cpe-count=0"
                )
                .run(context -> assertThat(context).hasFailed());
    }

    @EnableConfigurationProperties(WifiSyncProperties.class)
    static class PropertiesTestConfiguration {
    }
}
