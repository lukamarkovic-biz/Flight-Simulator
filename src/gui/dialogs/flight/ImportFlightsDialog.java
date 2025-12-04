package gui.dialogs.flight;

import java.awt.Frame;
import java.awt.TextField;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import gui.dialogs.BaseDialog;
import gui.dialogs.ResultDialog;
import gui.exceptions.ValidationException;
import gui.models.Airplane;
import gui.service.RegistrationService;
import gui.simulation.FlightSimulation;

/**
 * Modal dialog for importing flights from a CSV file.
 * 
 * Allows the user to specify a CSV file name, validates it, reads its contents,
 * and attempts to register each flight in the system using RegistrationService.
 * 
 * The expected CSV format is:
 * From,To,TakeOffTime,Duration
 * 
 * Lines with invalid data are collected and shown in a ResultDialog, while valid
 * flights are added to the simulation.
 * 
 * Extends BaseDialog and interacts with FlightSimulation for registering flights.
 */

public class ImportFlightsDialog extends BaseDialog {

    private TextField fileName;

    public ImportFlightsDialog(FlightSimulation owner, RegistrationService service) {
        super(owner, "Import Flights", service);
        fileName = addTextField("CSV file name: ", 20);
        finalizeAndShow();
    }

    @Override
    protected void onSave() throws ValidationException {
        String path = fileName.getText().trim();
        if (path.isEmpty()) {
            showError("File name cannot be empty.");
            return;
        }
        
        if (!path.toLowerCase().endsWith(".csv")) {
            showError("Only .csv files are supported.");
            return;
        }

        File f = new File(path);
        if (!f.exists() || !f.isFile()) {
            showError("File not found: " + path);
            return;
        }

        List<String> errors = new ArrayList<>();
        int imported = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            int lineNum = 0;
            while ((line = br.readLine()) != null) {
                lineNum++;
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length != 4) {
                    errors.add("Line " + lineNum + ": malformed (expected exactly 4 columns)");
                    continue;
                }

                String fromCode = parts[0].trim();
                String toCode = parts[1].trim();
                String takeOffTime = parts[2].trim();
                String durations = parts[3].trim();

                int duration;
                try {
                    duration = Integer.parseInt(durations);
                } catch (NumberFormatException nfe) {
                    errors.add("Line " + lineNum + ": invalid duration '" + durations + "'");
                    continue;
                }

                try {
                    Airplane a = service.createAndRegisterFlight(10, fromCode, toCode, takeOffTime, durations);
                    owner.registerFlight(a);
                    imported++;
                } catch (ValidationException ve) {
                    errors.add("Line " + lineNum + ": " + ve.getMessage());
                } catch (Exception ex) {
                    errors.add("Line " + lineNum + ": unexpected error (" + ex.getClass().getSimpleName() + ")");
                }
            }
        } catch (java.io.IOException ioe) {
            showError("I/O error while reading file: " + ioe.getMessage());
            return;
        }

        if (!errors.isEmpty()) {
            new ResultDialog((Frame)getOwner(), "Import results", errors);
            return;
        }

        setVisible(false);
        dispose();
    }
}
