package local.wifiadmin.platform;

import local.wifiadmin.api.model.WifiConfiguration;
import org.springframework.stereotype.Component;

@Component
class SoapWifiPlatformClient implements WifiPlatformClient {

    @Override
    public WifiConfiguration getWifiConfiguration(String cpeId) {
        throw new UnsupportedOperationException("SOAP platform client is not implemented yet");
    }

    @Override
    public WifiConfiguration updateWifiConfiguration(WifiConfiguration configuration) {
        throw new UnsupportedOperationException("SOAP platform client is not implemented yet");
    }
}
