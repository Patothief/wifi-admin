package local.wifiadmin.platform;

import jakarta.xml.ws.BindingProvider;
import local.wifiadmin.platform.soap.WifiPlatformPortType;
import local.wifiadmin.platform.soap.WifiPlatformService;
import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
class WifiPlatformSoapClientConfiguration {

    private static final String PLATFORM_NAMESPACE = "http://wifi-admin.local/platform/v1";

    @Bean
    WifiPlatformPortType wifiPlatformPort(WifiPlatformProperties properties) {
        WifiPlatformPortType port = new WifiPlatformService().getWifiPlatformPort();
        configureEndpoint(port, properties);
        configureHttpTransportCompatibility(port);
        configureNamespacePrefixes(port);
        configureTimeouts(port, properties);
        return port;
    }

    private void configureEndpoint(WifiPlatformPortType port, WifiPlatformProperties properties) {
        BindingProvider bindingProvider = (BindingProvider) port;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, properties.url());
    }

    private void configureHttpTransportCompatibility(WifiPlatformPortType port) {
        BindingProvider bindingProvider = (BindingProvider) port;
        bindingProvider.getRequestContext().put("org.apache.cxf.transport.http.forceVersion", "1.1");
        bindingProvider.getRequestContext().put("force.urlconnection.http.conduit", true);
    }

    private void configureNamespacePrefixes(WifiPlatformPortType port) {
        DataBinding dataBinding = ClientProxy.getClient(port).getEndpoint().getService().getDataBinding();
        if (dataBinding instanceof JAXBDataBinding jaxbDataBinding) {
            jaxbDataBinding.setNamespaceMap(Map.of(PLATFORM_NAMESPACE, "tns"));
        }
    }

    private void configureTimeouts(WifiPlatformPortType port, WifiPlatformProperties properties) {
        HTTPConduit conduit = (HTTPConduit) ClientProxy.getClient(port).getConduit();
        HTTPClientPolicy clientPolicy = new HTTPClientPolicy();
        clientPolicy.setConnectionTimeout(properties.connectTimeout().toMillis());
        clientPolicy.setReceiveTimeout(properties.readTimeout().toMillis());
        conduit.setClient(clientPolicy);
    }
}
