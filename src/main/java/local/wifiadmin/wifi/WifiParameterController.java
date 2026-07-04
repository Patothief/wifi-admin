package local.wifiadmin.wifi;

import local.wifiadmin.api.WiFiApi;
import local.wifiadmin.api.model.WifiConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
class WifiParameterController implements WiFiApi {

    private final WifiParameterService wifiParameterService;

    WifiParameterController(WifiParameterService wifiParameterService) {
        this.wifiParameterService = wifiParameterService;
    }

    @Override
    public ResponseEntity<WifiConfiguration> getWifiParameter(String cpeId) {
        return ResponseEntity.ok(wifiParameterService.get(cpeId));
    }

    @Override
    public ResponseEntity<WifiConfiguration> putWifiParameter(WifiConfiguration wifiConfiguration) {
        return ResponseEntity.ok(wifiParameterService.update(wifiConfiguration));
    }
}
