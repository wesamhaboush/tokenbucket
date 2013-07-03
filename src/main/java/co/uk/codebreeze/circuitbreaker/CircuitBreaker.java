package co.uk.codebreeze.circuitbreaker;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CircuitBreaker {


    enum State {
        OPEN, HALF_OPEN, CLOSED
    }
    private State state = State.OPEN;
    private final Runnable RESET_FAILURE_COUNTER = new Runnable(){

        @Override
        public void run() {
            failureCount.set(0);
        }
        
    };
    /**
     * The amount of failures until the circuit breaker is tripped.
     */
    private int tripThreshold;
    /**
     * How long to wait until the breaker is automatically reset.
     */
    private long tripTimeout;
    /**
     * The name of this breaker.
     */
    private final String breakerId;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

    public CircuitBreaker(int tripThreshold, long tripTimeout, String breakerId) {
        this.tripThreshold = tripThreshold;
        this.tripTimeout = tripTimeout;
        this.breakerId = breakerId;
    }

    private void resetFailureCount() {
        failureCount.set(0);
    }

    void success() {
        switch (state) {
            case CLOSED:
            case HALF_OPEN:
                resetFailureCount();
                state = State.CLOSED;
                break;
            case OPEN:
                state = State.HALF_OPEN;
        }
    }

    void failure() {
        switch (state) {
            case CLOSED:
                int value = failureCount.incrementAndGet();
                if(value >= tripThreshold){
                    state = State.OPEN;
                }
                break;
            case HALF_OPEN:
                state = State.OPEN;
                break;
            case OPEN:
        }
    }

    void start() {
        switch (state) {
            case CLOSED:
            case HALF_OPEN:
                break;
            case OPEN:
                SCHEDULER.schedule(RESET_FAILURE_COUNTER, tripTimeout, TimeUnit.MILLISECONDS);
                throw new RuntimeException("circuit breaker trip");
        }
    }
    
}
