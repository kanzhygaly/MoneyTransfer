package kz.ya.mt.api.dto;

import java.math.BigDecimal;

@Deprecated
public class MoneyTransfer {

    private final String fromAccountNo;
    private final String toAccountNo;
    private final BigDecimal amount;

    public MoneyTransfer(String fromAccountNo, String toAccountNo, BigDecimal amount) {
        this.fromAccountNo = fromAccountNo;
        this.toAccountNo = toAccountNo;
        this.amount = amount;
    }

    public String getFromAccountNo() {
        return fromAccountNo;
    }

    public String getToAccountNo() {
        return toAccountNo;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
