package gui.dialogs;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

/**
 * Modal dialog that displays results (list of messages), e.g., from an import process.
 * Allows the user to view errors and save the report to a file.
 */
public class ResultDialog extends Dialog {

    private TextArea textArea;
    private Button closeBtn;
    private Button saveBtn;

    /**
     * Creates a ResultDialog.
     *
     * @param owner    parent window (e.g., FlightSimulation)
     * @param title    dialog title
     * @param messages list of messages to display (can be empty)
     */
    public ResultDialog(Frame owner, String title, List<String> messages) {
        super(owner, title, true);
        initUi(messages);
        pack();
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    private void initUi(List<String> messages) {
        setLayout(new BorderLayout(8, 8));

        textArea = new TextArea(20, 80);
        textArea.setEditable(false);
        // fill text area
        StringBuilder sb = new StringBuilder();
        if (messages == null || messages.isEmpty()) {
            sb.append("No messages.");
        } else {
            for (String m : messages) {
                sb.append(m).append(System.lineSeparator());
            }
        }
        textArea.setText(sb.toString());

        add(textArea, BorderLayout.CENTER);

        Panel buttons = new Panel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        saveBtn = new Button("Save...");
        closeBtn = new Button("Close");
        buttons.add(saveBtn);
        buttons.add(closeBtn);
        add(buttons, BorderLayout.SOUTH);

        // actions
        closeBtn.addActionListener(e -> {
            setVisible(false);
            dispose();
        });

        saveBtn.addActionListener(e -> onSaveToFile());

        // ESC key closes the dialog
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent ke) {
                if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    setVisible(false);
                    dispose();
                }
            }
        });

        // Window close
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                setVisible(false);
                dispose();
            }
        });
    }

    private void onSaveToFile() {
        FileDialog fd = new FileDialog((Frame) getOwner(), "Save report as...", FileDialog.SAVE);
        fd.setVisible(true);
        String dir = fd.getDirectory();
        String file = fd.getFile();
        if (dir == null || file == null) return;

        File out = new File(dir, file);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(out))) {
            bw.write(textArea.getText());
            bw.flush();
        } catch (Exception ex) {
            // show a simple error to the user
            showError("Cannot save file: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        // Use a simple modal dialog to display the error
        Dialog d = new Dialog((Frame) getOwner(), "Error", true);
        d.setLayout(new BorderLayout(6, 6));
        Label l = new Label(message);
        l.setForeground(Color.RED);
        d.add(l, BorderLayout.CENTER);
        Button ok = new Button("OK");
        Panel p = new Panel(new FlowLayout(FlowLayout.CENTER));
        p.add(ok);
        d.add(p, BorderLayout.SOUTH);
        ok.addActionListener(e -> { d.setVisible(false); d.dispose(); });
        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }
}
