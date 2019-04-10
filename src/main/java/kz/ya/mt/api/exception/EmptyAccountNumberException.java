package kz.ya.mt.api.exception;

/**
 *
 * @author yerlana
 */
public class EmptyAccountNumberException extends RuntimeException {

    @Override
    public String getMessage() {
        return "Account number is empty or null";
    }
}