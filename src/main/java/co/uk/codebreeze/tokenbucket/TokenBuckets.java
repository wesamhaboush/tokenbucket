package co.uk.codebreeze.tokenbucket;



import com.google.common.base.Ticker;
import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.concurrent.TimeUnit;

public final class TokenBuckets
{

  private TokenBuckets() {}

    public static TokenBucket newStartedMemoryFixedIntervalRefill(int capacityTokens, long refillTokens, long period, TimeUnit unit) {
        final Ticker ticker = Ticker.systemTicker();
        final SemaphoreBackedTokenBucket tokenBucket = new SemaphoreBackedTokenBucket(0, capacityTokens, true);
        final RefillStrategy strategy = new MemoryFixedIntervalRefillStrategy(tokenBucket, ticker, BigDecimal.valueOf(refillTokens), period, unit, ticker.read());
        return tokenBucket;
    }
    
    public static TokenBucket newStartedMemoryNanoIntervalRefill(int capacityTokens, long period, TimeUnit unit) {
        final Ticker ticker = Ticker.systemTicker();
        final SemaphoreBackedTokenBucket tokenBucket = new SemaphoreBackedTokenBucket(0, capacityTokens, true);
        final BigDecimal unitsPerNano = BigDecimal.valueOf(capacityTokens).divide(BigDecimal.valueOf(unit.toNanos(period)), 10, RoundingMode.DOWN);
        final RefillStrategy strategy = new MemoryFixedIntervalRefillStrategy(tokenBucket, ticker, unitsPerNano, 1, TimeUnit.NANOSECONDS, ticker.read());
        return tokenBucket;
    }
}
