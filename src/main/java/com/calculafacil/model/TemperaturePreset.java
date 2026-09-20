package com.calculafacil.model;

public enum TemperaturePreset {
    SUN("Sol", 5778.0),
    OVEN("Horno", 1500.0),
    CMB("Fondo cósmico de microondas", 2.725),
    FILAMENT("Filamento", 2900.0);

    private final String label;
    private final Temperature temperature;

    TemperaturePreset(String label, double kelvin) {
        this.label = label;
        this.temperature = new Temperature(kelvin);
    }

    public String label() {
        return label;
    }

    public Temperature temperature() {
        return temperature;
    }
}
