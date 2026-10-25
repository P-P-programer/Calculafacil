package com.calculafacil.controller;

import com.calculafacil.model.BlackBodyRadiationModel;
import com.calculafacil.model.Emissivity;
import com.calculafacil.model.LengthUnit;
import com.calculafacil.model.SpectrumCurve;
import com.calculafacil.model.Temperature;
import com.calculafacil.model.TemperaturePreset;
import com.calculafacil.model.TemperatureUnit;
import com.calculafacil.service.SpectrumSampler;
import com.calculafacil.service.SpectralRadianceCalculator;
import com.calculafacil.service.StefanBoltzmannVerifier;
import com.calculafacil.view.CalculatorView;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador principal de la calculadora.
 *
 * <p>Coordinará los eventos de la vista y el modelo cuando se implemente la
 * lógica de la aplicación.</p>
 */
public class CalculatorController {

    private final BlackBodyRadiationModel model;
    private final CalculatorView view;
    private final List<SpectrumCurve> curves = new ArrayList<>();
    private final List<CurveDefinition> definitions = new ArrayList<>();
    private final SpectrumSampler sampler = new SpectrumSampler();
    private final SpectralRadianceCalculator calculator = new SpectralRadianceCalculator();

    public CalculatorController(BlackBodyRadiationModel model, CalculatorView view) {
        this.model = model;
        this.view = view;
        view.calculate().addActionListener(event -> addCurve());
        view.clear().addActionListener(event -> {
            curves.clear();
            definitions.clear();
            view.showCurves(curves, selectedLengthUnit());
            view.verification().setText("Añade una curva para ver la verificación.");
            view.peak().setText("λmax: --");
        });
        view.showPlanck().addActionListener(event -> refreshCurves());
        view.showRayleighJeans().addActionListener(event -> refreshCurves());
        view.showWien().addActionListener(event -> refreshCurves());
        view.presets().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && view.presets().getSelectedValue() != null) {
                TemperaturePreset preset = view.presets().getSelectedValue();
                view.temperature().setValue(preset.temperature().kelvin());
                view.temperatureUnit().setSelectedItem(TemperatureUnit.KELVIN);
            }
        });
    }

    public BlackBodyRadiationModel getModel() {
        return model;
    }

    public CalculatorView getView() {
        return view;
    }

    private void addCurve() {
        try {
            TemperatureUnit unit = (TemperatureUnit) view.temperatureUnit().getSelectedItem();
            Temperature selectedTemperature = unit.toKelvin(((Number) view.temperature().getValue()).doubleValue());
            Emissivity selectedEmissivity = new Emissivity(((Number) view.emissivity().getValue()).doubleValue());
            LengthUnit lengthUnit = selectedLengthUnit();
            double minimum = lengthUnit.toMeters(50.0);
            double maximum = lengthUnit.toMeters(5000.0);
            definitions.add(new CurveDefinition(selectedTemperature, selectedEmissivity));
            refreshCurves();
            double peakMeters = calculator.wavelengthPeak(selectedTemperature);
            view.peak().setText(String.format("λmax: %.3f %s", lengthUnit.fromMeters(peakMeters),
                    lengthUnit.symbol()));
            StefanBoltzmannVerifier result = StefanBoltzmannVerifier.verify(
                    sampler.samplePlanckWavelength("integración", 50e-9, 500e-6,
                            10_000, selectedTemperature, selectedEmissivity),
                    selectedTemperature, selectedEmissivity);
            view.verification().setText(String.format("Integral: %s W/m² | Teórica: %s W/m² | Error: %.3f%%",
                    CalculatorView.format(result.numericalPower()),
                    CalculatorView.format(result.theoreticalPower()),
                    result.relativeError() * 100.0));
            view.status().setText(String.format("Curva agregada para %.2f K (%.2f °C, %.2f °F). "
                    + "Puedes superponer más temperaturas.", selectedTemperature.kelvin(),
                    selectedTemperature.celsius(), selectedTemperature.fahrenheit()));
        } catch (IllegalArgumentException exception) {
            view.status().setText("Entrada no válida: " + exception.getMessage());
        }
    }

    private LengthUnit selectedLengthUnit() {
        return (LengthUnit) view.lengthUnit().getSelectedItem();
    }

    private void refreshCurves() {
        curves.clear();
        LengthUnit lengthUnit = selectedLengthUnit();
        for (CurveDefinition definition : definitions) {
            double minimum = lengthUnit.toMeters(50.0);
            double maximum = lengthUnit.toMeters(5000.0);
            String prefix = String.format("%.2f K, ε=%.2f", definition.temperature().kelvin(),
                    definition.emissivity().value());
            if (view.showPlanck().isSelected()) {
                curves.add(sampler.samplePlanckWavelength("Planck " + prefix, minimum, maximum,
                        600, definition.temperature(), definition.emissivity()));
            }
            if (view.showRayleighJeans().isSelected()) {
                curves.add(sampler.sampleWavelength("Rayleigh-Jeans " + prefix, minimum, maximum,
                        600, wavelength -> calculator.rayleighJeansByWavelength(
                                wavelength, definition.temperature(), definition.emissivity())));
            }
            if (view.showWien().isSelected()) {
                curves.add(sampler.sampleWavelength("Wien clásica " + prefix, minimum, maximum,
                        600, wavelength -> calculator.wienByWavelength(
                                wavelength, definition.temperature(), definition.emissivity())));
            }
        }
        view.showCurves(curves, lengthUnit);
    }

    private record CurveDefinition(Temperature temperature, Emissivity emissivity) {
    }
}
