package kz.ya.mt.api.exception;

public class AccountNotFoundException extends RuntimeException {

    private final String number;

    public AccountNotFoundException(final String number) {
        this.number = number;
    }

    @Override
    public String getMessage() {
        return String.format("Account with number %s was not found", number);
    }
}
