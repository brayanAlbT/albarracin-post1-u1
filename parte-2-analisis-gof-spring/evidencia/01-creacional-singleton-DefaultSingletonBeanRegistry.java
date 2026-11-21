package org.springframework.beans.factory.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.lang.Nullable;

/**
 * ============================================================================
 * EVIDENCIA 1 — PATRÓN CREACIONAL: SINGLETON (REGISTRY OF SINGLETONS)
 * ============================================================================
 * Repositorio: https://github.com/spring-projects/spring-framework
 * Módulo: spring-beans
 * Paquete: org.springframework.beans.factory.support
 * Clase: DefaultSingletonBeanRegistry.java
 *
 * Propósito en Spring Framework:
 * Implementa el patrón Singleton a nivel de contenedor IoC mediante un registro
 * concurrente de instancias compartidas ("Singleton Registry"), evitando el
 * antipatrón clásico de acoplamiento estático de la implementación tradicional del GoF.
 * ============================================================================
 */
public class DefaultSingletonBeanRegistry {

    /** Caché de primer nivel: instancias completas de beans Singleton (beanName -> bean instance) */
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

    /** Caché de segundo nivel: referencias tempranas de beans (para resolver dependencias circulares) */
    private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>(16);

    /** Caché de tercer nivel: fábricas de singletons para resolución diferida */
    private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16);

    /**
     * Obtiene el bean singleton registrado para el nombre dado.
     * Garantiza que exista una única instancia compartida en todo el contenedor.
     */
    @Nullable
    protected Object getSingleton(String beanName, boolean allowEarlyReference) {
        // 1. Búsqueda en la caché principal de singletons
        Object singletonObject = this.singletonObjects.get(beanName);
        if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
            // 2. Búsqueda en caché temprana para evitar bloqueos por referencias circulares
            singletonObject = this.earlySingletonObjects.get(beanName);
            if (singletonObject == null && allowEarlyReference) {
                synchronized (this.singletonObjects) {
                    singletonObject = this.singletonObjects.get(beanName);
                    if (singletonObject == null) {
                        singletonObject = this.earlySingletonObjects.get(beanName);
                        if (singletonObject == null) {
                            ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
                            if (singletonFactory != null) {
                                singletonObject = singletonFactory.getObject();
                                this.earlySingletonObjects.put(beanName, singletonObject);
                                this.singletonFactories.remove(beanName);
                            }
                        }
                    }
                }
            }
        }
        return singletonObject;
    }

    /**
     * Registra un nuevo objeto singleton en el contenedor de forma atómica y sincronizada.
     */
    protected void addSingleton(String beanName, Object singletonObject) {
        synchronized (this.singletonObjects) {
            this.singletonObjects.put(beanName, singletonObject);
            this.singletonFactories.remove(beanName);
            this.earlySingletonObjects.remove(beanName);
            this.registeredSingletons.add(beanName);
        }
    }
}
