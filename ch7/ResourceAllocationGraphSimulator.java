package ch7;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ResourceAllocationGraphSimulator {
    public static void main(String[] args) {
        // input: available, max, allocation 
        // create customer threads and run them forever: meaning allocation is [0...] and available is [1...]
        int n = 5;   // 5 resource types 
        // Bank bank = new ResourceAllocationGraph(n); // all n resources have 1 instance 
        Bank bank = new Banker(new int[]{3,4,5,3,2}); // must be of size 5
        (new Thread(new Customer(0, n, bank))).start();
        (new Thread(new Customer(1, n, bank))).start();
        (new Thread(new Customer(2, n, bank))).start();
    }
}

class Customer implements Runnable {
    private static Random rand = new Random();
    private int num;
    private int[] max; 
    private Bank bank; 
    public Customer(int number, int n, Bank bank) {
        this.bank = bank;
        this.num = number;  
        max = new int[n];
        for (int i=0; i<n; ++i) {
            max[i] = rand.nextInt(3);
        }
        // add customer to bank 
        bank.addCustomer(number, max);
    }
    public void run() {
        while (true) {
            // craft a request array 
            int[] request = new int[max.length];
            for (int i=0; i<max.length; ++i) {
                if (max[i] == 0) continue;  // respect the max array which represents claim 
                request[i] = rand.nextInt(3);
            }
            // ask the bank 
            try {
                synchronized (bank) {
                    while (!bank.requestResources(num, request)) {
                        System.out.println("I am waiting");
                        bank.wait();
                    }
                    bank.getState();
                }
                // System.out.println("I got the resources and I am gonna release them");
                Thread.sleep(1000);
                bank.releaseResources(num, request);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            Thread.yield();
        }
    }
}

// only for single instances, broken if input is invalid 
class ResourceAllocationGraph implements Bank {
    private Map<Integer, Integer> customerMap; 
    private int nrsc;
    private List<List<Integer>> edges; // directed edges representing clain and assigment edges (not request edges)
    private int N;  // number of customers + resources
    public ResourceAllocationGraph(int numberOfResourceClasses) {
        edges = new ArrayList<>();
        for (int i=0; i<numberOfResourceClasses; ++i) {
            edges.add(new ArrayList<>());
        }
        nrsc = numberOfResourceClasses;
        N = numberOfResourceClasses;
        customerMap = new HashMap<>();
    }
    private boolean isSafe() {
        // check cycle existence
        boolean[] visited = new boolean[N];
        boolean[] inStack = new boolean[N];
        for (int i=0; i<N; ++i) {
            if (isCyclic(i, visited, inStack)) {
                return false;
            }
        }
        return true; 
    }
    private boolean isCyclic(int i, boolean[] visited, boolean[] inStack) {
        if (inStack[i]) return true;
        if (visited[i]) return false;  // visited but not in the stack, can prune it 
        visited[i] = true; 
        inStack[i] = true; 
        List<Integer> neighbors = edges.get(i);
        for (Integer c : neighbors) {
            if (isCyclic(c, visited, inStack)) return true; 
        }
        inStack[i] = false;
        return false;
    }
    public synchronized void addCustomer(int customerNumber, int[] max) {  // max is a vector of 0's and 1's
        edges.add(new ArrayList<>());
        customerMap.put(customerNumber, edges.size()-1); 
        ++N;
        List<Integer> requests = edges.get(edges.size()-1);
        for (int i=0; i<max.length; ++i) {
            if (max[i] == 1) requests.add(i);  // add claim edge
        }
    }

    public synchronized void getState() {
        System.out.println("Graph: ");
        for (int i=0; i<edges.size(); ++i) {
            for (Integer j : edges.get(i)) {
                System.out.println("{" + i + " -> " + j + "}");
            }
        }
    }

    // check that R -> P
    public synchronized boolean releaseResources(int customerNumber, int[] rsrc) {
        if (rsrc.length != nrsc) return false;  // you changed the model!! 
        // convert assignment to claim 
        int customer = customerMap.get(customerNumber); 
        for (int r=0; r<rsrc.length; ++r) {
            if (rsrc[r] == 1) {
                if (!edges.get(r).contains(customer)) return false;
            }
        }
        for (int r=0; r<rsrc.length; ++r) {
            if (rsrc[r] == 1) {
                edges.get(r).remove(Integer.valueOf(customer));
                edges.get(customer).add(r);
            }
        }
        System.out.println("Release resources...");
        return true;
    }

    // check that p -> R
    public synchronized boolean requestResources(int customerNumber, int[] rsrc) {
        // simulate change claim to assignment
        int customer = customerMap.get(customerNumber);
        List<Integer> requests = edges.get(customer); 
        for (int r=0; r<rsrc.length; ++r) {
            if (rsrc[r] == 1) {
                if (!requests.contains(r)) return false;
            }
        }
        for (int r=0; r<rsrc.length; ++r) {
            if (rsrc[r] == 1) {
                requests.remove(Integer.valueOf(r)); 
                edges.get(r).add(customer);  // bug!!!!!!!!!!!!!!!!!!
            }
        }
        if (isSafe()) {
            System.out.println("Request ok!");
            return true;
        }
        // rollback 
        for (int r=0; r<rsrc.length; ++r) {
            if (rsrc[r] == 1) {
                requests.add(r);
                edges.get(r).remove(Integer.valueOf(customer)); 
            }
        }
        return false; 
    }
}