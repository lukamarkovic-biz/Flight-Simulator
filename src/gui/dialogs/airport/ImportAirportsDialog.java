package gui.dialogs.airport;

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
import gui.models.Airport;
import gui.service.RegistrationService;
import gui.simulation.FlightSimulation;


/**
 * Modal dialog for importing airports from a CSV file.
 *
 * The user specifies a CSV file name. The dialog validates that the file
 * exists and has a .csv extension. It reads airport data line by line,
 * expecting 4 columns: name, code, X, Y. Each valid row is registered via
 * RegistrationService and added to the simulation UI.
 *
 * Any errors encountered during parsing or validation are collected and
 * displayed in a ResultDialog after the import attempt.
 *
 * Extends BaseDialog and interacts with RegistrationService and FlightSimulation.
 */
public class ImportAirportsDialog extends BaseDialog {
    private TextField fileName;

    public ImportAirportsDialog(FlightSimulation owner, RegistrationService service) {
        super(owner, "Import Airports", service);
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
        try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length != 4) {
                    errors.add("Line " + lineNum + ": malformed (expected 4 columns)");
                    continue;
                }

                String name = parts[0].trim();
                String code = parts[1].trim().toUpperCase();
                String xs = parts[2].trim();
                String ys = parts[3].trim();

                try {
                    Airport a = service.createAndRegisterAirport(xs, ys, 10, name, code);
                    // obavesti UI forma da doda checkbox / prikaz
                    owner.registerAirport(a);
                    imported++;
                } catch (ValidationException ve) {
                    // fabrika / servis javili gresku za taj red
                    errors.add("Line " + lineNum + ": " + ve.getMessage());
                } catch (Exception ex) {
                    // neocekivana greska za tu liniju
                    errors.add("Line " + lineNum + ": unexpected error (" + ex.getClass().getSimpleName() + ")");
                }
            }
        } catch (java.io.IOException ioe) {
            showError("I/O error while reading file: " + ioe.getMessage());
            return;
        }

        if (!errors.isEmpty()) {
            new ResultDialog((Frame)getOwner(), "Import results", errors);
            // ostani otvoren ili zatvoriš dijalog u zavisnosti od logike
            return;
        }

        // uspešno importovano
        setVisible(false);
        dispose();
    }
}
