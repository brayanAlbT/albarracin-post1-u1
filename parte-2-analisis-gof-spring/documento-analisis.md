# Análisis de Patrones GoF en Spring Framework

---

## 1. Portada

* **Institución:** Universidad Francisco de Paula Santander (UFPS)
* **Facultad:** Facultad de Ingeniería
* **Programa:** Ingeniería de Sistemas
* **Asignatura:** Patrones de Diseño de Software (Código: 1156701-A)
* **Unidad:** Unidad 1 — Fundamentos de Patrones de Diseño y Buenas Prácticas
* **Actividad:** Post-contenido — Refactorización SOLID y Análisis de Patrones GoF en Spring Framework
* **Estudiante:** Brayan Smith Albarracín Tarazona
* **Correo Institucional:** brayansmithatar@ufps.edu.co
* **Fecha:** Noviembre de 2026

---

## 2. Introducción

El diseño de software empresarial contemporáneo enfrenta una constante tensión entre la extensibilidad de los sistemas, la mantenibilidad del código base y la mitigación del acoplamiento entre capas de infraestructura y de negocio. Los patrones de diseño catalogados originalmente por la "Banda de los Cuatro" (Gamma et al., 1994) proporcionan un vocabulario común y soluciones arquetípicas a problemas recurrentes de modelado orientado a objetos. No obstante, su implementación aislada puede degenerar en antipatrones si no se articula de manera sinérgica con los principios fundamentales de diseño de software promovidos por el movimiento *Clean Architecture* y los principios SOLID (Martin, 2017).

En este escenario, Spring Framework se erige como el caso de estudio de mayor relevancia en el ecosistema de la plataforma Java para analizar la aplicación industrial, pragmática y a gran escala de los patrones de diseño (Walls, 2022). A diferencia de demostraciones académicas sintéticas, el código fuente de Spring demuestra cómo un framework de infraestructura desacopla responsabilidades transversales complejas mediante la adopción sistemática de patrones de diseño GoF adaptados a su contenedor de Inversión de Control (IoC). El presente documento tiene como objetivo investigar, categorizar y analizar formalmente tres patrones de diseño pertenecientes a categorías GoF disjuntas (Creacional, Estructural y de Comportamiento) dentro de la base de código de Spring Framework, exponiendo el problema específico que cada uno resuelve en el entorno de ejecución de Spring Boot, contrastando su presencia frente a un escenario contrafactual de ausencia y evidenciando el principio SOLID que cada solución refuerza.

---

## 3. Análisis de Patrón 1 (Creacional)

### 3.1. Identificación y Definición del Patrón
* **Nombre del patrón:** Singleton (*Registry of Singletons*)
* **Categoría GoF:** Creacional
* **Definición técnica:** De acuerdo con Gamma et al. (1994), el patrón Singleton persigue garantizar que una clase disponga de una única instancia y proveer un punto de acceso global hacia ella. En el contexto de los contenedores modernos de Inversión de Control, este patrón trasciende la noción del constructor privado tradicional para convertirse en un *Singleton Registry* administrado por el ciclo de vida del contenedor (Pivotal Software, 2024).

### 3.2. Ubicación en el Código Fuente de Spring Framework
* **Módulo:** `spring-beans`
* **Paquete:** `org.springframework.beans.factory.support`
* **Clase Concreta:** `DefaultSingletonBeanRegistry`
* **Archivo de evidencia:** [`evidencia/01-creacional-singleton-DefaultSingletonBeanRegistry.java`](./evidencia/01-creacional-singleton-DefaultSingletonBeanRegistry.java)

### 3.3. Problema Específico que Resuelve en Spring Boot
En una arquitectura empresarial basada en Spring Boot, la inmensa mayoría de los servicios (`@Service`), componentes (`@Component`), repositorios (`@Repository`) y controladores (`@RestController`) están concebidos como objetos sin estado intrínseco mutable (*stateless*). El alcance por defecto (*default scope*) asignado a cualquier bean registrado en el contenedor IoC es precisamente `singleton`. 

