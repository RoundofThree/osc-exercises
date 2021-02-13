package ch4;

import java.util.ArrayList;

class Summation {
    public static void main(String[] args) {
        // parse input 
        if (args.length == 0) {
            System.err.println("Usage: Summation <upperint>");
            return;
        }
        if (Integer.parseInt(args[0].trim()) < 0) {
            System.err.println("Input must be <= 0");
            return;
        } 
        Sum s = new Sum();
        int upper = Integer.parseInt(args[0].trim());
        // divide into four threads
        ArrayList<Thread> threads = new ArrayList<Thread>();
        int fourth = upper/4; // floor division
        int lower = 0;
        while (lower <= upper) {
            Thread t = new Thread(new SummationRunner(lower, Math.min(upper, lower+fourth), s));
            threads.add(t);
            t.start();;
            lower += fourth + 1;
        }
        // wait for all threads 
        try {
            for (Thread t : threads) t.join();
        } catch (InterruptedException e) {}
        System.out.println(s.getSum());
    }
}

// Shared data 
class Sum {
    private int sum;

    public Sum() {
        sum = 0;
    }

    public int getSum() {
        return sum;
    }

    public synchronized void add(int val) {
        sum += val;  // maybe non atomic in bytecode 
    }
}

class SummationRunner implements Runnable {
    private int lower;
    private int upper;
    private Sum s;

    public SummationRunner(int lower, int upper, Sum s) {
        this.lower = lower;
        this.upper = upper;
        this.s = s;
    }
    // add summation of [lower, upper] to s
    public void run() {
        int tmp = 0;
        while (lower <= upper) {
            tmp += lower;
            ++lower;
        }
        s.add(tmp);
    }
}