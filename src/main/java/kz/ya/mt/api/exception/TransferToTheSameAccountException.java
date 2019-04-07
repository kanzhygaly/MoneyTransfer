package kz.ya.mt.api.exception;

public class TransferToTheSameAccountException extends RuntimeException {

    @Override
    public String getMessage() {
        return "Money transfer to the same account is not allowed";
    }
}