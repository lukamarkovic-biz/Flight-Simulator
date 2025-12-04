package gui.dialogs;

import java.awt.*;
import java.awt.event.*;

import gui.exceptions.AppException;
import gui.exceptions.ValidationException;
import gui.service.RegistrationService;
import gui.simulation.FlightSimulation;

/**
 * Base modal dialog for forms (airports, flights, etc.).
 * Provides a form panel, message label, Save/Cancel buttons,
 * and centralized error handling for AppException/ValidationException.
 */
public abstract class BaseDialog extends Dialog {
    protected FlightSimulation owner;
    protected RegistrationService service;
    protected Panel form;
    private Label msgLabel = new Label(" ");

    public BaseDialog(FlightSimulation owner, String title, RegistrationService service) {
        super(owner, title, true);
        this.owner = owner;
        this.service = service;
        setLayout(new BorderLayout(8, 8));
        form = new Panel(new GridLayout(0, 2, 6, 6));
        add(form, BorderLayout.CENTER);

        // message label row (default blank)
        form.add(new Label(""));
        form.add(msgLabel);

        Panel buttons = new Panel(new FlowLayout(FlowLayout.RIGHT));
        Button save = new Button("Save");
        Button cancel = new Button("Cancel");
        buttons.add(save);
        buttons.add(cancel);
        add(buttons, BorderLayout.SOUTH);

        save.addActionListener(e -> {
            try {
                onSave();
            } catch (Throwable ex) {
                handleException(ex);
            }
        });

        cancel.addActionListener(e -> {
            setVisible(false);
            dispose();
        });

        // derived classes will call attachEnterTo(...) for global Enter handling
        setResizable(false);
    }

    /**
     * Helper: add a label + textfield to the form and attach Enter key handling.
     */
    protected TextField addTextField(String label, int cols) {
        TextField tf = new TextField(cols);
        attachEnterTo(tf);
        form.add(new Label(label));
        form.add(tf);
        return tf;
    }

    /**
     * Display an error message in the message label.
     */
    protected void showError(String text) {
        msgLabel.setText(text);
        msgLabel.setForeground(Color.RED);
        pack();
    }

    /**
     * Attach Enter key handling to a TextField.
     * Pressing Enter will call onSave().
     */
    protected void attachEnterTo(TextField field) {
        KeyListener enter = new KeyAdapter() {
            public void keyPressed(KeyEvent ke) {
                if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        onSave();
                    } catch (Throwable ex) {
                        handleException(ex);
                    }
                }
            }
        };
        field.addKeyListener(enter);
    }

    /**
     * Pack, center, and show the dialog.
     */
    protected void finalizeAndShow() {
        pack();
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    /**
     * Centralized exception handling:
     * - if AppException or subclass: show user message
     * - if cause is AppException: unwrap and show message
     * - otherwise: log stack trace and show generic message
     */
    protected void handleException(Throwable ex) {
        if (ex instanceof AppException) {
            showError(((AppException) ex).getUserMessage());
            return;
        }

        Throwable cause = ex.getCause();
        if (cause instanceof AppException) {
            showError(((AppException) cause).getUserMessage());
            return;
        }

        // unexpected error: log and show generic message
        ex.printStackTrace();
        showError("Unexpected error. See log for details.");
    }

    /**
     * Derived classes must implement validation/save logic.
     */
    protected abstract void onSave() throws ValidationException;
}
