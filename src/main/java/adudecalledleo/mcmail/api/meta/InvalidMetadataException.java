package adudecalledleo.mcmail.api.meta;

public class InvalidMetadataException extends Exception {
    public InvalidMetadataException() {
        super();
    }

    public InvalidMetadataException(String message) {
        super(message);
    }

    public InvalidMetadataException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidMetadataException(Throwable cause) {
        super(cause);
    }
}
