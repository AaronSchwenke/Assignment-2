public class CrystalVase {

    public static int numThreads = 200;
    public static int numVisitsAllowed = 0;
    public static Thread[] threads = new Thread[numThreads];
    public static Guest[] guests = new Guest[numThreads];

    public static void main(String[] args)
    {
        for (int i=0; i<numThreads; i++)
        {
            Guest guest = new Guest(i);
            threads[i] = new Thread(guest);
            guests[i] = guest;
            threads[i].start();
        }
    }
}
