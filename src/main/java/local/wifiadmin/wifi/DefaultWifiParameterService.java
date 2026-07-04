package local.wifiadmin.wifi;

import local.wifiadmin.api.model.WifiConfiguration;
import local.wifiadmin.platform.WifiPlatformClient;
import org.springframework.stereotype.Service;

@Service
class DefaultWifiParameterService implements WifiParameterService {

    private final WifiPlatformClient platformClient;

    DefaultWifiParameterService(WifiPlatformClient platformClient) {
        this.platformClient = platformClient;
    }

    @Override
    public WifiConfiguration get(String cpeId) {
        return platformClient.getWifiConfiguration(cpeId);
    }

    @Override
    public WifiConfiguration update(WifiConfiguration configuration) {
        return platformClient.updateWifiConfiguration(configuration);
    }
}
