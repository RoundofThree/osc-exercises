package ch7;

import java.util.Random;


// Deadlock Prevention 
// Circular wait scheme --> lock ordering via System.identityHashcode(obj)

class LockOrdering {
    public static void main(String[] args) {
        OrderedLockBank bank = new OrderedLockBank();
        Account acc1 = new Account();
        Account acc2 = new Account();
        acc1.deposit(10.5);
        acc2.deposit(100);
        (new Thread(new BankSimulator(bank, acc1, acc2))).start();
        (new Thread(new BankSimulator(bank, acc2, acc1))).start();
    }
}

class BankSimulator implements Runnable {
    public static Random rand = new Random();
    OrderedLockBank bank;
    Account acc1; 
    Account acc2;
    public BankSimulator(OrderedLockBank bank, Account acc1, Account acc2) {
        this.bank = bank;
        this.acc1 = acc1; 
        this.acc2 = acc2; 
    }
    public void run() {
        while (true) {
            bank.transfer(acc1, acc2, rand.nextDouble());
            System.out.println("Transfer success");
            Thread.yield();
        }
    }
}

class OrderedLockBank {
    public static Object tieLock = new Object(); // shared by all objects of this class 
    public OrderedLockBank() {

    }
    public void transfer(Account from, Account to, double amount) {
        int fromHash = System.identityHashCode(from);
        int toHash = System.identityHashCode(to);
        if (fromHash < toHash) {
            synchronized (from) {
                synchronized (to) {
                    from.withdraw(amount);
                    to.deposit(amount);
                }
            }
        } else if (fromHash > toHash) {
            synchronized (to) {
                synchronized (from) {
                    from.withdraw(amount);
                    to.deposit(amount);
                }
            }
        } else {
            synchronized (tieLock) {
                synchronized (from) {
                    synchronized (to) {
                        from.withdraw(amount);
                        to.deposit(amount);
                    }
                }
            }
        }
    }
}

class Account {
    double balance;
    public Account() {
        this.balance = 0;
    }
    public void deposit(double amount) {
        balance += amount;
    }
    public void withdraw(double amount) {
        balance -= amount; 
    }
}