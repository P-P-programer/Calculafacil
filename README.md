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

La lógica física y la tecnología concreta de la interfaz se definirán en etapas
posteriores.

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
