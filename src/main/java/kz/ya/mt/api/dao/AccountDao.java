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

    private final ConcurrentMap<String, Account> datastore = new ConcurrentHashMap<>(1000);
    private volatile static AccountDao INSTANCE;

    private AccountDao() {
        // to prevent creating another instance of AccountDao using Reflection
        throw new RuntimeException("AccountDao is already initialized");
    }

    public static AccountDao getInstance() {
        if (INSTANCE == null) {
            synchronized (AccountDao.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AccountDao();
                }
            }            
        }
        return INSTANCE;
    }
        
    private Object readResolve(){
        // to prevent another instance of AccountDao during Serialization
        return INSTANCE;
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

        return datastore.putIfAbsent(number, account);
    }

    public void withdraw(Account account, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new TransferNegativeAmountException();
        }
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new TransferZeroAmountException();
        }
        if (!datastore.containsKey(account.getNumber())) {
            throw new AccountNotFoundException(account.getNumber());
        }

        final Account updatedAccount = datastore.get(account.getNumber());

        BigDecimal balance = updatedAccount.getBalance().subtract(amount);
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new NotEnoughFundsException(updatedAccount.getNumber());
        }
        updatedAccount.setBalance(balance);
        updatedAccount.setModifiedAt(LocalDateTime.now());

        datastore.replace(account.getNumber(), updatedAccount);
    }

    public void deposit(Account account, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new TransferNegativeAmountException();
        }
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new TransferZeroAmountException();
        }
        if (!datastore.containsKey(account.getNumber())) {
            throw new AccountNotFoundException(account.getNumber());
        }

        final Account updatedAccount = datastore.get(account.getNumber());

        BigDecimal balance = updatedAccount.getBalance().add(amount);
        updatedAccount.setBalance(balance);
        updatedAccount.setModifiedAt(LocalDateTime.now());

        datastore.replace(account.getNumber(), updatedAccount);
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
