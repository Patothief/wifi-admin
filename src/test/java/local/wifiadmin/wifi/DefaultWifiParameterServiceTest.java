package local.wifiadmin.wifi;

import local.wifiadmin.api.model.EncryptionType;
import local.wifiadmin.api.model.WifiBand;
import local.wifiadmin.api.model.WifiConfiguration;
import local.wifiadmin.error.BadRequestException;
import local.wifiadmin.platform.WifiPlatformClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultWifiParameterServiceTest {

    private final WifiPlatformClient platformClient = mock(WifiPlatformClient.class);
    private final DefaultWifiParameterService service = new DefaultWifiParameterService(
            platformClient,
            new WifiParameterValidator()
    );

    @Test
    void delegatesGetToPlatformClient() {
        WifiConfiguration expected = new WifiConfiguration("CPE_001", WifiBand._2_4_GHZ, "Office-2G")
                .encryptionType(EncryptionType.WPA2_PSK)
                .password("seed-wifi-01");

        when(platformClient.getWifiConfiguration("CPE_001")).thenReturn(expected);

        WifiConfiguration actual = service.get("CPE_001");

        assertThat(actual).isSameAs(expected);
        verify(platformClient).getWifiConfiguration("CPE_001");
    }

    @Test
    void delegatesUpdateToPlatformClient() {
        WifiConfiguration requested = new WifiConfiguration("CPE_001", WifiBand._5_GHZ, "Office-5G")
                .encryptionType(EncryptionType.WPA3_SAE)
                .password("new-password");

        when(platformClient.updateWifiConfiguration(requested)).thenReturn(requested);

        WifiConfiguration actual = service.update(requested);

        assertThat(actual).isSameAs(requested);
        verify(platformClient).updateWifiConfiguration(requested);
    }

    @Test
    void rejectsBlankCpeIdForGet() {
        assertThatThrownBy(() -> service.get(" "))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("cpeId must not be blank");

        verifyNoInteractions(platformClient);
    }

    @Test
    void rejectsMissingRequestBodyForUpdate() {
        assertThatThrownBy(() -> service.update(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Request body is required");

        verifyNoInteractions(platformClient);
    }

    @Test
    void rejectsBlankCpeIdForUpdate() {
        WifiConfiguration configuration = new WifiConfiguration(" ", WifiBand._2_4_GHZ, "Office-2G");

        assertThatThrownBy(() -> service.update(configuration))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("cpeId must not be blank");

        verifyNoInteractions(platformClient);
    }

    @Test
    void rejectsMissingWifiBandForUpdate() {
        WifiConfiguration configuration = new WifiConfiguration()
                .cpeId("CPE_001")
                .ssid("Office-2G");

        assertThatThrownBy(() -> service.update(configuration))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("wifiBand is required");

        verifyNoInteractions(platformClient);
    }

    @Test
    void rejectsBlankSsidForUpdate() {
        WifiConfiguration configuration = new WifiConfiguration("CPE_001", WifiBand._2_4_GHZ, " ");

        assertThatThrownBy(() -> service.update(configuration))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("ssid must not be blank");

        verifyNoInteractions(platformClient);
    }

    @Test
    void rejectsMissingPasswordForSecuredNetwork() {
        WifiConfiguration configuration = new WifiConfiguration("CPE_001", WifiBand._2_4_GHZ, "Office-2G")
                .encryptionType(EncryptionType.WPA2_PSK);

        assertThatThrownBy(() -> service.update(configuration))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("password is required when encryptionType is WPA2_PSK");

        verifyNoInteractions(platformClient);
    }

    @Test
    void acceptsMissingPasswordForOpenNetwork() {
        WifiConfiguration configuration = new WifiConfiguration("CPE_003", WifiBand._2_4_GHZ, "Guest-2G")
                .encryptionType(EncryptionType.OPEN);

        when(platformClient.updateWifiConfiguration(configuration)).thenReturn(configuration);

        WifiConfiguration actual = service.update(configuration);

        assertThat(actual).isSameAs(configuration);
        verify(platformClient).updateWifiConfiguration(configuration);
    }

    @Test
    void treatsMissingEncryptionTypeAsOpenForValidation() {
        WifiConfiguration configuration = new WifiConfiguration("CPE_003", WifiBand._2_4_GHZ, "Guest-2G");

        when(platformClient.updateWifiConfiguration(configuration)).thenReturn(configuration);

        WifiConfiguration actual = service.update(configuration);

        assertThat(actual).isSameAs(configuration);
        verify(platformClient).updateWifiConfiguration(configuration);
    }
}
