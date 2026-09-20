# Calcula Fácil

Base de una calculadora de radiación de cuerpo negro para física moderna,
organizada con el patrón Modelo-Vista-Controlador (MVC).

## Requisitos

- Java 17 o superior
- Maven 3.9 o superior

## Estructura

```text
src/
└── main/
    └── java/
        └── com/calculafacil/
            ├── App.java
            ├── controller/
            │   └── CalculatorController.java
            ├── model/
            │   └── BlackBodyRadiationModel.java
            └── view/
                └── CalculatorView.java
```

- **Model**: contendrá los datos, constantes y reglas físicas.
- **View**: contendrá la interfaz con la persona usuaria.
- **Controller**: coordinará los eventos de la vista y las operaciones del modelo.

La lógica física está desacoplada de la interfaz Swing. La primera interfaz
permite seleccionar temperatura y unidad, cargar presets, ajustar emisividad,
superponer curvas y consultar la verificación numérica de Stefan-Boltzmann.

## Interfaz inicial

Ejecutar desde Maven:

```bash
mvn package
java -jar target/calculadora-cuerpo-negro-0.1.0-SNAPSHOT.jar
```

En la ventana:

- Selecciona un preset o introduce una temperatura en K, °C o °F.
- Ajusta la emisividad `ε` entre 0 y 1 para explorar cuerpos grises.
- Pulsa **Agregar curva** para superponer temperaturas.
- Cambia la unidad del eje entre m, µm y nm.
- Consulta la integral numérica y el valor teórico de Stefan-Boltzmann en la
  barra inferior.

La gráfica actual representa `Bλ` en función de la longitud de onda. Las
curvas se calculan internamente en unidades SI y solo se convierten para su
presentación.

## Comandos Maven

Compilar:

```bash
mvn compile
```

Crear el JAR:

```bash
mvn package
```

Ejecutar el JAR generado:

```bash
java -jar target/calculadora-cuerpo-negro-0.1.0-SNAPSHOT.jar
```
