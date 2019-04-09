package kz.ya.mt.api.exception;

/**
 *
 * @author yerlan.akhmetov
 */
public class TransferNegativeAmountException extends RuntimeException {

    @Override
    public String getMessage() {
        return String.format("It's not allowed to transfer negative amount");
    }
}
