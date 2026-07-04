package local.wifiadmin.wifi;

import local.wifiadmin.api.model.EncryptionType;
import local.wifiadmin.api.model.WifiConfiguration;
import local.wifiadmin.error.BadRequestException;
import org.springframework.stereotype.Component;

@Component
class WifiParameterValidator {

    void validateCpeId(String cpeId) {
        if (isBlank(cpeId)) {
            throw new BadRequestException("cpeId must not be blank");
        }
    }

    void validateConfiguration(WifiConfiguration configuration) {
        if (configuration == null) {
            throw new BadRequestException("Request body is required");
        }

        validateCpeId(configuration.getCpeId());
        if (configuration.getWifiBand() == null) {
            throw new BadRequestException("wifiBand is required");
        }
        if (isBlank(configuration.getSsid())) {
            throw new BadRequestException("ssid must not be blank");
        }

        EncryptionType encryptionType = effectiveEncryptionType(configuration);
        if (encryptionType != EncryptionType.OPEN && isBlank(configuration.getPassword())) {
            throw new BadRequestException("password is required when encryptionType is " + encryptionType.getValue());
        }
    }

    private EncryptionType effectiveEncryptionType(WifiConfiguration configuration) {
        return configuration.getEncryptionType() == null ? EncryptionType.OPEN : configuration.getEncryptionType();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
