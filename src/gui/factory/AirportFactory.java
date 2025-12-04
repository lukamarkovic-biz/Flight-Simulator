package gui.factory;

import gui.models.Airport;
import gui.exceptions.ValidationException;

/**
 * Validation and creation of Airport objects.
 */
public final class AirportFactory {

    private AirportFactory() {}

    /**
     * Validates input and creates an Airport instance.
     * 
     * @param nsX   x coordinate in range -90..90
     * @param nsY   y coordinate in range -90..90
     * @param width pixel width for drawing (if <=0 uses default)
     * @param name  airport name (cannot be empty)
     * @param code  three-letter airport code (automatically normalized to uppercase)
     * @return Airport instance (does NOT add it to the model)
     * @throws ValidationException if validation fails
     */
    public static Airport create(double nsX, double nsY, int width, String name, String code) throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Airport name is required.");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new ValidationException("Airport code is required.");
        }

        String normalizedCode = code.trim().toUpperCase();
        if (!normalizedCode.matches("[A-Z]{3}")) {
            throw new ValidationException("Airport code must be exactly 3 uppercase letters (A-Z).");
        }

        if (!Double.isFinite(nsX) || nsX < -90.0 || nsX > 90.0) {
            throw new ValidationException("X coordinate must be in range -90..90.");
        }
        if (!Double.isFinite(nsY) || nsY < -90.0 || nsY > 90.0) {
            throw new ValidationException("Y coordinate must be in range -90..90.");
        }

        int finalWidth = width > 0 ? width : 8;

        return new Airport(nsX, nsY, finalWidth, name.trim(), normalizedCode);
    }
}
