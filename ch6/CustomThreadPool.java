package ch6;

import java.util.concurrent.LinkedBlockingQueue;

class CustomThreadPool {
    private int n;
    private final Worker[] w;
    private final LinkedBlockingQueue<Runnable> queue;
    public CustomThreadPool(int n) {
        this.n = n;
        w = new Worker[this.n];
        queue = new LinkedBlockingQueue<>(n);
        // start the worker threads
        for (int i=0; i<n; ++i) {
            w[i] = new Worker(queue);
            w[i].start();
        }
    }

    public void add(Runnable t) {
        synchronized (queue) {
            queue.add(t);
            queue.notify();
        }
    }

    public void stopPool() {
        for (Worker i : w) {
            i.interrupt();
        }
    }
}

class Worker extends Thread {
    private LinkedBlockingQueue<Runnable> q;
    public Worker(LinkedBlockingQueue<Runnable> q) {
        this.q = q;
    }
    public void run() {
        while (!interrupted()) {
            Runnable task;
            synchronized(q) {
                while (q.isEmpty()) {
                    try {
                        q.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                task = q.poll();
            }
            try {
                task.run();
            } catch (RuntimeException e) {}
        }
    }
}