package com.calculafacil.model;

public record Temperature(double kelvin) {

    public Temperature {
        if (!Double.isFinite(kelvin) || kelvin <= 0.0) {
            throw new IllegalArgumentException("La temperatura debe ser finita y mayor que 0 K.");
        }
    }

    public static Temperature fromCelsius(double celsius) {
        return new Temperature(celsius + 273.15);
    }

    public static Temperature fromFahrenheit(double fahrenheit) {
        return new Temperature((fahrenheit + 459.67) * 5.0 / 9.0);
    }

    public double celsius() {
        return kelvin - 273.15;
    }

    public double fahrenheit() {
        return kelvin * 9.0 / 5.0 - 459.67;
    }
}
