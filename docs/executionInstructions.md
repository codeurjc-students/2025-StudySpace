## 💻 Instrucciones de Ejecución

### Prerrequisitos

Para poder levantar el entorno de desarrollo y ejecutar las pruebas, es necesario tener instaladas y configuradas las siguientes herramientas en el sistema:

- Docker Desktop (en ejecución).
- Java 17 o superior y Maven (accesible mediante el comando mvn).
- Node.js (v18+) y su gestor de paquetes npm.
- Angular CLI (para la ejecución de pruebas del frontend con ng).

En caso de querer únicamente ejecutar la aplicación, bastará con tener Docker Desktop instalado y en ejecución, y solo requerirá de descargar la carpeta docker del proyecto, sin necesidad de instalar Java, Node o Angular CLI, ya que la aplicación se ejecuta completamente dentro de contenedores Docker.
De no ser este el caso siga con los pasos a continuación.

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
> Debido a que la aplicación utiliza un **certificado SSL auto-firmado**, el navegador bloqueará por defecto la visualización y acceso a la aplicación, a menos que se acepte el riesgo inicialmente.
>
> Este paso es fundamental para que se muestre la aplicación, al no confiar en el certificado por ser auto-firmado.

### Levantar el entorno con Docker

En la carpeta docker del proyecto, ejecutar los siguientes comandos para levantar el entorno con Docker, asegurándonos de que Docker Desktop/Engine está arrancado:

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

Antes de probar los tests para instalar las dependencias necesarias.

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

Si aún no tienes los node_modules instalados, ejecuta `npm install` antes de `npm start`.

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

Para realizar pruebas de concurrencia y estrés sobre el servidor y visualizar los resultados en un _dashboard_ interactivo, utilizamos Artillery conectado a **Artillery Cloud**, atacando el entorno E2E. Salvo para las pruebas de duración superior a 30 minutos ya que la version gratuita de artillery cloud no permite mas de ese tiempo de duracion en los test.

##### Pruebas para las fases 0 y 1

Desde la carpeta docker del proyecto, asegúrate de apagar cualquier contenedor previo por si acaso:

```bash
docker-compose down
```

Levanta la infraestructura E2E (Base de datos de pruebas H2 y MailHog):

```bash
docker-compose -f docker-compose.e2e.yml up -d
```

> Si vas a ejecutar la prueba de carga de la fase 1 (tanto la 1 como la 1-heavy) esta se realiza directamente sobre el docker-compose de desarrollo, no sobre el e2e, ya que prueba que el balanceador de carga funcione correctamente. Para ello deberá ejecutar:
>
> ```bash
> docker-compose -f docker-compose-dev.yml up -d --build
> ```
>
> Luego sáltese el paso de levantar el backend en local, ya que el docker-compose-dev.yml ya levanta la aplicación con 3 réplicas.

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
Pásale la clave de Artillery Cloud como variable de entorno (por seguridad, esta clave se inyecta como variable de entorno).
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

> O con el docker-compose de desarrollo si has levantado ese entorno:
>
> ```bash
> docker-compose -f docker-compose-dev.yml down -v
> ```


##### Pruebas para la fase 2

Para ejecutar las pruebas de la fase 2 tanto la de resistencia como la de estres se requiere de tener en **AWS(Amazon Web Sevices)** desplegada la aplicación mediante la plantilla que hay en la raiz del repositorio para **CloudFormation** que generara automaticamente todo lo necesario para que la aplicación funcione. En esta prueba la plantilla solo genera 1 replica de la aplicación sin balanceador de carga alguno por lo que las limitaciones de la aplicación son las que una `t3.micro` ofrece dentro de AWS. Para estas pruebas ya no sera necesario tener docker abierto ya que unicamente deberemos comunicarnos via terminal con la aplicación desplegada en AWS.

Una vez tenga levantada la aplicación en AWS esta le dara una ip donde se encuentra levantada para acceder. Guarde esa ip ya que la usaremos para los comandos que ejecutan las pruebas.


Ejecutaremos el siguiente comando en la carpeta de artillery (donde esta el archivo de test ubicado) refiriendonos al archivo de test correspondiente y sustituyendo la ip del comando por nuestra ip actual de la aplicación que obtuvimos previamente:


```bash
artillery run -t http://51.94.127.96 --output report.json load-test-phase-2-soak.yaml
```
Esto generara un archivo `report.json` en la misma carpeta.
Si queremos ver graficos más detallados de este archivo necesitaremos el archivo `load_graphics.py` el cual se encuentra también en la misma carpeta de artillery y tener `python` instalado para que nuestra maquina reconozca el comando:

```bash
python load_graphics.py
```

El cual nos generara un reporte visual en un archivo `.png` y un `.xlsx` donde podremos revisar de forma más visual los resultados.


**Las instrucciones que acabamos de ver son aplicables para ambas pruebas de ejecución de esta fase.**

>Si queremos el reporte de artillery solo podremos obtenerlo en la versión gratuita de la prueba de estres ya que la prueba de resistencia dura mas de 30 minutos.

Para obtenr el reporte de artillery de la prueba de estres se inyecta la clave de artillery cloud como se muestra en las pruebas de las fases 0 y 1 ejecutando:

```bash
$env:ARTILLERY_CLOUD_API_KEY="LA_CLAVE_AQUI"
```

Para proceder a ejecutar este comando a continuación:

```bash
artillery run -t http://51.94.127.96 --record load-test-phase-2-stress.yaml
```
Sustituyendo la ip por la ip donde este desplegada nuestra aplicación como se menciono en parrafos anteriores.


##### Pruebas para la fase 3



