package com.patrones.u1;

/**
 * Estrategia de descuento abierta a extensión y cerrada a modificación (OCP).
 * Define el contrato para cualquier algoritmo de cálculo de descuento.
 */
public interface DiscountStrategy {
    double apply(double total);
}
