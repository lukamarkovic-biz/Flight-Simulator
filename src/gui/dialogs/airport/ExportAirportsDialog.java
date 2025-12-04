package gui.dialogs.airport;

import java.awt.*;
import java.io.*;

import gui.dialogs.BaseDialog;
import gui.models.Airport;
import gui.service.RegistrationService;
import gui.simulation.FlightSimulation;


/**
 * Modal dialog for exporting all registered airports to a CSV file.
 *
 * Allows the user to specify a file name. The dialog validates that the file
 * name is not empty and has a .csv extension, then writes airport data
 * (name, code, X, Y) to the specified file.
 *
 * Extends BaseDialog and interacts with RegistrationService to get airport data.
 */

public class ExportAirportsDialog extends BaseDialog {

    private TextField fileName;

    public ExportAirportsDialog(FlightSimulation owner, RegistrationService service) {
        super(owner, "Export Airports", service);
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
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {

//            pw.println("Name,Code,X,Y");
            for (Airport a : service.getAirportsSnapshot()) {
                pw.printf("%s,%s,%.3f,%.3f%n",
                        a.getName(),
                        a.getCode(),
                        a.getX(),
                        a.getY());
            }

            setVisible(false);
            dispose();

        } catch (IOException ex) {
            showError("Error saving file: " + ex.getMessage());
        }
    }
}
