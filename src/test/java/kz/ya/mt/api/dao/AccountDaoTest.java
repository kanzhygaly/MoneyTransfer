package kz.ya.mt.api.dao;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import kz.ya.mt.api.exception.AccountAlreadyExistsException;
import kz.ya.mt.api.exception.AccountNotFoundException;
import kz.ya.mt.api.exception.EmptyAccountNumberException;
import kz.ya.mt.api.exception.NotEnoughFundsException;
import kz.ya.mt.api.exception.NullAccountBalanceException;
import kz.ya.mt.api.exception.NullInputAccountException;
import kz.ya.mt.api.exception.NullInputAmountException;
import kz.ya.mt.api.exception.TransferNegativeAmountException;
import kz.ya.mt.api.exception.TransferZeroAmountException;
import kz.ya.mt.api.model.Account;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author yerlan.akhmetov
 */
public class AccountDaoTest {

    @Before
    public void setUp() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        // reset singleton before each test
        Field instance = AccountDao.class.getDeclaredField("INSTANCE");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void constructorShouldBePrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<AccountDao> constructor = AccountDao.class.getDeclaredConstructor();

        Assert.assertTrue(Modifier.isPrivate(constructor.getModifiers()));

        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void instanceShouldBeTheSame() {
        AccountDao instance1 = AccountDao.getInstance();
        AccountDao instance2 = AccountDao.getInstance();

        Assert.assertEquals(instance1, instance2);
    }

    @Test
    public void shouldGetAccountWhenItExists() {
        final Account expResult = AccountDao.getInstance().create(BigDecimal.TEN);
        String number = expResult.getNumber();

        Account result = AccountDao.getInstance().get(number).get();

        Assert.assertEquals(expResult, result);
    }

    @Test
    public void shouldGetEmptyResultWhenAccountDoesNotExist() {
        String number = "wrongNumber";

        Optional<Account> result = AccountDao.getInstance().get(number);

        Assert.assertFalse(result.isPresent());
    }

    @Test
    public void shouldCreateNewAccount() {
        final Account account = AccountDao.getInstance().create(BigDecimal.TEN);

        final Account createdAccount = AccountDao.getInstance().get(account.getNumber()).get();

        Assert.assertEquals(createdAccount, account);
        Assert.assertEquals(createdAccount.getNumber(), account.getNumber());
        Assert.assertEquals(createdAccount.getBalance(), account.getBalance());
        Assert.assertEquals(createdAccount.getCreatedAt(), account.getCreatedAt());
        Assert.assertEquals(createdAccount.getModifiedAt(), account.getModifiedAt());
    }

    @Test(expected = AccountAlreadyExistsException.class)
    public void shouldNotCreateNewAccountWithNumberWhichAlreadyExists() {
        final Account account = AccountDao.getInstance().create(BigDecimal.TEN);

        AccountDao.getInstance().create(account.getNumber(), BigDecimal.TEN);
    }

    @Test(expected = EmptyAccountNumberException.class)
    public void shouldNotCreateAccountIfNumberIsNull() {
        AccountDao.getInstance().create(null, BigDecimal.TEN);
    }

    @Test(expected = EmptyAccountNumberException.class)
    public void shouldNotCreateAccountIfNumberIsEmpty() {
        AccountDao.getInstance().create("", BigDecimal.TEN);
    }

    @Test(expected = NullAccountBalanceException.class)
    public void shouldNotCreateAccountIfBalanceIsNull() {
        AccountDao.getInstance().create("some number", null);
    }

    @Test
    public void shouldWithdrawAccount() throws Exception {
        final Account account = AccountDao.getInstance().create(BigDecimal.TEN);
        LocalDateTime expectedModifiedAt = account.getModifiedAt();

        // send current thread to sleep for 1 sec, to make the difference in time
        Thread.sleep(1000);

        AccountDao.getInstance().withdraw(account, BigDecimal.ONE);

        final Account updatedAccount = AccountDao.getInstance().get(account.getNumber()).get();

        Assert.assertEquals(new BigDecimal(9), updatedAccount.getBalance());
        Assert.assertNotEquals(expectedModifiedAt.toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli(),
                updatedAccount.getModifiedAt().toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli());
    }

    @Test(expected = AccountNotFoundException.class)
    public void shouldNotWithdrawIfAccountDoesNotExist() {
        String number = "wrongNumber";

        AccountDao.getInstance().withdraw(new Account(number), BigDecimal.ONE);
    }

