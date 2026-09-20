package com.calculafacil.model;

public record SpectrumPoint(double independentValue, double radiance) {

    public SpectrumPoint {
        if (!Double.isFinite(independentValue) || !Double.isFinite(radiance) || radiance < 0.0) {
            throw new IllegalArgumentException("El punto espectral debe contener valores finitos y radiancia no negativa.");
        }
    }
}
