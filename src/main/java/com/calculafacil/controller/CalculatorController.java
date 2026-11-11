package com.calculafacil.controller;

import com.calculafacil.model.BlackBodyRadiationModel;
import com.calculafacil.model.Emissivity;
import com.calculafacil.model.LengthUnit;
import com.calculafacil.model.SpectrumCurve;
import com.calculafacil.model.Temperature;
import com.calculafacil.model.TemperaturePreset;
import com.calculafacil.model.TemperatureUnit;
import com.calculafacil.model.SpectralRepresentation;
import com.calculafacil.service.SpectrumSampler;
import com.calculafacil.service.SpectralRadianceCalculator;
import com.calculafacil.service.StefanBoltzmannVerifier;
import com.calculafacil.service.CsvSpectrumExporter;
import java.io.IOException;
import java.nio.file.Path;
import javax.swing.JFileChooser;
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
        view.representation().addActionListener(event -> {
            refreshCurves();
            updatePeakLabel();
            view.setTip("La representación cambió; las curvas se recalcularon automáticamente.");
        });
        view.frequencyUnit().addActionListener(event -> refreshCurves());
        view.lengthUnit().addActionListener(event -> {
            if (!isFrequencyRepresentation()) {
                refreshCurves();
                updatePeakLabel();
                view.setTip("La unidad del eje cambió; la gráfica se actualizó sin recalcular la física.");
            }
        });
        view.results().addActionListener(event -> {
            if (curves.isEmpty()) {
                view.alert("Primero debes agregar al menos una curva para consultar sus datos.");
            } else {
                view.showDataTable(curves, axisLabel(), axisFactor());
            }
        });
        view.help().addActionListener(event -> view.showHelp());
        view.export().addActionListener(event -> exportCurves());
        view.clear().addActionListener(event -> {
            curves.clear();
            definitions.clear();
            view.showCurves(curves, selectedLengthUnit());
            view.verification().setText("Añade una curva para ver la verificación.");
            view.peak().setText("Máximo: --");
            view.setTip("La gráfica está limpia. Selecciona una temperatura para comenzar.");
        });
        view.showPlanck().addActionListener(event -> {
            refreshCurves();
            view.setTip("Planck es la predicción completa; las otras curvas son aproximaciones históricas.");
        });
        view.showRayleighJeans().addActionListener(event -> {
            refreshCurves();
            view.setTip("Rayleigh-Jeans permite ver claramente la catástrofe ultravioleta.");
        });
        view.showWien().addActionListener(event -> {
            refreshCurves();
            view.setTip("Wien se aproxima bien en longitudes de onda pequeñas, pero no en todo el espectro.");
        });
        view.presets().addActionListener(event -> {
            TemperaturePreset preset = (TemperaturePreset) view.presets().getSelectedItem();
            if (preset != null) {
                view.temperature().setValue(preset.temperature().kelvin());
                view.temperatureUnit().setSelectedItem(TemperatureUnit.KELVIN);
                view.setTip("Caso seleccionado: " + preset + ". Pulsa «Agregar curva» para graficarlo.");
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
            updatePeakLabel();
            StefanBoltzmannVerifier result = StefanBoltzmannVerifier.verify(
                    sampler.samplePlanckWavelength("integración", 50e-9, 500e-6,
                            10_000, selectedTemperature, selectedEmissivity),
                    selectedTemperature, selectedEmissivity);
            view.verification().setText(String.format("∫Bλ dλ · π: %s W/m² | εσT⁴: %s W/m² | Error: %.3f%%",
                    CalculatorView.format(result.numericalPower()),
                    CalculatorView.format(result.theoreticalPower()),
                    result.relativeError() * 100.0));
            view.status().setText(String.format("Curva agregada para %.2f K (%.2f °C, %.2f °F). "
                    + "Puedes superponer más temperaturas.", selectedTemperature.kelvin(),
                    selectedTemperature.celsius(), selectedTemperature.fahrenheit()));
            view.setTip("Usa la rueda sobre el plano para ampliar y arrastra para recorrer la curva.");
        } catch (IllegalArgumentException exception) {
            view.alert("Entrada no válida: " + exception.getMessage());
        }
    }

    private LengthUnit selectedLengthUnit() {
        return (LengthUnit) view.lengthUnit().getSelectedItem();
    }

    private void exportCurves() {
        if (curves.isEmpty()) {
            view.alert("Primero debes agregar al menos una curva para exportar datos.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar datos espectrales");
        chooser.setSelectedFile(new java.io.File("radiacion-cuerpo-negro.csv"));
        if (chooser.showSaveDialog(view.frame()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path destination = chooser.getSelectedFile().toPath();
        try {
            new CsvSpectrumExporter().export(destination, curves, axisSymbol(), axisUnit(), axisFactor());
            view.status().setText("CSV exportado correctamente: " + destination);
            view.setTip("El CSV contiene el eje y la unidad seleccionados, más una columna por curva.");
        } catch (IOException | IllegalArgumentException exception) {
            view.alert("No se pudo exportar el CSV: " + exception.getMessage());
        }
    }

    private void refreshCurves() {
        curves.clear();
        SpectralRepresentation selectedRepresentation =
                (SpectralRepresentation) view.representation().getSelectedItem();
        if (selectedRepresentation == SpectralRepresentation.FREQUENCY) {
            refreshFrequencyCurves();
            return;
        }
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

    private void refreshFrequencyCurves() {
        String selectedUnit = (String) view.frequencyUnit().getSelectedItem();
        double factor = "THz".equals(selectedUnit) ? 1e-12 : 1.0;
        curves.clear();
        for (CurveDefinition definition : definitions) {
            double minimum = 1e10;
            double maximum = 1e16;
            String prefix = String.format("%.2f K, ε=%.2f", definition.temperature().kelvin(),
                    definition.emissivity().value());
            if (view.showPlanck().isSelected()) {
                curves.add(sampler.sample("Planck " + prefix, minimum, maximum, 600,
                        frequency -> calculator.planckByFrequency(frequency,
                                definition.temperature(), definition.emissivity())));
            }
            if (view.showRayleighJeans().isSelected()) {
                curves.add(sampler.sample("Rayleigh-Jeans " + prefix, minimum, maximum, 600,
                        frequency -> calculator.rayleighJeansByFrequency(frequency,
                                definition.temperature(), definition.emissivity())));
            }
            if (view.showWien().isSelected()) {
                curves.add(sampler.sample("Wien clásica " + prefix, minimum, maximum, 600,
                        frequency -> calculator.wienByFrequency(frequency,
                                definition.temperature(), definition.emissivity())));
            }
        }
        view.chart().setFrequencyCurves(curves, selectedUnit, factor);
    }

    private String axisSymbol() {
        return isFrequencyRepresentation() ? "ν" : "λ";
    }

    private String axisUnit() {
        if (isFrequencyRepresentation()) {
            return (String) view.frequencyUnit().getSelectedItem();
        }
        return selectedLengthUnit().symbol();
    }

    private double axisFactor() {
        if (isFrequencyRepresentation()) {
            return "THz".equals(view.frequencyUnit().getSelectedItem()) ? 1e-12 : 1.0;
        }
        return 1.0 / selectedLengthUnit().toMeters(1.0);
    }

    private String axisLabel() {
        return axisSymbol() + " (" + axisUnit() + ")";
    }

    private boolean isFrequencyRepresentation() {
        return view.representation().getSelectedItem() == SpectralRepresentation.FREQUENCY;
    }

    private void updatePeakLabel() {
        if (curves.isEmpty() || definitions.isEmpty()) {
            view.peak().setText("Máximo: --");
            return;
        }
        if (isFrequencyRepresentation()) {
            double peakFrequency = curves.stream()
                    .flatMap(curve -> curve.points().stream())
                    .max(java.util.Comparator.comparingDouble(point -> point.radiance()))
                    .map(point -> point.independentValue())
                    .orElse(0.0);
            view.peak().setText(String.format("νmáx: %.3g %s", peakFrequency * axisFactor(),
                    axisUnit()));
            return;
        }
        LengthUnit lengthUnit = selectedLengthUnit();
        double peakMeters = calculator.wavelengthPeak(definitions.get(0).temperature());
        view.peak().setText(String.format("λmax: %.3f %s", lengthUnit.fromMeters(peakMeters),
                lengthUnit.symbol()));
    }

    private record CurveDefinition(Temperature temperature, Emissivity emissivity) {
    }
}
