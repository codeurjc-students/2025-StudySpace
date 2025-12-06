
### 🎯 Objetivos
La aplicación sera capaz de realizar y cumplir los siguientes objetivos:

🔹 **Objetivos funcionales**

* Permitir a usuarios no registrados visualizar la disponibilidad de aulas.

* Ofrecer a usuarios registrados la posibilidad de reservar, modificar y cancelar sus reservas.

* Posibilitar a los administradores la gestión completa de aulas y usuarios.

* Incluir un sistema de búsqueda y filtrado avanzado de aulas.


🔹 **Objetivos técnicos**

* Implementación del backend con Java + Spring Boot.

* Base de datos relacional MySQL para la gestión de reservas, software, usuarios y aulas.

* Interfaz web desarrollada con Angular.

* Control de versiones,CI/CD y ramas mediante GitHub.

* Integración de diagramas en el proceso de diseño.


### Funcionalidades disponibles para los diferentes tipos de usuarios

| Funcionalidades básicas                                  | Usuario Anónimo | Usuario Registrado | Usuario Administrador |
|----------------------------------------------------------|:---------------:|:------------------:|:---------------------:|
| Visualizar los detalles del aula (nombre, software, ...) |        ✅        |         ✅          |           ✅           |
| Visualizar la disponibilidad de las aulas                |        ✅        |         ✅          |           ✅           |
| Crear/Cancelar tu propia reserva                         |                 |         ✅          |           ✅           |
| Cancelar las reservas de otros usuarios                  |                 |                    |           ✅           |
| Modificar/Borrar información del perfil                  |                 |         ✅          |           ✅           |
| Borrar otros usuarios o reservas de sus perfiles         |                 |                    |           ✅           |
| Añadir/Modificar/Borrar aulas                            |                 |                    |           ✅           |


| Funcionalidades intermedias                           | Usuario Anónimo | Usuario Registrado | Usuario Administrador |
|-------------------------------------------------------| :-------------------: | :----------------: |:---------------------:|
| Vista de calendario (día / semana / mes)              |           ✅           |          ✅         |           ✅           |
| Desactivar temporalmente aulas (mantenimiento, obras) |                       |                    |           ✅           |
| Pantalla de confirmación de reserva exitosa           |                       |          ✅         |           ✅           |

| Funcionalidades avanzadas                                   |      Usuario Anónimo     | Usuario Registrado | Usuario Administrador |
|-------------------------------------------------------------|:------------------:| :----------------: |:---------------------:|
| Algoritmo avanzado para filtrar aulas                       |           ✅           |          ✅         |           ✅           |
| Integración con calendarios externos (Google, Outlook)      |                    |          ✅         |           ✅           |
| Integración con aplicaciones de mapas externas (GoogleMaps) |                    |           ✅          |           ✅           |
| Estadísticas y reportes de uso de aulas                     |                    |                    |           ✅           |


