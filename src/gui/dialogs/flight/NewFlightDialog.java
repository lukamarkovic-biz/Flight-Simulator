package gui.dialogs.flight;

import java.awt.TextField;

import gui.dialogs.BaseDialog;
import gui.exceptions.ValidationException;
import gui.models.Airplane;
import gui.service.RegistrationService;
import gui.simulation.FlightSimulation;


/**
 * Modal dialog for creating a new flight manually.
 *
 * Allows the user to enter source and destination airport codes, take-off time,
 * and flight duration. Performs validation via RegistrationService and registers
 * the flight in the simulation if valid.
 *
 * Extends BaseDialog and interacts with FlightSimulation for flight registration.
 */

public class NewFlightDialog extends BaseDialog {
	    private TextField fromField, toField, takeOffField, durationField;

	    public NewFlightDialog(FlightSimulation owner, RegistrationService service) {
	        super(owner, "New Airport", service);

	        fromField = addTextField("Source airport code:", 20);
	        toField = addTextField("Destination airport code:", 20);
	        takeOffField = addTextField("Take off time (hh:mm):", 5);
	        durationField = addTextField("Flight duration (minutes):", 4);
	        
	        
	        finalizeAndShow();
	    }

	    @Override
	    protected void onSave() throws ValidationException {
	        String froms = fromField.getText().trim();
	        String tos = toField.getText().trim().toUpperCase();
	        String takeOffs = takeOffField.getText().trim();
	        String durations = durationField.getText().trim();
	        
	        Airplane a = service.createAndRegisterFlight(10, froms, tos, takeOffs, durations);       
	        owner.registerFlight(a);

	        setVisible(false); dispose();
	    }
}
