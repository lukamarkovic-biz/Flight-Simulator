package gui.idle;

import gui.timer.MyTimer;

import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Objects;

/**
 * IdleManager uses MyTimer (tickMs = 1000, ratio = 1) to count idle seconds.
 * When the warning threshold is reached (timeout - warningSeconds),
 * it shows a modal warning dialog.
 */
public class IdleManager {

    private final Frame owner;
    private final int timeoutSeconds;
    private final int warningSeconds;

    private final MyTimer timer;
    private final AWTEventListener awtListener;

    // idle counter (accessed from multiple threads) — synchronized blocks used
    private int idleSeconds = 0;

    // Warning dialog state (UI thread)
    private Dialog warningDialog;
    private Label warningLabel;

    private volatile boolean stopped = false;
    private boolean paused = false;

    public IdleManager(Frame owner) {
        this(owner, 60, 5);
    }

    public IdleManager(Frame owner, int timeoutSeconds, int warningSeconds) {
        this.owner = Objects.requireNonNull(owner);
        this.timeoutSeconds = timeoutSeconds;
        this.warningSeconds = warningSeconds;

        // MyTimer: tick every 1000ms, ratio 1 -> simMinutesPerTick = 1 (used as seconds)
        this.timer = new MyTimer(1000L, 1.0, this::onTick);

        // Global AWT listener for all user events that reset idle timer
        this.awtListener = event -> {
            if (event instanceof MouseEvent || event instanceof KeyEvent) {
                resetIdle();
            }
        };

        Toolkit.getDefaultToolkit().addAWTEventListener(awtListener,
                AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);

        // Start timer as daemon thread
        timer.start();
    }

    /** Resets the idle counter and closes any warning dialog. */
    public synchronized void resetIdle() {
        idleSeconds = 0;
        if (warningDialog != null) {
            EventQueue.invokeLater(() -> {
                try {
                    if (warningDialog != null) {
                        warningDialog.setVisible(false);
                        warningDialog.dispose();
                        warningDialog = null;
                        warningLabel = null;
                    }
                } catch (Throwable ignored) {}
            });
        }
    }

    /** Called once per second from MyTimer thread. */
    private void onTick() {
        if (stopped || paused) return;

        int newIdle;
        synchronized (this) {
            idleSeconds++;
            newIdle = idleSeconds;
        }

        int remaining = timeoutSeconds - newIdle;

        if (remaining <= 0) {
            // timeout reached -> close app on EDT
            EventQueue.invokeLater(() -> {
                try {
                    owner.dispose();
                } finally {
                    System.exit(0);
                }
            });
            stop();
            return;
        }

        if (remaining <= warningSeconds) {
            // show or update warning dialog on EDT
            EventQueue.invokeLater(() -> showOrUpdateWarning(remaining));
        }
    }

    private void showOrUpdateWarning(int secondsLeft) {
        if (warningDialog == null) {
            warningDialog = new Dialog(owner, "Warning", true);
            warningDialog.setLayout(new BorderLayout(8,8));
            warningLabel = new Label("Program will close in " + secondsLeft + " seconds.");
            warningDialog.add(warningLabel, BorderLayout.CENTER);

            warningDialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    warningDialog.setVisible(false);
                    warningDialog.dispose();
                    warningDialog = null;
                    warningLabel = null;
                }
            });

            warningDialog.pack();
            warningDialog.setLocationRelativeTo(owner);
            warningDialog.setVisible(true); // modal — blocks EDT while open
        } else {
            if (warningLabel != null) {
                warningLabel.setText("Program will close in " + secondsLeft + " seconds.");
            }
        }
    }

    /** Stops the manager and removes the global listener (call during windowClosing). */
    public void stop() {
        if (stopped) return;
        stopped = true;

        if (warningDialog != null) {
            try {
                EventQueue.invokeLater(() -> {
                    try {
                        warningDialog.setVisible(false);
                        warningDialog.dispose();
                    } catch (Throwable ignored) {}
                });
            } catch (Throwable ignored) {}
            warningDialog = null;
            warningLabel = null;
        }

        if (timer != null) {
            timer.stopTimer();
            timer.interrupt();
        }
    }

    /** Pauses the countdown (e.g., when simulation is running). */
    public void pause() {
        paused = true;
    }

    /** Resumes the countdown (e.g., when simulation is paused). */
    public void resume() {
        paused = false;
    }
}
