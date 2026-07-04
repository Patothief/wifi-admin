package local.wifiadmin.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wifi-admin.security")
public record WifiAdminSecurityProperties(
        boolean enabled,
        String headerName,
        String apiKey
) {

    private static final String DEFAULT_HEADER_NAME = "X-API-Key";

    public WifiAdminSecurityProperties {
        if (isBlank(headerName)) {
            headerName = DEFAULT_HEADER_NAME;
        }
        if (enabled && isBlank(apiKey)) {
            throw new IllegalArgumentException("wifi-admin.security.api-key must be configured when security is enabled");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
