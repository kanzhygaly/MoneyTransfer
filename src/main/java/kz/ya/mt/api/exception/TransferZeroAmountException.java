package kz.ya.mt.api.exception;

public class TransferZeroAmountException extends RuntimeException {

    @Override
    public String getMessage() {
        return String.format("It's not allowed to transfer ZERO amount");
    }
}
