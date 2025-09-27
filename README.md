# 2025-StudySpace

---

## 📜 Introducción



---

## 📌 Fase 1
### Prototipos de pantallas
Prototipos de las pantallas finales de la aplicación web y una breve descripcion debajo de cada una, explicando su funcionalidad y sentido:

* **Pantalla de incio**
  ![Foto pantalla inicio](images/screens/startScreen.png)
* **Pantalla de registrarse**
  ![Foto pantalla registro](images/screens/register.png)
* **Pantalla de inciar sesion**
  ![Foto pantalla iniciar sesion](images/screens/logIn.png)
* **Pantalla seleccionar campus**
  ![Foto pantalla selcionarCampus](images/screens/selectCampus.png)
* * **Pantalla seleccionar dia y hora para la reserva**
  ![Foto pantalla hourAndDay](images/screens/selectDayAndHourAvatar.png)
* **Pantalla seleccionar aula dentro de ese campus y esa fecha**

Aquí seran capaces de comprobar la información y detalles del aula proporcionados para los diferentes tipos de usuarios.
  ![Foto pantalla selectRoom](images/screens/selectRoom.png)
* * **Pantalla seleccionar opciones del administrador**
  ![Foto pantalla startScreenAdmin](images/screens/startScreenAdmin.png)
* **Pantalla seleccionar opciones de aulas para el administrador**
  ![Foto pantalla roomOptionsAdmin](images/screens/roomOptionsAdmin.png)
* **Pantalla crear nuevas aulas en el servicio**
  ![Foto pantalla createRoom](images/screens/createRoomAdmin.png)
* **Pantalla selecionar el aula a editar o borrarla**
  ![Foto pantalla editOrDeleteRoom](images/screens/editOrDeleteRoom.png)
* **Pantalla editar aulas existentes**

Con un campo especial para activar o desactivar la disponibilidad del aula si la situación (obras, reformas, ...) lo requiere.
  ![Foto pantalla editRoom](images/screens/editRoomScreen.png)
* **Pantalla seleccionar usuario para el administrador**
  ![Foto pantalla selectUserAdmin](images/screens/selectUserAdmin.png)
* **Pantalla administrar al usuario correspondiente como administrador**
  ![Foto pantalla editUserAdmin](images/screens/editUserAdmin.png)
* **Pantalla para editar el perfil de usuario**
  ![Foto pantalla editProfile](images/screens/editProfile.png)
* **Pantalla de exito al reservar un aula correctamente**
  ![Foto pantalla successfullReservation](images/screens/successfullReservation.png)
* **Pantalla para recordar registrarse al usuario anónimo**
  ![Foto pantalla notRegistredTryToBook](images/screens/notRegistredTryToBook.png)



### Diagramas de Navegación
Diagrama de navegación para reservar un aula y ver su disponibilidad.

![Foto pantalla diagramaNavegacion(NoAdmin)](images/diagrams/diagramaNavegacion(NoAdmin).png)

Diagrama de navegación de la acciones posibles siendo un administrador.

![Foto pantalla notRegistredTryToBook](images/diagrams/diagramaNavegacion(Admin).png)


### Funcionalidades diferentes tipos de usuarios

| Funcionalidades                                             | Usuario Anónimo | Usuario Registrado | Usuario Administrador |
|-------------------------------------------------------------|:---------------:|:------------------:|:---------------------:|
| Visualizar los detalles del aula (software, ubicación, ...) |        ✅        |         ✅          |           ✅           |
| Visualizar la disponibilidad de las aulas                   |        ✅        |         ✅          |           ✅           |
| Crear/Cancelar tu propia reserva                            |                 |         ✅          |           ✅           |
| Cancelar las reservas de otros usuarios                     |                 |                    |           ✅           |
| Modificar/Borrar información del perfil                     |                 |         ✅          |           ✅           |
| Borrar otros usuarios o reservas de sus perfiles            |                 |                    |           ✅           |
| Añadir/Modificar/Borrar aulas                               |                 |                    |           ✅           |


### Diagrama de Entidades

![Diagrama de Entidades](images/diagrams/DiagramaEntidades(Incompleto).png)



---

## 📌 Fase 2

