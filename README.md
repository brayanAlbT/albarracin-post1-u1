# Post-contenido — Unidad 1: Fundamentos de Patrones de Diseño y Buenas Prácticas

## Descripción
Repositorio del post-contenido de la Unidad 1 del curso **Patrones de Diseño de Software** — Sexto Semestre de Ingeniería de Sistemas en la Universidad Francisco de Paula Santander (UFPS). 

Este proyecto integra dos partes formativas complementarias evaluadas en conjunto:
1. **Parte 1 (`parte-1-refactorizacion-solid/`)**: Refactorización de una clase Java con múltiples responsabilidades (*God Object* `OrderProcessor`) aplicando rigurosamente los principios SOLID (**SRP**, **OCP**, **DIP**), comprobable mediante un proyecto Maven estándar y ejecutable.
2. **Parte 2 (`parte-2-analisis-gof-spring/`)**: Investigación profunda del código fuente oficial de Spring Framework para identificar, contrastar y documentar tres patrones de diseño GoF de categorías distintas (Creacional, Estructural y de Comportamiento), relacionándolos con sus respectivos principios SOLID y sustentándolos con extractos de código fuente real.

---

## Parte 1 — Refactorización SOLID

Proyecto Maven que refactoriza la clase legacy `OrderProcessor` aplicando el principio de responsabilidad única (SRP), el principio de abierto/cerrado (OCP) mediante el patrón Strategy, y el principio de inversión de dependencias (DIP) mediante inyección por constructor. Ver [`parte-1-refactorizacion-solid/`](./parte-1-refactorizacion-solid/).

### Análisis de Violaciones SOLID — Parte 1

| Principio | Método/Sección afectada | Descripción de la violación |
|---|---|---|
| **SRP** (Single Responsibility Principle) | `calculateTotal` + `applyDiscount` + `saveOrder` + `sendEmail` + `printReport` | La clase `OrderProcessor` concentra 5 responsabilidades completamente heterogéneas: cálculo fiscal, reglas de descuento comercial, persistencia en memoria, emisión de notificaciones por email y formateo visual de reportes en consola. Viola el principio de responsabilidad única al poseer múltiples razones para cambiar, lo que incrementa drásticamente el acoplamiento y el riesgo de efectos colaterales ante cualquier ajuste. |
| **OCP** (Open/Closed Principle) | `applyDiscount` (bifurcaciones `if/else` sobre `customerType`) | El método se encuentra cerrado a la extensión y abierto a la modificación. Para incorporar un nuevo tipo de descuento (por ejemplo, cliente `CORPORATIVO` o `ESTUDIANTE`), es indispensable modificar el código fuente existente de la clase, violando el principio al no permitir extender la funcionalidad sin alterar el código base ya probado. |
| **DIP** (Dependency Inversion Principle) | Toda la clase (dependencias internas sin abstracciones) | El servicio depende rígidamente de implementaciones concretas y manipula directamente la colección de almacenamiento, las salidas del sistema y los algoritmos sin mediar contratos ni abstracciones inyectadas. Esto anula la posibilidad de sustituir la capa de persistencia o realizar pruebas unitarias aisladas mediante dobles de prueba (*mocks/stubs*). |

### Componentes Refactorizados
* **`TaxCalculator`**: Responsable exclusivamente del cálculo matemático de subtotales e impuestos.
* **`OrderRepository`**: Responsable exclusivamente del almacenamiento y consulta en memoria de órdenes.
* **`EmailNotifier`**: Responsable exclusivamente de la notificación a clientes por correo electrónico.
* **`OrderReporter`**: Responsable exclusivamente del formateo y presentación visual de reportes en consola.
* **`DiscountStrategy`** e implementaciones (`VipDiscount`, `RegularDiscount`, `NoDiscount`): Aplican el patrón GoF **Strategy** permitiendo incorporar nuevas variantes de descuento sin modificar el código previo (OCP).
* **`OrderService`**: Orquesta el flujo de negocio recibiendo todas sus dependencias por constructor a través de abstracciones e inyección de dependencias (DIP).
* **`Main`**: Clase de demostración ejecutable que instancia las dependencias y procesa órdenes para clientes VIP y Regulares.

