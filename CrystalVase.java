import java.lang.System;
import java.lang.Math;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Arrays;

public class CrystalVase {

    public static int numThreads = 100, numVisitsAllowed = 0, capacity = 500, numVisitsPerGuest = 0;
    public static Thread[] threads = new Thread[numThreads];
    public static VaseGuest[] guests = new VaseGuest[numThreads];

    public static void main(String[] args)
    {
        long startTime = System.nanoTime();

        Queue queue = new Queue(capacity);

        for (int i=0; i<numThreads; i++)
        {
            VaseGuest guest = new VaseGuest(i, queue);
            threads[i] = new Thread(guest);
            guests[i] = guest;
            threads[i].start();
        }

        for (int i=0; i<numVisitsAllowed; i++)
        {
            int selectedGuest = (int)(Math.random() * numThreads);
            Thread.State state = threads[selectedGuest].getState();
            if (state == Thread.State.TERMINATED)
                continue;
            if (state != Thread.State.WAITING)
            {
                i--;
                continue;
            }

            guests[selectedGuest].threadNotify();

            if (VaseGuest.numSawVase == numThreads)
                break;
        }

        if (numVisitsAllowed == 0)
        {
            boolean allSawVase = false;
            while (!allSawVase)
            {
                int selectedGuest = (int)(Math.random() * numThreads);
                Thread.State state = threads[selectedGuest].getState();
                if (state != Thread.State.WAITING)
                    continue;

                guests[selectedGuest].threadNotify();

                allSawVase = VaseGuest.numSawVase == numThreads;
            }
        }

        VaseGuest.isDone = true;
        for (int i=0; i<numThreads; i++)
        {
            Thread.State state = threads[i].getState();
            while (state != Thread.State.TERMINATED)
            {
                if (state == Thread.State.WAITING)
                    guests[i].threadNotify();
                
                state = threads[i].getState();
            }

            try
            {
                threads[i].join();
            }
            catch (Exception e)
            {}
        }

        if (VaseGuest.numSawVase == numThreads)
            System.out.println("All guests saw the vase at least once");
        else
            System.out.println("Not every guest has seen the vase at least once");

        long endTime = System.nanoTime();
        double executionTime = (endTime - startTime) * 1E-9;
        System.out.println("Execution time: " + executionTime);
    }
}

class VaseGuest implements Runnable
{
    public int threadID;
    public Queue queue;
    public boolean sawVase;
    public static boolean isDone = false;
    public static int numSawVase = 0;
    public static ReentrantLock lock = new ReentrantLock();

    public VaseGuest(int threadID, Queue queue)
    {
        this.threadID = threadID;
        this.queue = queue;
        this.sawVase = false;
    }

    public void run()
    {
        if (CrystalVase.numVisitsPerGuest == 0)
        {
            while (!VaseGuest.isDone)
            {
                threadWait();
                boolean isQueued = false;
                while (!isQueued)
                    isQueued = this.queue.enq(this.threadID);

                int upFront = -1;
                while (upFront != this.threadID)
                    upFront = this.queue.readFront();

                int dequeuedID = this.queue.deq();
                if (dequeuedID == this.threadID)
                {
                    if (!this.sawVase)
                    {
                        System.out.println("Thread #" + this.threadID + " has seen the vase");
                        incNumSawVase();
                        this.sawVase = true;
                    }
                }
                else
                    System.out.println("Queue dequeued wrong guest");
            }
        }
        else
        {
            for (int i=0; i<CrystalVase.numVisitsPerGuest && !VaseGuest.isDone; i++)
            {
                threadWait();

                boolean isQueued = false;
                while (!isQueued)
                    isQueued = this.queue.enq(this.threadID);

                int upFront = -1;
                while (upFront != this.threadID)
                {
                    upFront = this.queue.readFront();
                }

                int dequeuedID = this.queue.deq();
                if (dequeuedID == this.threadID)
                {
                    if (!this.sawVase)
                    {
                        System.out.println("Thread #" + this.threadID + " has seen the vase");
                        incNumSawVase();
                        this.sawVase = true;
                    }
                }
                else
                    System.out.println("Queue dequeued wrong guest");
            }
        }
    }

    public void incNumSawVase()
    {
        lock.lock();
        try
        {
            VaseGuest.numSawVase++;
        }
        finally
        {
            lock.unlock();
        }
    }

    public synchronized void threadWait()
    {
        try
        {
            this.wait();
        }
        catch (Exception e)
        {}
    }

    public synchronized void threadNotify()
    {
        this.notify();
    }
}

class Queue
{
    public int head = 0, tail = 0, capacity;
    public int[] threadIDs;
    public ReentrantLock lock = new ReentrantLock();

    public Queue(int capacity)
    {
        this.threadIDs = new int[capacity];
        this.capacity = capacity;
        Arrays.fill(this.threadIDs, -1);
    }

    public boolean enq(int threadID)
    {
        lock.lock();
        try
        {
            if (this.tail - this.head == this.capacity)
                return false;
            this.threadIDs[this.tail%this.capacity] = threadID;
            this.tail++;
        }
        finally
        {
            lock.unlock();
        }

        return true;
    }

    public int deq()
    {
        if (this.head == this.tail)
            return -1;

        int threadID = this.threadIDs[this.head%this.capacity];
        this.threadIDs[this.head%this.capacity] = -1;
        this.head++;
        return threadID;
    }

    public int readFront()
    {
        lock.lock();
        try
        {
            if (this.head == this.tail)
                return -1;
        
            return this.threadIDs[this.head%this.capacity];
        }
        finally
        {
            lock.unlock();
        }
    }
}