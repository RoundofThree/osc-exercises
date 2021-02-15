package ch6;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ReaderWriterSimulator {
    public static void main(String[] args) {
        ReadWriteLock db = new LockDatabase();
        for (int i=0; i<3; ++i) {
            (new Thread(new Reader(db))).start();
        }
        (new Thread(new Writer(db))).start();
    }
}

class Writer implements Runnable {
    ReadWriteLock db;
    public Writer(ReadWriteLock l) {
        this.db = l;
    }   
    public void run() {
        while (true) {
            try {
                db.acquireWriteLock();
                System.out.println("I am writing.");
                db.releaseWriteLock();
            } catch (InterruptedException e) {
            }
        }
    }
}

class Reader implements Runnable {
    ReadWriteLock db;
    public Reader(ReadWriteLock l) {
        this.db = l;
    }   
    public void run() {
        while (true) {
            try {
                db.acquireReadLock();
                System.out.println("I am reading.");
                db.releaseReadLock();
            } catch (InterruptedException e) {
            }
        }
    }
}

interface ReadWriteLock {
    public void acquireReadLock() throws InterruptedException;
    public void releaseReadLock() throws InterruptedException;
    public void acquireWriteLock() throws InterruptedException;
    public void releaseWriteLock() throws InterruptedException;
}

// First problem: starvation of writers 
class SemaphoreFirstDatabase implements ReadWriteLock {
    private int readCount;
    private Semaphore mutex;
    private Semaphore db;
    public SemaphoreFirstDatabase() {
        readCount = 0;
        mutex = new Semaphore(1, true);
        db = new Semaphore(1, true);  // ensure FIFO fairness
    }
    public void acquireReadLock() throws InterruptedException {
        mutex.acquire(); 
        try {
            ++readCount;
            if (readCount == 1) { // first reader wait for current writer to finish 
                db.acquire();
            }
        } finally {
            mutex.release();
        }
    }
    public void releaseReadLock() throws InterruptedException {
        mutex.acquire();
        try {
            --readCount;
            if (readCount == 0) db.release();  // last reader 
        } finally {
            mutex.release();
        }
    }
    public void acquireWriteLock() throws InterruptedException {
        db.acquire();
    }
    public void releaseWriteLock() throws InterruptedException {
        db.release();
    }
}
// Second problem: starvation of readers
class SemaphoreSecondDatabase implements ReadWriteLock {
    private int writeCount, readCount;
    private Semaphore mutex1, mutex2, mutex3, read, write;
    public SemaphoreSecondDatabase() {
        writeCount = readCount = 0;
        mutex1 = new Semaphore(1, true);
        mutex2 = new Semaphore(1, true); 
        mutex3 = new Semaphore(1, true);
        read = new Semaphore(1, true);
        write = new Semaphore(1, true);
    }
    public void acquireReadLock() throws InterruptedException {
        mutex3.acquire();
        try {
            read.acquire();
            try {
                mutex1.acquire();
                try {
                    ++readCount;
                    if (readCount == 1) write.acquire();
                } finally {mutex1.release();}
            } finally {read.release();}
        } finally {mutex3.release();} 
    }
    public void releaseReadLock() throws InterruptedException {
        mutex1.acquire();
        try {   
            --readCount;
            if (readCount == 0) write.release();
        } finally {mutex1.release();}
    }
    public void acquireWriteLock() throws InterruptedException {
        mutex2.acquire();
        try {
            ++writeCount;
            if (writeCount == 1) read.acquire();  // hold until last writer, block readers to be added 
        } finally {mutex2.release();}
        write.acquire();  // avoid other writers
    }
    public void releaseWriteLock() throws InterruptedException {
        write.release();
        mutex2.acquire();
        try {
            --writeCount;
            if (writeCount == 0) read.release();
        } finally {mutex2.release();}
    }
}
// Third problem: no starvation
class SemaphoreThirdDatabase implements ReadWriteLock {
    private Semaphore mutex, db;
    // readCount is the number of readers reading 
    // writeCount is the number of writers who are waiting or writing 
    private int readCount, writeCount;
    public SemaphoreThirdDatabase() {
        mutex = new Semaphore(1, true);
        db = new Semaphore(1, true);
        readCount = writeCount = 0;
    }
    
