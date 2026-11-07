package com.calculafacil.view;

import com.calculafacil.model.LengthUnit;
import com.calculafacil.model.TemperaturePreset;
import com.calculafacil.model.TemperatureUnit;
import com.calculafacil.model.SpectrumCurve;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Image;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.SpinnerNumberModel;

public class CalculatorView {

    private static final Color GRAPHITE = new Color(38, 43, 51);
    private static final Color SLATE = new Color(74, 86, 100);
    private static final Color PETROLEUM = new Color(25, 92, 110);
    private static final Color SURFACE = new Color(247, 249, 251);
    private static final Color BORDER = new Color(205, 213, 221);

    private final JFrame frame = new JFrame("Calcula Fácil - Radiación de cuerpo negro");
    private final JTextArea status = new JTextArea(4, 30);
    private final JTextArea instructions = new JTextArea();
    private final JLabel tip = new JLabel();
    private final SpectrumChartPanel chart = new SpectrumChartPanel();
    private final JSpinner temperature = new JSpinner(new SpinnerNumberModel(5778.0, 0.01, 1_000_000.0, 10.0));
    private final JComboBox<TemperatureUnit> temperatureUnit = new JComboBox<>(TemperatureUnit.values());
    private final JSpinner emissivity = new JSpinner(new SpinnerNumberModel(1.0, 0.01, 1.0, 0.01));
    private final JComboBox<LengthUnit> lengthUnit = new JComboBox<>(LengthUnit.values());
    private final JList<TemperaturePreset> presets = new JList<>(TemperaturePreset.values());
    private final JButton calculate = new JButton("Agregar curva");
    private final JButton clear = new JButton("Limpiar");
    private final JLabel verification = new JLabel("Añade una curva para ver la verificación.");
    private final JCheckBox showPlanck = new JCheckBox("Planck", true);
    private final JCheckBox showRayleighJeans = new JCheckBox("Rayleigh-Jeans");
    private final JCheckBox showWien = new JCheckBox("Wien clásica");
    private final JLabel peak = new JLabel("λmax: --");
    private final JButton importantNotice = new JButton("♥ Aviso importante");
    private final JButton results = new JButton("Ver datos");
    private final JButton export = new JButton("Exportar CSV");
    private final JButton help = new JButton("Ayuda física");

