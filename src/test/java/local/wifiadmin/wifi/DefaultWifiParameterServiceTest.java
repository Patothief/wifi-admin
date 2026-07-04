package local.wifiadmin.wifi;

import local.wifiadmin.api.model.EncryptionType;
import local.wifiadmin.api.model.WifiBand;
import local.wifiadmin.api.model.WifiConfiguration;
import local.wifiadmin.platform.WifiPlatformClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultWifiParameterServiceTest {

    private final WifiPlatformClient platformClient = mock(WifiPlatformClient.class);
    private final DefaultWifiParameterService service = new DefaultWifiParameterService(platformClient);

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
}
