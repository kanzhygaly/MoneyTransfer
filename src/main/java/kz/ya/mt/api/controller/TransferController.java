package kz.ya.mt.api.controller;

import io.javalin.Context;
import kz.ya.mt.api.exception.AccountNotFoundException;
import kz.ya.mt.api.exception.NotEnoughFundsException;
import kz.ya.mt.api.exception.TransferToTheSameAccountException;
import kz.ya.mt.api.model.Account;
import kz.ya.mt.api.dao.AccountDao;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

public class TransferController {

    private static final int LOCK_WAIT_SEC = 5;

    public void process(final Context ctx) throws Exception {

        String fromAccountNo = ctx.formParam("fromAccountNo");
        String toAccountNo = ctx.formParam("toAccountNo");

        if (fromAccountNo.equals(toAccountNo)) {
            throw new TransferToTheSameAccountException();
        }

        BigDecimal amount = new BigDecimal(ctx.formParam("amount"));

        Account fromAccount = AccountDao.getInstance().get(fromAccountNo).orElseThrow(
                () -> new AccountNotFoundException(fromAccountNo));

        Account toAccount = AccountDao.getInstance().get(toAccountNo).orElseThrow(
                () -> new AccountNotFoundException(toAccountNo));

        if (fromAccount.getLock().tryLock(LOCK_WAIT_SEC, TimeUnit.SECONDS)) {
            try {
                if (fromAccount.getBalance().compareTo(amount) < 0) {
                    fromAccount.incFailedTransferCount();
                    throw new NotEnoughFundsException(fromAccount.getNumber());
                }

                if (toAccount.getLock().tryLock(LOCK_WAIT_SEC, TimeUnit.SECONDS)) {
                    try {
                        AccountDao.getInstance().withdraw(fromAccount, amount);
                        AccountDao.getInstance().deposit(toAccount, amount);
                        ctx.status(200); // OK
                    } finally {
                        toAccount.getLock().unlock();
                    }
                } else {
                    toAccount.incFailedTransferCount();
                    ctx.status(408); // Request Timeout
                }
            } finally {
                fromAccount.getLock().unlock();
            }
        } else {
            fromAccount.incFailedTransferCount();
            ctx.status(408); // Request Timeout
        }
    }
}
