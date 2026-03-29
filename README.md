# Perú Orchestrator

Microservice whose operation is to receive the incoming message and transmit it to the various microservices.

| Host              | GrpcPort |
|-------------------|----------|
| pgwp-orchestrator | 9097     |

## Tabla de Contenidos
- [Requisitos Previos](#requisitos-previos)
- [Instalación](#instalación)
- [Ejecución](#ejecución)
- [Configuración](#configuración)
- [Uso](#uso)
- [Pruebas](#pruebas)
- [Notas de Versión](#notas-de-versión)

## Requisitos Previos

Asegúrate de tener instalado lo siguiente para poder ejecutar el proyecto:
- Java JDK 17 o superior
- Maven 3.8 o superior
- Un cliente gRPC (como gRPCurl o Postman) para pruebas

## Instalación

1.  Clona el repositorio en tu máquina local:
    ```bash
    git clone <URL_DEL_REPOSITORIO>
    cd peru-orchestrator
    ```

2.  Construye el proyecto y descarga todas las dependencias con Maven:
    ```bash
    mvn clean install
    ```

## Ejecución

Puedes ejecutar la aplicación de las siguientes maneras, adicionalmente se debe de pasar las variables de entorno para la configuración del microservicio

- **Usando Maven y Spring Boot:**
  ```bash
  mvn spring-boot:run
  ```

## Configuración
La configuración de la aplicación se gestiona a través de archivos .yml ubicados en src/main/resources.

- **application-data.yml:** Contiene datos maestros de negocio, como información de monedas, procesos y BINs.

- **application-global.yml:** Define reglas de validación globales aplicadas a los filtros según el tipo de mensaje y red.

- **application-local.yml:**  Define reglas de orquestación y validación locales específicas para la red definida.

## Uso
El microservicio expone operaciones a través de gRPC. El principal punto de entrada es un servicio que procesa un mensaje ISO-8583.


## Release Notes

| Version | Description                                 |
|---------|---------------------------------------------|
| 2.1.15  | First version of the microservice           |


### LIVE
| Version | Description                    | Fecha      |
|---------|--------------------------------|------------|
| 2.1.15  | First version productive       | 17/09/2025 |

## Configuration

The configuration files (yml) are found:

- **application-data.yml**

  In this configuration file, the business master data, information related to currency, process and BIN are found.

- **application-global.yml**

  This configuration file defines global validation rules for the defined network type. The validation rules are applied to filters according to the type of message within the network, each one associated to a corresponding validation function.

- **application-local.yml**

  This configuration file defines local orchestration and validation rules according to the defined network.

## Service Examples

#### Request

Receives a String of characters (original format) ISO-8583

#### Response
Message returned by the postProcessMessage operation, this will depend on the calling microservice