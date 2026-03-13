## 🏗️ Arquitectura de Despliegue

La arquitectura del proyecto se basa en la contenedorización integral de los servicios, garantizando una ejecución idéntica en cualquier entorno. El sistema se orquesta localmente mediante `docker-compose` y se compone de tres contenedores principales comunicados a través de una red interna aislada:

- **🗄️ Base de Datos (MySQL):** Ejecuta la imagen oficial de MySQL. Por seguridad, el puerto interno (3306) no se expone a la máquina anfitriona, permitiendo acceso únicamente desde la red de Docker. Utiliza volúmenes persistentes para evitar la pérdida de datos.

- **⚙️ Servidor Backend (Spring Boot):** Ejecuta la API REST y procesa toda la lógica de negocio. Expone el puerto HTTPS (8443) y es el único autorizado para conectarse con la base de datos.

- **💻 Cliente Frontend (Angular):** Sirve los archivos estáticos generados por Angular. Para soportar el enrutamiento interno de la *Single Page Application* (SPA), el backend implementa un `SpaController` que redirige las rutas no pertenecientes a la API al `index.html`.


![Arquitectura de despliegue con contenedores Docker](../images/diagrams/diagramaArquitectura.png) 


