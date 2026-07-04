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
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SoapWifiPlatformMapperTest {

    private final SoapWifiPlatformMapper mapper = new SoapWifiPlatformMapper();

    @Test
    void mapsCpeIdToGetRequest() {
        GetCpeIdRequest request = mapper.toGetCpeIdRequest("CPE_001");

        assertThat(request.getCpeId()).isEqualTo("CPE_001");
    }

    @Test
    void mapsRestConfigurationToUpdateRequest() {
        WifiConfiguration configuration = new WifiConfiguration("CPE_001", WifiBand._2_4_GHZ, "Office-2G")
                .encryptionType(EncryptionType.WPA2_PSK)
                .password("secret");

        UpdateCpeIdRequest request = mapper.toUpdateCpeIdRequest(configuration);

        assertThat(request.getConfiguration().getCpeId()).isEqualTo("CPE_001");
        assertThat(request.getConfiguration().getWifiBand()).isEqualTo(WifiBandType.BAND_2_4_GHZ);
        assertThat(request.getConfiguration().getSsid()).isEqualTo("Office-2G");
        assertThat(request.getConfiguration().getEncryptionType().value()).isEqualTo("WPA2_PSK");
        assertThat(request.getConfiguration().getPassword()).isEqualTo("secret");
    }

    @Test
    void defaultsMissingRestEncryptionTypeToOpenForSoap() {
        WifiConfiguration configuration = new WifiConfiguration("CPE_003", WifiBand._2_4_GHZ, "Guest-2G");

        WifiConfigurationType soapConfiguration = mapper.toSoapConfiguration(configuration);

        assertThat(soapConfiguration.getEncryptionType()).isEqualTo(local.wifiadmin.platform.soap.EncryptionType.OPEN);
    }

    @Test
    void mapsSoapConfigurationToRestConfiguration() {
        WifiConfigurationType soapConfiguration = new WifiConfigurationType();
        soapConfiguration.setCpeId("CPE_004");
        soapConfiguration.setWifiBand(WifiBandType.BAND_5_GHZ);
        soapConfiguration.setSsid("Guest-5G");
        soapConfiguration.setEncryptionType(local.wifiadmin.platform.soap.EncryptionType.WPA_3_SAE);
        soapConfiguration.setPassword("seed-wifi-04");

        WifiConfiguration configuration = mapper.toRestConfiguration(soapConfiguration);

        assertThat(configuration.getCpeId()).isEqualTo("CPE_004");
        assertThat(configuration.getWifiBand()).isEqualTo(WifiBand._5_GHZ);
        assertThat(configuration.getSsid()).isEqualTo("Guest-5G");
        assertThat(configuration.getEncryptionType()).isEqualTo(EncryptionType.WPA3_SAE);
        assertThat(configuration.getPassword()).isEqualTo("seed-wifi-04");
    }

    @Test
    void defaultsMissingSoapEncryptionTypeToOpenForRest() {
        WifiConfigurationType soapConfiguration = new WifiConfigurationType();
        soapConfiguration.setCpeId("CPE_011");
        soapConfiguration.setWifiBand(WifiBandType.BAND_2_4_GHZ);
        soapConfiguration.setSsid("Demo-Open");

        WifiConfiguration configuration = mapper.toRestConfiguration(soapConfiguration);

        assertThat(configuration.getEncryptionType()).isEqualTo(EncryptionType.OPEN);
    }

    @Test
    void mapsGetResponseConfigurationToRestConfiguration() {
        GetCpeIdResponse response = new GetCpeIdResponse();
        response.setConfiguration(soapConfiguration("CPE_001"));

        WifiConfiguration configuration = mapper.toRestConfiguration(response);

        assertThat(configuration.getCpeId()).isEqualTo("CPE_001");
    }

    @Test
    void mapsUpdateResponseConfigurationToRestConfiguration() {
        UpdateCpeIdResponse response = new UpdateCpeIdResponse();
        response.setConfiguration(soapConfiguration("CPE_002"));

        WifiConfiguration configuration = mapper.toRestConfiguration(response);

        assertThat(configuration.getCpeId()).isEqualTo("CPE_002");
    }

    @Test
    void mapsAllEncryptionTypesByWireValue() {
        for (EncryptionType encryptionType : EncryptionType.values()) {
            WifiConfiguration configuration = new WifiConfiguration("CPE_001", WifiBand._5_GHZ, "Test")
                    .encryptionType(encryptionType);

            WifiConfiguration roundTripped = mapper.toRestConfiguration(mapper.toSoapConfiguration(configuration));

            assertThat(roundTripped.getEncryptionType()).isEqualTo(encryptionType);
        }
    }

    private WifiConfigurationType soapConfiguration(String cpeId) {
        WifiConfigurationType configuration = new WifiConfigurationType();
        configuration.setCpeId(cpeId);
        configuration.setWifiBand(WifiBandType.BAND_2_4_GHZ);
        configuration.setSsid("Office-2G");
        configuration.setEncryptionType(local.wifiadmin.platform.soap.EncryptionType.WPA_2_PSK);
        configuration.setPassword("seed-wifi-01");
        return configuration;
    }
}
