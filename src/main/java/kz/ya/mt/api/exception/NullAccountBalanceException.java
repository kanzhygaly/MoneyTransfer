package kz.ya.mt.api.exception;

/**
 *
 * @author yerlana
 */
public class NullAccountBalanceException extends RuntimeException {

    @Override
    public String getMessage() {
        return "Account balance is null";
    }
}