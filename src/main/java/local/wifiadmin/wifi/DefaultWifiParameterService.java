package local.wifiadmin.wifi;

import local.wifiadmin.api.model.WifiConfiguration;
import local.wifiadmin.platform.WifiPlatformClient;
import org.springframework.stereotype.Service;

@Service
class DefaultWifiParameterService implements WifiParameterService {

    private final WifiPlatformClient platformClient;
    private final WifiParameterValidator validator;

    DefaultWifiParameterService(WifiPlatformClient platformClient, WifiParameterValidator validator) {
        this.platformClient = platformClient;
        this.validator = validator;
    }

    @Override
    public WifiConfiguration get(String cpeId) {
        validator.validateCpeId(cpeId);
        return platformClient.getWifiConfiguration(cpeId);
    }

    @Override
    public WifiConfiguration update(WifiConfiguration configuration) {
        validator.validateConfiguration(configuration);
        return platformClient.updateWifiConfiguration(configuration);
    }
}
