package ch7;

public interface Bank {
    public static final int NUMBER_OF_CUSTOMERS = 5;
    public void addCustomer(int customerNumber, int[] max);
    public void getState(); // print available, maximum, need and allocation 
    public boolean requestResources(int customerNumber, int[] rsrc);
    public boolean releaseResources(int customerNumber, int[] rsrc);
}