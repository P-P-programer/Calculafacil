# Plan de implementación: Calculadora de radiación de cuerpo negro

## Resumen

Construir una aplicación Java 17 de escritorio con Swing y `Graphics2D` para
explorar la ley de Planck de forma didáctica y físicamente verificable. La
interfaz permitirá superponer varias temperaturas, comparar Planck con
Rayleigh-Jeans y Wien clásica, cambiar unidades, usar casos preconfigurados,
modelar un cuerpo gris, exportar datos e inspeccionar la verificación numérica
de Stefan-Boltzmann.

La regla física no vivirá en Swing: el dominio se organizará en constantes,
unidades, modelos de espectro, muestreo e integración. La UI consumirá
resultados inmutables y mostrará advertencias explícitas para evitar errores de
interpretación.

## Contexto técnico

- Java 17, Maven y MVC existente.
- UI: Swing + `Graphics2D`, sin dependencia gráfica externa.
- Precisión: `double`, con constantes SI exactas/documentadas.
- Longitud de onda interna: metros; temperatura interna: kelvin; frecuencia
  interna: hertz. Las unidades de presentación solo convierten entrada/salida.
- Curvas:
  - Planck por longitud de onda:
    `Bλ = 2hc² / (λ⁵ (exp(hc/(λkT)) - 1))`.
  - Planck por frecuencia:
    `Bν = 2hν³ / (c² (exp(hν/(kT)) - 1))`.
  - Rayleigh-Jeans como comparación histórica.
  - Wien clásica como aproximación de alta frecuencia.
- Para estabilidad numérica se usará una evaluación segura de `expm1` y
  límites explícitos para argumentos exponenciales extremos.
- La emisividad `ε` estará en `(0, 1]`; el cuerpo gris escala la radiancia
  espectral y la potencia integrada por `ε`.
- La integración se hará con trapecio compuesto inicialmente, con refinamiento
  de malla y tolerancia configurable. Como `Bλ` es radiancia por estereorradián,
  la verificación aplicará el factor hemisférico `π` y comparará contra
  `εσT⁴`, indicando error absoluto y relativo.

## Verificación de arquitectura

- **Separación de responsabilidades:** los paquetes `model` y `service` no
  conocerán Swing; `view` no implementará fórmulas.
- **Claridad física:** cada gráfica tendrá ley, variable independiente, unidades
  y magnitud en la leyenda; se mostrará una nota de que `Bλ` y `Bν` no son la
  misma curva transformada visualmente.
- **Seguridad numérica:** entradas inválidas se rechazan con mensajes concretos;
  no se usarán valores silenciosos por defecto.
- **Reproducibilidad:** las exportaciones CSV incluirán parámetros, unidades,
  leyenda de cada serie y una fila por muestra.
- **Accesibilidad didáctica:** tooltips, etiquetas completas, colores
  distinguibles y tabla de resultados además de la gráfica.

## Guías aplicadas

- Mantener MVC y nombres del proyecto existente.
- Preferir objetos inmutables (`record`) para parámetros y puntos de curva.
- Mantener las constantes físicas en una única clase de dominio, no duplicarlas
  en la vista.
- Usar tests deterministas sobre fórmulas, conversiones, integración y casos
  de uso; no probar píxeles como sustituto de pruebas de dominio.

## Requisitos

- REQ-001: Graficar interactivamente una o varias curvas de Planck superpuestas.
- REQ-002: Permitir unidades nm/µm/m, Hz/THz y K/°C/°F con conversiones claras.
- REQ-003: Comparar Planck con Rayleigh-Jeans y Wien clásica.
- REQ-004: Ofrecer Sol, horno, CMB y filamento como presets editables.
- REQ-005: Exportar la curva visible a CSV reproducible.
- REQ-006: Integrar numéricamente y verificar Stefan-Boltzmann.
- REQ-007: Permitir modo cuerpo gris con emisividad ε.
- REQ-008: Probar valores conocidos, incluido el pico solar cercano a 501 nm.
- REQ-009: Explicar magnitudes, unidades, aproximaciones y límites en la UI.
- REQ-010: Rechazar entradas inválidas y evitar inestabilidad numérica.
- REQ-011: Mantener la lógica física desacoplada de Swing.

