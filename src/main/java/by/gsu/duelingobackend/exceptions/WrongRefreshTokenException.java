package by.gsu.duelingobackend.exceptions;

public class WrongRefreshTokenException extends RuntimeException {

    public WrongRefreshTokenException(String message) {
        super(message);
    }
}
