## Deadlock prevention algorithms 

## Focused on Mutex
But some objects are non-shareable... 

## Focused on Hold and Wait
Two schemes:
1. Allocate all (max) needed R before the execution of P. Implementation: 
```java
Lock requestLock = new ReentrantLock();   // make resource request mutex
...
requestLock.lock(); // request is protected by mutex
semaR1.acquire();
semaR2.acquire();
semaR3.acquire();
requestLock.unlock(); // better in a finally block 
// full body
semaR3.release();
semaR2.release();
semaR1.release(); 
```

2. The P releases all R before requesting another R (say I have R1, I want R1 and R2 simultaneously, I need to release R1, and then request for {R1, R2} in one go). The implementation is similar:
```java
Lock requestLock = new ReentrantLock();
...
requestLock.lock();
semaR1.acquire();
semaR2.acquire();
requestLock.unlock();
// use R1 and R2
semaR1.release();
semaR2.release(); 
// now I want to use R3 along with R1
requestLock.lock();
semaR1.acquire();
semaR3.acquire();
requestLock.unlock();
// use R1 and R3
semaR1.release();
semaR3.release(); 
```

## Focused on No-Preemption
Consider to preempt R if requested by higher priority P, when in a waiting state. 
```java
while (!condition_to_continue) {
    wait();  // will release all hooked resources 
}
```

## Focused on Circular Waiting 
Resources are numbered. P can only request in order. See implementation in [LockOrdering.java](LockOrdering.java). 

## Deadlock avoidance algorithms
*Requires a priori information: maximum requested resources*

### For single instance resources 
We use a **Resource Allocation Graph scheme** with claim edges. 

Safety algorithm: check for cycle, in `O(n^2)` time. 
Resource request algorithm: simulate Request edge --> Assignment edge, then check safety algorithm. 

### For multiple instance resources 
We use **Banker's algo**. 

State variables: `AVAILABLE`, `MAX`, `ALLOCATION`, `NEED`

But the input you need is: `AVAILABLE`, `MAX`, `ALLOCATION`

Safety algorithm: `O(m x n^2)`. See implementation. 
Resource request algorithm: check for validity and availability of request, then simulate the request, then check safety. 


## Deadlock detection algorithms 

### For single instance resources
We use a **Wait-for graph**, consisting of only processes.

Detection algo: check for cycle, in `O(n^2)`. 

### For multiple instance resources 
We use a modification of **Banker's algo**. 
*Modification*: replace `NEED` to `REQUEST`. 
