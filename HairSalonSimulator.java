package Part_B;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class HairSalonSimulator {

    private static final int MAX_VISITORS = 10;

    private final Lock lock = new ReentrantLock();
    private final Condition barberCondition = lock.newCondition();
    private final Condition customerCondition = lock.newCondition();

    private volatile boolean isBarberBusy = false;
    private volatile int numberOfWaitingCustomers = 0;

    public void simulate() {
        Thread barberThread = new Thread(() -> {
            while (true) {
                try {
                    lock.lock();

                    while (!isBarberBusy && numberOfWaitingCustomers == 0) {
                        System.out.println("Barber is waiting for a customer");
                        barberCondition.await();
                    }

                    isBarberBusy = true;

                    if (numberOfWaitingCustomers > 0) {
                        customerCondition.signal();
                    }

                    System.out.println("Barber is cutting customer's hair");
                    Thread.sleep(5000);

                    isBarberBusy = false;

                    System.out.println("Barber is seeing off the customer");
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        });

        barberThread.start();

        for (int i = 0; i < MAX_VISITORS; i++) {
            new Thread(() -> {
                try {
                    lock.lock();

                    if (!isBarberBusy) {
                        isBarberBusy = true;
                        System.out.println("Customer is getting a haircut");
                        Thread.sleep(5000);
                        isBarberBusy = false;
                    } else {
                        numberOfWaitingCustomers++;
                        System.out.println("Customer is waiting in line");
                        customerCondition.await();
                        numberOfWaitingCustomers--;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }).start();
        }

        try {
            barberThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        HairSalonSimulator simulator = new HairSalonSimulator();
        simulator.simulate();
    }
}