## Pasos de implementación

### Fase 1 — Base y dominio físico

#### P1.1 [Cross-cutting] Consolidar constantes y tipos de dominio (REQ-006, REQ-008, REQ-011)

- Crear `PhysicalConstants` con `H`, `C`, `K_B`, `SIGMA` y `WIEN_DISPLACEMENT`,
  indicando fuente/documentación y unidades.
- Crear records para `Temperature`, `Wavelength`, `Frequency`, `Emissivity`,
  `SpectrumPoint`, `SpectrumCurve` y `SpectrumRequest`.
- Definir validaciones de rango y mensajes de error orientados al usuario.

#### P1.2 [Cross-cutting] Implementar conversiones de unidades (REQ-002, REQ-009, REQ-010)

- Añadir enums de unidad con factor SI, símbolo y texto explicativo.
- Implementar conversiones absolutas de temperatura correctamente para °C/°F
  y conversiones lineales para longitud/frecuencia.
- Probar redondeo de presentación sin contaminar los valores internos.

### Fase 2 — Servicios de cálculo

#### P2.1 [Cross-cutting] Implementar leyes espectrales estables (REQ-001, REQ-003, REQ-007, REQ-010, REQ-011)

- Extraer `SpectralRadianceCalculator`.
- Implementar Planck en λ y ν, Rayleigh-Jeans y Wien clásica.
- Aplicar emisividad solo a la radiancia final del cuerpo gris.
- Manejar overflow, λ/ν no positivos y temperaturas no válidas con errores
  explícitos.

#### P2.2 [Cross-cutting] Implementar muestreo, picos e integración (REQ-001, REQ-006, REQ-008)

- Crear `SpectrumSampler` con rango, número de muestras y escala lineal/logarítmica
  documentados.
- Crear `NumericalIntegrator` con trapecio compuesto y cálculo de error relativo.
- Crear `StefanBoltzmannVerifier` que compare integral y `εσT⁴`.
- Añadir cálculo de `λmax = b/T` como referencia didáctica, sin confundirlo con
  el máximo de una curva expresada en frecuencia.

#### P2.3 [Cross-cutting] Modelar presets y sesiones de comparación (REQ-001, REQ-003, REQ-004, REQ-007)

- Crear `TemperaturePreset` para Sol (5778 K), horno (1500 K), CMB (2.725 K) y
  filamento (2900 K).
- Crear `SpectrumSeries` con nombre, temperatura, emisividad, ley, color y
  visibilidad.
- Mantener una lista de series seleccionadas para superposición y exportación.

### Fase 3 — Interfaz Swing

#### P3.1 [Cross-cutting] Construir ventana y panel de parámetros (REQ-001, REQ-002, REQ-004, REQ-007, REQ-009, REQ-010)

- Completar `CalculatorView` con panel de entrada, lista de series y botones de
  preset, recalcular, limpiar y exportar.
- Mostrar temperatura en K y unidad elegida de forma simultánea cuando sea útil,
  con comparación °C/°F para evitar elecciones accidentales.
- Incluir control de emisividad visible solo o resaltado cuando esté activo el
  modo cuerpo gris.
- Mostrar mensajes de validación junto al campo causante, no solo en diálogos.

#### P3.2 [Cross-cutting] Crear gráfica científica con Graphics2D (REQ-001, REQ-003, REQ-009)

- Crear `SpectrumChartPanel` con ejes, ticks, leyenda, tooltip de coordenadas,
  zoom con rueda y desplazamiento horizontal.
- Escalar automáticamente sin ocultar valores pequeños ni dividir por cero.
- Diferenciar Planck, Rayleigh-Jeans y Wien por color y estilo de línea.
- Mostrar claramente variable, unidad y densidad espectral (`Bλ` o `Bν`).

