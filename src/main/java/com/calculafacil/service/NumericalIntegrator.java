package com.calculafacil.service;

import com.calculafacil.model.SpectrumPoint;
import com.calculafacil.model.SpectrumCurve;

public final class NumericalIntegrator {

    public double trapezoid(SpectrumCurve curve) {
        double total = 0.0;
        var points = curve.points();
        for (int index = 1; index < points.size(); index++) {
            SpectrumPoint previous = points.get(index - 1);
            SpectrumPoint current = points.get(index);
            double width = current.independentValue() - previous.independentValue();
            if (width <= 0.0) {
                throw new IllegalArgumentException("Los puntos deben estar ordenados de forma creciente.");
            }
            total += width * (previous.radiance() + current.radiance()) / 2.0;
        }
        return total;
    }
}
