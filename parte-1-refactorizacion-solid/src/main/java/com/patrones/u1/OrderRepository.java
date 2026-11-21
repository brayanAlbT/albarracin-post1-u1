package com.patrones.u1;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsable únicamente de la persistencia y consulta en memoria de las órdenes.
 * Cumple con SRP al aislar el almacenamiento de datos del procesamiento de negocio.
 */
public class OrderRepository {
    private final List<String> orders = new ArrayList<>();

    public void save(String orderId, double total) {
        orders.add(orderId + ":" + total);
        System.out.println("[DB] Orden guardada: " + orderId);
    }

    public List<String> findAll() {
        return List.copyOf(orders);
    }
}
