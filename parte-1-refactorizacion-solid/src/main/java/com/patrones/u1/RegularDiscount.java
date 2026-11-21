package com.patrones.u1;

/**
 * Aplica un descuento del 5% para clientes de categoría Regular.
 */
public class RegularDiscount implements DiscountStrategy {
    @Override
    public double apply(double total) {
        return total * 0.95;
    }
}
