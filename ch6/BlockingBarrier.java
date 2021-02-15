package ch6;

interface Barrier {
    /**
    * Each thread calls this method when it reaches
    * the barrier. All threads are released to continue
    * processing when thelast thread calls this method.
    */
    public void waitForOthers() throws InterruptedException;
    /**
    * Release all threads from waiting for the barrier.
    * Any future calls to waitForOthers() will not wait
    * until the Barrier is set again with a call
    * to the constructor.
    */
    public void freeAll() throws InterruptedException;
}

public class BlockingBarrier implements Barrier {
    private int curr, n;
    private boolean freed;
    public BlockingBarrier(int n) {
        this.n = n;
        curr = n;
        freed = false;
    }
    public synchronized void waitForOthers() throws InterruptedException {
        --curr;
        if (curr > 0 && !freed) {  // wait 
            wait();
        }
        if (curr == 0) notifyAll();
        ++curr;
    }

    public synchronized void freeAll() throws InterruptedException {
        freed = true;
        notifyAll();
    }
}