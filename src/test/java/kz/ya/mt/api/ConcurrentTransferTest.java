package kz.ya.mt.api;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import kz.ya.mt.api.controller.TransferController;
import kz.ya.mt.api.dao.AccountDao;
import kz.ya.mt.api.exception.NotEnoughFundsException;
import kz.ya.mt.api.model.Account;
import net.jodah.concurrentunit.Waiter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author yerlan.akhmetov
 */
public class ConcurrentTransferTest {

    private static final int NUMBER_OF_THREADS = 3;

    private ExecutorService executorService;
    private Waiter waiter;
    private TransferController transferController;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        waiter = new Waiter();
        executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        transferController = new TransferController();
        
        // reset singleton before each test
        Field instance = AccountDao.class.getDeclaredField("INSTANCE");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @After
    public void tearDown() {
        executorService.shutdown();
    }

    @Test
    public void shouldPassOnlyOneTransfer() throws Exception {
        final Account acc1 = AccountDao.getInstance().create(BigDecimal.TEN);
        final Account acc2 = AccountDao.getInstance().create(BigDecimal.ZERO);

        // send 10: AC1 -> AC2
        executorService.submit(() -> transfer(acc1, acc2, BigDecimal.TEN));
        // send 10: AC1 -> AC2
        executorService.submit(() -> transfer(acc1, acc2, BigDecimal.TEN));

        // block the main thread, until 1 thread is completed
        waiter.await(2, TimeUnit.SECONDS, 1);

        BigDecimal acc1Balance = AccountDao.getInstance().get(acc1.getNumber()).get().getBalance();
        BigDecimal acc2Balance = AccountDao.getInstance().get(acc2.getNumber()).get().getBalance();

        Assert.assertEquals(acc1Balance, BigDecimal.ZERO);
        Assert.assertEquals(acc2Balance, BigDecimal.TEN);
//        Assert.assertEquals(1, acc1.getFailCount());
//        Assert.assertEquals(0, acc2.getFailCount());
    }

    @Test
    public void shouldHandleConcurrentTransfers() throws Exception {
        final Account acc1 = AccountDao.getInstance().create(new BigDecimal(200));
        final Account acc2 = AccountDao.getInstance().create(new BigDecimal(50));

        // send 20: AC1 -> AC2
        executorService.submit(() -> transfer(acc1, acc2, new BigDecimal(20)));
        // send 15: AC2 -> AC1
        executorService.submit(() -> transfer(acc2, acc1, new BigDecimal(15)));
        // send 50: AC1 -> AC2
        executorService.submit(() -> transfer(acc1, acc2, new BigDecimal(50)));

        // block the main thread, until all 3 threads are completed
        waiter.await(5, TimeUnit.SECONDS, 3);

        BigDecimal acc1Balance = AccountDao.getInstance().get(acc1.getNumber()).get().getBalance();
        BigDecimal acc2Balance = AccountDao.getInstance().get(acc2.getNumber()).get().getBalance();

        Assert.assertEquals(acc1Balance, new BigDecimal(145));
        Assert.assertEquals(acc2Balance, new BigDecimal(105));
        Assert.assertEquals(0, acc1.getFailCount());
        Assert.assertEquals(0, acc2.getFailCount());
    }

    private void transfer(Account from, Account to, BigDecimal amount) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(3000));

            boolean isCompletedSuccessfully = transferController.performTransaction(from, to, amount);

            System.out.println("Transfer [" + from.getNumber() + "] -> [" + to.getNumber()
                    + "] result is " + isCompletedSuccessfully);

            System.out.println(String.format("executing: thread [%s], time: %d ms",
                    Thread.currentThread().getName(),
                    TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS))
            );

            // notify waiter that current thread is completed
            waiter.resume();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    @Test(expected = NotEnoughFundsException.class)
    public void shouldThrowNotEnoughFundsException() throws Exception {
        final Account acc1 = AccountDao.getInstance().create(BigDecimal.ZERO);
        final Account acc2 = AccountDao.getInstance().create(BigDecimal.ZERO);

        try {
            boolean isCompletedSuccessfully = transferController.performTransaction(acc1, acc2, BigDecimal.TEN);

            Assert.assertFalse(isCompletedSuccessfully);
        } catch (InterruptedException ex) {
            Assert.fail(ex.getMessage());
        }

        Assert.assertEquals(BigDecimal.ZERO, acc1.getBalance());
        Assert.assertEquals(BigDecimal.ZERO, acc2.getBalance());
        Assert.assertEquals(1, acc1.getFailCount());
        Assert.assertEquals(0, acc2.getFailCount());
    }

    @Test
    public void shouldFailToTransferWhenSenderIsLocked() throws Exception {
        final Account acc1 = AccountDao.getInstance().create(BigDecimal.TEN);
        final Account acc2 = AccountDao.getInstance().create(BigDecimal.TEN);

        acc1.getLock().lock();

        Thread checkThread = new Thread(() -> {
            try {
                boolean isCompletedSuccessfully = transferController.performTransaction(acc1, acc2, BigDecimal.ONE);

                Assert.assertFalse(isCompletedSuccessfully);
            } catch (InterruptedException ex) {
                Assert.fail(ex.getMessage());
            }

            Assert.assertEquals(BigDecimal.TEN, acc1.getBalance());
            Assert.assertEquals(BigDecimal.TEN, acc2.getBalance());
            Assert.assertEquals(1, acc1.getFailCount());
            Assert.assertEquals(0, acc2.getFailCount());
        });

        try {
            checkThread.start();
            checkThread.join();
        } finally {
            acc1.getLock().unlock();
        }
    }

    @Test
    public void shouldFailToTransferWhenReceiverIsLocked() throws Exception {
        final Account acc1 = AccountDao.getInstance().create(BigDecimal.TEN);
        final Account acc2 = AccountDao.getInstance().create(BigDecimal.TEN);

        acc2.getLock().lock();

        Thread checkThread = new Thread(() -> {
            try {
                boolean isCompletedSuccessfully = transferController.performTransaction(acc1, acc2, BigDecimal.ONE);

                Assert.assertFalse(isCompletedSuccessfully);
            } catch (InterruptedException ex) {
                Assert.fail(ex.getMessage());
            }

            Assert.assertEquals(BigDecimal.TEN, acc1.getBalance());
            Assert.assertEquals(BigDecimal.TEN, acc2.getBalance());
            Assert.assertEquals(0, acc1.getFailCount());
            Assert.assertEquals(1, acc2.getFailCount());
        });

        try {
            checkThread.start();
            checkThread.join();
        } finally {
            acc2.getLock().unlock();
        }
    }
}