#### P3.3 [Cross-cutting] Mostrar tabla y verificación numérica (REQ-006, REQ-008, REQ-009)

- Añadir panel con λmax, valor máximo, potencia integrada, `εσT⁴`, error
  relativo y estado de verificación.
- Permitir inspeccionar muestras en una tabla para que el gráfico no sea la única
  fuente de información.

### Fase 4 — Exportación, tests y documentación

#### P4.1 [Cross-cutting] Exportar CSV reproducible (REQ-005, REQ-009)

- Implementar `CsvSpectrumExporter` con diálogo de guardado, UTF-8, encabezados
  de metadatos y columnas de todas las series visibles.
- Exportar tanto valores SI como valores de presentación seleccionados, dejando
  explícita la unidad en el encabezado.

#### P4.2 [Cross-cutting] Añadir tests unitarios y de regresión física (REQ-006, REQ-008, REQ-010, REQ-011)

- Configurar JUnit 5 en Maven.
- Probar constantes y conversiones K/°C/°F, nm/µm/m y Hz/THz.
- Verificar Planck positivo, finito y monotónico en los extremos esperados.
- Verificar el pico solar cercano a 501 nm con tolerancia documentada.
- Verificar Wien, Rayleigh-Jeans, cuerpo negro ε=1 y cuerpo gris ε<1.
- Verificar integración contra Stefan-Boltzmann dentro de una tolerancia
  explícita y probar convergencia al refinar la malla.
- Probar validaciones de entradas y exportación CSV.

#### P4.3 [Cross-cutting] Completar documentación de uso y física (REQ-002, REQ-003, REQ-004, REQ-009)

- Actualizar `README.md` con ejecución, controles, ecuaciones, unidades,
  convenciones y limitaciones.
- Añadir una ayuda integrada breve: qué significa `Bλ`, por qué Rayleigh-Jeans
  diverge en el ultravioleta, qué representa Wien y qué cambia ε.

## Estructura objetivo

```text
src/main/java/com/calculafacil/
├── App.java
├── controller/CalculatorController.java
├── model/
│   ├── PhysicalConstants.java
│   ├── Temperature.java
│   ├── Wavelength.java
│   ├── Frequency.java
│   ├── Emissivity.java
│   ├── SpectrumPoint.java
│   ├── SpectrumCurve.java
│   └── TemperaturePreset.java
├── service/
│   ├── SpectralRadianceCalculator.java
│   ├── SpectrumSampler.java
│   ├── NumericalIntegrator.java
│   ├── StefanBoltzmannVerifier.java
│   └── CsvSpectrumExporter.java
└── view/
    ├── CalculatorView.java
    └── SpectrumChartPanel.java
src/test/java/com/calculafacil/
├── model/
├── service/
└── view/
```

## Tareas

### Estado actual de la implementación

La entrega inicial está completada para las tareas T001–T005, T007–T009 y T011:

- El dominio físico, las conversiones y las leyes espectrales están implementados
  y cubiertos por tests.
- La interfaz Swing permite agregar y superponer curvas de Planck, usar presets,
  cambiar unidades y emisividad, limpiar la gráfica y consultar la verificación
  de Stefan-Boltzmann.
- El pipeline de distribución portable para Windows y Linux está implementado en
  `.github/workflows/release.yml`. macOS queda fuera temporalmente porque el
  runner no estuvo disponible; no bloquea la publicación de las otras plataformas.
- `README.md` ya documenta ejecución local y descargas desde Releases, pero la
  ayuda integrada de física aún está pendiente.

### Fase 1

- [x] T001 [Plan:P1.1] Crear constantes físicas y records de dominio en `src/main/java/com/calculafacil/model/`.
- [x] T002 [P] [Plan:P1.2] Implementar enums y convertidores de unidades en `src/main/java/com/calculafacil/model/`.
- [x] T003 [P] [Plan:P1.1] Configurar JUnit 5 y estructura de tests en `pom.xml` y `src/test/java/`.

### Fase 2

