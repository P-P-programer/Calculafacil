package com.calculafacil.controller;

import com.calculafacil.model.BlackBodyRadiationModel;
import com.calculafacil.view.CalculatorView;

/**
 * Controlador principal de la calculadora.
 *
 * <p>Coordinará los eventos de la vista y el modelo cuando se implemente la
 * lógica de la aplicación.</p>
 */
public class CalculatorController {

    private final BlackBodyRadiationModel model;
    private final CalculatorView view;

    public CalculatorController(BlackBodyRadiationModel model, CalculatorView view) {
        this.model = model;
        this.view = view;
    }

    public BlackBodyRadiationModel getModel() {
        return model;
    }

    public CalculatorView getView() {
        return view;
    }
}
