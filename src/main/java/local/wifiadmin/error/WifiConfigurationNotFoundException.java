package local.wifiadmin.error;

public class WifiConfigurationNotFoundException extends RuntimeException {

    public WifiConfigurationNotFoundException(String message) {
        super(message);
    }
}
