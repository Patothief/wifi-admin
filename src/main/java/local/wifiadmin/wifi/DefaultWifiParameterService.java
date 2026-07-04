package local.wifiadmin.wifi;

import local.wifiadmin.api.model.WifiConfiguration;
import local.wifiadmin.error.WifiConfigurationNotFoundException;
import local.wifiadmin.platform.WifiPlatformClient;
import local.wifiadmin.persistence.WifiConfigurationRepository;
import org.springframework.stereotype.Service;

@Service
class DefaultWifiParameterService implements WifiParameterService {

    private final WifiPlatformClient platformClient;
    private final WifiConfigurationRepository repository;
    private final WifiParameterValidator validator;

    DefaultWifiParameterService(
            WifiPlatformClient platformClient,
            WifiConfigurationRepository repository,
            WifiParameterValidator validator
    ) {
        this.platformClient = platformClient;
        this.repository = repository;
        this.validator = validator;
    }

    @Override
    public WifiConfiguration get(String cpeId) {
        validator.validateCpeId(cpeId);
        return repository.findByCpeId(cpeId)
                .orElseThrow(() -> new WifiConfigurationNotFoundException(
                        "WiFi configuration not found in database for cpeId " + cpeId
                ));
    }

    @Override
    public WifiConfiguration update(WifiConfiguration configuration) {
        validator.validateConfiguration(configuration);
        WifiConfiguration updatedConfiguration = platformClient.updateWifiConfiguration(configuration);
        return repository.save(updatedConfiguration);
    }
}