El desafío arquitectónico que enfrenta Spring radica en proveer una única instancia compartida por beanName a lo largo de toda la aplicación, pero evitando los graves inconvenientes del patrón Singleton clásico de GoF: constructores privados rígidos, variables globales estáticas, imposibilidad de herencia y fuerte resistencia a las pruebas unitarias automatizadas con dobles de prueba (*mocks*).

`DefaultSingletonBeanRegistry` resuelve este desafío implementando un registro concurrente de tres niveles de caché (`singletonObjects`, `earlySingletonObjects` y `singletonFactories`). Esta estructura permite que el framework construya los beans como objetos Java ordinarios (POJOs), controle su ciclo de vida de forma declarativa, resuelva dependencias cíclicas complejas y suministre la misma instancia compartida a cualquier consumidor que la solicite mediante `@Autowired` o `getBean()`.

### 3.4. Análisis Contrafactual
Si Spring Boot careciera del patrón Singleton implementado mediante este registro centralizado:
1. **Colapso de Memoria y Recursos:** Cada punto de inyección requeriría instanciar un nuevo objeto del servicio o repositorio. Si un sistema atiende miles de peticiones concurrentes y cuenta con 100 controladores que consumen un repositorio, se crearían millones de instancias efímeras redundantes, saturando la memoria Heap y generando sobrecarga extrema en el *Garbage Collector*.
2. **Imposibilidad de Compartir Estados Críticos:** Componentes compartidos indispensables para la infraestructura, tales como pools de conexiones a bases de datos (`HikariCP`), administradores de transacciones (`PlatformTransactionManager`) y cachés en memoria local, perderían su sincronización y coherencia global al fragmentarse en múltiples instancias descoordinadas.

### 3.5. Evidencia de Código Fuente
El siguiente extracto del código fuente de `DefaultSingletonBeanRegistry.java` ilustra el acceso concurrente y la preservación de la unicidad de las instancias:

```java
// Paquete: org.springframework.beans.factory.support.DefaultSingletonBeanRegistry
public class DefaultSingletonBeanRegistry {

    /** Caché principal de Singletons compartidos (beanName -> instancia del bean) */
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

    /** Caché secundaria para referencias tempranas de beans (resuelve dependencias circulares) */
    private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>(16);

    /**
     * Retorna la instancia compartida registrada para el bean especificado.
     * Si no existe en la caché principal, examina la caché temprana sincronizada.
     */
    @Nullable
    protected Object getSingleton(String beanName, boolean allowEarlyReference) {
        // Consulta rápida sin bloqueo en la caché principal concurrente
        Object singletonObject = this.singletonObjects.get(beanName);
        
        if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
            singletonObject = this.earlySingletonObjects.get(beanName);
            if (singletonObject == null && allowEarlyReference) {
                // Doble comprobación sincronizada para concurrencia segura
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
}
```

### 3.6. Principio SOLID Reforzado
* **Principio de Responsabilidad Única (SRP):** El patrón desacopla por completo a las clases de negocio de la responsabilidad de gestionar su propio ciclo de vida e instanciación. En el diseño de software tradicional, una clase que implementa Singleton asume dos razones para cambiar: la lógica de su dominio y el control de su propia unicidad. En Spring, la clase únicamente contiene lógica de negocio, delegando a `DefaultSingletonBeanRegistry` la administración de su ciclo de vida único.
* **Principio de Inversión de Dependencias (DIP):** Los consumidores no dependen de una referencia estática concreta tipo `MiServicio.getInstance()`, sino de abstracciones inyectadas por el contenedor.

---

## 4. Análisis de Patrón 2 (Estructural)

