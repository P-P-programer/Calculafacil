package com.calculafacil;

import com.calculafacil.model.Emissivity;
import com.calculafacil.model.LengthUnit;
import com.calculafacil.model.Temperature;
import com.calculafacil.model.TemperatureUnit;
import com.calculafacil.service.SpectralRadianceCalculator;
import com.calculafacil.service.SpectrumSampler;
import com.calculafacil.service.StefanBoltzmannVerifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhysicsTest {

    private final Emissivity blackBody = new Emissivity(1.0);
    private final SpectralRadianceCalculator calculator = new SpectralRadianceCalculator();

    @Test
    void convertsTemperatureScales() {
        assertEquals(300.0, Temperature.fromCelsius(26.85).kelvin(), 1e-10);
        assertEquals(300.0, Temperature.fromFahrenheit(80.33).kelvin(), 0.02);
        assertEquals(26.85, TemperatureUnit.CELSIUS.fromKelvin(300.0), 1e-10);
    }

    @Test
    void convertsNanometersToMeters() {
        assertEquals(501e-9, LengthUnit.NANOMETER.toMeters(501.0), 1e-18);
    }

    @Test
    void sunPeakIsNearKnownWavelength() {
        var temperature = new Temperature(5778.0);
        double bestWavelength = 0.0;
        double bestRadiance = -1.0;
        for (int nanometers = 350; nanometers <= 800; nanometers++) {
            double wavelength = nanometers * 1e-9;
            double radiance = calculator.planckByWavelength(wavelength, temperature, blackBody);
            if (radiance > bestRadiance) {
                bestRadiance = radiance;
                bestWavelength = nanometers;
            }
        }
        assertEquals(501.0, bestWavelength, 4.0);
    }

    @Test
    void greyBodyScalesRadiance() {
        double black = calculator.planckByWavelength(500e-9, new Temperature(5778), blackBody);
        double grey = calculator.planckByWavelength(500e-9, new Temperature(5778), new Emissivity(0.4));
        assertEquals(0.4, grey / black, 1e-12);
    }

    @Test
    void wavelengthPeakUsesWienDisplacementLaw() {
        assertEquals(501.4e-9, calculator.wavelengthPeak(new Temperature(5778.0)), 1.0e-9);
    }

    @Test
    void historicalApproximationsAreFiniteAndNonNegative() {
        double wavelength = 500e-9;
        var temperature = new Temperature(5778.0);
        assertTrue(calculator.rayleighJeansByWavelength(wavelength, temperature, blackBody) > 0.0);
        assertTrue(calculator.wienByWavelength(wavelength, temperature, blackBody) > 0.0);
    }

    @Test
    void integrationApproachesStefanBoltzmann() {
        var temperature = new Temperature(5778);
        var curve = new SpectrumSampler().samplePlanckWavelength(
                "Sol", 50e-9, 500e-6, 20_000, temperature, blackBody);
        var verification = StefanBoltzmannVerifier.verify(curve, temperature, blackBody);
        assertTrue(verification.withinTolerance(0.01),
                () -> "Error relativo: " + verification.relativeError());
    }

    @Test
    void rejectsInvalidTemperatureAndEmissivity() {
        assertThrows(IllegalArgumentException.class, () -> new Temperature(0));
        assertThrows(IllegalArgumentException.class, () -> new Emissivity(1.1));
    }
}
