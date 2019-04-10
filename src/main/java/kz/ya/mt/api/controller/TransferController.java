package kz.ya.mt.api.controller;

import io.javalin.Context;
import kz.ya.mt.api.exception.AccountNotFoundException;
import kz.ya.mt.api.exception.NotEnoughFundsException;
import kz.ya.mt.api.exception.TransferToTheSameAccountException;
import kz.ya.mt.api.model.Account;
import kz.ya.mt.api.dao.AccountDao;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author yerlan.akhmetov
 */
public class TransferController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransferController.class);
    private static final int LOCK_WAIT_SEC = 5;

    public void process(final Context context) throws Exception {

        String fromAccountNo = context.formParam("fromAccountNo", String.class)
                .check(input -> input != null).get();
        String toAccountNo = context.formParam("toAccountNo", String.class)
                .check(input -> input != null).get();

        if (fromAccountNo.equals(toAccountNo)) {
            throw new TransferToTheSameAccountException();
        }
        
        BigDecimal amount = context.formParam("amount", BigDecimal.class)
                .check(input -> input != null).get();

        Account fromAccount = AccountDao.getInstance().get(fromAccountNo).orElseThrow(
                () -> new AccountNotFoundException(fromAccountNo));

        Account toAccount = AccountDao.getInstance().get(toAccountNo).orElseThrow(
                () -> new AccountNotFoundException(toAccountNo));
        
        boolean isCompletedSuccessfully = performTransaction(fromAccount, toAccount, amount);
        
        if (isCompletedSuccessfully) {
            context.status(200); // OK
        } else {
            context.status(408); // Request Timeout
        }
    }
    
    public boolean performTransaction(Account fromAccount, Account toAccount, BigDecimal amount) throws InterruptedException {
        boolean isCompletedSuccessfully;

        LOGGER.debug("[" + Thread.currentThread().getName() + "] try to lock Sender");
        if (fromAccount.getLock().tryLock(LOCK_WAIT_SEC, TimeUnit.SECONDS)) {
            try {
                if (fromAccount.getBalance().compareTo(amount) < 0) {
                    fromAccount.incFailedTransferCount();
                    throw new NotEnoughFundsException(fromAccount.getNumber());
                }

                LOGGER.debug("[" + Thread.currentThread().getName() + "] try to lock Receiver");
                if (toAccount.getLock().tryLock(LOCK_WAIT_SEC, TimeUnit.SECONDS)) {
                    try {
                        AccountDao.getInstance().withdraw(fromAccount, amount);
                        AccountDao.getInstance().deposit(toAccount, amount);
                        isCompletedSuccessfully = true;
                    } finally {
                        LOGGER.debug("[" + Thread.currentThread().getName() + "] unlock Receiver");
                        toAccount.getLock().unlock();
                    }
                } else {
                    toAccount.incFailedTransferCount();
                    LOGGER.debug("[" + Thread.currentThread().getName() + "] unable to lock Receiver");
                    isCompletedSuccessfully = false;
                }
            } finally {                
                LOGGER.debug("[" + Thread.currentThread().getName() + "] unlock Sender");
                fromAccount.getLock().unlock();
            }
        } else {
            fromAccount.incFailedTransferCount();
            LOGGER.debug("[" + Thread.currentThread().getName() + "] unable to lock Sender");
            isCompletedSuccessfully = false;
        }
        
        return isCompletedSuccessfully;
    }
}
