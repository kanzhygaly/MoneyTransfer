/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.ya.mt.api;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import kz.ya.mt.api.dao.AccountDao;
import kz.ya.mt.api.model.Account;

/**
 *
 * @author yerlana
 */
public class ConcurrentTransferTest {

    private static final int NUMBER_OF_THREADS = 3;

    private ExecutorService executorService;
    private Waiter waiter;

    @Before
    public void setUp() {
        waiter = new Waiter();
        executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    }

    @After
    public void tearDown() {
        executorService.shutdown();
    }

    @Test
//    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void shouldHandleConcurrentTransactions() throws Exception {
        // given
        final Account acc1 = AccountDao.getInstance().create(new BigDecimal(100));
        final Account acc2 = AccountDao.getInstance().create(new BigDecimal(50));

        // send 5 EUR: AC1 -> AC2
        final Transaction transaction1 = Transaction
                .builder()
                .id("transaction_1")
                .createdAt(LocalDateTime.now())
                .from(sender)
                .to(receiver)
                .money(Money.of(CurrencyUnit.EUR, 5))
                .build();

        // send 10 EUR: AC1 -> AC2
        final Transaction transaction2 = Transaction
                .builder()
                .id("transaction_2")
                .createdAt(LocalDateTime.now())
                .from(sender)
                .to(receiver)
                .money(Money.of(CurrencyUnit.EUR, 10))
                .build();

        // send 1 EUR: AC2 -> AC1
        final Transaction transaction3 = Transaction
                .builder()
                .id("transaction_3")
                .createdAt(LocalDateTime.now())
                .from(receiver)
                .to(sender)
                .money(Money.of(CurrencyUnit.EUR, 1))
                .build();

        // when
        executorService.submit(() -> commitTransaction(transaction1));
        executorService.submit(() -> commitTransaction(transaction2));
        executorService.submit(() -> commitTransaction(transaction3));

        waiter.await(5, TimeUnit.SECONDS, 3);

        // then
        Money senderMoney = accountRepository.get(sender.number()).get().money();
        Money receiverMoney = accountRepository.get(receiver.number()).get().money();

        assertThat(transactionRepository.get().size()).isEqualTo(3);
        assertThat(senderMoney).isEqualTo(Money.of(CurrencyUnit.EUR, 86));
        assertThat(receiverMoney).isEqualTo(Money.of(CurrencyUnit.EUR, 64));
    }

    @Test
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void shouldPassOnlyOneTransaction() throws Exception {
        // given
        final Account sender = createSenderAccount("AC1", Money.of(CurrencyUnit.EUR, 10));
        final Account receiver = createReceiverAccount("AC2", Money.of(CurrencyUnit.EUR, 0));
        accountRepository.create(sender);
        accountRepository.create(receiver);

        // send 10 EUR: AC1 -> AC2
        final Transaction transaction1 = Transaction
                .builder()
                .id("transaction_1")
                .createdAt(LocalDateTime.now())
                .from(sender)
                .to(receiver)
                .money(Money.of(CurrencyUnit.EUR, 10))
                .build();

        // send 10 EUR: AC1 -> AC2
        final Transaction transaction2 = Transaction
                .builder()
                .id("transaction_2")
                .createdAt(LocalDateTime.now())
                .from(sender)
                .to(receiver)
                .money(Money.of(CurrencyUnit.EUR, 10))
                .build();

        // when
        executorService.submit(() -> commitTransaction(transaction1));
        executorService.submit(() -> commitTransaction(transaction2));

        waiter.await(5, TimeUnit.SECONDS, 1);

        // then
        Money senderMoney = accountRepository.get(sender.number()).get().money();
        Money receiverMoney = accountRepository.get(receiver.number()).get().money();

        assertThat(transactionRepository.get().size()).isEqualTo(1);
        assertThat(senderMoney).isEqualTo(Money.of(CurrencyUnit.EUR, 0));
        assertThat(receiverMoney).isEqualTo(Money.of(CurrencyUnit.EUR, 10));
    }

    private void commitTransaction(Transaction transaction) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(3000));
            
            transactionRepository.commit(transaction);
            
            waiter.assertNotNull(transaction);
            
            System.out.println(String.format("executing: %s, time: %d ms, thread: %s",
                    transaction.id(),
                    TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS),
                    Thread.currentThread().getName())
            );
            
            waiter.resume();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
