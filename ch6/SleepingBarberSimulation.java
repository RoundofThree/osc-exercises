package ch6;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class SleepingBarberSimulation {
    public static void main(String[] args) {
        BarberShop s = new MonitorBarberShop(3);
        for (int i=0; i<5; ++i) {
            (new Thread(new Client(s))).start();
        }
        (new Thread(new Barber(s))).start();
    }
}

class Barber implements Runnable {
    private BarberShop s;
    public Barber(BarberShop s) {
        this.s = s;
    }
    public void run() {
        while (true) {
            try {
                s.serve();
            } catch (InterruptedException e) {}
            Thread.yield();
        }
    }
}

class Client implements Runnable {
    private BarberShop s;
    public Client(BarberShop s) {
        this.s = s;
    }
    public void run() {
        while (true) {
            try {
                s.enter();
            } catch (InterruptedException e) {}
            Thread.yield();
        }
    }
}

interface BarberShop {
    public void enter() throws InterruptedException;
    public void serve() throws InterruptedException;
}

// using monitor
class MonitorBarberShop implements BarberShop {
    private int chairs, max;
    Condition barberAvailable, clientAvailable;
    Lock k;
    public MonitorBarberShop(int n) {
        k = new ReentrantLock();
        max = n;
        chairs = n;
        barberAvailable = k.newCondition();
        clientAvailable = k.newCondition();
    }
    public void enter() throws InterruptedException {
        k.lock();
        try {
            if (chairs > 0) {
                --chairs;
                clientAvailable.signal();
                barberAvailable.await();  // this is signal and continue
                System.out.println("Cutting hair...");
            }
            System.out.println("So sad...");
        } finally {k.unlock();}
    }
    public void serve() throws InterruptedException {
        k.lock();
        try {
            while (chairs == max) clientAvailable.await();
            barberAvailable.signal();
            ++chairs;
        } finally {k.unlock();}
    }
}

// using semaphor
// Semaphor freeBarber(0)
// Semaphor clientAvailable(0)
// client releases clientAvailable and acquires freeBarber
// barber releases freeBarber and acquires clientAvailable 
