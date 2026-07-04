package local.wifiadmin.persistence;

import local.wifiadmin.api.model.EncryptionType;
import local.wifiadmin.api.model.WifiBand;
import local.wifiadmin.api.model.WifiConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(JdbcWifiConfigurationRepository.class)
class JdbcWifiConfigurationRepositoryTest {

    @Autowired
    private WifiConfigurationRepository repository;

    @Test
    void savesAndFindsConfiguration() {
        WifiConfiguration configuration = new WifiConfiguration("CPE_001", WifiBand._2_4_GHZ, "Office-2G")
                .encryptionType(EncryptionType.WPA2_PSK)
                .password("seed-wifi-01");

        repository.save(configuration);

        Optional<WifiConfiguration> actual = repository.findByCpeId("CPE_001");

        assertThat(actual).contains(configuration);
    }

    @Test
    void updatesExistingConfiguration() {
        repository.save(new WifiConfiguration("CPE_001", WifiBand._2_4_GHZ, "Office-2G")
                .encryptionType(EncryptionType.WPA2_PSK)
                .password("seed-wifi-01"));

        WifiConfiguration updated = new WifiConfiguration("CPE_001", WifiBand._5_GHZ, "Office-5G")
                .encryptionType(EncryptionType.WPA3_SAE)
                .password("new-password");

        repository.save(updated);

        assertThat(repository.findByCpeId("CPE_001")).contains(updated);
    }

    @Test
    void returnsEmptyWhenConfigurationDoesNotExist() {
        assertThat(repository.findByCpeId("UNKNOWN")).isEmpty();
    }
}
