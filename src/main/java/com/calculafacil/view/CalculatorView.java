package com.calculafacil.view;

import com.calculafacil.model.LengthUnit;
import com.calculafacil.model.TemperaturePreset;
import com.calculafacil.model.TemperatureUnit;
import com.calculafacil.model.SpectrumCurve;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

public class CalculatorView {

    private final JFrame frame = new JFrame("Calcula Fácil - Radiación de cuerpo negro");
    private final JTextArea status = new JTextArea(4, 30);
    private final SpectrumChartPanel chart = new SpectrumChartPanel();
    private final JSpinner temperature = new JSpinner(new SpinnerNumberModel(5778.0, 0.01, 1_000_000.0, 10.0));
    private final JComboBox<TemperatureUnit> temperatureUnit = new JComboBox<>(TemperatureUnit.values());
    private final JSpinner emissivity = new JSpinner(new SpinnerNumberModel(1.0, 0.01, 1.0, 0.01));
    private final JComboBox<LengthUnit> lengthUnit = new JComboBox<>(LengthUnit.values());
    private final JList<TemperaturePreset> presets = new JList<>(TemperaturePreset.values());
    private final JButton calculate = new JButton("Agregar curva");
    private final JButton clear = new JButton("Limpiar");
    private final JLabel verification = new JLabel("Añade una curva para ver la verificación.");

    public CalculatorView() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1100, 700));
        frame.setLayout(new BorderLayout(8, 8));
        frame.add(createControls(), BorderLayout.WEST);
        frame.add(chart, BorderLayout.CENTER);
        frame.add(createResults(), BorderLayout.SOUTH);
        status.setEditable(false);
        status.setLineWrap(true);
        status.setWrapStyleWord(true);
        status.setText("Selecciona un preset o introduce una temperatura. "
                + "Las curvas muestran Bλ en W·sr⁻¹·m⁻³.");
    }

    private JPanel createControls() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Parámetros"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        addRow(panel, c, 0, "Temperatura:", temperature);
        addRow(panel, c, 1, "Escala:", temperatureUnit);
        addRow(panel, c, 2, "Emisividad ε:", emissivity);
        addRow(panel, c, 3, "Eje λ:", lengthUnit);
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2;
        panel.add(new JLabel("Presets"), c);
        c.gridy = 5;
        panel.add(new JScrollPane(presets), c);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(calculate);
        buttons.add(clear);
        c.gridy = 6;
        panel.add(buttons, c);
        c.gridy = 7;
        panel.add(status, c);
        return panel;
    }

    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, java.awt.Component component) {
        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 1;
        panel.add(new JLabel(label), c);
        c.gridx = 1;
        panel.add(component, c);
    }

    private JPanel createResults() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Verificación de Stefan-Boltzmann"));
        panel.add(verification);
        return panel;
    }

    public JFrame frame() {
        return frame;
    }

    public JSpinner temperature() {
        return temperature;
    }

    public JComboBox<TemperatureUnit> temperatureUnit() {
        return temperatureUnit;
    }

    public JSpinner emissivity() {
        return emissivity;
    }

    public JComboBox<LengthUnit> lengthUnit() {
        return lengthUnit;
    }

    public JList<TemperaturePreset> presets() {
        return presets;
    }

    public JButton calculate() {
        return calculate;
    }

    public JButton clear() {
        return clear;
    }

    public SpectrumChartPanel chart() {
        return chart;
    }

    public JLabel verification() {
        return verification;
    }

    public JTextArea status() {
        return status;
    }

    public void showCurves(List<SpectrumCurve> curves, LengthUnit unit) {
        chart.setCurves(curves, unit);
    }

    public static String format(double value) {
        return new DecimalFormat("0.###E0").format(value);
    }
}