### 4.1. Identificación y Definición del Patrón
* **Nombre del patrón:** Proxy (*Dynamic AOP Proxy*)
* **Categoría GoF:** Estructural
* **Definición técnica:** Según Gamma et al. (1994), el patrón Proxy proporciona un intermediario o sustituto de otro objeto para controlar y filtrar el acceso al mismo. En Spring AOP, el Proxy envuelve al objeto de destino (*target bean*) interceptando los llamados a sus métodos para ejecutar comportamiento transversal previo y posterior (*Advices*).

### 4.2. Ubicación en el Código Fuente de Spring Framework
* **Módulo:** `spring-aop`
* **Paquete:** `org.springframework.aop.framework`
* **Clase Concreta:** `JdkDynamicAopProxy` (implementación basada en interfaces JDK) y su homólogo `CglibAopProxy` (basado en subclases con CGLIB)
* **Archivo de evidencia:** [`evidencia/02-estructural-proxy-JdkDynamicAopProxy.java`](./evidencia/02-estructural-proxy-JdkDynamicAopProxy.java)

### 4.3. Problema Específico que Resuelve en Spring Boot
En las aplicaciones empresariales existen preocupaciones transversales (*cross-cutting concerns*) que atraviesan horizontalmente múltiples capas de la aplicación:
1. Gestión de transacciones de base de datos (`@Transactional`).
2. Seguridad declarativa y autorización basada en roles (`@PreAuthorize`).
3. Almacenamiento en caché de resultados de cómputo (`@Cacheable`).
4. Monitoreo, trazabilidad distribuida y métricas de rendimiento.

Incrustar estas operaciones directamente en cada método de servicio contamina gravemente la lógica de negocio, introduce acoplamiento con las APIs de infraestructura (por ejemplo, `TransactionTemplate` o `SecurityContextHolder`) y genera duplicación masiva de código redundante (*boilerplate*).

Spring Boot soluciona esto generando dinámicamente un **Proxy** que sustituye al bean real dentro del contenedor. Cuando un cliente inyecta una interfaz o clase anotada con `@Transactional`, no recibe el objeto de negocio directo, sino una instancia del Proxy (`JdkDynamicAopProxy`). Al invocar un método, el Proxy intercepta la llamada, inicia la transacción en el manejador de base de datos, ejecuta el método de negocio real del *target*, comete la transacción si finaliza con éxito o efectúa *rollback* en caso de excepción no controlada.

### 4.4. Análisis Contrafactual
Si Spring Boot no hiciera uso del patrón Proxy para su motor de AOP:
1. **Contaminación del Dominio con Código Boilerplate:** Cada método requeriría abrir bloques `try-catch-finally`, obtener conexiones de base de datos manuales, verificar privilegios criptográficos y emitir métricas, elevando el tamaño del código de negocio en más de un 200%.
2. **Riesgo Crítico de Inconsistencia Transaccional:** La omisión humana de un bloque `commit` o `rollback` en una operación financiera dejaría transacciones abiertas en la base de datos o causaría corrupción irreversible de datos en escenarios de error no previstos.

### 4.5. Evidencia de Código Fuente
El siguiente extracto de `JdkDynamicAopProxy.java` demuestra cómo el proxy intercepta la invocación, recopila la cadena de interceptores y delega el flujo de control:

```java
// Paquete: org.springframework.aop.framework.JdkDynamicAopProxy
final class JdkDynamicAopProxy implements AopProxy, InvocationHandler {

    private final AdvisedSupport advised;

    @Override
    @Nullable
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object target = this.advised.getTargetSource().getTarget();
        Class<?> targetClass = (target != null ? target.getClass() : null);

        // 1. Obtiene la cadena de interceptores (Aspects, Advices) para este método
        List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);

        Object retVal;
        if (chain.isEmpty()) {
            // Sin interceptores: invoca el método del bean directamente por reflexión
            retVal = AopUtils.invokeJoinpointUsingReflection(target, method, args);
        } else {
            // Con interceptores: teje el aspecto envolviendo la ejecución en ReflectiveMethodInvocation
            MethodInvocation invocation = new ReflectiveMethodInvocation(
                proxy, target, method, args, targetClass, chain
            );
            retVal = invocation.proceed(); // Ejecuta Advices previos -> Target -> Advices posteriores
        }
        return retVal;
    }
}
```

