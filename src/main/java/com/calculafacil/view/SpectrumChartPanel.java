package com.calculafacil.view;

import com.calculafacil.model.LengthUnit;
import com.calculafacil.model.SpectrumCurve;
import com.calculafacil.model.SpectrumPoint;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

public final class SpectrumChartPanel extends JPanel {

    private static final int LEFT = 75;
    private static final int RIGHT = 25;
    private static final int TOP = 35;
    private static final int BOTTOM = 55;
    private static final Color[] COLORS = {
        new Color(20, 80, 180), new Color(210, 70, 40), new Color(30, 140, 80),
        new Color(150, 70, 170), new Color(210, 130, 20)
    };

    private List<SpectrumCurve> curves = List.of();
    private LengthUnit unit = LengthUnit.NANOMETER;
    private double zoom = 1.0;
    private double viewStart = 0.0;
    private int pointerX = -1;
    private int pointerY = -1;
    private boolean dragging;
    private int dragStartX;
    private double dragStartViewStart;

    public SpectrumChartPanel() {
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createTitledBorder("Radiancia espectral de Planck"));
        setToolTipText("");
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent event) {
                updatePointer(event);
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                if (dragging && !curves.isEmpty()) {
                    double range = dataRange();
                    double visibleRange = range / zoom;
                    double plotWidth = Math.max(1, getWidth() - LEFT - RIGHT);
                    double delta = (dragStartX - event.getX()) * range / plotWidth;
                    viewStart = clamp(dragStartViewStart + delta, 0.0, range - visibleRange);
                }
                updatePointer(event);
            }

            @Override
            public void mousePressed(MouseEvent event) {
                if (isInsidePlot(event.getPoint())) {
                    dragging = true;
                    dragStartX = event.getX();
                    dragStartViewStart = viewStart;
                }
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                dragging = false;
            }
        };
        addMouseMotionListener(mouse);
        addMouseListener(mouse);
        addMouseWheelListener(event -> {
            if (isInsidePlot(event.getPoint())) {
                zoomAt(event.getX(), event.getWheelRotation() < 0 ? 1.25 : 1.0 / 1.25);
                repaint();
            }
        });
    }

    public void setCurves(List<SpectrumCurve> curves, LengthUnit unit) {
        this.curves = List.copyOf(curves);
        this.unit = unit;
        resetViewport();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int width = getWidth() - LEFT - RIGHT;
        int height = getHeight() - TOP - BOTTOM;
        if (width <= 0 || height <= 0) {
            g.dispose();
            return;
        }
        g.setColor(new Color(40, 40, 40));
        g.drawLine(LEFT, TOP, LEFT, TOP + height);
        g.drawLine(LEFT, TOP + height, LEFT + width, TOP + height);
        g.drawString("λ (" + unit.symbol() + ")", LEFT + width / 2 - 20, getHeight() - 15);
        g.rotate(-Math.PI / 2);
        g.drawString("Bλ (W·sr⁻¹·m⁻³)", -TOP - height / 2 - 45, 18);
        g.rotate(Math.PI / 2);
        if (curves.isEmpty()) {
            g.drawString("Agrega una temperatura para comenzar.", LEFT + 25, TOP + height / 2);
            g.dispose();
            return;
        }
        double dataMinX = dataMinimum();
        double dataRange = dataRange();
        double visibleRange = dataRange / zoom;
        double minX = dataMinX + viewStart;
        double maxX = minX + visibleRange;
        double maxY = curves.stream().flatMap(curve -> curve.points().stream())
                .mapToDouble(SpectrumPoint::radiance).max().orElse(1.0);
        if (maxX <= minX || maxY <= 0.0) {
            g.dispose();
            return;
        }
        g.setClip(LEFT, TOP, width, height);
        drawGrid(g, width, height);
        for (int curveIndex = 0; curveIndex < curves.size(); curveIndex++) {
            drawCurve(g, curves.get(curveIndex), curveIndex, width, height, minX, maxX, maxY);
        }
        drawPointer(g, width, height, minX, maxX, maxY);
        g.setClip(null);
        g.setColor(new Color(40, 40, 40));
        g.drawRect(LEFT, TOP, width, height);
        g.dispose();
    }

    private void drawCurve(Graphics2D g, SpectrumCurve curve, int curveIndex, int width,
            int height, double minX, double maxX, double maxY) {
        g.setColor(COLORS[curveIndex % COLORS.length]);
        float[] dash = curve.name().contains("Rayleigh") ? new float[]{8f, 5f}
                : curve.name().contains("Wien") ? new float[]{3f, 4f} : null;
        g.setStroke(dash == null ? new BasicStroke(2f) : new BasicStroke(2f,
                BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1f, dash, 0f));
        var points = new ArrayList<Point>();
        curve.points().forEach(point -> {
            double x = unit.fromMeters(point.independentValue());
            if (x >= minX && x <= maxX) {
                points.add(new Point(
                        LEFT + (int) ((x - minX) / (maxX - minX) * width),
                        TOP + height - (int) (point.radiance() / maxY * height)));
            }
        });
        for (int index = 1; index < points.size(); index++) {
            g.drawLine(points.get(index - 1).x, points.get(index - 1).y,
                    points.get(index).x, points.get(index).y);
        }
        g.fillRect(LEFT + 10, TOP + 12 + curveIndex * 20, 12, 3);
        g.drawString(curve.name(), LEFT + 28, TOP + 16 + curveIndex * 20);
    }

    private void drawGrid(Graphics2D g, int width, int height) {
        g.setStroke(new BasicStroke(1f));
        g.setColor(new Color(232, 236, 242));
        for (int index = 1; index < 10; index++) {
            int x = LEFT + index * width / 10;
            int y = TOP + index * height / 10;
            g.drawLine(x, TOP, x, TOP + height);
            g.drawLine(LEFT, y, LEFT + width, y);
        }
    }

    private void drawPointer(Graphics2D g, int width, int height,
            double minX, double maxX, double maxY) {
        if (!isInsidePlot(new Point(pointerX, pointerY))) {
            return;
        }
        double selectedX = minX + (pointerX - LEFT) * (maxX - minX) / width;
        int crosshairX = (int) Math.round(LEFT + (selectedX - minX) / (maxX - minX) * width);
        g.setColor(new Color(70, 75, 85, 150));
        g.setStroke(new BasicStroke(1f));
        g.drawLine(crosshairX, TOP, crosshairX, TOP + height);
        int cardX = Math.min(crosshairX + 12, LEFT + width - 265);
        int cardY = Math.max(TOP + 8, Math.min(pointerY - 45, TOP + height - 120));
        int cardHeight = 32 + curves.size() * 19;
        g.setColor(new Color(255, 255, 255, 235));
        g.fillRoundRect(cardX, cardY, 250, cardHeight, 8, 8);
        g.setColor(new Color(75, 80, 90));
        g.drawRoundRect(cardX, cardY, 250, cardHeight, 8, 8);
        g.drawString(String.format("λ = %.4g %s", selectedX, unit.symbol()), cardX + 10, cardY + 20);
        for (int index = 0; index < curves.size(); index++) {
            SpectrumCurve curve = curves.get(index);
            SpectrumPoint closest = closestPoint(curve, selectedX);
            double pointValue = unit.fromMeters(closest.independentValue());
            int pointX = LEFT + (int) ((pointValue - minX) / (maxX - minX) * width);
            int pointY = TOP + height - (int) (closest.radiance() / maxY * height);
            g.setColor(COLORS[index % COLORS.length]);
            g.fillOval(pointX - 4, pointY - 4, 8, 8);
            g.drawString(String.format("%s: %.4g", shortName(curve.name()), closest.radiance()),
                    cardX + 10, cardY + 39 + index * 19);
        }
    }

    private SpectrumPoint closestPoint(SpectrumCurve curve, double value) {
        return curve.points().stream().min((first, second) -> Double.compare(
                Math.abs(unit.fromMeters(first.independentValue()) - value),
                Math.abs(unit.fromMeters(second.independentValue()) - value))).orElseThrow();
    }

    private String shortName(String name) {
        int separator = name.indexOf(' ');
        return separator > 0 ? name.substring(0, separator) : name;
    }

    private void updatePointer(MouseEvent event) {
        pointerX = event.getX();
        pointerY = event.getY();
        setToolTipText(isInsidePlot(event.getPoint())
                ? "Punto más cercano marcado · arrastra para desplazarte · rueda para zoom" : null);
        repaint();
    }

    private void zoomAt(int x, double factor) {
        double range = dataRange();
        double oldVisibleRange = range / zoom;
        double cursorRatio = (x - LEFT) / (double) Math.max(1, getWidth() - LEFT - RIGHT);
        double cursorValue = viewStart + cursorRatio * oldVisibleRange;
        zoom = clamp(zoom * factor, 1.0, 20.0);
        double newVisibleRange = range / zoom;
        viewStart = clamp(cursorValue - cursorRatio * newVisibleRange, 0.0, range - newVisibleRange);
    }

    private boolean isInsidePlot(Point point) {
        return point != null && point.x >= LEFT && point.x <= getWidth() - RIGHT
                && point.y >= TOP && point.y <= getHeight() - BOTTOM;
    }

    private double dataMinimum() {
        return curves.stream().flatMap(curve -> curve.points().stream())
                .mapToDouble(point -> unit.fromMeters(point.independentValue())).min().orElse(0.0);
    }

    private double dataRange() {
        double minimum = dataMinimum();
        double maximum = curves.stream().flatMap(curve -> curve.points().stream())
                .mapToDouble(point -> unit.fromMeters(point.independentValue())).max().orElse(1.0);
        return Math.max(1e-12, maximum - minimum);
    }

    private void resetViewport() {
        zoom = 1.0;
        viewStart = 0.0;
        pointerX = -1;
        pointerY = -1;
    }

    private double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
