package local.wifiadmin.platform;

import jakarta.xml.soap.SOAPFault;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.soap.SOAPFaultException;
import local.wifiadmin.api.model.WifiConfiguration;
import local.wifiadmin.error.PlatformCommunicationException;
import local.wifiadmin.error.PlatformNotFoundException;
import local.wifiadmin.platform.soap.GetCpeIdResponse;
import local.wifiadmin.platform.soap.UpdateCpeIdResponse;
import local.wifiadmin.platform.soap.WifiPlatformPortType;
import org.springframework.stereotype.Component;

@Component
class SoapWifiPlatformClient implements WifiPlatformClient {

    private static final String GET_CPE_ID_SOAP_ACTION = "http://wifi-admin.local/platform/v1#getCpeID";
    private static final String UPDATE_CPE_ID_SOAP_ACTION = "http://wifi-admin.local/platform/v1#updateCpeId";

    private final WifiPlatformPortType port;
    private final SoapWifiPlatformMapper mapper;

    SoapWifiPlatformClient(WifiPlatformPortType port, SoapWifiPlatformMapper mapper) {
        this.port = port;
        this.mapper = mapper;
    }

    @Override
    public WifiConfiguration getWifiConfiguration(String cpeId) {
        try {
            useSoapAction(GET_CPE_ID_SOAP_ACTION);
            GetCpeIdResponse response = port.getCpeID(mapper.toGetCpeIdRequest(cpeId));
            if (response == null || response.getConfiguration() == null) {
                throw new PlatformCommunicationException("Platform returned an empty getCpeID response");
            }
            return mapper.toRestConfiguration(response);
        } catch (SOAPFaultException exception) {
            throw mapSoapFault(exception);
        } catch (WebServiceException exception) {
            throw new PlatformCommunicationException("Failed to communicate with SOAP platform", exception);
        }
    }

    @Override
    public WifiConfiguration updateWifiConfiguration(WifiConfiguration configuration) {
        try {
            useSoapAction(UPDATE_CPE_ID_SOAP_ACTION);
            UpdateCpeIdResponse response = port.updateCpeId(mapper.toUpdateCpeIdRequest(configuration));
            if (response == null || response.getConfiguration() == null) {
                throw new PlatformCommunicationException("Platform returned an empty updateCpeId response");
            }
            return mapper.toRestConfiguration(response);
        } catch (SOAPFaultException exception) {
            throw mapSoapFault(exception);
        } catch (WebServiceException exception) {
            throw new PlatformCommunicationException("Failed to communicate with SOAP platform", exception);
        }
    }

    private void useSoapAction(String soapAction) {
        BindingProvider bindingProvider = (BindingProvider) port;
        bindingProvider.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY, true);
        bindingProvider.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, soapAction);
    }

    private RuntimeException mapSoapFault(SOAPFaultException exception) {
        SOAPFault fault = exception.getFault();
        String faultString = fault == null ? null : fault.getFaultString();
        String faultCode = fault == null ? null : fault.getFaultCode();

        if (containsIgnoreCase(faultString, "not found") || containsIgnoreCase(faultCode, "notfound")) {
            return new PlatformNotFoundException("CPE not found on platform", exception);
        }

        return new PlatformCommunicationException("SOAP platform returned a fault", exception);
    }

    private boolean containsIgnoreCase(String value, String expected) {
        return value != null && value.toLowerCase().contains(expected.toLowerCase());
    }
}
