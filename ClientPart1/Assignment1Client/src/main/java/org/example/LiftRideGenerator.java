package org.example;

import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class LiftRideGenerator implements Runnable {
    private final BlockingQueue<LiftRide> queue;
    private final int totalEvents;

    public LiftRideGenerator(BlockingQueue<LiftRide> queue, int totalEvents) {
        this.queue = queue;
        this.totalEvents = totalEvents;
    }

    @Override
    public void run() {
        for (int i = 0; i < totalEvents; i++) {
            LiftRide liftRide = generateRandomLiftRide();
            try {
                queue.put(liftRide);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private LiftRide generateRandomLiftRide() {
        Random random = new Random();
        LiftRide liftRide = new LiftRide();
        liftRide.setSkierID(random.nextInt(100000) + 1);
        liftRide.setResortID(random.nextInt(10) + 1);
        liftRide.setLiftID(random.nextInt(40) + 1);
        liftRide.setSeasonID("2024");
        liftRide.setDayID("1");
        liftRide.setTime(random.nextInt(360) + 1);
        return liftRide;
    }
}

