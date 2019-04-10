package kz.ya.mt.api.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author yerlana
 */
public class AccountTest {

    @Test
    public void objectsShouldBeEqual() {
        Account accountOne = new Account("sameNumber", BigDecimal.ONE);
        Account accountTwo = new Account("sameNumber", BigDecimal.ONE);

        boolean isTheSame = accountOne.equals(accountTwo);

        Assert.assertTrue(isTheSame);
    }

    @Test
    public void objectsShouldBeEqualWhenTheyAreTheSameInstance() {
        Account account = new Account("sameNumber", BigDecimal.ONE);

        boolean isTheSame = account.equals(account);

        Assert.assertTrue(isTheSame);
    }

    @Test
    public void objectsShouldNotBeEqualWhenOneIsNull() {
        Account account = new Account("sameNumber", BigDecimal.ONE);

        boolean isTheSame = account.equals(null);

        Assert.assertFalse(isTheSame);
    }

    @Test
    public void objectsShouldBeInTheSameBucket() {
        Account accountOne = new Account("sameNumber", BigDecimal.ONE);
        Account accountTwo = new Account("sameNumber", BigDecimal.ONE);

        boolean isTheSame = accountOne.hashCode() == accountTwo.hashCode();

        Assert.assertTrue(isTheSame);
    }

    @Test
    public void shouldNotBeTheSameAsOtherObject() {
        Account account = new Account("sameNumber", BigDecimal.ONE);
        Object anotherObject = new Object();

        boolean isTheSame = account.equals(anotherObject);

        Assert.assertFalse(isTheSame);
    }
    
    @Test
    public void objectsShouldNotBeEqualWhenNumberIsDifferent() {
        Account accountOne = new Account("number1", BigDecimal.ONE);
        Account accountTwo = new Account("number2", BigDecimal.ONE);

        boolean isTheSame = accountOne.equals(accountTwo);

        Assert.assertFalse(isTheSame);
    }

    @Test
    public void objectsShouldBeEqualWhenNumberIsSameButBalanceIsDifferent() {
        Account accountOne = new Account("number1", BigDecimal.ONE);
        Account accountTwo = new Account("number1", BigDecimal.TEN);

        boolean isTheSame = accountOne.equals(accountTwo);

        Assert.assertTrue(isTheSame);
    }
    
    @Test
    public void objectsShouldNotBeEqualWhenNumberIsSameButCreatedAtIsDifferent() throws Exception {
        Account accountOne = new Account("number1", BigDecimal.ONE);
        
        // send current thread to sleep for 1 sec, to make the difference in time
        Thread.sleep(1000);
        
        Account accountTwo = new Account("number1", BigDecimal.TEN);

        boolean isTheSame = accountOne.equals(accountTwo);

        Assert.assertFalse(isTheSame);
    }
    
    @Test
    public void objectsShouldBeEqualWhenNumberIsSameButModifiedAtIsDifferent() {
        Account accountOne = new Account("number1", BigDecimal.ONE);
        Account accountTwo = new Account("number1", BigDecimal.TEN);
        accountTwo.setModifiedAt(LocalDateTime.MIN);

        boolean isTheSame = accountOne.equals(accountTwo);

        Assert.assertTrue(isTheSame);
    }
}
