package kz.ya.mt.api.exception;

/**
 *
 * @author yerlan.akhmetov
 */
public class AccountAlreadyExistsException extends RuntimeException {

    private final String number;

    public AccountAlreadyExistsException(final String number) {
        this.number = number;
    }

    @Override
    public String getMessage() {
        return String.format("Account with number %s is already exist", number);
    }
}
