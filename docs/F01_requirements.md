### 🎯 Objetivos

La aplicación sera capaz de realizar y cumplir los siguientes objetivos:

🔹 **Objetivos funcionales**

- Permitir a usuarios no registrados visualizar la disponibilidad de aulas.

- Ofrecer a usuarios registrados la posibilidad de reservar y cancelar sus reservas.

- Posibilitar a los administradores la gestión completa de reservas, softwares, aulas y usuarios.

- Incluir un sistema de búsqueda y filtrado avanzado de aulas.

🔹 **Objetivos técnicos**

- Implementación del backend con Java + Spring Boot.

- Base de datos relacional MySQL para la gestión de reservas, software, usuarios y aulas.

- Interfaz web desarrollada con Angular.

- Control de versiones, CI/CD y ramas mediante GitHub.

- Integración de diagramas en el proceso de diseño.

### Funcionalidades disponibles para los diferentes tipos de usuarios

| Funcionalidades básicas                                  | Usuario Anónimo | Usuario Registrado | Usuario Administrador |
| -------------------------------------------------------- | :-------------: | :----------------: | :-------------------: |
| Visualizar los detalles del aula (nombre, software, ...) |       ✅        |         ✅         |          ✅           |
| Visualizar la disponibilidad de las aulas                |       ✅        |         ✅         |          ✅           |
| Crear/Cancelar tu propia reserva                         |                 |         ✅         |          ✅           |
| Cancelar las reservas de otros usuarios                  |                 |                    |          ✅           |
| Modificar información del perfil                         |                 |         ✅         |          ✅           |
| Borrar otros usuarios o reservas de sus perfiles         |                 |                    |          ✅           |
| Consultar/modificar información de otros usuarios        |                 |                    |          ✅           |
| Añadir/Modificar/Borrar aulas                            |                 |                    |          ✅           |

| Funcionalidades intermedias                           | Usuario Anónimo | Usuario Registrado | Usuario Administrador |
| ----------------------------------------------------- | :-------------: | :----------------: | :-------------------: |
| Vista de calendario (día / semana / mes)              |       ✅        |         ✅         |          ✅           |
| Estadísticas del uso de un aula en concreto           |       ✅        |         ✅         |          ✅           |
| Desactivar temporalmente aulas (mantenimiento, obras) |                 |                    |          ✅           |
| Estadísticas del uso global de las aulas              |                 |                    |          ✅           |

| Funcionalidades avanzadas                              | Usuario Anónimo | Usuario Registrado | Usuario Administrador |
| ------------------------------------------------------ | :-------------: | :----------------: | :-------------------: |
| Algoritmo avanzado para filtrar aulas                  |       ✅        |         ✅         |          ✅           |
| Integración con calendarios externos (Google, Outlook) |                 |         ✅         |          ✅           |
| Sistema de notificaciones por correo electrónico       |                 |         ✅         |          ✅           |
