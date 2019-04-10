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
import kz.ya.mt.api.exception.AccountAlreadyExistsException;
import kz.ya.mt.api.exception.EmptyAccountNumberException;
import kz.ya.mt.api.exception.NullAccountBalanceException;
import kz.ya.mt.api.exception.NullInputAccountException;
import kz.ya.mt.api.exception.NullInputAmountException;

/**
 *
 * @author yerlan.akhmetov
 */
public class AccountDao {

    private final ConcurrentMap<String, Account> datastore = new ConcurrentHashMap<>(1000);
    private volatile static AccountDao INSTANCE;

    private AccountDao() {
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

    public Optional<Account> get(String number) {
        final Account account = datastore.get(number);

        if (account == null) {
            return Optional.empty();
        }
        return Optional.of(account);
    }

    public Account create(String number, BigDecimal balance) {
        if (number == null || number.isEmpty()) {
            throw new EmptyAccountNumberException();
        }
        if (balance == null) {
            throw new NullAccountBalanceException();
        }
        
        final Account account = new Account(number, balance);

        if (datastore.putIfAbsent(number, account) != null) {
            throw new AccountAlreadyExistsException(number);
        }

        return account;
    }

    public Account create(BigDecimal balance) {
        if (balance == null) {
            throw new NullAccountBalanceException();
        }
        
        String number = UUID.randomUUID().toString();

        final Account account = new Account(number, balance);
        
        datastore.putIfAbsent(number, account);

        return account;
    }

    public void withdraw(Account account, BigDecimal amount) {
        if (account == null) {
            throw new NullInputAccountException();
        }
        if (amount == null) {
            throw new NullInputAmountException();
        }
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
        if (account == null) {
            throw new NullInputAccountException();
        }
        if (amount == null) {
            throw new NullInputAmountException();
        }
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
        if (account == null) {
            throw new NullInputAccountException();
        }
        if (!datastore.containsKey(account.getNumber())) {
            throw new AccountNotFoundException(account.getNumber());
        }
        datastore.remove(account.getNumber());
    }

    public void clearDatastore() {
        datastore.clear();
    }
    
    public boolean isDatastoreIsEmpty() {
        return datastore.isEmpty();
    }
}
