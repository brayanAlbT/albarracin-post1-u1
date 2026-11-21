package com.patrones.u1;

/**
 * Aplica un descuento del 15% para clientes de categoría VIP.
 */
public class VipDiscount implements DiscountStrategy {
    @Override
    public double apply(double total) {
        return total * 0.85;
    }
}
