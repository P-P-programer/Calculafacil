package com.calculafacil.model;

public record Emissivity(double value) {

    public Emissivity {
        if (!Double.isFinite(value) || value <= 0.0 || value > 1.0) {
            throw new IllegalArgumentException("La emisividad debe estar en el intervalo (0, 1].");
        }
    }
}
