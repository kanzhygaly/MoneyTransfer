package kz.ya.mt.api.exception;

/**
 *
 * @author yerlana
 */
public class NullInputAmountException extends RuntimeException {

    @Override
    public String getMessage() {
        return "Transfer input amount is null";
    }
}