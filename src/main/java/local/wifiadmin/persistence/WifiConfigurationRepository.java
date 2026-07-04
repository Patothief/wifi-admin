package local.wifiadmin.persistence;

import local.wifiadmin.api.model.WifiConfiguration;

import java.util.Optional;

public interface WifiConfigurationRepository {

    Optional<WifiConfiguration> findByCpeId(String cpeId);

    WifiConfiguration save(WifiConfiguration configuration);
}
