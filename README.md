# 2025-StudySpace

---

## 📜 Introducción
StudySpace es una aplicación web diseñada para gestionar de manera eficiente la reserva de aulas de informática en un entorno universitario. El objetivo principal de esta aplicación web es ofrecer a los estudiantes, profesores y administradores una plataforma intuitiva que permita consultar la disponibilidad de los espacios, visualizar sus características y realizar reservas de forma sencilla.



---

## 📌 Fase 1
### Prototipos de pantallas
Prototipos de las pantallas finales de la aplicación web y una breve descripcion debajo de cada una, explicando su funcionalidad y sentido:

* **Pantalla de incio**

Es la pantalla inical donde comenzamos a navegar por la aplicación.

  ![Foto pantalla inicio](images/screens/startScreen.png)
* **Pantalla de registrarse**

Pantalla pensada para crear nuevos usuarios con su nombre y correo electrónico.

  ![Foto pantalla registro](images/screens/register.png)
* **Pantalla de inciar sesion**

Pantalla para que los usuarios ya existentes puedan acceder a la aplicación. Con un apartado para mandar un correo de vuelta si olvidan su contraseña.

  ![Foto pantalla iniciar sesion](images/screens/logIn.png)
* **Pantalla seleccionar si buscar reserva por aula o por fecha**

Pantalla para elegir si empezar buscando por aula o por fecha. 

A partir de estas pantallas aparecera el 
nombre de usuario y foto si son usuarios registrados/administradores y tienen una. De no ser asi aparecera el 
logo y nombre por defecto y no les dejara acceder a ninguna de las funfionalidades del perfil.

![Foto pantalla selcionarCampus](images/screens/bookingOptions.png)

* **Pantalla seleccionar aula**

Pantalla para seleccionar un aula de entre todas las existentes, por si nuestro requisito no es 
que este disponible a una hora en concreto como poder reservar un aula en concreto.

![Foto pantalla selcionarCampus](images/screens/advancedSearchRoom.png)

* **Pantalla seleccionar dia y hora del aula seleccionada**

Pantalla con un calendario que mostrara en función de la disponibilidad del aula seleccionada,
con rojo los dias que no esta disponible o esta llena todo el día y con amarillo
los dias que hay bastantes reservas en ese aula pero aun queda algún hueco que coger.
De los dias que se seleccione se podra ver que horas queda el aula disponible sin reserva.

![Foto pantalla selcionarCampus](images/screens/roomDisponibility.png)

* **Pantalla seleccionar campus**

Pantalla para seleccionar un campus de entre los disponibles.

  ![Foto pantalla selcionarCampus](images/screens/selectCampus.png)
* **Pantalla seleccionar día y hora para la reserva**

Calendario donde poder seleccionar el día y en base a eso apareceran las horas disponibles para elegir abajo en ese día
Se seleccionara una hora de entrada y una de salida.

  ![Foto pantalla hourAndDay](images/screens/selectDayAndHourAvatar.png)
* **Pantalla seleccionar aula dentro de ese campus y esa fecha**

Aquí seran capaces de comprobar la información y detalles del aula proporcionados para los diferentes tipos de usuarios.
En esta pantalla solo saldran las aulas que esten disponibles en la franja horaria seleccionada.

  ![Foto pantalla selectRoom](images/screens/selectRoom.png)
* **Pantalla seleccionar opciones del administrador**

Página principal donde salen las opciones que tiene el administrador justo despues de registrarse.

  ![Foto pantalla startScreenAdmin](images/screens/startScreenAdmin.png)
* **Pantalla seleccionar opciones de aulas para el administrador**

Pantalla para ver y seleccionar de entre todas las opciones que tiene disponibles el administrador para las aulas.

  ![Foto pantalla roomOptionsAdmin](images/screens/roomOptionsAdmin.png)

* **Pantalla estadísticas de las aulas**

Pantalla para ver las estadísticas por día de ocupación de las aulas.

![Foto pantalla roomOptionsAdmin](images/screens/roomStatistics.png)