### Instrucciones de Compilación y Ejecución (Parte 1)
Desde el directorio raíz o dentro de `parte-1-refactorizacion-solid/`:
```bash
cd parte-1-refactorizacion-solid
mvn compile
mvn exec:java -Dexec.mainClass="com.patrones.u1.Main"
```

Salida esperada en consola:
```text
[DB] Orden guardada: ORD-001
[EMAIL] Enviando a vip@mail.com confirmación de orden ORD-001
[DB] Orden guardada: ORD-002
[EMAIL] Enviando a reg@mail.com confirmación de orden ORD-002
=== Reporte de Órdenes ===
  ORD-001:354.025
  ORD-002:226.1
```

---

## Parte 2 — Análisis de Patrones GoF en Spring Framework

Se investigó el código fuente de Spring Framework ([spring-projects/spring-framework](https://github.com/spring-projects/spring-framework)), analizando 3 patrones de diseño de categorías distintas:

| # | Patrón | Categoría | Clase en Spring | Módulo | SOLID Asociado |
|---|--------|-----------|-----------------|--------|----------------|
| 1 | **Singleton** (*Registry of Singletons*) | Creacional | `org.springframework.beans.factory.support.DefaultSingletonBeanRegistry` | `spring-beans` | **SRP**, **DIP** |
| 2 | **Proxy** (*Dynamic AOP Proxy*) | Estructural | `org.springframework.aop.framework.JdkDynamicAopProxy` | `spring-aop` | **OCP**, **SRP** |
| 3 | **Observer** (*Application Event Multicaster*) | Comportamiento | `org.springframework.context.event.SimpleApplicationEventMulticaster` | `spring-context` | **OCP**, **DIP** |

El análisis detallado con estructura formal de 7 secciones, análisis contrafactual, conexiones teóricas y referencias en formato APA se encuentra en:
👉 [`parte-2-analisis-gof-spring/documento-analisis.md`](./parte-2-analisis-gof-spring/documento-analisis.md)

Las evidencias de código fuente extraídas de Spring Framework se ubican en:
👉 [`parte-2-analisis-gof-spring/evidencia/`](./parte-2-analisis-gof-spring/evidencia/)

---

## Herramientas Utilizadas
- **Lenguaje:** Java 17 / OpenJDK 21
- **Gestor de Construcción:** Apache Maven 3.8+ / 3.9+
- **Editor / Entorno:** Visual Studio Code con Extension Pack for Java
- **Control de Versiones:** Git 2.x y GitHub
- **Caso de Estudio de Investigación:** Código fuente oficial de Spring Framework v6.x

---

## Conclusiones
La realización de este post-contenido permitió constatar de forma práctica la sinergia indispensable entre los principios SOLID y los patrones de diseño GoF en el desarrollo de software mantenible. En la Parte 1, la refactorización sistemática del *God Object* `OrderProcessor` demostró cómo la segregación de responsabilidades (SRP), la extensibilidad mediante polimorfismo con Strategy (OCP) y la inyección de dependencias por constructor (DIP) transforman un bloque legacy frágil en una arquitectura limpia, desacoplada y altamente testeable. Por su parte, el análisis del código fuente de Spring Framework en la Parte 2 evidenció que los patrones GoF no deben aplicarse de forma rígida o aislada, sino adaptados intencionalmente para evitar antipatrones históricos: el framework reinventa el Singleton a través de un registro gestionado en su contenedor IoC, materializa la programación orientada a aspectos mediante Proxies dinámicos para aislar preocupaciones transversales, y desacopla la comunicación intermodular adoptando el patrón Observer en su bus de eventos. En conjunto, ambas actividades consolidan la habilidad para concebir software con rigurosidad arquitectónica, alta cohesión y bajo acoplamiento.