### 4.6. Principio SOLID Reforzado
* **Principio de Abierto/Cerrado (OCP):** El código de los servicios de negocio permanece cerrado a la modificación pero completamente abierto a la extensión. Es posible añadir auditoría, control de transacciones o telemetría a un servicio sin modificar ni una sola línea de su clase, simplemente configurando nuevos interceptores o anotaciones que el Proxy ejecutará.
* **Principio de Responsabilidad Única (SRP):** Se garantiza que la clase de servicio conserve como única responsabilidad la ejecución de sus reglas de dominio, delegando las responsabilidades de infraestructura al Proxy.

---

## 5. Análisis de Patrón 3 (Comportamiento)

### 5.1. Identificación y Definición del Patrón
* **Nombre del patrón:** Observer (*Application Event Multicaster / Publisher-Subscriber*)
* **Categoría GoF:** Comportamiento
* **Definición técnica:** Gamma et al. (1994) definen el patrón Observer como aquel que establece una relación de dependencia uno a muchos entre objetos, de manera que cuando un objeto cambia de estado, todos sus dependientes son notificados y actualizados de forma automática e independiente.

### 5.2. Ubicación en el Código Fuente de Spring Framework
* **Módulo:** `spring-context`
* **Paquete:** `org.springframework.context.event`
* **Componentes Clave:** 
  * Sujeto / Publicador: `ApplicationEventPublisher` (interfaz implementada por `ApplicationContext`).
  * Despachador / Multidifusor: `SimpleApplicationEventMulticaster` (gestiona los observadores).
  * Observador: `ApplicationListener<E>` (interfaz para los suscriptores o métodos con `@EventListener`).
* **Archivo de evidencia:** [`evidencia/03-comportamiento-observer-SimpleApplicationEventMulticaster.java`](./evidencia/03-comportamiento-observer-SimpleApplicationEventMulticaster.java)

### 5.3. Problema Específico que Resuelve en Spring Boot
En arquitecturas orientadas a eventos o sistemas modulares de backend, diversos procesos de negocio requieren desencadenar acciones secundarias tras la ocurrencia de un evento principal. Por ejemplo, al crearse una orden (`OrderCreatedEvent`), el sistema debe:
1. Enviar una notificación por correo al comprador.
2. Actualizar el stock en el inventario.
3. Emitir métricas a un sistema de analítica en tiempo real.
4. Notificar a un servicio de mensajería logística.

Si el servicio que registra la orden debiera inyectar y llamar explícitamente a cada uno de esos cuatro servicios, se violaría la cohesión y se crearía un acoplamiento directo inmanejable. Además, el servicio de órdenes tendría que ser modificado cada vez que un nuevo módulo de la organización desee enterarse de la creación de una orden.

Spring Framework resuelve esta problemática adoptando el patrón Observer mediante su infraestructura de eventos. El emisor simplemente inyecta la abstracción `ApplicationEventPublisher` y publica el evento (`publisher.publishEvent(new OrderCreatedEvent(this, orderId))`). La clase concreta `SimpleApplicationEventMulticaster` consulta el registro de escuchadores suscritos (`ApplicationListener`), resuelve la correspondencia de tipos genéricos en tiempo de ejecución (`ResolvableType`) y notifica a cada observador, permitiendo incluso despachar la notificación de manera asíncrona mediante un `Executor`.

### 5.4. Análisis Contrafactual
Si Spring Boot no contara con el patrón Observer para su bus interno de eventos:
1. **Acoplamiento Fuerte y Ciclos de Dependencia:** Los servicios de negocio se verían obligados a depender directamente de múltiples colaboradores periféricos, propiciando dependencias circulares que impiden el inicio ordenado de los beans en el contexto.
2. **Rigidez y Resistencia al Cambio:** Agregar un nuevo proceso de auditoría o integración externa requeriría alterar el código fuente del flujo transaccional crítico original, arriesgando la estabilidad del sistema y violentando las fronteras entre módulos.

