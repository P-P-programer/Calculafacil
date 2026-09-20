# Decisiones de diseño

## Swing + Graphics2D

Se eligió Swing porque el proyecto ya apunta a Java 17 y la primera versión no
necesita añadir módulos JavaFX ni resolver un runtime gráfico adicional. Un
panel `Graphics2D` permite implementar ejes, leyendas, tooltips y zoom de forma
controlada. Si más adelante se requieren análisis de grandes volúmenes,
animaciones o zoom avanzado, la capa de dominio podrá reutilizarse con JavaFX.

## Constantes físicas en el modelo

Las constantes deben estar en una clase única del dominio (`PhysicalConstants`)
y ser consultadas por los servicios de cálculo. No deben estar en la vista ni
duplicadas en cada fórmula. Esto hace visibles las unidades, permite tests
deterministas y evita inconsistencias entre Planck, Wien y Stefan-Boltzmann.

## Temperatura y unidades

Kelvin será la unidad canónica. Celsius y Fahrenheit son escalas con offset, no
factores de escala: `K = °C + 273.15` y `K = (°F + 459.67) × 5/9`. La UI
mostrará la unidad elegida y, cuando el espacio lo permita, su equivalente en K
para evitar que el usuario confunda una temperatura de entrada con una escala
de visualización.

## Integración

Se comenzará con trapecio compuesto por su sencillez y trazabilidad. La
verificación reportará la tolerancia y el error relativo; no se mostrará
“correcto” si el dominio de integración o la malla no alcanza la tolerancia.
