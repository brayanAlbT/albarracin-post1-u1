package com.patrones.u1;

import java.util.List;

/**
 * Responsable únicamente del formateo y presentación visual de reportes de órdenes en consola.
 * Cumple con SRP al desacoplar la capa de visualización de los datos y su persistencia.
 */
public class OrderReporter {
    public void print(List<String> orders) {
        System.out.println("=== Reporte de Órdenes ===");
        orders.forEach(o -> System.out.println("  " + o));
    }
}
