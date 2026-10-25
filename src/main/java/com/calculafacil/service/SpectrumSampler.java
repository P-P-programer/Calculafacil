package com.calculafacil.service;

import com.calculafacil.model.Emissivity;
import com.calculafacil.model.SpectrumCurve;
import com.calculafacil.model.SpectrumPoint;
import com.calculafacil.model.Temperature;
import java.util.ArrayList;
import java.util.function.DoubleUnaryOperator;

public final class SpectrumSampler {

    public SpectrumCurve sample(String name, double minimum, double maximum, int samples,
            DoubleUnaryOperator function) {
        if (!Double.isFinite(minimum) || !Double.isFinite(maximum) || minimum <= 0.0 || maximum <= minimum) {
            throw new IllegalArgumentException("El rango espectral no es válido.");
        }
        if (samples < 2) {
            throw new IllegalArgumentException("Se requieren al menos dos muestras.");
        }
        var points = new ArrayList<SpectrumPoint>(samples);
        double step = (maximum - minimum) / (samples - 1);
        for (int index = 0; index < samples; index++) {
            double independentValue = minimum + index * step;
            points.add(new SpectrumPoint(independentValue, function.applyAsDouble(independentValue)));
        }
        return new SpectrumCurve(name, points);
    }

    public SpectrumCurve samplePlanckWavelength(String name, double minimum, double maximum,
            int samples, Temperature temperature, Emissivity emissivity) {
        var calculator = new SpectralRadianceCalculator();
        return sample(name, minimum, maximum, samples,
                wavelength -> calculator.planckByWavelength(wavelength, temperature, emissivity));
    }

    public SpectrumCurve sampleWavelength(String name, double minimum, double maximum,
            int samples, java.util.function.DoubleUnaryOperator function) {
        return sample(name, minimum, maximum, samples, function);
    }
}
