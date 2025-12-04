package gui.dialogs.flight;

import java.awt.TextField;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;

import gui.dialogs.BaseDialog;
import gui.service.RegistrationService;
import gui.simulation.FlightSimulation;

/**
 * Modal dialog for exporting all flights to a CSV file.
 * 
 * Allows the user to specify a CSV file name, validates it, and writes
 * all flights currently registered in the system to the file in the format:
 * From,To,TakeOffTime,Duration
 * 
 * Extends BaseDialog and uses RegistrationService to access the flights.
 */
public class ExportFlightsDialog extends BaseDialog {

    private TextField fileName;

    public ExportFlightsDialog(FlightSimulation owner, RegistrationService service) {
        super(owner, "Export Flights", service);
        fileName = addTextField("CSV file name:", 20);
        finalizeAndShow();
    }

    @Override
    protected void onSave() {
        String path = fileName.getText().trim();
        if (path.isEmpty()) {
            showError("File name cannot be empty.");
            return;
        }
        
        if (!path.toLowerCase().endsWith(".csv")) {
            showError("File must have a .csv extension.");
            return;
        }

        File file = new File(path);
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {

//            writer.println("From,To,TakeOffTime,Duration");
            for (var flight : service.getFlightsSnapshot()) {
                String fromCode = flight.getFrom().getCode();
                String toCode = flight.getTo().getCode();
                String takeOff = String.format("%02d:%02d",
                        flight.getHours(),
                        flight.getMinutes());
                int duration = flight.getDuration();

                writer.printf("%s,%s,%s,%d%n", fromCode, toCode, takeOff, duration);
            }

            setVisible(false);
            dispose();

        } catch (IOException e) {
            showError("Error saving file: " + e.getMessage());
        }
    }
}
