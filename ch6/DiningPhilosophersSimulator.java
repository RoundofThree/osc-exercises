package ch6;

import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DiningPhilosophersSimulator {
    public static void main(String[] args) {
        Table t = new MonitorTable();
        for (int i=0; i<5; ++i) {
            (new Thread(new Philosopher(t, i))).start();
        }
    }
}

class Philosopher implements Runnable {
    private Table t;
    private int s;
    public Philosopher(Table t,int seat) {
        this.t = t;
        this.s = seat;
    }
    public void run() {
        while (true) {
            try {
                t.getForks(s);
                System.out.println(s + " is eating.");
                //Thread.sleep(100);
                t.releaseForks(s);
                Thread.sleep(100);
            } catch (InterruptedException e) {}
        }
    }
}

interface Table {
    public void getForks(int k) throws InterruptedException;
    public void releaseForks(int k) throws InterruptedException;
}

// the semaphore vesion is the same as Semaphore[5], each with 1 permit 
class MonitorTable implements Table {
    private int[] forks; // determines the ownership of the fork at i
    private Condition[] available;
    private Lock key;
    public MonitorTable() {
        key = new ReentrantLock();
        available = new Condition[5];
        forks = new int[5];
        for (int i=0; i<5; ++i) {
            available[i] = key.newCondition();
        }
        Arrays.fill(forks, -1);
    }
    public void getForks(int k) throws InterruptedException {
        key.lock();
        try {
            if (k == 4) {  // avoid deadlock
                while (forks[(k+1)%5]!=-1) available[(k+1)%5].await();
                forks[(k+1)%5] = k;
                while (forks[(k+5-1)%5]!=-1) available[(k+5-1)%5].await();
                forks[(k+5-1)%5] = k;
            } else {
                while (forks[(k+5-1)%5]!=-1) available[(k+5-1)%5].await();
                forks[(k+5-1)%5] = k;
                while (forks[(k+1)%5]!=-1) available[(k+1)%5].await();
                forks[(k+1)%5] = k;
            }
        } finally {key.unlock();}
    }
    public void releaseForks(int k) throws InterruptedException {
        key.lock();
        try {
            forks[(k+5-1)%5] = -1;
            available[(k+5-1)%5].signal();
            forks[(k+1)%5] = -1;
            available[(k+1)%5].signal();
        } finally {key.unlock();}
    }
}

// semaphore version: use Semaphor[5] instead of Condition[5]