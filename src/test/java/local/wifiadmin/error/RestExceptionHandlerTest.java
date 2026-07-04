package local.wifiadmin.error;

import local.wifiadmin.api.model.ErrorBody;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class RestExceptionHandlerTest {

    private final RestExceptionHandler handler = new RestExceptionHandler();

    @Test
    void mapsPlatformNotFoundToNotFound() {
        ResponseEntity<ErrorBody> response = handler.handlePlatformNotFound(
                new PlatformNotFoundException("CPE not found on platform")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getCode()).isEqualTo("NOT_FOUND");
        assertThat(response.getBody().getMessage()).isEqualTo("CPE not found on platform");
    }

    @Test
    void mapsWifiConfigurationNotFoundToNotFound() {
        ResponseEntity<ErrorBody> response = handler.handleWifiConfigurationNotFound(
                new WifiConfigurationNotFoundException("WiFi configuration not found in database for cpeId UNKNOWN")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getCode()).isEqualTo("NOT_FOUND");
        assertThat(response.getBody().getMessage())
                .isEqualTo("WiFi configuration not found in database for cpeId UNKNOWN");
    }

    @Test
    void mapsPlatformCommunicationToBadGateway() {
        ResponseEntity<ErrorBody> response = handler.handlePlatformCommunication(
                new PlatformCommunicationException("Failed to communicate with SOAP platform")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody().getCode()).isEqualTo("BAD_GATEWAY");
        assertThat(response.getBody().getMessage()).isEqualTo("Failed to communicate with SOAP platform");
    }
}
