package com.calculafacil.model;

import java.util.List;

public record SpectrumCurve(String name, List<SpectrumPoint> points) {

    public SpectrumCurve {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("La curva debe tener un nombre.");
        }
        points = List.copyOf(points);
        if (points.size() < 2) {
            throw new IllegalArgumentException("La curva debe contener al menos dos puntos.");
        }
    }
}
