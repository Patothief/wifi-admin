package local.wifiadmin.error;

public class PlatformNotFoundException extends RuntimeException {

    public PlatformNotFoundException(String message) {
        super(message);
    }

    public PlatformNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
