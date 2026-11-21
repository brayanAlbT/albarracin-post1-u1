package org.springframework.aop.framework;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.lang.Nullable;

/**
 * ============================================================================
 * EVIDENCIA 2 — PATRÓN ESTRUCTURAL: PROXY (AOP DYNAMIC PROXY)
 * ============================================================================
 * Repositorio: https://github.com/spring-projects/spring-framework
 * Módulo: spring-aop
 * Paquete: org.springframework.aop.framework
 * Clase: JdkDynamicAopProxy.java
 *
 * Propósito en Spring Framework:
 * Proporciona un objeto intermediario (Proxy) que intercepta todas las llamadas
 * a métodos de la interfaz de un bean de Spring, permitiendo tejer aspectos
 * transversales (transacciones @Transactional, seguridad @Secured, telemetría)
 * sin modificar una sola línea del código de negocio real del servicio.
 * ============================================================================
 */
final class JdkDynamicAopProxy implements AopProxy, InvocationHandler {

    /** Configuración del asesor AOP que contiene la cadena de interceptores y el objeto objetivo */
    private final AdvisedSupport advised;

    public JdkDynamicAopProxy(AdvisedSupport config) {
        this.advised = config;
    }

    /**
     * Intercepta la llamada al método de la interfaz e inyecta la lógica de los aspectos (Advices).
     */
    @Override
    @Nullable
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object target = this.advised.getTargetSource().getTarget();
        Class<?> targetClass = (target != null ? target.getClass() : null);

        // 1. Obtener la cadena de interceptores (Aspects, Advices) para este método concreto
        List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);

        Object retVal;
        // 2. Si no hay interceptores registrados, invocar el método directamente por reflexión
        if (chain.isEmpty()) {
            retVal = AopUtils.invokeJoinpointUsingReflection(target, method, args);
        } else {
            // 3. Si existen interceptores, encapsular la llamada en una invocación reflexiva
            // que recorre la cadena de interceptores antes y después del método real
            MethodInvocation invocation = new ReflectiveMethodInvocation(
                proxy, target, method, args, targetClass, chain
            );
            retVal = invocation.proceed();
        }

        return retVal;
    }
}
