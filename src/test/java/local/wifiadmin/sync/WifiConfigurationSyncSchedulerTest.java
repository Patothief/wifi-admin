package local.wifiadmin.sync;

import local.wifiadmin.api.model.EncryptionType;
import local.wifiadmin.api.model.WifiBand;
import local.wifiadmin.api.model.WifiConfiguration;
import local.wifiadmin.persistence.WifiConfigurationRepository;
import local.wifiadmin.platform.WifiPlatformClient;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WifiConfigurationSyncSchedulerTest {

    private final WifiPlatformClient platformClient = mock(WifiPlatformClient.class);
    private final WifiConfigurationRepository repository = mock(WifiConfigurationRepository.class);

    @Test
    void synchronizesConfiguredNumberOfCpes() {
        WifiConfiguration first = configuration("CPE_001");
        WifiConfiguration second = configuration("CPE_002");
        WifiConfiguration third = configuration("CPE_003");
        when(platformClient.getWifiConfiguration("CPE_001")).thenReturn(first);
        when(platformClient.getWifiConfiguration("CPE_002")).thenReturn(second);
        when(platformClient.getWifiConfiguration("CPE_003")).thenReturn(third);

        scheduler(3).synchronizeConfiguredCpes();

        verify(platformClient).getWifiConfiguration("CPE_001");
        verify(platformClient).getWifiConfiguration("CPE_002");
        verify(platformClient).getWifiConfiguration("CPE_003");
        verify(repository).save(first);
        verify(repository).save(second);
        verify(repository).save(third);
    }

    @Test
    void continuesSyncWhenOneCpeFails() {
        WifiConfiguration first = configuration("CPE_001");
        WifiConfiguration third = configuration("CPE_003");
        when(platformClient.getWifiConfiguration("CPE_001")).thenReturn(first);
        when(platformClient.getWifiConfiguration("CPE_002")).thenThrow(new RuntimeException("platform unavailable"));
        when(platformClient.getWifiConfiguration("CPE_003")).thenReturn(third);

        scheduler(3).synchronizeConfiguredCpes();

        verify(platformClient).getWifiConfiguration("CPE_001");
        verify(platformClient).getWifiConfiguration("CPE_002");
        verify(platformClient).getWifiConfiguration("CPE_003");
        verify(repository).save(first);
        verify(repository).save(third);
    }

    private WifiConfigurationSyncScheduler scheduler(int cpeCount) {
        return new WifiConfigurationSyncScheduler(
                platformClient,
                repository,
                new WifiSyncProperties("0 0 2 * * *", cpeCount)
        );
    }

    private WifiConfiguration configuration(String cpeId) {
        return new WifiConfiguration(cpeId, WifiBand._2_4_GHZ, "Office")
                .encryptionType(EncryptionType.WPA2_PSK)
                .password("seed-wifi-01");
    }
}
