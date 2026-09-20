package com.calculafacil.service;

import com.calculafacil.model.Emissivity;
import com.calculafacil.model.PhysicalConstants;
import com.calculafacil.model.Temperature;

public final class SpectralRadianceCalculator {

    public double planckByWavelength(double wavelengthMeters, Temperature temperature, Emissivity emissivity) {
        validatePositiveFinite(wavelengthMeters, "La longitud de onda");
        double exponent = PhysicalConstants.PLANCK * PhysicalConstants.SPEED_OF_LIGHT
                / (wavelengthMeters * PhysicalConstants.BOLTZMANN * temperature.kelvin());
        return emissivity.value() * safePlanckNumeratorWavelength(wavelengthMeters) / Math.expm1(exponent);
    }

    public double planckByFrequency(double frequencyHertz, Temperature temperature, Emissivity emissivity) {
        validatePositiveFinite(frequencyHertz, "La frecuencia");
        double exponent = PhysicalConstants.PLANCK * frequencyHertz
                / (PhysicalConstants.BOLTZMANN * temperature.kelvin());
        return emissivity.value() * 2.0 * PhysicalConstants.PLANCK * Math.pow(frequencyHertz, 3)
                / (PhysicalConstants.SPEED_OF_LIGHT * PhysicalConstants.SPEED_OF_LIGHT * Math.expm1(exponent));
    }

    public double rayleighJeansByWavelength(double wavelengthMeters, Temperature temperature, Emissivity emissivity) {
        validatePositiveFinite(wavelengthMeters, "La longitud de onda");
        return emissivity.value() * 2.0 * PhysicalConstants.SPEED_OF_LIGHT * PhysicalConstants.BOLTZMANN
                * temperature.kelvin() / Math.pow(wavelengthMeters, 4);
    }

    public double wienByWavelength(double wavelengthMeters, Temperature temperature, Emissivity emissivity) {
        validatePositiveFinite(wavelengthMeters, "La longitud de onda");
        double exponent = PhysicalConstants.PLANCK * PhysicalConstants.SPEED_OF_LIGHT
                / (wavelengthMeters * PhysicalConstants.BOLTZMANN * temperature.kelvin());
        return emissivity.value() * safePlanckNumeratorWavelength(wavelengthMeters) * Math.exp(-exponent);
    }

    private double safePlanckNumeratorWavelength(double wavelengthMeters) {
        return 2.0 * PhysicalConstants.PLANCK * PhysicalConstants.SPEED_OF_LIGHT
                * PhysicalConstants.SPEED_OF_LIGHT / Math.pow(wavelengthMeters, 5);
    }

    private void validatePositiveFinite(double value, String label) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(label + " debe ser finita y mayor que 0.");
        }
    }
}
