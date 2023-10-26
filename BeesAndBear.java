package Part_A;

import java.util.concurrent.CountDownLatch;

public class BeesAndBear {

    private static final int N = 10; // capacity of the honey pot

    private static volatile int honey = 0; // current amount of honey in the pot
    private static volatile boolean bearIsAwake = false; // flag indicating whether the bear is awake
    private static final CountDownLatch honeyPotFullLatch = new CountDownLatch(N); // latch that is used to signal the bear when the honey pot is full

    private static class Bee extends Thread {
        private int id;

        public Bee(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            while (true) {
                // print that the bee is collecting one portion of honey
                System.out.println("Bee #" + id + " is collecting one portion of honey");

                // collect one portion of honey
                int portion = 1;

                // synchronize on the honey pot to add the portion of honey
                synchronized (honeyPotFullLatch) {
                    honey += portion;

                    // print that the bee has added the portion of honey to the pot
                    System.out.println("Bee #" + id + " has added a portion of honey to the pot");

                    // check if the honey pot is full
                    if (honey == N) {
                        // print that the bee has woken up the bear
                        System.out.println("Bee #" + id + " has woken up the bear");

                        // wake up the bear
                        bearIsAwake = true;

                        // count down the latch to signal the bear that the honey pot is full
                        honeyPotFullLatch.countDown();
                    }
                }

                // sleep for 5000 ms
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // wait for the bear to finish eating the honey
                synchronized (honeyPotFullLatch) {
                    while (bearIsAwake) {
                        try {
                            honeyPotFullLatch.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private static class Bear extends Thread {
        @Override
        public void run() {
            while (true) {
                // print that the bear is waiting for the honey pot to be full
                System.out.println("The bear is waiting for the honey pot to be full");

                // wait for the honey pot to be full
                synchronized (honeyPotFullLatch) {
                    while (!bearIsAwake) {
                        try {
                            honeyPotFullLatch.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // print that the bear is eating the honey
                System.out.println("The bear is eating the honey");

                // eat all the honey
                honey = 0;

                // print that the bear has finished eating the honey
                System.out.println("The bear has finished eating the honey");

                // fall asleep
                bearIsAwake = false;

                // sleep for 5000 ms
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // notify the bees that the honey pot is empty
                honeyPotFullLatch.countDown();
            }
        }
    }

    public static void main(String[] args) {
        // create n bees
        Bee[] bees = new Bee[N];
        for (int i = 0; i < N; i++) {
            bees[i] = new Bee(i);
            bees[i].start();
        }

        // create the bear
        Bear bear = new Bear();
        bear.start();

        // wait for all bees and the bear to finish
        try {
            bear.join();
            for (Bee bee : bees) {
                bee.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
