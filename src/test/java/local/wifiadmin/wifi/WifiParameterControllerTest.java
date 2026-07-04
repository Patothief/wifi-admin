package local.wifiadmin.wifi;

import com.fasterxml.jackson.databind.ObjectMapper;
import local.wifiadmin.api.model.EncryptionType;
import local.wifiadmin.api.model.WifiBand;
import local.wifiadmin.api.model.WifiConfiguration;
import local.wifiadmin.error.BadRequestException;
import local.wifiadmin.error.PlatformCommunicationException;
import local.wifiadmin.error.PlatformNotFoundException;
import local.wifiadmin.error.WifiConfigurationNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WifiParameterController.class)
class WifiParameterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WifiParameterService service;

    @Test
    void getWifiParameterReturnsConfiguration() throws Exception {
        when(service.get("CPE_001")).thenReturn(configuration("CPE_001"));

        mockMvc.perform(get("/wifi-parameter/{cpeId}", "CPE_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpeId").value("CPE_001"))
                .andExpect(jsonPath("$.wifiBand").value("BAND_2_4_GHZ"))
                .andExpect(jsonPath("$.ssid").value("Office-2G"))
                .andExpect(jsonPath("$.encryptionType").value("WPA2_PSK"))
                .andExpect(jsonPath("$.password").value("seed-wifi-01"));

        verify(service).get("CPE_001");
    }

    @Test
    void putWifiParameterReturnsUpdatedConfiguration() throws Exception {
        WifiConfiguration request = configuration("CPE_001").ssid("Office-Updated");
        when(service.update(any(WifiConfiguration.class))).thenReturn(request);

        mockMvc.perform(put("/wifi-parameter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpeId").value("CPE_001"))
                .andExpect(jsonPath("$.ssid").value("Office-Updated"));

        verify(service).update(any(WifiConfiguration.class));
    }

    @Test
    void mapsValidationFailureToBadRequestBody() throws Exception {
        when(service.get(" ")).thenThrow(new BadRequestException("cpeId must not be blank"));

        mockMvc.perform(get("/wifi-parameter/{cpeId}", " "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("cpeId must not be blank"));
    }

    @Test
    void mapsPlatformNotFoundToNotFoundBody() throws Exception {
        when(service.get("UNKNOWN")).thenThrow(new PlatformNotFoundException("CPE not found on platform"));

        mockMvc.perform(get("/wifi-parameter/{cpeId}", "UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("CPE not found on platform"));
    }

    @Test
    void mapsDatabaseNotFoundToNotFoundBody() throws Exception {
        when(service.get("UNKNOWN")).thenThrow(
                new WifiConfigurationNotFoundException("WiFi configuration not found in database for cpeId UNKNOWN")
        );

        mockMvc.perform(get("/wifi-parameter/{cpeId}", "UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("WiFi configuration not found in database for cpeId UNKNOWN"));
    }

    @Test
    void mapsPlatformCommunicationFailureToBadGatewayBody() throws Exception {
        when(service.get("CPE_001")).thenThrow(
                new PlatformCommunicationException("Failed to communicate with SOAP platform")
        );

        mockMvc.perform(get("/wifi-parameter/{cpeId}", "CPE_001"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("BAD_GATEWAY"))
                .andExpect(jsonPath("$.message").value("Failed to communicate with SOAP platform"));
    }

    @Test
    void mapsMalformedJsonToBadRequestBody() throws Exception {
        mockMvc.perform(put("/wifi-parameter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Request body is malformed or contains unsupported values"));
    }

    private WifiConfiguration configuration(String cpeId) {
        return new WifiConfiguration(cpeId, WifiBand._2_4_GHZ, "Office-2G")
                .encryptionType(EncryptionType.WPA2_PSK)
                .password("seed-wifi-01");
    }
}
