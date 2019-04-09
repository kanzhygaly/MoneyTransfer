package kz.ya.mt.api.exception;

/**
 *
 * @author yerlan.akhmetov
 */
public class TransferToTheSameAccountException extends RuntimeException {

    @Override
    public String getMessage() {
        return "Money transfer to the same account is not allowed";
    }
}