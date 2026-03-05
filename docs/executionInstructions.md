## 💻 Instrucciones de Ejecución

### Prerrequisitos

Para poder levantar el entorno y ejecutar las pruebas, es necesario tener instaladas y configuradas las siguientes herramientas en el sistema:

- Docker Desktop (en ejecución).
- Java 17 o superior y Maven (accesible mediante el comando mvn).
- Node.js (v18+) y su gestor de paquetes npm.
- Angular CLI (para la ejecución de pruebas del frontend con ng).

---

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
