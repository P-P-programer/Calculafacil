# Calcula Fácil

Aplicación de escritorio para explorar la **radiación de cuerpo negro** con la
ley de Planck. Está orientada a estudiantes y personas que necesitan comparar
modelos físicos, observar cómo cambia el espectro con la temperatura y exportar
los datos para analizarlos después.

La aplicación usa Java Swing y `Graphics2D`, con una arquitectura MVC y una
capa de cálculo físico independiente de la interfaz.

## Requisitos

- Java 17 o superior
- Maven 3.9 o superior para compilar desde el código fuente

Las versiones descargadas desde GitHub Releases incluyen su propio runtime de
Java y no requieren una instalación adicional.

## Funcionalidades

- Curvas de Planck para varias temperaturas superpuestas.
- Comparación opcional con Rayleigh-Jeans y Wien clásica.
- Presets:
  - Sol: 5778 K
  - Horno: 1500 K
  - Fondo cósmico de microondas: 2.725 K
  - Filamento: 2900 K
- Temperatura en K, °C o °F.
- Longitud de onda en m, µm o nm.
- Modo cuerpo gris mediante emisividad `ε`, con `0 < ε ≤ 1`.
- Cálculo y visualización de `λmax = b/T`.
- Verificación numérica de Stefan-Boltzmann.
- Zoom alrededor del cursor y desplazamiento horizontal por arrastre.
- Línea de referencia, marcadores y tarjeta de valores al mover el cursor.
- Tabla de muestras mediante **Ver datos**.
- Exportación CSV mediante **Exportar CSV**.
- Ayuda física integrada mediante **Ayuda física**.
- Aviso especial con imagen integrada en la aplicación.

## Uso de la aplicación

1. Selecciona un preset o introduce una temperatura.
2. Elige la escala de temperatura y la unidad del eje.
3. Ajusta la emisividad si quieres estudiar un cuerpo gris.
4. Pulsa **Agregar curva**.
5. Activa o desactiva Planck, Rayleigh-Jeans y Wien clásica.
6. Usa la rueda del mouse sobre la gráfica para acercar o alejar.
7. Arrastra dentro del plano para desplazarte horizontalmente.
8. Mueve el cursor para consultar los valores de las curvas.
9. Pulsa **Ver datos** para inspeccionar las muestras.
10. Pulsa **Exportar CSV** para guardar los datos visibles.

La caja de consejos debajo de la gráfica cambia según la acción realizada y
explica el siguiente paso recomendado.

## Interpretación física

La aplicación representa `Bλ`, la radiancia espectral por longitud de onda:

```text
Bλ = 2hc² / [λ⁵ (exp(hc/(λkT)) - 1)]
```

La longitud de onda del máximo se calcula mediante la ley de desplazamiento de
Wien:

```text
λmax = b / T
```

Las comparaciones históricas sirven para visualizar que:

- Rayleigh-Jeans funciona como aproximación en longitudes de onda grandes, pero
  diverge hacia el ultravioleta.
- Wien clásica describe bien una región del espectro, pero no todo el rango.
- Planck proporciona la descripción completa del espectro.

La integral de `Bλ` se multiplica por `π` porque `Bλ` es radiancia por
estereorradián. Así se puede comparar la potencia hemisférica con:

```text
P = εσT⁴
```

Los cálculos se realizan internamente en unidades SI:

- Longitud: metros.
- Temperatura: kelvin.
- Frecuencia: hertz.

Las unidades seleccionadas solo afectan la entrada y la presentación.

## Datos y exportación

**Ver datos** abre una vista previa con las muestras de las curvas visibles.
**Exportar CSV** guarda exactamente esas series, con la unidad de longitud de
onda seleccionada y radiancia en unidades SI.

El CSV se puede abrir directamente con:

- Microsoft Excel.
- LibreOffice Calc.
- Google Sheets.

No se genera `.xlsx` en esta fase porque CSV es más portable, reproducible y no
requiere dependencias adicionales.

## Ejecutar desde Maven

Compilar:

```bash
mvn compile
```

Ejecutar tests:

```bash
mvn test
```

Crear el JAR:

```bash
mvn package
```

Ejecutar:

```bash
java -jar target/calculadora-cuerpo-negro-0.1.0-SNAPSHOT.jar
```

## Descargas para Windows y Linux

El workflow [release.yml](.github/workflows/release.yml) genera aplicaciones
portables para Windows y Linux cuando se publica un tag con formato `v*`.

Para publicar una versión desde un entorno Git autenticado:

```bash
git push origin main
git tag v0.1.3
git push origin v0.1.3
```

GitHub Actions ejecuta los tests, compila el proyecto y publica los archivos en
[GitHub Releases](https://github.com/P-P-programer/Calculafacil/releases):

```text
CalculaFacil-v0.1.3-windows.zip
CalculaFacil-v0.1.3-linux.tar.gz
```

Actualmente macOS no forma parte del workflow porque el runner requerido no
estuvo disponible. Esto no bloquea la generación de las versiones para Windows
y Linux.

## Estructura del proyecto

```text
src/
├── main/
│   ├── java/com/calculafacil/
│   │   ├── App.java
│   │   ├── controller/CalculatorController.java
│   │   ├── model/
│   │   │   ├── PhysicalConstants.java
│   │   │   ├── Temperature.java
│   │   │   ├── Emissivity.java
│   │   │   ├── LengthUnit.java
│   │   │   ├── TemperatureUnit.java
│   │   │   └── ...
│   │   ├── service/
│   │   │   ├── SpectralRadianceCalculator.java
│   │   │   ├── SpectrumSampler.java
│   │   │   ├── NumericalIntegrator.java
│   │   │   ├── StefanBoltzmannVerifier.java
│   │   │   └── CsvSpectrumExporter.java
│   │   └── view/
│   │       ├── CalculatorView.java
│   │       └── SpectrumChartPanel.java
│   └── resources/images/moncada.png
└── test/java/com/calculafacil/PhysicsTest.java
```

- **Model:** constantes, unidades y objetos físicos.
- **Service:** fórmulas, muestreo, integración y exportación.
- **View:** interfaz Swing y gráfica.
- **Controller:** coordinación de eventos y estado de la sesión.

## Estado del desarrollo

El plan y el estado de tareas se mantienen en:

- [Plan de desarrollo](.github/modernize/black-body-radiation/plan.md)
- [Estado actual](.github/modernize/black-body-radiation/checkpoints/current-status.yaml)

Antes de publicar una nueva versión se recomienda ejecutar:

```bash
mvn test
mvn package
```
