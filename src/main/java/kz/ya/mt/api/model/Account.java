package kz.ya.mt.api.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author yerlan.akhmetov
 */
public class Account {

    private final String number;
    private BigDecimal balance;
    private final LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private final transient Lock lock = new ReentrantLock();
    private final transient LongAdder failCounter = new LongAdder();

    public Account(String number) {
        this.number = number;
        this.balance = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
    }

    public Account(String number, BigDecimal balance) {
        this.number = number;
        this.balance = balance;
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
    }

    public String getNumber() {
        return number;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(LocalDateTime modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public Lock getLock() {
        return lock;
    }

    public void incFailedTransferCount() {
        failCounter.increment();
    }
    
    public long getFailCount() {
        return failCounter.sum();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Account account = (Account) o;
        return Objects.equals(number, account.number)
                && Objects.equals(createdAt, account.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, createdAt);
    }

    @Override
    public String toString() {
        return "Account [ " + number + " ] with balance \'" + balance + "\' was created at " + createdAt;
    }
}
