package gui.service;

import java.util.ArrayList;
import java.util.List;

import gui.exceptions.DuplicateEntityException;
import gui.exceptions.ValidationException;
import gui.factory.AirportFactory;
import gui.factory.AirplaneFactory;
import gui.models.Airplane;
import gui.models.Airport;
import gui.models.SimulationModel;

/**
 * Helper service class that encapsulates creation and registration of airports and flights.
 * Throws concrete ValidationException (including DuplicateEntityException) for all detectable errors:
 * parsing errors, duplicates, missing airports, invalid inputs, etc.
 */
public class RegistrationService {
    private final SimulationModel model;

    public RegistrationService(SimulationModel model) {
        if (model == null) throw new NullPointerException("model cannot be null");
        this.model = model;
    }

    /**
     * Creates and registers an airport using string coordinates (e.g., from TextField input).
     * Throws ValidationException for all validation errors (invalid number, format, duplicates, etc.).
     *
     * @param nsX x-coordinate as string
     * @param nsY y-coordinate as string
     * @param width drawing width
     * @param name airport name
     * @param code 3-letter airport code
     * @return registered Airport
     * @throws ValidationException if any validation fails
     */
    public Airport createAndRegisterAirport(String nsX, String nsY, int width, String name, String code) throws ValidationException {
        if (nsX == null || nsX.trim().isEmpty()) throw new ValidationException("X coordinate is required.");
        if (nsY == null || nsY.trim().isEmpty()) throw new ValidationException("Y coordinate is required.");
        if (code == null || code.trim().isEmpty()) throw new ValidationException("Airport code is required.");

        double x, y;
        try {
            x = Double.parseDouble(nsX.trim());
        } catch (NumberFormatException nfe) {
            throw new ValidationException("X coordinate is not a valid number.");
        }
        try {
            y = Double.parseDouble(nsY.trim());
        } catch (NumberFormatException nfe) {
            throw new ValidationException("Y coordinate is not a valid number.");
        }

        String normalizedCode = code.trim().toUpperCase();

        // check for duplicate code in model
        if (model.getAirport(normalizedCode) != null) {
            throw new DuplicateEntityException("Airport with code " + normalizedCode + " already exists.");
        }

        // delegate semantic validation to factory (throws ValidationException if invalid)
        Airport a = AirportFactory.create(x, y, width, name, normalizedCode);

        // check for duplicate coordinates
        for (Airport existing : model.getAirports()) {
            if (Double.compare(existing.getX(), a.getX()) == 0 &&
                Double.compare(existing.getY(), a.getY()) == 0) {
                throw new DuplicateEntityException(
                    String.format("An airport already exists at coordinates: (%.3f, %.3f)", a.getNsX(), a.getNsY())
                );
            }
        }

        // register in model
        model.addAirport(a);
        return a;
    }

    /**
     * Creates and registers a flight. Throws ValidationException for all validation errors.
     *
     * @param width pixel width for drawing the airplane
     * @param fromCode 3-letter departure airport code
     * @param toCode 3-letter destination airport code
     * @param takeOffTime "HH:MM"
     * @param durations duration in minutes as string
     * @return registered Airplane
     * @throws ValidationException if validation fails
     */
    public Airplane createAndRegisterFlight(int width, String fromCode, String toCode, String takeOffTime, String durations) throws ValidationException {
        if (fromCode == null || fromCode.trim().isEmpty()) throw new ValidationException("Departure airport is required.");
        if (toCode == null || toCode.trim().isEmpty()) throw new ValidationException("Destination airport is required.");
        if (durations == null || durations.trim().isEmpty()) throw new ValidationException("Flight duration is required.");

        int duration;
        try {
            duration = Integer.parseInt(durations);
        } catch (NumberFormatException nfe) {
            throw new ValidationException("Duration is not a valid number.");
        }

        String normFrom = fromCode.trim().toUpperCase();
        String normTo = toCode.trim().toUpperCase();

        Airport from = model.getAirport(normFrom);
        if (from == null) throw new ValidationException("Departure airport (" + normFrom + ") does not exist.");
        Airport to = model.getAirport(normTo);
        if (to == null) throw new ValidationException("Destination airport (" + normTo + ") does not exist.");

        // factory will throw ValidationException for invalid format, duration, or same airport
        Airplane ap = AirplaneFactory.create(width, from, to, takeOffTime, duration);

        // check for duplicate flight
        for (Airplane existing : model.getFlights()) {
            boolean sameRoute = existing.getFrom().getCode().equals(ap.getFrom().getCode())
                    && existing.getTo().getCode().equals(ap.getTo().getCode());
            boolean sameDeparture = existing.getDepartureInMinutes() == ap.getDepartureInMinutes();
            boolean sameDuration = existing.getDuration() == ap.getDuration();
            if (sameRoute && sameDeparture && sameDuration) {
                throw new DuplicateEntityException("A similar flight already exists (same departure, destination, time, and duration).");
            }
        }

        model.addFlight(ap);
        return ap;
    }

    /** Read-only snapshot copy of airports. */
    public List<Airport> getAirportsSnapshot() {
        return new ArrayList<>(model.getAirports());
    }

    /** Read-only snapshot copy of flights. */
    public List<Airplane> getFlightsSnapshot() {
        return new ArrayList<>(model.getFlights());
    }
}
