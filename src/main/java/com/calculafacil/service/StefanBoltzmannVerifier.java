package com.calculafacil.service;

import com.calculafacil.model.Emissivity;
import com.calculafacil.model.PhysicalConstants;
import com.calculafacil.model.SpectrumCurve;
import com.calculafacil.model.Temperature;

public record StefanBoltzmannVerifier(double numericalPower, double theoreticalPower,
        double relativeError) {

    public static StefanBoltzmannVerifier verify(SpectrumCurve curve, Temperature temperature,
            Emissivity emissivity) {
        // B_lambda is radiance per steradian; integrating over the outward
        // hemisphere introduces the Lambertian factor pi.
        double numerical = Math.PI * new NumericalIntegrator().trapezoid(curve);
        double theoretical = emissivity.value() * PhysicalConstants.STEFAN_BOLTZMANN
                * Math.pow(temperature.kelvin(), 4);
        return new StefanBoltzmannVerifier(numerical, theoretical,
                Math.abs(numerical - theoretical) / theoretical);
    }

    public boolean withinTolerance(double tolerance) {
        return relativeError <= tolerance;
    }
}