    public void acquireReadLock() throws InterruptedException {
        mutex.acquire();
        try {
            if (writeCount > 0 || readCount == 0) {  // enter waiting list if there are writers waiting or readers terminated 
                mutex.release();  // return the permit for now 
                db.acquire();  // wait in the queue
                mutex.acquire();
            }
            ++readCount;  
        } finally {
            mutex.release();
        }
    }
    public void releaseReadLock() throws InterruptedException {
        mutex.acquire();
        try {
            --readCount;
            if (readCount == 0) {
                db.release();  // last reader releases 
            }
        } finally {mutex.release();}
    }
    public void acquireWriteLock() throws InterruptedException {
        mutex.acquire(); 
        try {
            ++writeCount;  // increase count of writers who are waiting or executing 
        } finally {mutex.release();}
        db.acquire();
    }
    public void releaseWriteLock() throws InterruptedException {
        mutex.acquire(); 
        try {
            --writeCount;
        } finally {mutex.release();}
        db.release();
    }
}

// Monitor: WRONG --> starvation of writers 
class MonitorDatabase implements ReadWriteLock {
    // writeCount are writers writing
    // readCount are readers reading 
    private int activeRead, activeWrite;
    private int waitRead, waitWrite;
    public MonitorDatabase() {
        activeRead = activeWrite = 0;
        waitRead = waitWrite = 0;
    }
    public synchronized void acquireReadLock() throws InterruptedException {
        // 
        if (waitWrite + activeWrite > 0) {
            // wait until it's ready to read
            ++waitRead;
            while (activeWrite+waitWrite>0) wait();
            --waitRead;
        }
        ++activeRead;
        notifyAll();
    }
    public synchronized void releaseReadLock() throws InterruptedException {
        --activeRead;
        if (activeRead == 0) notifyAll();  // notify writers 
    }
    public synchronized void acquireWriteLock() throws InterruptedException {
        if (activeWrite>0 || activeRead>0) {
            ++waitWrite;
            while (activeWrite>0 || activeRead>0) wait();
            --waitWrite;
        }
        activeWrite = 1;
    }
    public synchronized void releaseWriteLock() throws InterruptedException {
        activeWrite = 0;
        notifyAll();  // notify readers if there are waitingreaders, otherwise notify writers 
    }
}

// CORRECT 
class LockDatabase implements ReadWriteLock {
    private ReentrantLock key;
    private Condition canRead, canWrite;
    private int readers, writers, wr, ww;  // active and waiting r and w
    public LockDatabase() {
        readers = writers = wr = ww = 0;
        key = new ReentrantLock();
        canRead = key.newCondition();
        canWrite = key.newCondition();
    }

    public void acquireReadLock() throws InterruptedException {
        key.lock();
        try {
            if (writers + ww > 0) {
                ++wr;
                canRead.await();
                --wr;
            }
            ++readers;
            canRead.signal();
        } finally {key.unlock();}
    }

    public void releaseReadLock() throws InterruptedException {
        key.lock();
        try {
            if (--readers == 0) canWrite.signal();
        } finally {key.unlock();}
    }

    public void acquireWriteLock() throws InterruptedException {
        key.lock();
        try {
            if (writers == 1 || readers > 0) {
                ++ww;
                canWrite.await();
                --ww;
            }
            writers = 1;
        } finally {key.unlock();}
    }

    public void releaseWriteLock() throws InterruptedException {
        key.lock();
        try {
            writers = 0;
            if (wr > 0) canRead.signal();
            else canWrite.signal();
        } finally {key.unlock();}
    }

}
