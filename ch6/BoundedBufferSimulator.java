package ch6;

import java.util.Date;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedBufferSimulator {
    public static void main(String[] args) {
        Buffer<Date> buff = new SemaphoreBoundedBuffer<Date>();
        for (int i=0; i<5; ++i) {
            (new Thread(new Producer(buff))).start();
            (new Thread(new Consumer(buff))).start();
        }
    }
}

// Producer class
class Producer implements Runnable {
    Buffer<Date> buffer;

    public Producer(Buffer<Date> buff) {
        this.buffer = buff;
    }
    public void run() {
        try {
            while (true) {
                Thread.sleep(100);
                buffer.insert(new Date());
                System.out.println("Producer produces.");
            }
        } catch (InterruptedException e) {}
    }
}

// Consumer class
class Consumer implements Runnable {
    Buffer<Date> buffer;
    public Consumer(Buffer<Date> buff) {
        this.buffer = buff;
    }
    public void run() {
        try {
            while (true) {
                Thread.sleep(100);
                Date d = buffer.remove();
                System.out.println("Consumer consumes " + d.getTime());
            }
        } catch (InterruptedException e) {}
    }
}

interface Buffer<E> {
    public void insert(E i) throws InterruptedException;
    public E remove() throws InterruptedException;
}

// using Semaphores (lower level)
class SemaphoreBoundedBuffer<E> implements Buffer<E> {
    private static final int BUFFER_SIZE = 5;
    private E[] buffer; 
    private int in, out;
    private Semaphore mutex;  
    private Semaphore empty;
    private Semaphore full;

    public SemaphoreBoundedBuffer() {
        in=0; out=0;
        mutex = new Semaphore(1);
        empty = new Semaphore(BUFFER_SIZE);
        full = new Semaphore(0);
        buffer = (E[]) new Object[BUFFER_SIZE];
    }

    public void insert(E i) throws InterruptedException {
        empty.acquire();
        try {
            mutex.acquire();
            try {
                buffer[in] = i;
                in = (in+1)%BUFFER_SIZE;
            } finally { 
                mutex.release();
            }
        } finally {
            full.release();
        }
    }

    public E remove() throws InterruptedException {
        full.acquire();
        E ret;
        try {
            mutex.acquire();
            try {
                ret = buffer[out];
                out = (out+1)%BUFFER_SIZE;
            } finally {
                mutex.release();
            }
        } finally {
            empty.release();
        }
        return ret;
    }
}

// using monitors (higher level)
class MonitorBoundedBuffer<E> implements Buffer<E> {
    // use wait and notify and synchronized
    private static final int BUFFER_SIZE = 5;
    private E[] buffer;
    private int count, in, out;
    public MonitorBoundedBuffer() {
        count = 0;
        in = 0; out = 0;
        buffer = (E[]) new Object[BUFFER_SIZE];
    }

    public synchronized void insert(E i) throws InterruptedException {
        while (count == BUFFER_SIZE) {
            wait();
        }
        buffer[in] = i;
        in = (in+1)%BUFFER_SIZE;
        ++count;
        notifyAll();
    }
    // must compete for the lock of the buffer object 
    public synchronized E remove() throws InterruptedException {
        while (count == 0) {
            wait();  // goes to wait queue
        }
        E ret = buffer[out];
        out = (out+1)%BUFFER_SIZE;
        --count;
        notifyAll();
        return ret;
    }
}

// using reentrant lock --> key.lock(); 
// using condition variables --> newCondition()
class LockBoundedBuffer<E> implements Buffer<E> {
    private static final int BUFFER_SIZE = 5;
    private Lock key; 
    private E[] buffer;
    private int in, out, count;
    private Condition notEmpty, notFull;
    public LockBoundedBuffer() {
        key = new ReentrantLock();
        notEmpty = key.newCondition();  // notifies 
        notFull = key.newCondition();
        buffer = (E[]) new Object[BUFFER_SIZE];
        in = 0; out=0; count = 0;
    }
    public void insert(E i) throws InterruptedException {
        key.lock(); 
        try {
            while (count == BUFFER_SIZE) notFull.await();  // like wait, release the lock and enter wait queue
            buffer[in] = i;
            in = (in+1)%BUFFER_SIZE;
            ++count;
            notEmpty.signal(); // signal is best because we know which threads are waiting for this condition
        } finally {
            key.unlock();
        }
    }
    public E remove() throws InterruptedException {
        key.lock(); 
        E ret;
        try {
            while (count == 0) notEmpty.await();
            ret = buffer[out];
            out = (out+1)%BUFFER_SIZE;
            --count;
            notFull.signal();
        } finally {
            key.unlock();
        }
        return ret;
    }
}
