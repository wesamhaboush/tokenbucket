package co.uk.codebreeze.tokenbucket;

import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;

public class TokenBucketsTest {
    
    @Test
    public void testMemoryBasedOne() throws InterruptedException {
        //given
        final long periodInSeconds = 1;
        final TimeUnit periodUnit = TimeUnit.SECONDS;
        final long tokensToAddEachPeriod = 5;
        final int capacity = 20;

        //when
        final TokenBucket tokenBucket = TokenBuckets.newStartedMemoryFixedIntervalRefill(
                capacity,
                tokensToAddEachPeriod,
                periodInSeconds,
                periodUnit);
        
        final boolean firstCantConsume = tokenBucket.tryAcquire();
        
        Thread.sleep(1002);
        
        final boolean thenManageToGetFive = tokenBucket.tryAcquire(5);
        
        Thread.sleep(5000);
        
        
        final boolean succeedToGetCapacity = tokenBucket.tryAcquire(20);
        final boolean failToGetMoreThanCapacity = tokenBucket.tryAcquire(1);
        
        //then
        assertFalse(firstCantConsume);
        assertTrue(thenManageToGetFive);
        assertFalse(failToGetMoreThanCapacity);
        assertTrue(succeedToGetCapacity);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMemoryBasedOneFailsOnTwoLargeTokensRequested() throws InterruptedException {
        //given
        final long periodInSeconds = 1;
        final TimeUnit periodUnit = TimeUnit.SECONDS;
        final long tokensToAddEachPeriod = 5;
        final int capacity = 20;

        //when
        final TokenBucket tokenBucket = TokenBuckets.newStartedMemoryFixedIntervalRefill(
                capacity,
                tokensToAddEachPeriod,
                periodInSeconds,
                periodUnit);

        Thread.sleep(5000);
        
        //then
        //should throw exception
        tokenBucket.tryAcquire(capacity + 1);
    }
    
    @Test
    public void testMemoryBasedWhenCapacityIsEvenlyDistributedOverPeriod() throws InterruptedException {
        //given
        final long periodInSeconds = 10;
        final TimeUnit periodUnit = TimeUnit.SECONDS;
        final int capacity = 20;

        //when
        final TokenBucket tokenBucket = TokenBuckets.newStartedMemoryNanoIntervalRefill(
                capacity,
                periodInSeconds,
                periodUnit);

        
        final boolean firstCantConsume = tokenBucket.tryAcquire();

        Thread.sleep(1002);

        final boolean thenManageToGetTwo = tokenBucket.tryAcquire(2);

        Thread.sleep(9000);


        final boolean succeedToGetCapacity = tokenBucket.tryAcquire(18);
        
        final boolean failToGetMoreThanCapacity = tokenBucket.tryAcquire(1);

        //then
        assertFalse(firstCantConsume);
        assertTrue(thenManageToGetTwo);
        assertFalse(failToGetMoreThanCapacity);
        assertTrue(succeedToGetCapacity);
    }
}