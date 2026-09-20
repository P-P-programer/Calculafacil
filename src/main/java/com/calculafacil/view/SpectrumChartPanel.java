package com.calculafacil.view;

import com.calculafacil.model.LengthUnit;
import com.calculafacil.model.SpectrumCurve;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

public final class SpectrumChartPanel extends JPanel {

    private static final Color[] COLORS = {
        new Color(20, 80, 180), new Color(210, 70, 40), new Color(30, 140, 80),
        new Color(150, 70, 170), new Color(210, 130, 20)
    };
    private List<SpectrumCurve> curves = List.of();
    private LengthUnit unit = LengthUnit.NANOMETER;

    public SpectrumChartPanel() {
        setBackground(Color.WHITE);
        setBorder(javax.swing.BorderFactory.createTitledBorder("Radiancia espectral de Planck"));
    }

    public void setCurves(List<SpectrumCurve> curves, LengthUnit unit) {
        this.curves = List.copyOf(curves);
        this.unit = unit;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int left = 75;
        int right = 25;
        int top = 35;
        int bottom = 55;
        int width = getWidth() - left - right;
        int height = getHeight() - top - bottom;
        g.setColor(new Color(40, 40, 40));
        g.drawLine(left, top, left, top + height);
        g.drawLine(left, top + height, left + width, top + height);
        g.drawString("λ (" + unit.symbol() + ")", left + width / 2 - 20, getHeight() - 15);
        g.rotate(-Math.PI / 2);
        g.drawString("Bλ (W·sr⁻¹·m⁻³)", -top - height / 2 - 45, 18);
        g.rotate(Math.PI / 2);
        if (curves.isEmpty()) {
            g.drawString("Agrega una temperatura para comenzar.", left + 25, top + height / 2);
            g.dispose();
            return;
        }
        double minX = curves.stream().flatMap(curve -> curve.points().stream())
                .mapToDouble(point -> unit.fromMeters(point.independentValue())).min().orElse(0);
        double maxX = curves.stream().flatMap(curve -> curve.points().stream())
                .mapToDouble(point -> unit.fromMeters(point.independentValue())).max().orElse(1);
        double maxY = curves.stream().flatMap(curve -> curve.points().stream())
                .mapToDouble(point -> point.radiance()).max().orElse(1);
        for (int curveIndex = 0; curveIndex < curves.size(); curveIndex++) {
            SpectrumCurve curve = curves.get(curveIndex);
            g.setColor(COLORS[curveIndex % COLORS.length]);
            g.setStroke(new BasicStroke(2f));
            var points = new ArrayList<java.awt.Point>();
            curve.points().forEach(point -> points.add(new java.awt.Point(
                    left + (int) ((unit.fromMeters(point.independentValue()) - minX) / (maxX - minX) * width),
                    top + height - (int) (point.radiance() / maxY * height))));
            for (int index = 1; index < points.size(); index++) {
                g.drawLine(points.get(index - 1).x, points.get(index - 1).y,
                        points.get(index).x, points.get(index).y);
            }
            g.fillRect(left + 10, top + 12 + curveIndex * 20, 12, 3);
            g.drawString(curve.name(), left + 28, top + 16 + curveIndex * 20);
        }
        g.dispose();
    }
}