### 5.5. Evidencia de Código Fuente
El siguiente fragmento de `SimpleApplicationEventMulticaster.java` muestra la iteración polimórfica sobre los observadores y el soporte para notificación síncrona o asíncrona:

```java
// Paquete: org.springframework.context.event.SimpleApplicationEventMulticaster
public class SimpleApplicationEventMulticaster extends AbstractApplicationEventMulticaster {

    @Nullable
    private Executor taskExecutor;

    @Override
    public void multicastEvent(final ApplicationEvent event, @Nullable ResolvableType eventType) {
        ResolvableType type = (eventType != null ? eventType : ResolvableType.forInstance(event));
        Executor executor = getTaskExecutor();

        // 1. Obtiene la colección polimórfica de escuchadores (Observers) suscritos al tipo de evento
        for (ApplicationListener<?> listener : getApplicationListeners(event, type)) {
            // 2. Si se configuró un TaskExecutor, despacha la notificación de forma asíncrona
            if (executor != null) {
                executor.execute(() -> invokeListener(listener, event));
            } else {
                // 3. De lo contrario, invoca el observador de manera síncrona en el hilo actual
                invokeListener(listener, event);
            }
        }
    }

    private void invokeListener(ApplicationListener<?> listener, ApplicationEvent event) {
        doInvokeListener((ApplicationListener) listener, event);
    }
}
```

### 5.6. Principio SOLID Reforzado
* **Principio de Inversión de Dependencias (DIP):** El publicador de eventos no depende de clases receptoras concretas ni de implementaciones de notificación; únicamente depende de la abstracción `ApplicationEventPublisher` y del contrato inmutable del evento.
* **Principio de Abierto/Cerrado (OCP):** Es posible agregar ilimitados suscriptores nuevos a un evento en cualquier momento del ciclo de vida del software sin alterar ni una sola línea de código en el publicador.

---

## 6. Conclusiones

La investigación y análisis del código fuente de Spring Framework permiten constatar que los patrones de diseño clásicos propuestos por el GoF no constituyen meras recetas teóricas para la programación orientada a objetos, sino los cimientos arquitectónicos sobre los cuales se sostienen los frameworks empresariales modernos de clase mundial. 

Una lección fundamental derivada de este análisis es que la implementación madura de los patrones de diseño exige una reinterpretación crítica para evitar antipatrones históricos. Spring ilustra esta evolución al transformar el patrón **Singleton** tradicional (cuya naturaleza estática clásica violaba la testeabilidad y la cohesión) en un registro administrado por IoC; al emplear el patrón **Proxy** dinámico para materializar la programación orientada a aspectos y mantener el principio de responsabilidad única en las capas de negocio; y al articular el patrón **Observer** para lograr un desacoplamiento absoluto en la comunicación intermodular orientada a eventos. 

Para el diseño de software profesional, la adopción reflexiva de estos principios y patrones proporciona una guía rigurosa: el código debe concebirse desde su origen favoreciendo la inversión de dependencias, la extensibilidad polimórfica y la segregación estricta de responsabilidades, garantizando que los sistemas resultantes sean robustos, mantenibles y preparados para evolucionar ante requisitos cambiantes.

---

## 7. Referencias

* Gamma, E., Helm, R., Johnson, R., & Vlissides, J. (1994). *Design Patterns: Elements of Reusable Object-Oriented Software*. Addison-Wesley Professional.
* Martin, R. C. (2017). *Clean Architecture: A Craftsman's Guide to Software Structure and Design*. Prentice Hall.
* Pivotal Software & Spring Framework Team. (2024). *Spring Framework Reference Documentation (Version 6.1.x)*. VMware Tanzu. https://docs.spring.io/spring-framework/reference/
* Walls, C. (2022). *Spring in Action* (6th ed.). Manning Publications.
