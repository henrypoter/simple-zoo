package com.github.mairbek.zoo.demo;

import com.github.mairbek.zoo.LeaderElector;
import com.github.mairbek.zoo.Zoo;
import com.github.mairbek.zoo.ZooClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LeaderDemo {
    private static final Logger LOG = LoggerFactory.getLogger(LeaderDemo.class);
    
    public static void main(String[] args) {
        LOG.info("Starting demo process");
        ExecutorService executor = Executors.newSingleThreadExecutor();

        LeaderProcess leaderProcess = new LeaderProcess();

        executor.execute(leaderProcess);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Zoo zoo = new ZooClient()
                .endpoint("localhost:2181")
                .timeout(2000)
                .connect();

        LeaderElector leaderElector = new LeaderElector(zoo, "/election-demo");

        leaderElector.participate(leaderProcess);

    }

    public static class LeaderProcess implements Runnable, LeaderElector.Listener {
        private static final Logger LOG = LoggerFactory.getLogger(LeaderProcess.class);
        
        private volatile State state = State.IDLE;

        @Override
        public void electedAsLeader() {
            state = State.LEADER;
        }

        @Override
        public void run() {
            LOG.info("Started work");
            while (true) {
                doWork();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        private void doWork() {
            switch (state) {
                case IDLE:
                    LOG.info("Idle");
                    break;
                case LEADER:
                    LOG.info("Leading");
                    break;
                default:
                    throw new IllegalStateException("Unknown state");
            }
        }

        private static enum State {
            IDLE, LEADER
        }

    }
}
