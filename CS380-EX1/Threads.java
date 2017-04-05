import java.util.Random;

// Shows a simple example of creating multiple threads.
// This program has three threads, not two!
// The main method itself is running in the first thread.
// It then creates two more threads.
// If your program needs two threads, you only have to create one!
public final class Threads {

    // Shared variable used by both created threads
    private static volatile int x = 0;

    public static void main(String[] args) throws InterruptedException {

        // Creates a Runnable object that simply increments x then waits a random
        // amount of time up to 1000 milliseconds in a loop.
        Runnable adder = () -> {
            Random random = new Random();
            while (true) {
                x++;
                try {
                    Thread.sleep(random.nextInt(1000));
                } catch (InterruptedException e) {
                    return;
                }
            }
        };

        // Creates a Runnable object that simply decrements x then waits a random
        // amount of time up to 1000 milliseconds in a loop.
        Runnable subber = () -> {
            Random random = new Random();
            while (true) {
                x--;
                try {
                    Thread.sleep(random.nextInt(1000));
                } catch (InterruptedException e) {
                    return;
                }
            }
        };

        // Creates thread objects that will execute each of the above Runnables.
        Thread adderThread = new Thread(adder);
        Thread subberThread = new Thread(subber);

        // Starts the threads.  The code in the above Runnables will now be executing
        // in the background as the main thread continues to the while loop below.
        adderThread.start();
        subberThread.start();

        // No modification of x occurs in this loop.  To prove the above Runnables are
        // actually executing in the background, we will see that the value of x being
        // printed will be changing over time.        
        while (true) {
            System.out.println("x = " + x);
            Thread.sleep(1000);
        }
    }
}