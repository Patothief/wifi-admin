package local.wifiadmin.error;

public class PlatformCommunicationException extends RuntimeException {

    public PlatformCommunicationException(String message) {
        super(message);
    }

    public PlatformCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
