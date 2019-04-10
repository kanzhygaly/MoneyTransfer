package kz.ya.mt.api.exception;

/**
 *
 * @author yerlana
 */
public class NullInputAccountException extends RuntimeException {

    @Override
    public String getMessage() {
        return "Transfer input account is null";
    }
}