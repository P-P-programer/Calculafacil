package com.calculafacil.model;

public enum TemperatureUnit {
    KELVIN("K") {
        @Override
        public Temperature toKelvin(double value) {
            return new Temperature(value);
        }

        @Override
        public double fromKelvin(double kelvin) {
            return kelvin;
        }
    },
    CELSIUS("°C") {
        @Override
        public Temperature toKelvin(double value) {
            return Temperature.fromCelsius(value);
        }

        @Override
        public double fromKelvin(double kelvin) {
            return kelvin - 273.15;
        }
    },
    FAHRENHEIT("°F") {
        @Override
        public Temperature toKelvin(double value) {
            return Temperature.fromFahrenheit(value);
        }

        @Override
        public double fromKelvin(double kelvin) {
            return kelvin * 9.0 / 5.0 - 459.67;
        }
    };

    private final String symbol;

    TemperatureUnit(String symbol) {
        this.symbol = symbol;
    }

    public abstract Temperature toKelvin(double value);

    public abstract double fromKelvin(double kelvin);

    public String symbol() {
        return symbol;
    }
}
