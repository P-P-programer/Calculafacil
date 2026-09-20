package com.calculafacil.model;

public enum LengthUnit {
    METER("m", 1.0),
    MICROMETER("µm", 1e-6),
    NANOMETER("nm", 1e-9);

    private final String symbol;
    private final double metersPerUnit;

    LengthUnit(String symbol, double metersPerUnit) {
        this.symbol = symbol;
        this.metersPerUnit = metersPerUnit;
    }

    public double toMeters(double value) {
        return value * metersPerUnit;
    }

    public double fromMeters(double meters) {
        return meters / metersPerUnit;
    }

    public String symbol() {
        return symbol;
    }
}
