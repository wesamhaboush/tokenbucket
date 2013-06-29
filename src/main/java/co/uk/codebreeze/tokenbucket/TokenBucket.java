package co.uk.codebreeze.tokenbucket;

public interface TokenBucket {

    public boolean tryAcquire();

    public boolean tryAcquire(int numTokens);

    public void acquire();

    public void acquire(int numTokens);
}
