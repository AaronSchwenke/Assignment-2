import java.lang.System;
import java.lang.Math;

public class Birthday
{
    public static int numThreads = 200;
    public static int numInvites = 0;
    public static Thread[] threads = new Thread[numThreads];
    public static Guest[] guests = new Guest[numThreads];
    public static boolean cupcakeAvailable = true;

    public static final int UNTIL_ALL_FOUND = 0;

    public static void main(String[] args)
    {
        long startTime = System.nanoTime();

        Counter counter = new Counter(0);
        threads[0] = new Thread(counter);
        guests[0] = counter;
        threads[0].start();

        for (int i=1; i<numThreads; i++)
        {
            Guest guest = new Guest(i);
            threads[i] = new Thread(guest);
            guests[i] = guest;
            threads[i].start();
        }

        for (int i=0; i<numInvites; i++)
        {
            int selectedGuest = (int)(Math.random() * numThreads);
            Thread.State state = threads[selectedGuest].getState();
            while (state != Thread.State.WAITING)
            {
                state = threads[selectedGuest].getState();
            }

            guests[selectedGuest].threadNotify();
            
            if (selectedGuest == 0)
            {
                state = threads[0].getState();
                while (state != Thread.State.WAITING)
                {
                    state = threads[0].getState();
                }

                if (Counter.everyoneEntered)
                    break;
            }
        }

        if (numInvites == UNTIL_ALL_FOUND)
        {
            while (!Counter.everyoneEntered)
            {
                int selectedGuest = (int)(Math.random() * numThreads);
            Thread.State state = threads[selectedGuest].getState();
            while (state != Thread.State.WAITING)
            {
                state = threads[selectedGuest].getState();
            }

            guests[selectedGuest].threadNotify();
            
            if (selectedGuest == 0)
            {
                state = threads[0].getState();
                while (state != Thread.State.WAITING)
                {
                    state = threads[0].getState();
                }
            }
            }
        }

        for (int i=0; i<numThreads; i++)
        {
            guests[i].done = true;
        }

        long endTime = System.nanoTime();
        double executionTime = (endTime - startTime) * 1E-9;

        if (Counter.everyoneEntered)
            System.out.println("Everyone has entered the maze at least once.");
        else
            System.out.println("The counter cannot guarantee everyone entered the maze at least once.");

        System.out.println("Execution time: " + executionTime);
    }

    public static synchronized boolean replaceCupcakeIfNeeded()
    {
        boolean cupcakeAvailable = Birthday.cupcakeAvailable;
        if (!cupcakeAvailable)
        {
            Birthday.cupcakeAvailable = true;
        }
        return cupcakeAvailable;
    }

    public static synchronized boolean eatCupcakeIfPossible()
    {
        boolean cupcakeAvailable = Birthday.cupcakeAvailable;
        if (cupcakeAvailable)
        {
            Birthday.cupcakeAvailable = false;
        }
        return cupcakeAvailable;
    }
}

class Counter extends Guest
{
    public int count = 1;
    public static boolean everyoneEntered = false;

    public Counter(int threadID)
    {
        super(threadID);
    }

    public void run()
    {
        this.threadWait();
        System.out.println("Guest #" + (threadID + 1) + " has entered the maze.");
        while (!done)
        {
            boolean cupcakeWasEaten = Birthday.replaceCupcakeIfNeeded();
            if (cupcakeWasEaten)
                this.count++;

            if (this.count == Birthday.numThreads)
                Counter.everyoneEntered = true;

            threadWait();
        }
    }
}

class Guest implements Runnable
{
    public boolean satisfied = false, done = false;
    public int threadID;

    public Guest(int threadID)
    {
        this.threadID = threadID;
    }

    public void run()
    {
        this.threadWait();
        System.out.println("Guest #" + (threadID + 1) + " has entered the maze.");
        while (!done)
        {
            if (!this.satisfied)
            {
                boolean cupcakeAvailable = Birthday.eatCupcakeIfPossible();
                if (cupcakeAvailable)
                    this.satisfied = true;
            }

            threadWait();
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

        return;
    }

    public synchronized void threadNotify()
    {
        this.notify();
    }
}