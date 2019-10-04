package lab8.server;

class WardrobeOverflowException extends RuntimeException {
    WardrobeOverflowException() {
        super("Гардероб переполнен");
    }
}
