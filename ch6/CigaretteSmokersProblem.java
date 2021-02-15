package ch6;

import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class CigaretteSmokersProblem {
    public static void main(String[] args) {
        IngTable t = new MonitorIngTable();
        (new Thread(new Agent(t))).start();
        (new Thread(new Smoker(t, 0, 1))).start();
        (new Thread(new Smoker(t, 1, 2))).start();
        (new Thread(new Smoker(t, 0, 2))).start();
    }
}

class Agent implements Runnable {
    private IngTable t;
    public Agent(IngTable t) {
        this.t = t;
    }
    public void run() {
        while (true) {
            try {
                t.fillTable();
            } catch (InterruptedException e) {}
            // Thread.yield();
        }
    }
}

class Smoker implements Runnable {
    private IngTable t;
    private int a, b;
    public Smoker(IngTable t, int a, int b) {
        this.t = t;
        this.a = a; this.b = b;
    }
    public void run() {
        while (true) {
            try {
                t.use(a, b);
            } catch (InterruptedException e) {}
        }
    }
}

interface IngTable {
    public void fillTable() throws InterruptedException;
    public void use(int a, int b) throws InterruptedException;
}

// using monitors and condition variables 
class MonitorIngTable implements IngTable {
    private Condition consumed, ready;
    private Random r;
    private Lock k;
    boolean[] ings;
    public MonitorIngTable() {
        k = new ReentrantLock();
        consumed = k.newCondition();
        ready = k.newCondition();
        ings = new boolean[3];
        r = new Random();
    }
    public void fillTable() throws InterruptedException {
        k.lock();
        try {
            if (ings[0] || ings[1] || ings[2]) {
                consumed.await();
            }
            int tmp = r.nextInt(3);
            ings[tmp] = true;
            int tmp2 = r.nextInt(3);
            while (tmp2 == tmp) tmp2 = r.nextInt(3);
            ings[tmp2] = true;
            System.out.println("There you go!");
            ready.signalAll(); // there could be some smokers not happy with the current ingredients 
        } finally {
            k.unlock();
        }
    }
    public void use(int a, int b) throws InterruptedException {
        k.lock();
        try {
            while (!ings[a] || !ings[b]) ready.await();
            ings[a] = false;
            ings[b] = false;
            System.out.println("Ummmmm... I am OK now");
            consumed.signal();  // all agents should behave identical to this signal 
        } finally {
            k.unlock();
        }
    }
}

// semaphores: 
// mutex on agent code and smoker code
// agent acquires Empty sem and then releases Full sem 
// smoker acquires Full sem and then releases Empty sem