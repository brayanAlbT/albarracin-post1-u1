# Post-contenido — Unidad 1: Fundamentos de Patrones de Diseño y Buenas Prácticas

## Descripción
Repositorio del post-contenido de la Unidad 1 del curso **Patrones de Diseño de Software** (Sexto Semestre - Ingeniería de Sistemas, UFPS). Contiene dos partes complementarias: la refactorización SOLID de un *God Object* legacy (`parte-1-refactorizacion-solid/`) y el análisis riguroso de patrones de diseño GoF en el código fuente de Spring Framework (`parte-2-analisis-gof-spring/`).

---

## Parte 1 — Refactorización SOLID de un God Object

En esta sección se identifican y corrigen las violaciones arquitectónicas presentes en la clase legacy `OrderProcessor`, la cual concentraba múltiples responsabilidades desconectadas (cálculo tributario, lógica de descuentos, persistencia en memoria, emisión de correos y generación de reportes en consola).

### Análisis de Violaciones SOLID — Parte 1

| Principio | Método/Sección afectada | Descripción de la violación |
|---|---|---|
| **SRP** (Single Responsibility Principle) | `calculateTotal` + `applyDiscount` + `saveOrder` + `sendEmail` + `printReport` | La clase `OrderProcessor` actúa como un *God Object* con 5 responsabilidades divergentes. Posee múltiples razones para cambiar: modificaciones tributarias, nuevas reglas de descuento, migraciones del motor de base de datos, cambios en el proveedor de correos o variaciones en el formato visual del reporte impactan a la misma clase, elevando el riesgo de efectos colaterales y degradando la mantenibilidad. |
| **OCP** (Open/Closed Principle) | `applyDiscount` (sentencias `if/else` sobre `customerType`) | El método se encuentra cerrado a la extensión y abierto a la modificación. Para dar soporte a una nueva categoría de cliente (por ejemplo, `CORPORATIVO` o `ESTUDIANTE`) es obligatorio modificar el código fuente existente de la clase y recompilarla, incrementando la fragilidad del sistema y violando la extensibilidad mediante polimorfismo. |
| **DIP** (Dependency Inversion Principle) | Toda la clase (dependencias internas sin abstracciones) | Los módulos de alto nivel y las operaciones de bajo nivel (persistencia, envío de correos, salida estándar) se encuentran fuertemente acoplados de forma rígida dentro de la misma clase mediante instanciaciones directas y llamadas directas sin mediar interfaces o contratos abstractos. No es posible sustituir la implementación de persistencia ni simularla mediante dobles de prueba (*mocks/stubs*). |

---

### Estructura de Clases Refactorizadas

Para resolver estas violaciones, se desagregó `OrderProcessor` en clases de alta cohesión y bajo acoplamiento:

1. **`TaxCalculator`**: Responsabilidad exclusiva del cálculo del subtotal e impuestos fiscales según la tasa configurada.
2. **`OrderRepository`**: Responsabilidad exclusiva de la persistencia y consulta en memoria de órdenes.
3. **`EmailNotifier`**: Responsabilidad exclusiva de la comunicación externa con clientes vía correo.
4. **`OrderReporter`**: Responsabilidad exclusiva del formateo y salida visual de reportes.
5. **`DiscountStrategy`** (Patrón Strategy): Interfaz polimórfica que define el contrato de cálculo de descuentos, implementada por `VipDiscount`, `RegularDiscount` y `NoDiscount` (abierto a extensión sin modificar clases existentes).
6. **`OrderService`**: Orquestador de negocio que aplica **DIP** al recibir todos sus colaboradores inyectados por constructor.
7. **`Main`**: Punto de entrada ejecutable que ensambla las dependencias y demuestra el procesamiento de órdenes para clientes VIP y Regulares.
