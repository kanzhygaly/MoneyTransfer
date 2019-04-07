package kz.ya.mt.api.dao;

import kz.ya.mt.api.model.Account;
import kz.ya.mt.api.exception.AccountNotFoundException;
import kz.ya.mt.api.exception.NotEnoughFundsException;
import kz.ya.mt.api.exception.TransferNegativeAmountException;
import kz.ya.mt.api.exception.TransferZeroAmountException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AccountDao {

    private final ConcurrentMap<String, Account> datastore = new ConcurrentHashMap<>();
    private static AccountDao localInstance = null;

    private AccountDao() {
    }

    public static AccountDao getInstance() {
        if (localInstance == null) {
            localInstance = new AccountDao();
        }
        return localInstance;
    }

    public Optional<Account> get(String number) {
        final Account account = datastore.get(number);

        if (account == null) {
            return Optional.empty();
        }

        return Optional.of(account);
    }

    public Account create(BigDecimal balance) {
        String number = UUID.randomUUID().toString();

        final Account account = new Account(number, balance);

        datastore.put(number, account);

        return account;
    }

    public void withdraw(Account account, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new TransferNegativeAmountException();
        }
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new TransferZeroAmountException();
        }
        if (!datastore.containsKey(account.getNumber())){
            throw new AccountNotFoundException(account.getNumber());
        }

        final Account updatedAccount = datastore.get(account.getNumber());

        BigDecimal balance = updatedAccount.getBalance().subtract(amount);
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new NotEnoughFundsException(updatedAccount.getNumber());
        }
        updatedAccount.setBalance(balance);
        updatedAccount.setModifiedAt(LocalDateTime.now());

        datastore.put(account.getNumber(), updatedAccount);
    }

    public void deposit(Account account, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new TransferNegativeAmountException();
        }
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new TransferZeroAmountException();
        }
        if (!datastore.containsKey(account.getNumber())){
            throw new AccountNotFoundException(account.getNumber());
        }

        final Account updatedAccount = datastore.get(account.getNumber());

        BigDecimal balance = updatedAccount.getBalance().add(amount);
        updatedAccount.setBalance(balance);
        updatedAccount.setModifiedAt(LocalDateTime.now());

        datastore.put(account.getNumber(), updatedAccount);
    }

    public void delete(Account account) {
        if (!datastore.containsKey(account.getNumber())) {
            throw new AccountNotFoundException(account.getNumber());
        }
        datastore.remove(account.getNumber());
    }

    public void clear() {
        datastore.clear();
    }
}
