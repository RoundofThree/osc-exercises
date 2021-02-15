## Synchronisation techniques

1. Semaphores (counting semaphores or binary semaphores (mutex locks))
2. Monitors (Java monitor uses signal-and-continue)
3. ReentrantLock 
4. Condition variables of locks 

Really, semaphores are identical to monitors. When `wait()` is called, the thread returns the lock and enters the wait set until awakened. This is identical to releasing the mutex permit and trying to acquire a permit from a semaphor that represents the condition to be awakened. Basically, semaphores are like conditions that the monitor can have, but they are different in that condition signals can be ignored if the wait set is empty. Thus, semaphores are prone to human errors (ouch!).