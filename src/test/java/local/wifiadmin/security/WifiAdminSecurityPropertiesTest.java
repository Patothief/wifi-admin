package local.wifiadmin.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class WifiAdminSecurityPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PropertiesTestConfiguration.class);

    @Test
    void bindsDisabledSecurityWithDefaultHeaderName() {
        contextRunner
                .withPropertyValues("wifi-admin.security.enabled=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(WifiAdminSecurityProperties.class);
                    WifiAdminSecurityProperties properties = context.getBean(WifiAdminSecurityProperties.class);
                    assertThat(properties.enabled()).isFalse();
                    assertThat(properties.headerName()).isEqualTo("X-API-Key");
                });
    }

    @Test
    void bindsEnabledSecurityWithApiKey() {
        contextRunner
                .withPropertyValues(
                        "wifi-admin.security.enabled=true",
                        "wifi-admin.security.header-name=X-Internal-Key",
                        "wifi-admin.security.api-key=test-secret"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(WifiAdminSecurityProperties.class);
                    WifiAdminSecurityProperties properties = context.getBean(WifiAdminSecurityProperties.class);
                    assertThat(properties.enabled()).isTrue();
                    assertThat(properties.headerName()).isEqualTo("X-Internal-Key");
                    assertThat(properties.apiKey()).isEqualTo("test-secret");
                });
    }

    @Test
    void rejectsEnabledSecurityWithoutApiKey() {
        contextRunner
                .withPropertyValues(
                        "wifi-admin.security.enabled=true",
                        "wifi-admin.security.api-key="
                )
                .run(context -> assertThat(context).hasFailed());
    }

    @EnableConfigurationProperties(WifiAdminSecurityProperties.class)
    static class PropertiesTestConfiguration {
    }
}
