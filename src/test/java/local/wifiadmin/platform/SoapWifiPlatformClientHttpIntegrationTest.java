package local.wifiadmin.platform;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import jakarta.xml.ws.BindingProvider;
import local.wifiadmin.api.model.EncryptionType;
import local.wifiadmin.api.model.WifiBand;
import local.wifiadmin.api.model.WifiConfiguration;
import local.wifiadmin.error.PlatformNotFoundException;
import local.wifiadmin.platform.soap.WifiPlatformPortType;
import local.wifiadmin.platform.soap.WifiPlatformService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoapWifiPlatformClientHttpIntegrationTest {

    private final AtomicReference<String> lastSoapAction = new AtomicReference<>();
    private final AtomicReference<String> lastRequestBody = new AtomicReference<>();

    private HttpServer server;
    private SoapWifiPlatformClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/platform", this::handlePlatformRequest);
        server.start();

        WifiPlatformPortType port = new WifiPlatformService().getWifiPlatformPort();
        ((BindingProvider) port).getRequestContext().put(
                BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                "http://localhost:" + server.getAddress().getPort() + "/platform"
        );
        client = new SoapWifiPlatformClient(port, new SoapWifiPlatformMapper());
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void getCallsSoapPlatformAndMapsResponse() {
        WifiConfiguration configuration = client.getWifiConfiguration("CPE_001");

        assertThat(configuration.getCpeId()).isEqualTo("CPE_001");
        assertThat(configuration.getWifiBand()).isEqualTo(WifiBand._2_4_GHZ);
        assertThat(configuration.getSsid()).isEqualTo("Office-2G");
        assertThat(configuration.getEncryptionType()).isEqualTo(EncryptionType.WPA2_PSK);
        assertThat(configuration.getPassword()).isEqualTo("seed-wifi-01");
        assertThat(lastSoapAction.get()).contains("getCpeID");
        assertThat(lastRequestBody.get()).contains("GetCpeIdRequest");
        assertThat(lastRequestBody.get()).contains("CPE_001");
    }

    @Test
    void updateCallsSoapPlatformAndMapsResponse() {
        WifiConfiguration request = new WifiConfiguration("CPE_001", WifiBand._5_GHZ, "Office-5G")
                .encryptionType(EncryptionType.WPA3_SAE)
                .password("new-password");

        WifiConfiguration configuration = client.updateWifiConfiguration(request);

        assertThat(configuration.getCpeId()).isEqualTo("CPE_001");
        assertThat(configuration.getWifiBand()).isEqualTo(WifiBand._5_GHZ);
        assertThat(configuration.getSsid()).isEqualTo("Office-5G");
        assertThat(configuration.getEncryptionType()).isEqualTo(EncryptionType.WPA3_SAE);
        assertThat(configuration.getPassword()).isEqualTo("new-password");
        assertThat(lastSoapAction.get()).contains("updateCpeId");
        assertThat(lastRequestBody.get()).contains("UpdateCpeIdRequest");
        assertThat(lastRequestBody.get()).contains("Office-5G");
    }

    @Test
    void soapFaultMapsToPlatformNotFound() {
        assertThatThrownBy(() -> client.getWifiConfiguration("UNKNOWN"))
                .isInstanceOf(PlatformNotFoundException.class)
                .hasMessage("CPE not found on platform");
    }

    private void handlePlatformRequest(HttpExchange exchange) throws IOException {
        lastSoapAction.set(exchange.getRequestHeaders().getFirst("SOAPAction"));
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        lastRequestBody.set(requestBody);

        String responseBody;
        if (requestBody.contains("UNKNOWN")) {
            responseBody = notFoundFault();
        } else if (requestBody.contains("UpdateCpeIdRequest")) {
            responseBody = updateResponse();
        } else {
            responseBody = getResponse();
        }

        byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/xml; charset=utf-8");
        exchange.sendResponseHeaders(200, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.close();
    }

    private String getResponse() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tns="http://wifi-admin.local/platform/v1">
                  <soap:Body>
                    <tns:GetCpeIdResponse>
                      <tns:configuration>
                        <tns:cpeId>CPE_001</tns:cpeId>
                        <tns:wifiBand>BAND_2_4_GHZ</tns:wifiBand>
                        <tns:ssid>Office-2G</tns:ssid>
                        <tns:encryptionType>WPA2_PSK</tns:encryptionType>
                        <tns:password>seed-wifi-01</tns:password>
                      </tns:configuration>
                    </tns:GetCpeIdResponse>
                  </soap:Body>
                </soap:Envelope>
                """;
    }

    private String updateResponse() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tns="http://wifi-admin.local/platform/v1">
                  <soap:Body>
                    <tns:UpdateCpeIdResponse>
                      <tns:configuration>
                        <tns:cpeId>CPE_001</tns:cpeId>
                        <tns:wifiBand>BAND_5_GHZ</tns:wifiBand>
                        <tns:ssid>Office-5G</tns:ssid>
                        <tns:encryptionType>WPA3_SAE</tns:encryptionType>
                        <tns:password>new-password</tns:password>
                      </tns:configuration>
                    </tns:UpdateCpeIdResponse>
                  </soap:Body>
                </soap:Envelope>
                """;
    }

    private String notFoundFault() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <soap:Fault>
                      <faultcode>tns:NotFound</faultcode>
                      <faultstring>CPE not found</faultstring>
                    </soap:Fault>
                  </soap:Body>
                </soap:Envelope>
                """;
    }
}
