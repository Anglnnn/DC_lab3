package Part_A;

import java.util.concurrent.CountDownLatch;

public class BeesAndBear {

    private static final int N = 10;

    private static volatile int honey = 0;
    private static volatile boolean bearIsAwake = false;
    private static final CountDownLatch honeyPotFullLatch = new CountDownLatch(N);

    private static class Bee extends Thread {
        private int id;

        public Bee(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            while (true) {

                System.out.println("Bee #" + id + " is collecting one portion of honey");


                int portion = 1;


                synchronized (honeyPotFullLatch) {
                    honey += portion;

                    System.out.println("Bee #" + id + " has added a portion of honey to the pot");

                    if (honey == N) {
                        System.out.println("Bee #" + id + " has woken up the bear");

                        bearIsAwake = true;

                        honeyPotFullLatch.countDown();
                    }
                }

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

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
                System.out.println("The bear is waiting for the honey pot to be full");

                synchronized (honeyPotFullLatch) {
                    while (!bearIsAwake) {
                        try {
                            honeyPotFullLatch.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                System.out.println("The bear is eating the honey");

                honey = 0;

                System.out.println("The bear has finished eating the honey");

                bearIsAwake = false;

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                honeyPotFullLatch.countDown();
            }
        }
    }

    public static void main(String[] args) {
        Bee[] bees = new Bee[N];
        for (int i = 0; i < N; i++) {
            bees[i] = new Bee(i);
            bees[i].start();
        }

        Bear bear = new Bear();
        bear.start();

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
