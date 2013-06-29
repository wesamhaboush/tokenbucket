package co.uk.codebreeze.tokenbucket;

import com.google.common.base.Ticker;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import java.util.concurrent.TimeUnit;

public class MemoryFixedIntervalRefillStrategy implements RefillStrategy {

    //1 sec is the minimum update period, because less than that it becomes busy loop
    private static final long DEFAULT_MIN_UPDATE_NANOS = TimeUnit.SECONDS.toNanos(1);
    private final Ticker ticker;
    private final BigDecimal tokensPerPeriod;
    private final long periodInNanos;
    private long previousRefillTime;
    private final Semaphore semaphore;
    //BidDecimal balance will be changing all the time
    private BigDecimal balance;

    public MemoryFixedIntervalRefillStrategy(Semaphore semaphore, Ticker ticker, BigDecimal numTokens, long period, TimeUnit unit, long startTimeInNanos) {
        this.ticker = ticker;
        this.tokensPerPeriod = numTokens;
        this.periodInNanos = unit.toNanos(period);
        this.previousRefillTime = startTimeInNanos;
        this.balance = BigDecimal.ZERO;
        this.semaphore = semaphore;
        //run every second
        if (unit.toNanos(period) > DEFAULT_MIN_UPDATE_NANOS) {
            Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new Runnable() {
                public void run() {
                    refill();
                }
            }, 0, period, unit);
        } else {
            Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new Runnable() {
                public void run() {
                    refill();
                }
            }, 0, DEFAULT_MIN_UPDATE_NANOS, TimeUnit.NANOSECONDS);
        }
    }

    public void refill() {
        final long nowInNanos = ticker.read();
        //how many nanos passed since last refill
        final long differenceInNanos = nowInNanos - previousRefillTime;
        //register this as last time the refiller was used, which will be used in future calculations
        previousRefillTime = nowInNanos;
        //now the difference should tell you how much tokens you need to add
        //the equation is: differenceInNanos/periodInNanos * numTokensPerPeriod
        final BigDecimal tokensToAdd =
                BigDecimal.valueOf(differenceInNanos)
                .divide(BigDecimal.valueOf(periodInNanos), 10, RoundingMode.DOWN)
                .multiply(tokensPerPeriod);
        //now add this to the balance
        balance = balance.add(tokensToAdd);
        //now take out the integral part, which means the remainder fraction stays in balance for next time
        final int tokensToReturn = balance.intValue();
        balance = balance.subtract(BigDecimal.valueOf(tokensToReturn));
//        System.out.println(String.format("balance[%s], tokensToReturn[%s]", balance, tokensToReturn));
        semaphore.release(tokensToReturn);
    }
}
