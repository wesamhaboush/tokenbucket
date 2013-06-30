package co.uk.codebreeze.tokenbucket;

import java.util.concurrent.Semaphore;

public class SemaphoreBackedTokenBucket extends Semaphore implements TokenBucket {

    private final int capacity;

    public SemaphoreBackedTokenBucket(final int initial, final int capacity) {
        this(initial, capacity, false);
    }

    public SemaphoreBackedTokenBucket(final int initial, final int capacity, final boolean fair) {
        super(initial, fair);
        if (initial > capacity) {
            throw new IllegalArgumentException(String.format("initial[%s] cannot be larger than capacity[%s]", initial, capacity));
        }
        this.capacity = capacity;
    }

    @Override
    public boolean tryAcquire() {
        return SemaphoreBackedTokenBucket.this.tryAcquire(1);
    }

    @Override
    public boolean tryAcquire(int numTokens) {
        System.out.println(String.format("tryAcquire tokens[%s], available[%s]", numTokens, availablePermits()));
        if (numTokens > capacity) {
            throw new IllegalArgumentException(String.format("cannot grant [%s] tokens, it is more than this bucket's capacity[%s]", numTokens, capacity));
        }
        return super.tryAcquire(numTokens);
    }

    @Override
    public void acquire() {
        SemaphoreBackedTokenBucket.this.acquire(1);
    }

    @Override
    public void acquire(int numTokens) {
        if (numTokens > capacity) {
            throw new IllegalArgumentException(String.format("cannot grant [%s] tokens, it is more than this bucket's capacity[%s]", numTokens, capacity));
        }
        try {
            super.acquire(numTokens);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void release() {
        SemaphoreBackedTokenBucket.this.release(1);
    }

    @Override
    public void release(int permits) {
        //top permits if it does not exceed capacity to do so, otherwise, top to capacity
        super.release(Math.min(permits, capacity - availablePermits()));
        System.out.println("tokenBucket size:" + this.availablePermits());
    }
}
