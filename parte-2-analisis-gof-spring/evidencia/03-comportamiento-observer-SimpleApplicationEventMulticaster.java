package org.springframework.context.event;

import java.util.Collection;
import java.util.concurrent.Executor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

/**
 * ============================================================================
 * EVIDENCIA 3 — PATRÓN DE COMPORTAMIENTO: OBSERVER (EVENT MULTICASTER)
 * ============================================================================
 * Repositorio: https://github.com/spring-projects/spring-framework
 * Módulo: spring-context
 * Paquete: org.springframework.context.event
 * Clase: SimpleApplicationEventMulticaster.java
 *
 * Propósito en Spring Framework:
 * Actúa como el Sujeto Concreto (Concrete Subject) / Multidifusor dentro de la
 * implementación del patrón Observer de Spring Framework. Administra el registro
 * de observadores (ApplicationListener) y distribuye eventos (ApplicationEvent)
 * de forma síncrona o asíncrona hacia todos los escuchadores interesados.
 * ============================================================================
 */
public class SimpleApplicationEventMulticaster extends AbstractApplicationEventMulticaster {

    @Nullable
    private Executor taskExecutor;

    public void setTaskExecutor(@Nullable Executor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    /**
     * Multidifunde el evento a todos los observadores registrados que coincidan con el tipo.
     */
    @Override
    public void multicastEvent(final ApplicationEvent event, @Nullable ResolvableType eventType) {
        ResolvableType type = (eventType != null ? eventType : ResolvableType.forInstance(event));
        Executor executor = getTaskExecutor();

        // 1. Itera sobre la lista de observadores suscritos a este tipo de evento
        for (ApplicationListener<?> listener : getApplicationListeners(event, type)) {
            // 2. Si hay un ejecutor configurado, notifica al observador de forma asíncrona
            if (executor != null) {
                executor.execute(() -> invokeListener(listener, event));
            } else {
                // 3. Notificación directa y síncrona al observador
                invokeListener(listener, event);
            }
        }
    }

    /**
     * Despacha la notificación invocando el método onApplicationEvent del observador.
     */
    private void invokeListener(ApplicationListener<?> listener, ApplicationEvent event) {
        // Enlaza la ejecución del observador manejando posibles errores y propagaciones
        doInvokeListener((ApplicationListener) listener, event);
    }
}
