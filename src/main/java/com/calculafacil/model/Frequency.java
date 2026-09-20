package com.calculafacil.model;

public record Frequency(double hertz) {

    public Frequency {
        if (!Double.isFinite(hertz) || hertz <= 0.0) {
            throw new IllegalArgumentException("La frecuencia debe ser finita y mayor que 0 Hz.");
        }
    }
}
