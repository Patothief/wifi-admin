package local.wifiadmin.wifi;

import local.wifiadmin.api.model.WifiConfiguration;

public interface WifiParameterService {

    WifiConfiguration get(String cpeId);

    WifiConfiguration update(WifiConfiguration configuration);
}
