package gui.factory;

import gui.models.Airplane;
import gui.models.Airport;
import gui.exceptions.ValidationException;

/**
 * Factory for creating Airplane (flight) objects.
 */
public final class AirplaneFactory {

    private AirplaneFactory() {}

    /**
     * Creates an Airplane after validation.
     *
     * @param width       pixel width for drawing the airplane
     * @param from        departure airport (must be non-null)
     * @param to          destination airport (must be non-null and different from 'from')
     * @param takeOffTime string in format "HH:MM"
     * @param duration    flight duration in minutes (>0)
     * @return Airplane object
     * @throws ValidationException if any validation fails
     */
    public static Airplane create(int width, Airport from, Airport to, String takeOffTime, int duration) throws ValidationException {
        if (from == null) throw new ValidationException("Departure airport does not exist.");
        if (to == null) throw new ValidationException("Destination airport does not exist.");
        if (from.getCode().equals(to.getCode())) throw new ValidationException("Departure and destination airports must be different.");
        if (duration <= 0) throw new ValidationException("Flight duration must be a positive number of minutes.");
        if (takeOffTime == null || takeOffTime.trim().isEmpty()) throw new ValidationException("Take-off time is required.");

        String t = takeOffTime.trim();
        String[] parts = t.split(":");
        if (parts.length != 2) throw new ValidationException("Time must be in HH:MM format.");

        int hh, mm;
        try {
            hh = Integer.parseInt(parts[0]);
            mm = Integer.parseInt(parts[1]);
        } catch (NumberFormatException ex) {
            throw new ValidationException("Invalid time format. Use HH:MM with numbers.");
        }

        if (hh < 0 || hh > 23 || mm < 0 || mm > 59) {
            throw new ValidationException("Take-off time must be between 00:00 and 23:59.");
        }

        String normalizedTime = String.format("%02d:%02d", hh, mm);
        int finalWidth = width > 0 ? width : 6;

        return new Airplane(finalWidth, from, to, normalizedTime, duration);
    }
}
