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

> **Nota importante: Certificado SSL**
>
> Debido a que la aplicación utiliza un **certificado SSL auto-firmado**, el navegador bloqueará por defecto la visualizacion y acceso a la aplicación, a menos que se acepte el riesgo inicialemnete.
>
> Este paso es fundamental para que se muestre la aplicación, al no confiar en el certificado por ser auto-firmado.

### Levantar el entorno con Docker

En la carpeta docker del proyecto, ejecutar los siguientes comandos para levantar el entorno con Docker, asegurandonos de que Docker Desktop/Engine está arrancado:

```bash
docker-compose pull
docker-compose up -d
```

Para detener el entorno, ejecutar:

```bash
docker-compose down
```

---

Si quieres levantar el entorno de desarrollo, ejecuta en la carpeta docker:

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

Desde la carpeta docker del proyecto, asegúrate de apagar cualquier contenedor previo por si acaso:

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

---

#### Pruebas de Carga y Estrés (con Artillery)

Para realizar pruebas de concurrencia y estrés sobre el servidor y visualizar los resultados en un _dashboard_ interactivo, utilizamos Artillery conectado a **Artillery Cloud**, atacando el entorno E2E.

Desde la carpeta docker del proyecto, asegúrate de apagar cualquier contenedor previo por si acaso:

```bash
docker-compose down
```

Levanta la infraestructura E2E (Base de datos de pruebas H2 y MailHog):

```bash
docker-compose -f docker-compose.e2e.yml up -d
```

Dentro de la carpeta `backend`, ejecutamos el servidor con el perfil de pruebas:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=e2e
```

No es necesario ejecutar el frontend para estas pruebas, ya que se centran en el backend.

Abre otra terminal, dirígete a la carpeta donde están los tests de carga (dentro de `backend/src/test`).

Si no quieres usar la clave de Artillery Cloud, puedes ejecutar los tests sin ella y no ver reporte visual alguno con:

```bash
artillery run load-test-phase-1.yml
```

Si quieres ver el reporte visual:
Pasale la clave de Artillery Cloud como variable de entorno (por seguridad, esta clave se inyecta como variable de entorno).
En PowerShell, ejecuta:

```bash
$env:ARTILLERY_CLOUD_API_KEY="LA_CLAVE_AQUI"
```

O si es en bash:

```bash
export ARTILLERY_CLOUD_API_KEY="LA_CLAVE_AQUI"
```

Una vez configurada la clave en la sesión actual de la terminal, ejecuta el test indicando que grabe los resultados con el flag --record:

```bash
artillery run --record load-test-phase-1.yml
```

Al finalizar la prueba, la consola imprimirá un Run URL (ej. https://app.artillery.io/...). Haz clic en ese enlace o cópialo en tu navegador para acceder al reporte interactivo, donde podrás analizar gráficas detalladas de latencia, distribución de códigos HTTP y rendimiento bajo estrés.

Finalmente, puedes apagar el backend (con Ctrl+C). Luego apaga la infraestructura:

```bash
docker-compose -f docker-compose.e2e.yml down -v
```
