package com.calculafacil.service;

import com.calculafacil.model.LengthUnit;
import com.calculafacil.model.SpectrumCurve;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class CsvSpectrumExporter {

    public void export(Path destination, List<SpectrumCurve> curves, LengthUnit unit) throws IOException {
        if (curves.isEmpty()) {
            throw new IllegalArgumentException("No hay curvas visibles para exportar.");
        }
        try (BufferedWriter writer = Files.newBufferedWriter(destination, StandardCharsets.UTF_8)) {
            writer.write("# Calcula Fácil - radiación de cuerpo negro");
            writer.newLine();
            writer.write("# Longitud de onda (" + unit.symbol() + "), radiancia en SI (W·sr^-1·m^-3)");
            writer.newLine();
            writer.write("lambda_" + unit.symbol());
            for (SpectrumCurve curve : curves) {
                writer.write("," + escape(curve.name()));
            }
            writer.newLine();
            int rows = curves.get(0).points().size();
            for (int row = 0; row < rows; row++) {
                writer.write(Double.toString(unit.fromMeters(curves.get(0).points().get(row).independentValue())));
                for (SpectrumCurve curve : curves) {
                    if (row >= curve.points().size()) {
                        throw new IllegalArgumentException("Las curvas no tienen el mismo número de muestras.");
                    }
                    writer.write("," + Double.toString(curve.points().get(row).radiance()));
                }
                writer.newLine();
            }
        }
    }

    private String escape(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