    public CalculatorView() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1100, 700));
        frame.setBackground(SURFACE);
        frame.setLayout(new BorderLayout(8, 8));
        frame.add(createHeader(), BorderLayout.NORTH);
        frame.add(createControls(), BorderLayout.WEST);
        frame.add(createChartArea(), BorderLayout.CENTER);
        frame.add(createResults(), BorderLayout.SOUTH);
        configureComponentStyles();
        status.setEditable(false);
        status.setLineWrap(true);
        status.setWrapStyleWord(true);
        status.setText("Selecciona un preset o introduce una temperatura. "
                + "Las curvas muestran Bλ en W·sr⁻¹·m⁻³.");
        setTip("Empieza con el preset Sol y pulsa «Agregar curva» para explorar su máximo.");
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 2));
        header.setBackground(GRAPHITE);
        header.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        JLabel title = new JLabel("Calcula Fácil  /  Radiación de cuerpo negro");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        header.add(title, BorderLayout.WEST);
        instructions.setEditable(false);
        instructions.setOpaque(false);
        instructions.setForeground(new Color(225, 232, 238));
        instructions.setFont(instructions.getFont().deriveFont(12f));
        instructions.setText("Guía rápida: agrega una temperatura · rueda para zoom · arrastra para desplazarte · "
                + "pasa el cursor para consultar valores");
        header.add(instructions, BorderLayout.CENTER);
        importantNotice.setToolTipText("Abrir aviso importante");
        importantNotice.addActionListener(event -> showImportantNotice());
        header.add(importantNotice, BorderLayout.EAST);
        return header;
    }

    private JPanel createChartArea() {
        JPanel area = new JPanel(new BorderLayout(0, 6));
        area.setBackground(SURFACE);
        area.add(chart, BorderLayout.CENTER);
        JPanel tipBox = new JPanel(new BorderLayout());
        tipBox.setBackground(new Color(235, 242, 245));
        tipBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 211, 218)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        tipBox.add(tip, BorderLayout.CENTER);
        area.add(tipBox, BorderLayout.SOUTH);
        return area;
    }

    private JPanel createControls() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Parámetros"));
        panel.setBackground(SURFACE);
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
        buttons.setOpaque(false);
        buttons.add(calculate);
        buttons.add(clear);
        buttons.add(results);
        buttons.add(export);
        buttons.add(help);
        c.gridy = 6;
        panel.add(buttons, c);
        c.gridy = 7;
        JPanel laws = new JPanel(new FlowLayout(FlowLayout.LEFT));
        laws.setOpaque(false);
        laws.add(showPlanck);
        laws.add(showRayleighJeans);
        laws.add(showWien);
        panel.add(laws, c);
        c.gridy = 8;
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
        panel.setBackground(SURFACE);
        panel.add(peak);
        panel.add(verification);
        return panel;
    }

    private void configureComponentStyles() {
        frame.getContentPane().setBackground(SURFACE);
        chart.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(), "Radiancia espectral de Planck",
                        javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                        new Font(Font.SANS_SERIF, Font.BOLD, 13), GRAPHITE)));
        calculate.setBackground(PETROLEUM);
        calculate.setForeground(Color.WHITE);
        calculate.setFocusPainted(false);
        clear.setBackground(new Color(232, 236, 240));
        clear.setForeground(GRAPHITE);
        clear.setFocusPainted(false);
        status.setBackground(new Color(238, 242, 245));
        status.setForeground(SLATE);
        status.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(5, 6, 5, 6)));
        presets.setBackground(Color.WHITE);
        presets.setSelectionBackground(new Color(206, 230, 236));
        presets.setSelectionForeground(GRAPHITE);
        verification.setForeground(SLATE);
        peak.setForeground(PETROLEUM);
        peak.setFont(peak.getFont().deriveFont(Font.BOLD));
        tip.setForeground(SLATE);
        tip.setFont(tip.getFont().deriveFont(Font.PLAIN, 12f));
        importantNotice.setBackground(new Color(112, 50, 75));
        importantNotice.setForeground(Color.WHITE);
        importantNotice.setFocusPainted(false);
        importantNotice.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        results.setBackground(new Color(232, 236, 240));
        export.setBackground(PETROLEUM);
        export.setForeground(Color.WHITE);
        help.setBackground(new Color(232, 236, 240));
        results.setFocusPainted(false);
        export.setFocusPainted(false);
        help.setFocusPainted(false);
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

    public JCheckBox showPlanck() {
        return showPlanck;
    }

    public JCheckBox showRayleighJeans() {
        return showRayleighJeans;
    }

    public JCheckBox showWien() {
        return showWien;
    }

    public JLabel peak() {
        return peak;
    }

    public JButton results() {
        return results;
    }

    public JButton export() {
        return export;
    }

    public JButton help() {
        return help;
    }

    public void showDataTable(List<SpectrumCurve> curves, LengthUnit unit) {
        if (curves.isEmpty()) {
            setTip("Agrega al menos una curva antes de consultar sus datos.");
            return;
        }
        String[] columns = new String[curves.size() + 1];
        columns[0] = "λ (" + unit.symbol() + ")";
        for (int index = 0; index < curves.size(); index++) {
            columns[index + 1] = curves.get(index).name();
        }
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
        int rows = curves.get(0).points().size();
        for (int row = 0; row < rows; row++) {
            Object[] values = new Object[columns.length];
            values[0] = String.format("%.6g", unit.fromMeters(curves.get(0).points().get(row).independentValue()));
            for (int curve = 0; curve < curves.size(); curve++) {
                values[curve + 1] = String.format("%.6g", curves.get(curve).points().get(row).radiance());
            }
            tableModel.addRow(values);
        }
        JTable table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFillsViewportHeight(true);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(900, 480));
        javax.swing.JOptionPane.showMessageDialog(frame, scroll,
                "Datos de curvas visibles", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    public void showHelp() {
        JTextArea text = new JTextArea("""
                ¿Qué estás viendo?

                Planck (Bλ) describe la radiancia espectral de un cuerpo negro.
                λmax = b/T indica la longitud de onda del máximo.

                Comparaciones históricas:
                • Rayleigh-Jeans funciona en longitudes de onda grandes, pero diverge
                  hacia el ultravioleta.
                • Wien es útil en la región de alta frecuencia, pero no describe
                  correctamente todo el espectro.

                Cómo usar la gráfica:
                • Rueda: zoom alrededor del cursor.
                • Arrastre: desplazamiento horizontal.
                • Cursor: valores del punto más cercano.

                Unidades:
                • La longitud de onda se calcula internamente en metros.
                • La temperatura física se convierte internamente a kelvin.
                • ε=1 es cuerpo negro; ε<1 es cuerpo gris.

                Stefan-Boltzmann:
                La integral de Bλ se multiplica por π para integrar la radiancia
                sobre el hemisferio emisor y compararla con εσT⁴.
                """);
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setFont(text.getFont().deriveFont(13f));
        text.setBackground(SURFACE);
        text.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JScrollPane scroll = new JScrollPane(text);
        scroll.setPreferredSize(new Dimension(620, 420));
        javax.swing.JOptionPane.showMessageDialog(frame, scroll,
                "Ayuda física", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    public void setTip(String message) {
        tip.setText("  Consejo: " + message);
    }

    private void showImportantNotice() {
        var imageUrl = CalculatorView.class.getResource("/images/moncada.png");
        if (imageUrl == null) {
            javax.swing.JOptionPane.showMessageDialog(frame,
                    "No se encontró el recurso /images/moncada.png.",
                    "Aviso importante",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        Image image = new ImageIcon(imageUrl).getImage()
                .getScaledInstance(240, 240, Image.SCALE_SMOOTH);
        JPanel content = new JPanel(new BorderLayout(12, 8));
        content.setBackground(SURFACE);
        content.add(new JLabel(new ImageIcon(image)), BorderLayout.WEST);
        JLabel message = new JLabel("<html><div style='width:260px'>"
                + "<h2>♥ Aviso importante</h2>"
                + "<p><b>Moncada, el mejor profesor de todos.</b></p>"
                + "<p>Atentamente,<br>los mejores hombres del mundo.</p>"
                + "</div></html>");
        content.add(message, BorderLayout.CENTER);
        javax.swing.JOptionPane.showMessageDialog(frame, content,
                "Aviso importante", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    public void showCurves(List<SpectrumCurve> curves, LengthUnit unit) {
        chart.setCurves(curves, unit);
    }

    public static String format(double value) {
        return new DecimalFormat("0.###E0").format(value);
    }
}
