package local.wifiadmin.platform;

import local.wifiadmin.api.model.EncryptionType;
import local.wifiadmin.api.model.WifiBand;
import local.wifiadmin.api.model.WifiConfiguration;
import local.wifiadmin.platform.soap.GetCpeIdRequest;
import local.wifiadmin.platform.soap.GetCpeIdResponse;
import local.wifiadmin.platform.soap.UpdateCpeIdRequest;
import local.wifiadmin.platform.soap.UpdateCpeIdResponse;
import local.wifiadmin.platform.soap.WifiBandType;
import local.wifiadmin.platform.soap.WifiConfigurationType;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
class SoapWifiPlatformMapper {

    GetCpeIdRequest toGetCpeIdRequest(String cpeId) {
        GetCpeIdRequest request = new GetCpeIdRequest();
        request.setCpeId(cpeId);
        return request;
    }

    UpdateCpeIdRequest toUpdateCpeIdRequest(WifiConfiguration configuration) {
        UpdateCpeIdRequest request = new UpdateCpeIdRequest();
        request.setConfiguration(toSoapConfiguration(configuration));
        return request;
    }

    WifiConfiguration toRestConfiguration(GetCpeIdResponse response) {
        Objects.requireNonNull(response, "response must not be null");
        return toRestConfiguration(response.getConfiguration());
    }

    WifiConfiguration toRestConfiguration(UpdateCpeIdResponse response) {
        Objects.requireNonNull(response, "response must not be null");
        return toRestConfiguration(response.getConfiguration());
    }

    WifiConfigurationType toSoapConfiguration(WifiConfiguration configuration) {
        Objects.requireNonNull(configuration, "configuration must not be null");

        WifiConfigurationType soapConfiguration = new WifiConfigurationType();
        soapConfiguration.setCpeId(configuration.getCpeId());
        soapConfiguration.setWifiBand(WifiBandType.fromValue(configuration.getWifiBand().getValue()));
        soapConfiguration.setSsid(configuration.getSsid());
        soapConfiguration.setEncryptionType(toSoapEncryptionType(configuration.getEncryptionType()));
        soapConfiguration.setPassword(configuration.getPassword());
        return soapConfiguration;
    }

    WifiConfiguration toRestConfiguration(WifiConfigurationType configuration) {
        Objects.requireNonNull(configuration, "configuration must not be null");

        return new WifiConfiguration(
                configuration.getCpeId(),
                WifiBand.fromValue(configuration.getWifiBand().value()),
                configuration.getSsid()
        )
                .encryptionType(toRestEncryptionType(configuration.getEncryptionType()))
                .password(configuration.getPassword());
    }

    private local.wifiadmin.platform.soap.EncryptionType toSoapEncryptionType(EncryptionType encryptionType) {
        EncryptionType effectiveEncryptionType = encryptionType == null ? EncryptionType.OPEN : encryptionType;
        return local.wifiadmin.platform.soap.EncryptionType.fromValue(effectiveEncryptionType.getValue());
    }

    private EncryptionType toRestEncryptionType(local.wifiadmin.platform.soap.EncryptionType encryptionType) {
        if (encryptionType == null) {
            return EncryptionType.OPEN;
        }
        return EncryptionType.fromValue(encryptionType.value());
    }
}
