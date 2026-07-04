package local.wifiadmin.platform;

import jakarta.xml.soap.SOAPFault;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPFactory;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.soap.SOAPFaultException;
import local.wifiadmin.api.model.EncryptionType;
import local.wifiadmin.api.model.WifiBand;
import local.wifiadmin.api.model.WifiConfiguration;
import local.wifiadmin.error.PlatformCommunicationException;
import local.wifiadmin.error.PlatformNotFoundException;
import local.wifiadmin.platform.soap.GetCpeIdRequest;
import local.wifiadmin.platform.soap.GetCpeIdResponse;
import local.wifiadmin.platform.soap.UpdateCpeIdRequest;
import local.wifiadmin.platform.soap.UpdateCpeIdResponse;
import local.wifiadmin.platform.soap.WifiBandType;
import local.wifiadmin.platform.soap.WifiConfigurationType;
import local.wifiadmin.platform.soap.WifiPlatformPortType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class SoapWifiPlatformClientTest {

    private final WifiPlatformPortType port = mock(
            WifiPlatformPortType.class,
            withSettings().extraInterfaces(BindingProvider.class)
    );
    private final Map<String, Object> requestContext = new HashMap<>();
    private final SoapWifiPlatformClient client = new SoapWifiPlatformClient(
            port,
            new SoapWifiPlatformMapper()
    );

    SoapWifiPlatformClientTest() {
        when(((BindingProvider) port).getRequestContext()).thenReturn(requestContext);
    }

    @Test
    void getsWifiConfigurationFromPlatform() {
        GetCpeIdResponse response = new GetCpeIdResponse();
        response.setConfiguration(soapConfiguration("CPE_001"));
        when(port.getCpeID(any(GetCpeIdRequest.class))).thenReturn(response);

        WifiConfiguration configuration = client.getWifiConfiguration("CPE_001");

        assertThat(configuration.getCpeId()).isEqualTo("CPE_001");
        assertThat(configuration.getWifiBand()).isEqualTo(WifiBand._2_4_GHZ);
        assertThat(configuration.getEncryptionType()).isEqualTo(EncryptionType.WPA2_PSK);
        assertThat(configuration.getPassword()).isEqualTo("seed-wifi-01");
        assertThat(requestContext).containsEntry(
                BindingProvider.SOAPACTION_URI_PROPERTY,
                "http://wifi-admin.local/platform/v1#getCpeID"
        );
        verify(port).getCpeID(any(GetCpeIdRequest.class));
    }

    @Test
    void updatesWifiConfigurationOnPlatform() {
        UpdateCpeIdResponse response = new UpdateCpeIdResponse();
        response.setConfiguration(soapConfiguration("CPE_002"));
        when(port.updateCpeId(any(UpdateCpeIdRequest.class))).thenReturn(response);

        WifiConfiguration request = new WifiConfiguration("CPE_002", WifiBand._2_4_GHZ, "Office-2G")
                .encryptionType(EncryptionType.WPA2_PSK)
                .password("seed-wifi-01");

        WifiConfiguration configuration = client.updateWifiConfiguration(request);

        assertThat(configuration.getCpeId()).isEqualTo("CPE_002");
        assertThat(requestContext).containsEntry(
                BindingProvider.SOAPACTION_URI_PROPERTY,
                "http://wifi-admin.local/platform/v1#updateCpeId"
        );
        verify(port).updateCpeId(any(UpdateCpeIdRequest.class));
    }

    @Test
    void mapsNotFoundSoapFaultToPlatformNotFound() throws SOAPException {
        when(port.getCpeID(any(GetCpeIdRequest.class))).thenThrow(soapFault("NotFound", "CPE not found"));

        assertThatThrownBy(() -> client.getWifiConfiguration("UNKNOWN"))
                .isInstanceOf(PlatformNotFoundException.class)
                .hasMessage("CPE not found on platform");
    }

    @Test
    void mapsUnknownSoapFaultToPlatformCommunication() throws SOAPException {
        when(port.getCpeID(any(GetCpeIdRequest.class))).thenThrow(soapFault("Client", "Unknown SOAPAction"));

        assertThatThrownBy(() -> client.getWifiConfiguration("CPE_001"))
                .isInstanceOf(PlatformCommunicationException.class)
                .hasMessage("SOAP platform returned a fault");
    }

    @Test
    void mapsWebServiceFailureToPlatformCommunication() {
        when(port.getCpeID(any(GetCpeIdRequest.class))).thenThrow(new WebServiceException("timeout"));

        assertThatThrownBy(() -> client.getWifiConfiguration("CPE_001"))
                .isInstanceOf(PlatformCommunicationException.class)
                .hasMessage("Failed to communicate with SOAP platform");
    }

    @Test
    void rejectsEmptyPlatformResponseAsPlatformCommunication() {
        when(port.getCpeID(any(GetCpeIdRequest.class))).thenReturn(new GetCpeIdResponse());

        assertThatThrownBy(() -> client.getWifiConfiguration("CPE_001"))
                .isInstanceOf(PlatformCommunicationException.class)
                .hasMessage("Platform returned an empty getCpeID response");
    }

    private SOAPFaultException soapFault(String faultCode, String faultString) throws SOAPException {
        SOAPFault fault = SOAPFactory.newInstance().createFault(
                faultString,
                new QName("http://wifi-admin.local/platform/v1", faultCode, "tns")
        );
        return new SOAPFaultException(fault);
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
