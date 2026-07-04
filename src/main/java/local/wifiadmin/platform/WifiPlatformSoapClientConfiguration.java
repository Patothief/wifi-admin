package local.wifiadmin.platform;

import jakarta.xml.ws.BindingProvider;
import local.wifiadmin.platform.soap.WifiPlatformPortType;
import local.wifiadmin.platform.soap.WifiPlatformService;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class WifiPlatformSoapClientConfiguration {

    @Bean
    WifiPlatformPortType wifiPlatformPort(WifiPlatformProperties properties) {
        WifiPlatformPortType port = new WifiPlatformService().getWifiPlatformPort();
        configureEndpoint(port, properties);
        configureTimeouts(port, properties);
        return port;
    }

    private void configureEndpoint(WifiPlatformPortType port, WifiPlatformProperties properties) {
        BindingProvider bindingProvider = (BindingProvider) port;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, properties.url());
    }

    private void configureTimeouts(WifiPlatformPortType port, WifiPlatformProperties properties) {
        HTTPConduit conduit = (HTTPConduit) ClientProxy.getClient(port).getConduit();
        HTTPClientPolicy clientPolicy = new HTTPClientPolicy();
        clientPolicy.setConnectionTimeout(properties.connectTimeout().toMillis());
        clientPolicy.setReceiveTimeout(properties.readTimeout().toMillis());
        conduit.setClient(clientPolicy);
    }
}
