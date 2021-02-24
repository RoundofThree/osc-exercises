package ch7;

// the difference here is that customer number here is hardcoded
class Banker implements Bank {
    private int n; // number of P
    private int m; // number of R 
    private int[] available; 
    private int[][] max, allocated, need;
    
    public Banker(int[] initialResources) {
        m = initialResources.length;
        n = Bank.NUMBER_OF_CUSTOMERS;  
        available = new int[m];
        System.arraycopy(initialResources, 0, available, 0, m);
        max = new int[n][m];
        allocated = new int[n][m];
        need = new int[n][m];
    }

    @Override
    public void addCustomer(int customerNumber, int[] maxDemand) {
        System.arraycopy(maxDemand, 0, max[customerNumber], 0, maxDemand.length);
        System.arraycopy(maxDemand, 0, need[customerNumber], 0, maxDemand.length);
    }

    @Override
    public void getState() {
		System.out.print("Available = \t[");
		for (int i = 0; i < m-1; i++)
			System.out.print(available[i]+" ");
		System.out.println(available[m-1]+"]");
		System.out.print("\nAllocation = \t");
		for (int i = 0; i < n; i++) {
			System.out.print("[");
			for (int j = 0; j < m-1; j++)
				System.out.print(allocated[i][j]+" ");
			System.out.print(allocated[i][m-1]+"]");
		}
		System.out.print("\nMax = \t\t");
		for (int i = 0; i < n; i++) {
			System.out.print("[");
			for (int j = 0; j < m-1; j++)
				System.out.print(max[i][j]+" ");
			System.out.print(max[i][m-1]+"]");
		}
		System.out.print("\nNeed = \t\t");
		for (int i = 0; i < n; i++) {
			System.out.print("[");
			for (int j = 0; j < m-1; j++)
				System.out.print(need[i][j]+" ");
			System.out.print(need[i][m-1]+"]");
		}

		System.out.println();
		if (isSafe()) {
			System.out.println("We are safe,no deadlock :D");
		} else System.out.println("We are not safe,deadlock waring ");

	    System.out.println();
    }

    // helper
    private boolean arrLessThan(int[] a, int[] b) {
        for (int i=0; i<a.length; ++i) {
            if (a[i] >= b[i]) return false;
        }
        return true;
    }

    // safety algorithm 
    private boolean isSafe() {
        boolean[] finish = new boolean[n];  // default false 
        int[] work = new int[m];
        System.arraycopy(available, 0, work, 0, m);
        for (int p=0; p<n; ++p) {
            if (finish[p] == true) continue; 
            // if need[p] > work then continue;
            if (arrLessThan(work, need[p])) continue;
            // update work array 
            for (int i=0; i<m; ++i) {
                work[i] += allocated[p][i];
            }
            finish[p] = true;
            p = -1;  // refresh again 
        }
        for (boolean b : finish) {
            if (!b) return false;
        }
        return true;
    }

    @Override
    public synchronized boolean requestResources(int customerNumber, int[] request) {
        for (int i=0; i<request.length; ++i) {
            if (request[i] > need[customerNumber][i]) return false;
            if (request[i] > available[i]) return false;
        }
        // simulate request granted 
        // available -= request 
        // allocated += request 
        // need -= request
        for (int i=0; i<m; ++i) {
            available[i] -= request[i];
            allocated[customerNumber][i] += request[i];
            need[customerNumber][i] -= request[i];
        }
        if (isSafe()) return true;
        // else rollback 
        for (int i=0; i<m; ++i) {
            available[i] += request[i];
            allocated[customerNumber][i] -= request[i];
            need[customerNumber][i] += request[i];
        }
        return false;
    }

    @Override
	public synchronized boolean releaseResources(int customerNumber, int[] resources) { 
        for (int i=0; i<resources.length; ++i) {
            if (allocated[customerNumber][i] < resources[i]) return false;
        }
		for (int i=0; i<m; ++i) {
            allocated[customerNumber][i] -= resources[i];
            need[customerNumber][i] += resources[i];
            available[i] += resources[i];
        }
		return true;
	}
}