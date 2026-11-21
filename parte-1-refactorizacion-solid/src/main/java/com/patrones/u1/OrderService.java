package com.patrones.u1;

import java.util.List;

/**
 * Servicio de orquestación de procesamiento de órdenes.
 * Cumple con DIP (Principio de Inversión de Dependencias) al depender de abstracciones
 * y recibir sus colaboradores inyectados a través del constructor.
 */
public class OrderService {
    private final TaxCalculator taxCalculator;
    private final OrderRepository repository;
    private final EmailNotifier notifier;
    private final DiscountStrategy discountStrategy;

    /**
     * Inyección de dependencias por constructor (DIP).
     * El servicio desacopla la creación de instancias de su utilización.
     */
    public OrderService(TaxCalculator taxCalculator,
                        OrderRepository repository,
                        EmailNotifier notifier,
                        DiscountStrategy discountStrategy) {
        this.taxCalculator = taxCalculator;
        this.repository = repository;
        this.notifier = notifier;
        this.discountStrategy = discountStrategy;
    }

    /**
     * Procesa una orden calculando impuestos, aplicando descuentos según la estrategia,
     * persistiendo el resultado y enviando la confirmación correspondiente.
     */
    public void processOrder(String orderId, String email, List<Double> prices) {
        double total = taxCalculator.calculateTotal(prices);
        double discounted = discountStrategy.apply(total);
        repository.save(orderId, discounted);
        notifier.sendConfirmation(email, orderId);
    }
}