    @Test(expected = NullInputAccountException.class)
    public void shouldNotWithdrawIfAccountIsNull() {
        AccountDao.getInstance().withdraw(null, BigDecimal.ONE);
    }

    @Test(expected = NullInputAmountException.class)
    public void shouldNotWithdrawIfAmountIsNull() {
        AccountDao.getInstance().withdraw(new Account("some number"), null);
    }

    @Test(expected = TransferNegativeAmountException.class)
    public void shouldNotWithdrawIfAmountIsNegative() {
        AccountDao.getInstance().withdraw(new Account("some number"), new BigDecimal(-1));
    }

    @Test(expected = TransferZeroAmountException.class)
    public void shouldNotWithdrawIfAmountIsZero() {
        AccountDao.getInstance().withdraw(new Account("some number"), BigDecimal.ZERO);
    }

    @Test(expected = NotEnoughFundsException.class)
    public void shouldNotWithdrawIfAccountHasNotEnoughFunds() {
        final Account account = AccountDao.getInstance().create(BigDecimal.ZERO);

        AccountDao.getInstance().withdraw(account, BigDecimal.ONE);
    }

    @Test
    public void shouldDepositAccount() throws Exception {
        final Account account = AccountDao.getInstance().create(BigDecimal.ZERO);
        LocalDateTime expectedModifiedAt = account.getModifiedAt();

        // send current thread to sleep for 1 sec, to make the difference in time
        Thread.sleep(1000);

        AccountDao.getInstance().deposit(account, BigDecimal.ONE);

        final Account updatedAccount = AccountDao.getInstance().get(account.getNumber()).get();

        Assert.assertEquals(BigDecimal.ONE, updatedAccount.getBalance());
        Assert.assertNotEquals(expectedModifiedAt.toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli(),
                updatedAccount.getModifiedAt().toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli());
    }

    @Test(expected = AccountNotFoundException.class)
    public void shouldNotDepositIfAccountDoesNotExist() {
        String number = "wrongNumber";

        AccountDao.getInstance().deposit(new Account(number), BigDecimal.ONE);
    }

    @Test(expected = NullInputAccountException.class)
    public void shouldNotDepositIfAccountIsNull() {
        AccountDao.getInstance().deposit(null, BigDecimal.ONE);
    }

    @Test(expected = NullInputAmountException.class)
    public void shouldNotDepositIfAmountIsNull() {
        AccountDao.getInstance().deposit(new Account("some number"), null);
    }

    @Test(expected = TransferNegativeAmountException.class)
    public void shouldNotDepositIfAmountIsNegative() {
        AccountDao.getInstance().deposit(new Account("some number"), new BigDecimal(-1));
    }

    @Test(expected = TransferZeroAmountException.class)
    public void shouldNotDepositIfAmountIsZero() {
        AccountDao.getInstance().deposit(new Account("some number"), BigDecimal.ZERO);
    }

    @Test
    public void shouldDeleteAccount() {
        final Account account = AccountDao.getInstance().create(BigDecimal.ZERO);
        String number = account.getNumber();

        AccountDao.getInstance().delete(account);

        Optional<Account> result = AccountDao.getInstance().get(number);

        Assert.assertFalse(result.isPresent());
    }

    @Test(expected = AccountNotFoundException.class)
    public void shouldNotDeleteIfAccountDoesNotExist() {
        String number = "wrongNumber";

        AccountDao.getInstance().delete(new Account(number));
    }

    @Test(expected = NullInputAccountException.class)
    public void shouldNotDeleteIfAccountIsNull() {
        AccountDao.getInstance().delete(null);
    }

    @Test
    public void shouldClearDatastore() {
        Assert.assertTrue(AccountDao.getInstance().isDatastoreIsEmpty());

        AccountDao.getInstance().create(BigDecimal.ZERO);

        Assert.assertFalse(AccountDao.getInstance().isDatastoreIsEmpty());

        AccountDao.getInstance().clearDatastore();

        Assert.assertTrue(AccountDao.getInstance().isDatastoreIsEmpty());
    }

    @Test
    public void shouldReturnTrueIfDatastoreIsEmpty() {
        Assert.assertTrue(AccountDao.getInstance().isDatastoreIsEmpty());
    }
}
