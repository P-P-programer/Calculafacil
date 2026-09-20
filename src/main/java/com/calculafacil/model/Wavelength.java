package com.calculafacil.model;

public record Wavelength(double meters) {

    public Wavelength {
        if (!Double.isFinite(meters) || meters <= 0.0) {
            throw new IllegalArgumentException("La longitud de onda debe ser finita y mayor que 0 m.");
        }
    }
}
