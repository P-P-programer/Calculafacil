package com.calculafacil.model;

public enum SpectralRepresentation {
    WAVELENGTH("Longitud de onda λ", "λ", "Bλ", "m") {
        @Override
        public double toSi(double value) {
            return value;
        }

        @Override
        public double fromSi(double value) {
            return value;
        }
    },
    FREQUENCY("Frecuencia ν", "ν", "Bν", "Hz") {
        @Override
        public double toSi(double value) {
            return value;
        }

        @Override
        public double fromSi(double value) {
            return value;
        }
    };

    private final String label;
    private final String symbol;
    private final String radianceLabel;
    private final String siUnit;

    SpectralRepresentation(String label, String symbol, String radianceLabel, String siUnit) {
        this.label = label;
        this.symbol = symbol;
        this.radianceLabel = radianceLabel;
        this.siUnit = siUnit;
    }

    public String label() {
        return label;
    }

    public String symbol() {
        return symbol;
    }

    public String radianceLabel() {
        return radianceLabel;
    }

    public String siUnit() {
        return siUnit;
    }

    public abstract double toSi(double value);

    public abstract double fromSi(double value);

    @Override
    public String toString() {
        return label;
    }
}