* **Pantalla crear nuevas aulas en el servicio**

Recoge el nombre, capacidad, campus, edificio, planta, ubicación exacta y software de el aula a crear.

  ![Foto pantalla createRoom](images/screens/createRoomAdmin.png)
* **Pantalla selecionar el aula a editar o borrarla**

Aquí apareceran todas las aulas existentes disponibles para borrarse o editarse.

  ![Foto pantalla editOrDeleteRoom](images/screens/editOrDeleteRoom.png)
* **Pantalla editar aulas existentes**

Igual a los datos recopilados en la pantalla de crear aula
pero con un campo especial para activar o desactivar la disponibilidad del aula si la situación (obras, reformas, ...) lo requiere.

  ![Foto pantalla editRoom](images/screens/editRoomScreen.png)
* **Pantalla seleccionar usuario para el administrador**

Menu para el administrador donde poder seleccionar de entre todos los usuarios existentes.

  ![Foto pantalla selectUserAdmin](images/screens/selectUserAdmin.png)
* **Pantalla administrar al usuario correspondiente como administrador**

Pantalla para permitir al administrador ver la información de cierto usuario o borrarlo o borrar una reserva 
suya de haber algun problema con ello. También saldran todas las reservas realizadas por ese usuario, solo 
dejara borrar las que aun esten vigentes. Estaran ordenadas las reservas de más reciente a más antigua.

  ![Foto pantalla editUserAdmin](images/screens/editUserAdmin.png)
* **Pantalla para editar el perfil de usuario**

Pantalla que nos permite personalizar el perfil cambiando el alias, icono, o nuestro email o reservas de ser necesario.
Aquí saldran todas nuestras reservas como un historial.

  ![Foto pantalla editProfile](images/screens/editProfile.png)

* **Pantalla de exito al reservar un aula correctamente**

Pantalla de confirmación que muestra que la reserva se ha completado correctamente.

  ![Foto pantalla successfullReservation](images/screens/successfullReservation.png)

* **Pantalla para recordar registrarse al usuario anónimo**

Pantalla que recuerda al usuario anónimo que la función de reservar está disponible solo para usuarios registrados o administradores.

  ![Foto pantalla notRegistredTryToBook](images/screens/notRegisteredTryToBook.png)

* **Pantalla de error**

Pantalla de aviso en caso de que ocurra algún error durante la navegación por la aplicación.

  ![Foto pantalla notRegistredTryToBook](images/screens/errorScreen.png)


### Diagramas de Navegación

* **Diagrama de navegación para reservar un aula y ver su disponibilidad.**

![Foto pantalla diagramaNavegacion(NoAdmin)](images/diagrams/diagramaNavegacion(NoAdmin).png)

* **Diagrama de navegación de la acciones posibles siendo un administrador.**

![Foto pantalla notRegistredTryToBook](images/diagrams/diagramaNavegacion(Admin).png)

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



### 🛠️ Metodología
El desarrollo seguirá un enfoque iterativo y secuencial, con entregas periódicas y revisión por el tutor en cada fase.

* **Fase 1:** Definición de funcionalidades y pantallas (Finalización: 30 septiembre)

* **Fase 2:** Configuración repositorio, CI y Sonar (Finalización: 3 noviembre)

* **Fase 3:** Funcionalidad básica con pruebas: Unit, Int y E2E (Finalización: 1 diciembre)

* **Fase 4:** Versión 0.1 - Funcionalidad completa y Docker (Finalización: 5 enero)

* **Fase 5:** Memoria (Finalización: 15 enero)


Diagrama de Gantt:

![Diagrama de Gantt](images/diagrams/diagramaGantt.png)

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





### Diagrama de Entidades

![Diagrama de Entidades](images/diagrams/diagramaEntidades.png)



---

## 📌 Fase 2

---
## 👨‍💻 Autores
* **Alumno:** Guilermo Arenal Estebaranz
* **Tutor:** Michel Maes Bermejo

