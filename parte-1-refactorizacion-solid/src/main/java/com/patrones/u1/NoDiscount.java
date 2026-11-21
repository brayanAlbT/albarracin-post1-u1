package com.patrones.u1;

/**
 * Estrategia nula / por defecto: no aplica deducciones sobre el total.
 */
public class NoDiscount implements DiscountStrategy {
    @Override
    public double apply(double total) {
        return total;
    }
}
