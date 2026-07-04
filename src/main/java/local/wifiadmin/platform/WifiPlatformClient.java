package local.wifiadmin.platform;

import local.wifiadmin.api.model.WifiConfiguration;

public interface WifiPlatformClient {

    WifiConfiguration getWifiConfiguration(String cpeId);

    WifiConfiguration updateWifiConfiguration(WifiConfiguration configuration);
}
