package com.calculafacil.model;

public enum FrequencyUnit {
    HERTZ("Hz", 1.0),
    TERAHERTZ("THz", 1e12);

    private final String symbol;
    private final double hertzPerUnit;

    FrequencyUnit(String symbol, double hertzPerUnit) {
        this.symbol = symbol;
        this.hertzPerUnit = hertzPerUnit;
    }

    public double toHertz(double value) {
        return value * hertzPerUnit;
    }

    public double fromHertz(double hertz) {
        return hertz / hertzPerUnit;
    }

    public String symbol() {
        return symbol;
    }
}