- [x] T004 [Plan:P2.1] Implementar `SpectralRadianceCalculator` para Planck, Rayleigh-Jeans, Wien y cuerpo gris.
- [x] T005 [P] [Plan:P2.2] Implementar `SpectrumSampler` y `NumericalIntegrator`.
- [ ] T006 [P] [Plan:P2.2] Completar cálculo de `λmax = b/T` y conservar la verificación de Stefan-Boltzmann.
- [x] T007 [P] [Plan:P2.3] Implementar presets y modelo de series superpuestas.

### Fase 3

- [x] T008 [Plan:P3.1] Completar `CalculatorView` con entradas, presets, unidades, emisividad y validación.
- [x] T009 [Plan:P3.2] Implementar `SpectrumChartPanel` con Graphics2D y leyenda básica.
- [ ] T010 [Plan:P3.3] Añadir tabla de muestras y panel completo de verificación numérica.
- [x] T011 [Plan:P3.1] Conectar eventos en `CalculatorController` y arrancar Swing desde `App.java`.

### Fase 4

- [ ] T012 [P] [Plan:P4.1] Implementar exportación CSV con metadatos y unidades.
- [ ] T013 [P] [Plan:P4.2] Ampliar tests de comparación histórica, estabilidad numérica, emisividad e integración.
- [ ] T014 [Plan:P4.3] Completar README y ayuda integrada de física.
- [ ] T015 [Plan:P4.2] Ejecutar `mvn test`, `mvn package` y una prueba manual de presets, comparación, zoom y CSV.

### Fase 5 — Interacción y comparación didáctica

- [x] T016 [Plan:P2.1,P3.2] Añadir controles para mostrar u ocultar Planck, Rayleigh-Jeans y Wien clásica.
- [x] T017 [P] [Plan:P2.2] Implementar cálculo de `λmax = b/T` y mostrar el pico en los resultados.
- [x] T018 [P] [Plan:P3.2] Añadir zoom acotado, arrastre horizontal, retícula y tarjeta visual con λ, radiancia y marcadores.
- [ ] T019 [P] [Plan:P3.3] Añadir tabla de muestras, temperatura, pico, potencia integrada y error relativo.
- [ ] T020 [Plan:P4.3] Añadir ventana de ayuda con ecuaciones, unidades, aproximaciones y explicación del factor `π`.
- [ ] T021 [Plan:P4.1] Implementar exportación CSV de las series visibles con metadatos y unidades.
- [ ] T022 [Plan:P4.2] Cubrir con tests las nuevas leyes, pico de Wien, CSV y validaciones de rango.
- [ ] T023 [Plan:P4.2] Validar manualmente la experiencia en Windows/Linux y publicar una nueva versión portable.

## Requirement Mapping

| REQ ID | Descripción | Plan Items | Evidencia |
|---|---|---|---|
| REQ-001 | Curvas Planck superpuestas | P2.2, P2.3, P3.2 | Sampler, series y chart |
| REQ-002 | Selector de unidades | P1.2, P3.1 | Convertidores y controles |
| REQ-003 | Comparación histórica | P2.1, P3.2 | Calculadora y estilos de línea |
| REQ-004 | Presets físicos | P2.3, P3.1 | TemperaturePreset y botones |
| REQ-005 | Exportación CSV | P4.1 | CsvSpectrumExporter |
| REQ-006 | Integración Stefan-Boltzmann | P2.2, P3.3, P4.2 | Verifier y tests |
| REQ-007 | Cuerpo gris | P2.1, P3.1, P4.2 | Emissivity y tests |
| REQ-008 | Tests de valores conocidos | P2.2, P4.2 | Test de pico solar |
| REQ-009 | Claridad didáctica | P3.2, P3.3, P4.3 | Leyenda, tabla y ayuda |
| REQ-010 | Validación numérica y de entradas | P1.2, P2.1, P4.2 | Validadores y regresiones |
| REQ-011 | Física desacoplada de Swing | P1.1, P2.1, P4.2 | model/service sin dependencias UI |
