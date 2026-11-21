package com.patrones.u1;

import java.util.List;

/**
 * Responsable únicamente del cálculo de subtotales e impuestos para una orden.
 * Cumple con SRP al encapsular la tasa impositiva y el algoritmo de cálculo fiscal.
 */
public class TaxCalculator {
    private final double taxRate;

    public TaxCalculator(double taxRate) {
        this.taxRate = taxRate;
    }

    public double calculateTotal(List<Double> prices) {
        double subtotal = prices.stream().mapToDouble(Double::doubleValue).sum();
        return subtotal + (subtotal * taxRate);
    }
}
