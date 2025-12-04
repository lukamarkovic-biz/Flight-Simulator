package gui.timer;

/**
 * Simple simulation timer running in a separate thread.
 * Supports pausing, resuming, and stopping.
 * Calls a callback on each tick to update simulation state.
 */
public class MyTimer extends Thread {

    private final long tickMs;               // Tick duration in real milliseconds
    private final long simMinutesPerTick;    // Number of simulation minutes per tick
    private final double realToSimRatio;     // Conversion ratio: e.g., 1s real = 10 sim min

    private boolean running = false;
    private boolean paused = false;
    private long simTimeInMinutes = 0;

    private final Runnable onTick;            // Callback invoked on each tick

    public MyTimer(long tickMs, double realToSimRatio, Runnable onTick) {
        this.tickMs = tickMs;
        this.realToSimRatio = realToSimRatio;
        // Calculate fixed simulation minutes per tick
        this.simMinutesPerTick = Math.max(1, Math.round((tickMs / 1000.0) * realToSimRatio));
        this.onTick = onTick;
    }

    @Override
    public void run() {
        running = true;
        while (true) {
            synchronized (this) {
                if (!running) break;
                if (paused) {
                    try { wait(); } catch (InterruptedException ignored) {}
                    continue;
                }
                simTimeInMinutes += simMinutesPerTick;
            }

            // Call the callback
            try {
                if (onTick != null) onTick.run();
            } catch (Throwable t) {
                t.printStackTrace();
                stopTimer(); // Safely stop in case of an error
            }

            try {
                Thread.sleep(tickMs);
            } catch (InterruptedException ignored) {}
        }
    }

    /** Pauses the timer. */
    public synchronized void pauseTimer() {
        paused = true;
    }

    /** Resumes the timer if paused. */
    public synchronized void resumeTimer() {
        if (paused) {
            paused = false;
            notifyAll();
        }
    }

    /** Stops the timer completely. */
    public synchronized void stopTimer() {
        running = false;
        paused = false;
        notifyAll();
    }

    /** Resets simulation time to zero without starting the timer. */
    public synchronized void resetTimer() {
        simTimeInMinutes = 0;
    }

    /** Returns the current simulation time in minutes. */
    public synchronized long getSimTimeInMinutes() {
        return simTimeInMinutes;
    }

    /** Returns the fixed number of simulation minutes per tick. */
    public long getSimMinutesPerTick() {
        return simMinutesPerTick;
    }
}
