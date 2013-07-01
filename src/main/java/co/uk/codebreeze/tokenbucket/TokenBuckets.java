package co.uk.codebreeze.tokenbucket;



import com.google.common.base.Ticker;
import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.concurrent.TimeUnit;

public final class TokenBuckets
{

  private TokenBuckets() {}

  /*
   * use to create a bucket with a capacity X, filling it at a rate of N per period P. The very generic case is this one. The 
   */
    public static TokenBucket newStartedMemoryFixedIntervalRefill(int capacityTokens, long refillTokens, long period, TimeUnit unit) {
        final Ticker ticker = Ticker.systemTicker();
        final SemaphoreBackedTokenBucket tokenBucket = new SemaphoreBackedTokenBucket(0, capacityTokens, true);
        final RefillStrategy strategy = new MemoryFixedIntervalRefillStrategy(tokenBucket, ticker, BigDecimal.valueOf(refillTokens), period, unit, ticker.read());
        return tokenBucket;
    }
    
    /*
     * fill complete bucket every period! This is the special case of capacity = refill Magnitude / period.
     * expect peaks at each interval end like this:
     * |\   |\___|\   |\
     * | \__|      \  | \
     * |            \_|  \
     */
    public static TokenBucket newDelayedPulsedIntervalRefill(int capacityTokens, long delay, TimeUnit delayUnit, long period, TimeUnit periodUnit) {
        final Ticker ticker = Ticker.systemTicker();
        final BigDecimal periodInNanos = BigDecimal.valueOf(periodUnit.toNanos(period));
        final BigDecimal delayInNanos = BigDecimal.valueOf(delayUnit.toNanos(delay));
        final BigDecimal unitsPerNano = BigDecimal.valueOf(capacityTokens).divide(periodInNanos, 10, RoundingMode.DOWN);
        System.out.println("period in nanos:" + periodInNanos);
        System.out.println("delay in nanos:" + delayInNanos);
        System.out.println("units per nanos:" + unitsPerNano);
        //equation: initialTokens = ( 1 - (delay / period ) ) * unitsPerNano
//        final int initialTokensByRatioOfDelay = (BigDecimal.ONE.subtract(delayInNanos.divide(periodInNanos))).multiply(BigDecimal.valueOf(capacityTokens)).intValue();
        final int initialTokensByRatioOfDelay = ((delayInNanos.divide(periodInNanos))).multiply(BigDecimal.valueOf(capacityTokens)).intValue();
        System.out.println("initial tokens:" + initialTokensByRatioOfDelay);
        final SemaphoreBackedTokenBucket tokenBucket = new SemaphoreBackedTokenBucket(initialTokensByRatioOfDelay, capacityTokens, true);
        final RefillStrategy strategy = new MemoryFixedIntervalRefillStrategy(tokenBucket, ticker, BigDecimal.valueOf(capacityTokens), period, periodUnit, ticker.read() - delayUnit.toNanos(delay));
        return tokenBucket;
    }
    
    /*
     * trickle tokens at a rate decided by capacity/period. This is the special case where the rate is decided 
     * by dividing capacity/periodInNanos and period of refilling is 1 nano
     * expect a curve that looks like (no flat areas because we are always filling:
     * 
     * 
     * 
     *   /\          /\/\
     *  /  \/\/\/\/\/    \
     * /                  \/\
     */
    public static TokenBucket newStartedMemoryNanoIntervalRefill(int capacityTokens, long period, TimeUnit unit) {
        final Ticker ticker = Ticker.systemTicker();
        final SemaphoreBackedTokenBucket tokenBucket = new SemaphoreBackedTokenBucket(0, capacityTokens, true);
        final BigDecimal unitsPerNano = BigDecimal.valueOf(capacityTokens).divide(BigDecimal.valueOf(unit.toNanos(period)), 10, RoundingMode.DOWN);
        final RefillStrategy strategy = new MemoryFixedIntervalRefillStrategy(tokenBucket, ticker, unitsPerNano, 1, TimeUnit.NANOSECONDS, ticker.read());
        return tokenBucket;
    }
}
