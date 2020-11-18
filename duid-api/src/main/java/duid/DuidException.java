package duid;

public class DuidException extends RuntimeException {
    public DuidException(String s) {
        super(s);
    }

    public DuidException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
