package co.uk.codebreeze.tokenbucket;

import com.google.common.base.Ticker;
import java.math.BigDecimal;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;

public class MemoryFixedIntervalRefillStrategyTest {
    
    @Test
    public void testFillsBasedOnRate() throws InterruptedException {
        //given
        final long periodInSeconds = 3;
        final TimeUnit periodUnit = TimeUnit.SECONDS;
        final BigDecimal tokensToAddEachPeriod = BigDecimal.valueOf(2);
        final Ticker ticker = Ticker.systemTicker();
        final Semaphore semaphore = new Semaphore(0, true);
//        System.out.println("periodInSeconds:" + periodInSeconds);
//        System.out.println("amountToAddPerPeriod:" + tokensToAddEachPeriod);
//        System.out.println("semaphore:" + semaphore);
        
        //when
        final MemoryFixedIntervalRefillStrategy refiller = new MemoryFixedIntervalRefillStrategy(
                semaphore,
                ticker, 
                tokensToAddEachPeriod,
                periodInSeconds,
                periodUnit,
                ticker.read());
        
        final int zeroToBegin = semaphore.drainPermits();
//        System.out.println("zeroToBegin:" + zeroToBegin);
        
        Thread.sleep(1000);
//        System.out.println("1 second later ..");
        
        final int stillZero = zeroToBegin + semaphore.drainPermits();
//        System.out.println("stillZero:" + stillZero);
        
        Thread.sleep(3000);
//        System.out.println("3 seconds later..");
        
        final int nowTwo = stillZero + semaphore.drainPermits();
//        System.out.println("nowTwo:" + nowTwo);
        
        Thread.sleep(3000);
//        System.out.println("3 seconds later..");
        
        final int nowFour = nowTwo + semaphore.drainPermits();
//        System.out.println("nowFour:" + nowFour);
        
        Thread.sleep(12000);
//        System.out.println("12 seconds later..");
        
        final int nowTwelve = nowFour + semaphore.drainPermits();
//        System.out.println("nowTwelve:" + nowTwelve);
        
        //then
        
        assertEquals(0, zeroToBegin);
        assertEquals(0, stillZero);
        assertEquals(2, nowTwo);
        assertEquals(4, nowFour);
        assertEquals(12, nowTwelve);
    }
}