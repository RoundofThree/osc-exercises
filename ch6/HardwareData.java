package ch6;

class HardwareData<E> {
    private E d;
    public HardwareData(E d) {
        this.d = d;
    }
    public E get() {
        return d;
    }
    public void set(E i) {
        this.d = i;
    }
    public synchronized E getAndSet(E i) {
        E old = d;
        d = i;
        return old;
    }
    public synchronized void swap(HardwareData<E> o) {
        E tmp = d;
        d = o.get();
        o.set(tmp);
    }
}