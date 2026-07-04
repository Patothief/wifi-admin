package local.wifiadmin.wifi;

import com.fasterxml.jackson.databind.ObjectMapper;
import local.wifiadmin.api.model.EncryptionType;
import local.wifiadmin.api.model.WifiBand;
import local.wifiadmin.api.model.WifiConfiguration;
import local.wifiadmin.persistence.WifiConfigurationRepository;
import local.wifiadmin.platform.WifiPlatformClient;
import local.wifiadmin.security.WifiAdminSecurityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = WifiParameterController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        DefaultWifiParameterService.class,
        WifiParameterValidator.class,
        WifiParameterValidationMvcTest.SecurityTestConfiguration.class
})
class WifiParameterValidationMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WifiPlatformClient platformClient;

    @MockBean
    private WifiConfigurationRepository repository;

    @Test
    void putRejectsSecuredNetworkWithoutPasswordBeforeCallingPlatformOrDatabase() throws Exception {
        WifiConfiguration request = new WifiConfiguration("CPE_001", WifiBand._2_4_GHZ, "Office-2G")
                .encryptionType(EncryptionType.WPA2_PSK);

        mockMvc.perform(put("/wifi-parameter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("password is required when encryptionType is WPA2_PSK"));

        verifyNoInteractions(platformClient, repository);
    }

    @TestConfiguration
    static class SecurityTestConfiguration {

        @Bean
        WifiAdminSecurityProperties wifiAdminSecurityProperties() {
            return new WifiAdminSecurityProperties(false, "X-API-Key", null);
        }
    }
}
