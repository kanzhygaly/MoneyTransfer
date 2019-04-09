package kz.ya.mt.api.exception;

/**
 *
 * @author yerlan.akhmetov
 */
public class NotEnoughFundsException extends RuntimeException {

    private final String number;

    public NotEnoughFundsException(final String number) {
        this.number = number;
    }

    @Override
    public String getMessage() {
        return String.format("Not enough funds on Account %s", number);
    }
}
