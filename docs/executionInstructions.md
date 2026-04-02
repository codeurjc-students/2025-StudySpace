## 💻 Instrucciones de Ejecución

### Prerrequisitos

Para poder levantar el entorno de desarrollo y ejecutar las pruebas, es necesario tener instaladas y configuradas las siguientes herramientas en el sistema:

- Docker Desktop (en ejecución).
- Java 17 o superior y Maven (accesible mediante el comando mvn).
- Node.js (v18+) y su gestor de paquetes npm.
- Angular CLI (para la ejecución de pruebas del frontend con ng).

---

### Clonado del repositorio

El primer paso antes de levantar cualquier entorno es obtener el código fuente desde el repositorio oficial y acceder al directorio del proyecto. En una terminal, ejecuta:

```bash
git clone https://github.com/codeurjc-students/2025-StudySpace
cd 2025-StudySpace
```

---

> **Nota importante: Certificado SSL y Visualización de Imágenes**
>
> Debido a que la aplicación utiliza un **certificado SSL auto-firmado**, el navegador bloqueará por defecto las peticiones al backend, lo que impedirá la correcta visualización de las imágenes.
>
> **Antes de proceder con el despliegue**, es necesario autorizar el certificado en el navegador:
>
> 1. Una vez levantado el entorno (paso siguiente), acceda a: [https://localhost:8443/api/users/1/image](https://localhost:8443/api/users/1/image) o a [https://localhost:8443/api/rooms/1/image](https://localhost:8443/api/rooms/1/image).
> 2. En la pantalla de advertencia, haga clic en **"Configuración avanzada"** y seleccione **"Acceder a localhost (sitio no seguro)"**.
>
> Este paso es fundamental para que el frontend pueda mostrar las imágenes de la aplicación, al no confiar en el certificado por ser auto-firmado.

### Levantar el entorno con Docker

En la raíz del proyecto, ejecutar los siguientes comandos para levantar el entorno con Docker, asegurandonos de que Docker Desktop/Engine está arrancado:

```bash
docker-compose pull
docker-compose up -d
```

Para detener el entorno, ejecutar:

```bash
docker-compose down
```

---

Si quieres levantar el entorno de desarrollo:

```bash
docker-compose -f docker-compose-dev.yml up -d --build
```

Para detener el entorno, ejecutar:

```bash
docker-compose -f docker-compose-dev.yml down
```

---

### Ejecución de pruebas

#### Backend

Dentro de la carpeta `backend`, ejecutar en una terminal con mvn instalado:

```bash
mvn -B clean verify

```

---

#### Frontend

Dentro de la carpeta `frontend`, ejecutar (Si no tienes los node_modules instalados.):

```bash
npm install
```

Antes de probar los test para instalar las dependencias necesarias.

Para estos test ejecutar en la misma carpeta frontend:

```bash
ng test
```

---

#### Pruebas E2E (con Playwright)

Para levantar el entorno de pruebas E2E es necesario seguir estos pasos.

Desde la raíz del proyecto, asegúrate de apagar cualquier contenedor previo por si acaso:

```bash
docker-compose down
```

Luego, levanta el contenedor configurado para E2E:

```bash
docker-compose -f docker-compose.e2e.yml up -d
```

Ahora vamos a ejecutar el backend y frontend en local para que Playwright pueda probarlos.
Dentro de la carpeta `backend`, ejecutamos:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=e2e
```

Y dentro de la carpeta `frontend`, ejecutamos:

```bash
npm start
```

Si aun no tienes los node_modules instalados, ejecuta `npm install` antes de `npm start`.

Como último paso, realizamos las pruebas E2E con Playwright, en la carpeta `frontend`, ejecutamos:

```bash
BASE_URL=https://localhost:4200 npx playwright test --ui
```

o

```bash
$env:BASE_URL="https://localhost:4200"; npx playwright test --ui
```

Si lo ejecutas en PowerShell.

Para cerrar Playwright una vez acabadas las pruebas, ejecutamos:

```bash
docker-compose -f docker-compose.e2e.yml down
```

#### Pruebas de Carga y Estrés (con Artillery)

Para realizar pruebas de concurrencia y estrés sobre el servidor, utilizamos Artillery atacando el entorno E2E.

Desde la raíz del proyecto, asegúrate de apagar cualquier contenedor previo por si acaso:

```bash
docker-compose down
```

Levanta la infraestructura E2E (Base de datos de pruebas H2 y MailHog):

```bash
docker-compose -f docker-compose.e2e.yml up -d
```

Dentro de la carpeta `backend`, ejecutamos:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=e2e
```

No es necesario ejecutar el frontend para estas pruebas, ya que se centran en el backend.

Abre otra terminal, dirígete a la carpeta donde están los tests de carga (dentro de `backend/src/test`) y ejecuta Artillery con:

```bash
artillery run load-test-phase-0.yml
```

Al finalizar, puedes apagar el backend (con Ctrl+C). Luego apaga la infraestructura:

```bash
docker-compose -f docker-compose.e2e.yml down
```
