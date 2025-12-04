package gui.dialogs.airport;

import java.awt.*;
import java.util.regex.Pattern;

import gui.dialogs.BaseDialog;
import gui.exceptions.ValidationException;
import gui.models.Airport;
import gui.service.RegistrationService;
import gui.simulation.FlightSimulation;


/**
 * Modal dialog for creating a new airport.
 *
 * The user provides:
 *   - Name of the airport
 *   - 3-letter code
 *   - X and Y coordinates (each between -90 and 90)
 *
 * The dialog validates input via RegistrationService and, if valid,
 * registers the airport in the simulation UI.
 *
 * Extends BaseDialog and interacts with RegistrationService and FlightSimulation.
 */
public class NewAirportDialog extends BaseDialog {
    private static final Pattern CODE_RE = Pattern.compile("^[A-Z]{3}$");
    private TextField nameField, codeField, xField, yField;
    public NewAirportDialog(FlightSimulation owner, RegistrationService service) {
        super(owner, "New Airport", service);

        nameField = addTextField("Name:", 20);
        codeField = addTextField("Code (3):", 6);
        xField = addTextField("X (-90..90):", 8);
        yField = addTextField("Y (-90..90):", 8);
        
        
        finalizeAndShow();
    }

    @Override
    protected void onSave() throws ValidationException {
        String name = nameField.getText().trim();
        String code = codeField.getText().trim().toUpperCase();
        String xs = xField.getText().trim();
        String ys = yField.getText().trim();

        Airport a = service.createAndRegisterAirport(xs, ys, 10, name, code);        
        owner.registerAirport(a);

        setVisible(false); dispose();
    }
}
