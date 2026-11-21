package com.patrones.u1;

/**
 * Responsable únicamente de la emisión de notificaciones de confirmación por correo electrónico.
 * Cumple con SRP al separar el canal de comunicación externa de la lógica de procesamiento.
 */
public class EmailNotifier {
    public void sendConfirmation(String email, String orderId) {
        System.out.println("[EMAIL] Enviando a " + email + " confirmación de orden " + orderId);
    }
}
