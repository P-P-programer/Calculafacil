package com.calculafacil;

import com.calculafacil.controller.CalculatorController;
import com.calculafacil.model.BlackBodyRadiationModel;
import com.calculafacil.view.CalculatorView;
import javax.swing.SwingUtilities;

/**
 * Punto de entrada de la aplicación.
 *
 * <p>La composición de las capas se incorporará cuando se defina la interfaz
 * de usuario y el flujo de cálculo.</p>
 */
public final class App {

    private App() {
        // Clase de utilidad; no se instancia.
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CalculatorView view = new CalculatorView();
            new CalculatorController(new BlackBodyRadiationModel(), view);
            view.frame().setLocationByPlatform(true);
            view.frame().setVisible(true);
        });
    }
}
