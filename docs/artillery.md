## Documentación de pruebas de carga y concurrencia

El objetivo de estas pruebas es validar el rendimiento de la aplicación bajo estrés, evaluar la correcta distribución del tráfico mediante el balanceador de carga (HAProxy) y confirmar que las políticas de concurrencia de reservas operan correctamente sin permitir solapamientos.

### Entorno de pruebas (hardware y software)

Las pruebas que se han ejecutado en un entorno local en una máquina con las siguientes especificaciones:

#### Especificaciones de hardware (máquina local):

##### Fases 0 y 1

Estas pruebas se llevaron a cabo en mi ordenador local con estas especificaciones:

- Sistema Operativo: [ Windows 10 Home (Versión 22H2, arquitectura de 64 bits) ]

- Procesador (CPU): [Intel(R) Core(TM) i7-1065G7 CPU @ 1.30GHz - 1.50 GHz. Cuenta con 4 núcleos físicos y 8 procesadores lógicos.]

- Memoria RAM: [ 8 GB ]

- Almacenamiento: [238 GB SSD ]

##### Fase 2   REVISAR ESTOOOOOO  SO? HAY MAS ESPECIFICACIONES IMPORTANTES?
En esta fase se desplegó la infraestructura en Amazon Web Services (AWS) mediante una plantilla de CloudFormation, utilizando recursos limitados correspondientes a la capa gratuita para establecer un entorno de pruebas controlado y de bajo coste:

- **Instancia EC2 (Servidor de Aplicaciones):** instancia `t3.micro` (2 vCPU, 1 GB de memoria RAM), ejecutando una única instancia contenerizada de la aplicación mediante Docker, sin réplicas ni políticas de auto-escalado activas.

- **Base de Datos RDS (Persistencia Relacional):** 1x instancia de base de datos `db.t3.micro` ejecutando MySQL 8.0 (2 vCPU, 1 GB de memoria RAM y 20GiB de Almacenamiento), configurada de forma aislada e independiente del servidor de aplicaciones.

- **Almacenamiento S3:** Un bucket de AWS S3 para la persistencia y distribución de imágenes asociadas a los espacios universitarios y fotos de perfil de los usuarios, sustituyendo el almacenamiento local basado en MinIO de las fases previas.


#### Especificaciones de software:

##### Fases 0 y 1

- Docker Desktop v4.29.0 configurado con el backend de WSL 2 (Windows Subsystem for Linux).
  Debido a esta configuración, los límites de recursos (CPU, memoria RAM, ...) no son estáticos, sino que son gestionados y asignados dinámicamente por el propio sistema operativo Windows según la demanda de los contenedores.

- 1 Balanceador de carga HAProxy (v2.8).

- 3 Réplicas de la aplicación.

- 1 Contenedor MySQL (v8.0).

- 1 Contenedor MinIO. (latest)

##### Fase 2

- 1 única instancia docker con la aplicación contenedorizada (EC2).

- 1 única instancia de base de datos (RDS).

- 1 única instancia de almacenamiento de ficheros (Bucket S3).


### Instrucciones de Ejecución

Para ejecutar las pruebas de carga, se han seguido los pasos descritos al final del archivo [**executionInstructions.md**](docs/executionInstructions.md)

### Herramienta y Escenario de Prueba

Se ha utilizado Artillery en local conectado a Artillery Cloud para la captura y visualización de telemetría.
El escenario simulado por cada Usuario Virtual (UV) replica el comportamiento real dentro de la aplicación de un usuario registrado
ya que se espera sean de estos la gran mayoria de la demanda de la aplicacióón. El flujo de acciones de los UV es el siguiente:

Petición POST a /api/auth/login para autenticación.

Petición GET a /api/auth/me para obtener datos de sesión.

Petición GET a /api/rooms para listar espacios disponibles.

Petición POST a /api/reservations para reservar una sala, se han programado varias peticiones compitiendo por la misma sala (roomId: 1) y en la misma franja horaria para comprobar la respuesta concurrente de la aplicación ante el estrés.

Petición POST a /api/auth/logout para cerrar la sesión.

### Fase 0(load-test-phase-0): Prueba de concurrencia local (sin balanceador de carga)

Esta prueba establece una línea base atacando directamente a una única instancia del backend desplegada en local (http://127.0.0.1:8080/).

- Configuración de carga: 10 usuarios/segundo durante 5 segundos (Total: 50 Usuarios).

- Resultados Esperados: El sistema debe gestionar el bloqueo a nivel de base de datos. De las 50 peticiones concurrentes para reservar la misma sala, se espera obtener exactamente un código HTTP 201 (reserva exitosa) y 49 códigos HTTP 400 (rechazo por concurrencia), demostrando que el bloqueo de la base de datos funciona como se espera.

- Resultados de ejecución:
  - Completados: 50 UVs (100% de éxito en ejecución).
  - Tiempos de respuesta: Mediana (p50) de 109ms y un p95 de 433ms.

  - Validación de Concurrencia: De las 50 peticiones concurrentes para reservar la misma sala, se obtuvo exactamente un código HTTP 201 y 49 códigos HTTP 400, demostrando que el bloqueo de la base de datos funciona como se espera.

  - Captura de pantalla de Artillery Cloud de este test:

    ![Captura resultados test 0 de artillery](../images/screenshots-artillery/load-test-phase-0.png)
    [Enlace al reporte completo en Artillery Cloud](https://app.artillery.io/opmgtbvasi7hy/load-tests/ttrq9_5atq3hydrf5cr77xktqf8p85wxaqf_ybtm)

- Conclusiones de la prueba: La aplicación maneja correctamente la concurrencia a nivel de base de datos, permitiendo solo una reserva exitosa y rechazando las demás. Sin embargo, el tiempo de respuesta p95 de 433ms indica que bajo esta carga, la aplicación puede experimentar cierta latencia, lo que sugiere que la arquitectura monolítica sin balanceo de carga puede no ser óptima para manejar cargas más altas.

### Fase 1A(load-test-phase-1): Prueba de carga y concurrencia local sostenida en arquitectura distribuida (con balanceador de carga)

Esta prueba evalúa la aplicación contenerizada completa (el docker-compose-dev.yml para desarrollo), atacando al balanceador de carga HAProxy (https://localhost/api) que funciona con un Round Robin gestionando las 3 replicas de la aplicación.

- Configuración de carga:
  - Fase de calentamiento (Warm up): 15 segundos a 2 UVs/seg.

  - Fase de carga sostenida: 30 segundos a 3 UVs/seg(se tuvo que bajar de 5UV/seg a 3UV/seg debido a que si no la CPU no daba a basto a tantas peticiones y algunas fallarian por ETIMEDOUT, y aun a pesar de este ajuste solo los test iniciales que se realizan estan libres de ETIMEDOUT por lo que esta es la cifra limite de aforo para la aplicación en local).

  - Total generado: 120 Usuarios.

- Algoritmo de Balanceo: Dynamic Round Robin.

- Resultados Esperados: El sistema debe distribuir la carga entre las 3 réplicas del backend, manteniendo tiempos de respuesta razonables. De las 120 peticiones concurrentes para reservar la misma sala, se espera obtener exactamente un código HTTP 201 (reserva exitosa) y 119 códigos HTTP 400 (rechazo por concurrencia), demostrando que el bloqueo de la base de datos sigue funcionando correctamente incluso bajo una carga más alta y distribuida.

- Resultados de ejecución:
  - Completados: 118 UVs completados con éxito. Hubo 2 fallos menores por ETIMEDOUT (1.67%), algo esperado al saturar la red interna de Docker en localhost y no disponer de más recursos para estos 2 usuarios mostrando que el umbral tolerable para mi aplicación esta en 3 usuarios por segundo ya que pruebas más alla de eso generan muchos más usuarios con errores ETIMEDOUT y con 3 usuarios por segundo el primer test que se corre de artillery es capaz de pasar completamente limpio y al segundo ya comienzan a aparecer errores de ETIMEDOUT.
  - Tiempos de respuesta: Mediana (p50) de 34ms y un p95 de 105ms.

  - Captura de pantalla de Artillery Cloud de este test:

    ![Captura resultados test 1A de artillery](../images/screenshots-artillery/load-test-phase-1.png)
    [Enlace al reporte completo en Artillery Cloud](https://app.artillery.io/opmgtbvasi7hy/load-tests/tdqza_e6dxacbw3rc3bka5p7bjckrdg5dy7_3emn)

  - Captura de pantalla de Artillery Cloud de este test con mas usuarios por segundo para mostrar la diferencia y el aforo limite de la aplicación en local para este test:

    ![Captura resultados test 1A de artillery](../images/screenshots-artillery/load-test-phase-1A-primero.png)
    [Enlace al reporte completo en Artillery Cloud](https://app.artillery.io/opmgtbvasi7hy/load-tests/tyttk_hcf64ykt5a67dhz9y7r56xk89c8cw_9j6b)

- Conclusiones de la prueba: A pesar de inyectar más del doble de usuarios virtuales que en la Fase 0, la arquitectura distribuida redujo el tiempo de respuesta p95 de 433ms a 162ms. Nuevamente, la regla de concurrencia se mantuvo sólida: 1 única reserva exitosa (HTTP 201) y 117 rechazos controlados (HTTP 400).

### Fase 1B(load-test-phase-1-heavy): Prueba de carga de procesamiento intensivo en entorno local

Tras validar la concurrencia y el limite de usuarios por segundo en local en mi máquina en la prueba anterior(1A), esta prueba busca estresar la CPU y la base de datos mediante endpoints que requieren cálculos complejos, búsquedas avanzadas y agregaciones de datos.
Con el fin de evaluar el rendimiento de la arquitectura ante lógica de negocio costosa aprovechando propiedades de la aplicación como:

- Hibernate Search: Búsquedas difusas (fuzzy search) y full-text.

- Cálculos geográficos: Uso de la fórmula de Haversine para distancias entre campus.

- Generación de mapas de calor: Procesamiento iterativo de calendarios y disponibilidad.

- Estadísticas dinámicas: Agregaciones en tiempo real de la ocupación.

- Configuración de carga:
  - Fase de calentamiento: 15 segundos a 2 UV/seg.

  - Fase de carga sostenida: 30 segundos a 2 UV/seg(se tuvo que bajar de 3UV/seg a 2UV/seg debido a que si no la CPU no daba a basto a tantas peticiones).

  - Total generado: 90 Usuarios (flujo completo de 6 peticiones pesadas por usuario).

- Algoritmo de Balanceo: Dynamic Round Robin.

- Resultados Esperados: El sistema debe gestionar la carga de procesamiento intensivo sin degradar significativamente los tiempos de respuesta. Se espera que el sistema mantenga,aunque sean superiores, unos tiempos de respuesta razonables no muy distantes a los obtenidos en la prueba 1A, demostrando que la arquitectura puede manejar operaciones complejas incluso bajo carga.

- Resultados de ejecución:
  - Completados: 90 UVs (100% de éxito).

  - Tiempos de respuesta: Mediana (p50) de 34ms y un p95 de 116ms. A pesar de la complejidad de los cálculos (Smart Search y Calendar), la distribución en 3 réplicas permite mantener latencias por debajo de los 100ms para el 95% de los usuarios.

  - Captura de pantalla de Artillery Cloud de este test:

    ![Captura resultados test 1B de artillery](../images/screenshots-artillery/load-test-phase-1-heavy.png)
    [Enlace al reporte completo en Artillery Cloud](https://app.artillery.io/opmgtbvasi7hy/load-tests/txe97_jxk3jxxjr8ht4q6cnf43xggqewz7p_t448)

Conclusiones de la prueba: La aplicación demuestra una alta capacidad de cómputo en local manteniendo unos tiempos de respuesta razonables para los procesos que se le piden, a pesar de las limitaciones del hardware sobre el que se ejecutan las pruebas, ante consultas bastante estresantes debido a la cantidad de computo que llevan.


### Fase 2A(load-test-phase-2-stress): Prueba de Esfuerzo y Límite en entorno AWS con una replica

El objetivo de esta prueba fue estresar la capacidad de procesamiento de la infraestructura en la nube bajo un escenario que simula el comportamiento de picos de demanda. Se buscó forzar los endpoints críticos del backend mediante tres perfiles de usuarios simulados:

- **Perfil de Consulta Pura (70% de la carga):** Usuarios que acceden a la plataforma para listar y filtrar espacios universitarios (`/api/search/rooms`), simulando un comportamiento pasivo.
- **Perfil de Reserva Directa (20% de la carga):** Usuarios activos que efectúan consultas y proceden a intentar registrar de forma inmediata una reserva fija (`/api/reservations`).
- **Perfil de Búsqueda Inteligente (10% de la carga):** Usuarios que invocan de manera intensiva el algoritmo avanzado de sugerencias alternativas y disponibilidad temporal (`/api/reservations/smart-search`).

#### Configuración de la carga:
La primera prueba se estructuró en cuatro fases progresivas de inyección en Artillery: 
una fase de calentamiento a 2 UV/seg durante 60 segundos, seguida de una rampa agresiva incrementando hasta 10 UV/seg durante 120 segundos, aproximándose al límite teórico entre 10 y 12 UV/seg en los siguientes 120 segundos, y finalizando con un pico sostenido de 12 UV/seg durante 120 segundos adicionales.

La segunda prueba se estructuró también en cuatro fases progresivas: 
las 2 primeras fases fueron iguales a las del test anterior después seguimos entre 10 y 15 UV/seg en los siguientes 120 segundos, y finalizamos con un pico sostenido de 17 UV/seg durante 120 segundos adicionales.


#### Resultados de ejecución del primer test:
- **Completados con éxito lógico:** 100% de los usuarios virtuales completaron sus flujos de navegación sin provocar caídas del servicio de aplicaciones o interrupciones críticas del contenedor (cero errores HTTP 500).
- **Rendimiento por Endpoint:**
  - `/api/auth/login`, `/api/search/rooms` y `/api/reservations/smart-search`: 100% de respuestas exitosas (HTTP 200). La infraestructura absorbió eficientemente las búsquedas de texto plano indexadas con Apache Lucene / Hibernate Search.
  - `/api/reservations`: Se registro un 0,37% (4 respuestas del total mandado a este endpoint) de las respuestas como **HTTP 400 Bad Request** debido a la generación aleatoria de reservas que alguna coincidencia accidental genera, frente a las 1065 (99,63%) confirmaciones exitosas con código **HTTP 201 Created**.
  - **Tiempos de respuesta (Latencia):** Mientras que las lecturas mantuvieron una mediana (p50) baja y estable en torno a los 133ms y media de 190ms, los intentos de escrituras concurrentes provocaron picos de degradación en los percentiles más altos, alcanzando un p95 de 478ms y un p99 de 983 ms.


#### Captura de pantalla de Artillery Cloud del primer test:

![Captura resultados test 2A de artillery](../images/screenshots-artillery/load-test-phase-2stress.png)
[Enlace al reporte completo en Artillery Cloud](https://app.artillery.io/opmgtbvasi7hy/load-tests/tcgay_x4yjnawbjrekhhebmx3x7eznmeyxr_ch6t)

#### Conclusiones de la primera prueba de esfuerzo:
En la gráfica se pueden apreciar ciertos picos al inicio de cada fase de este test cuando los usuarios subian drasticamente. A partir de los 10 usuarios en adelante al finalizar la fase 2 estos picos se volvieron más y más pronunciados en cuanto al tiempo máximo de espera de p95 de la aplicación. 
Si nos fijamos en la linea naranja, verde  y azul veremos que justo cuando la aplicación presenciaba una caida en la demanda de peticiones y un pico en los usuarios activos en la aplicación justo en ese momento los tiempos de respuesta se disparaban puesto que la aplicación no pudo procesar tan rápido ese aumento en los usuarios activos. 
Esto nos muestra que nuestra aplicación cuando esta rondando los 90 usuarios activos aproximadamente (dentro de las limitaciones que ofrece una replica en la capa gratuita de AWS) permanece con unos tiempos de respuesta bajos acordes al número de peticiones que llegan, pero cuando estos usuarios activos suben por encima de los 90, la aplicación comienza a no dar a basto para atender todas las peticiones y comienza a ponerlas en cola, lo que genera que se disparen los tiempos de respuesta. Aun asi en este test se puede apreciar que los 3600 usuarios generados pudieron completar con éxito sus cometidos (salvo esos 4 errores 400 debidos a unas reservas que no cumplian con las reglas de negocio de la aplicación debido la generación aleatoria de estas para lograr abastecer más de 1000 solicitudes de reserva).

Esto muestra que la aplicación soporta flujos de hasta 12 usuarios nuevos siendo solo 1 sola réplica, aunque la velocidad de la repuesta se deteriore a partir de 10 usuarios nuevos.



#### Resultados de ejecución del segundo test:
- **Completados con éxito lógico:** 63,38%% de los usuarios virtuales completaron sus flujos de navegación sin provocar errores HTTP 500. En cambio un 36,62% de los usuarios sufrieron de estos errores 500 debido al tiempo de espera que estuvieron en la cola por la saturación de esta prueba frente a la anterior.
- **Rendimiento por Endpoint:**
  - `/api/auth/login`, `/api/search/rooms` y `/api/reservations/smart-search`: 100% de respuestas exitosas (HTTP 200).
  - `/api/reservations`: Se registro un 0,16% (1 respuesta del total mandado a este endpoint) de las respuestas como **HTTP 400 Bad Request** debido a la generación aleatoria de reservas que alguna coincidencia accidental genera, frente a las 628 (99,84%) confirmaciones exitosas.
  - **Tiempos de respuesta (Latencia):** La mediana (p50) subio hasta los 268ms (más del doble que en la prueba anterior), la media también subio bastante de los 190ms hasta los 1,3 segundos(más de 6 veces el tiempo anterior). Los intentos de escrituras también aumentaron bastante en tiempo alcanzando un p95 de 6,9 segundos y un p99 de 7,8segundos. Siendo la peor respuesta de 8 segundos puesto que mas alla de este tiempo la aplicación no respondio a las peticiones.


#### Captura de pantalla de Artillery Cloud del segundo test:

![Captura resultados test 2A de artillery](../images/screenshots-artillery/load-test-phase-2stress2.png)
[Enlace al reporte completo en Artillery Cloud](https://app.artillery.io/opmgtbvasi7hy/load-tests/tebjh_y6g9gaw5e6ejd4cjyq7pjy9kpn639_bh8b)

#### Conclusiones de la segunda prueba de esfuerzo:
Al mantenerse igual las 2 primeras fases la grafica genera resultados similares en estas. No es hasta la 3 fase que vemos cambios significativos donde podemos apreciar que los usuarios comienzan a fallar en cuanto las respuestas p95 comienzan a tardar mas debido al exceso de usuarios activos en la aplicación. Como podemos comprobar antes de fallar los usuarios activos en la aplicación rondan los 100 usuarios (120 en el momento antes de comenzar a perder peticiones por no dar a basto) como mencionamos en las conclusiones del test anterior con unos 90 usuarios activos a la vez pudimos ver que la aplicación comenzaba a tardar bastante tiempo pero lograba reponderlos a todos. Sin embargo aquí con 100 usuarios activos simultaneamente vemos que la aplicación comienza a fallar. 


#### Conclusiones generales de las pruebas de esfuerzo:
Estos dos test nos muestran que el cuello de botella de la aplicación se encuentra entre los 90 y 100 usuarios activos y entre los 12 y 15 usuarios generados por segundo, más alla de este límite (para la versión gratuita de AWS y una sola replica disponible) la aplicación no es capaz de abastecer a todas las peticiones pendientes. 
También nos muestran estos resultados que el endpoint más crítico de la aplicación (debido a las comprobaciones que se lleban a cabo antes de completar una reserva asi como el  bloqueo de la base de datos para evitar reservas duplicadas en diferentes usuarios) es el endpoint de /api/reservations/ aunque el endpoint más lento sea api/reservations/smart-search debido a que sus tiempos son parejos a los de las reservas pero muchos menos usuarios estaban atacando este endpoint.
Después de estos 2 endpoints el más rápido en base a los test realizados seria la barra de busqueda de aulas mediante api/search/rooms y el segundo más rápido el propio inicio de sesión de la página web en /api/auth/login. Estos endpoints, a diferencia de los anteriores que recibian menor carga de usuarios para simular un comportamiento real de la aplicación, recibieron el 100% de los usuarios, todos pasaron por ellos.
Estas son las capturas de la primera prueba de estres (aunque para esta conclusión se han comparado los resultados de más pruebas para asegurarse):

![Captura resultados test 2A endpoints](../images/screenshots-artillery/load-test-phase-2stressEndpoint.png)
![Captura resultados test 2A endpoints](../images/screenshots-artillery/load-test-phase-2stressEndpoint2.png)



---

### Fase 2B (load-test-phase-2-soak): Prueba de Resistencia y Estabilidad Sostenida (Soak Test) en entorno AWS

El propósito de esta fase fue someter a la instancia única de AWS a una carga continua de usuarios virtuales (`arrivalRate: 5` usuarios por segundo) con el fin de evaluar la fatiga del sistema a lo largo del tiempo, vigilando el comportamiento del recolector de basura de la JVM (*Garbage Collector*), el uso sostenido del pool de conexiones de base de datos HikariCP y la posible presencia de fugas de memoria (*Memory Leaks*).

#### Limitación del plan gratuito de la herramienta Artillery Cloud:
Debido a las políticas comerciales implementadas en la plataforma de Artillery, la visualización y captura de telemetría a través de su servicio en la nube (*Artillery Cloud*) se encuentra restringida a un máximo estricto de 30 minutos de duración en su plan gratuito. Dado que una prueba de resistencia requiere analizar el comportamiento persistente del sistema operativo y de los contenedores más allá de dicho umbral temporal, se optó por una estrategia diferente. 

Se configuró Artillery para volcar todas las métricas e intervalos de telemetría del test en formato `.json`. Posteriormente, se diseñó e implementó un script automatizado en Python que procesa dicho JSON de forma local. Este script extrae de forma secuencial las métricas agregadas por periodos e implementa un generador de gráficos (gráficas de latencia percentilada, tasa de transferencia y volumen de códigos HTTP), garantizando graficas con el sufiente detalle como para suplir las de *Artillery Cloud*.

#### Resultados de ejecución y telemetría:
- **Volumen Total:** Se procesó un flujo histórico masivo que superó los 36,120 usuarios virtuales creados, traduciéndose en un total acumulado de 72,502 peticiones HTTP procesadas por el servidor.
- **Códigos de Estado y Errores de Flujo:**
  - Respuestas exitosas de sesión y lectura (HTTP 200): 68,783 peticiones.
  - Reservas confirmadas (HTTP 201): 108 peticiones.
  - Rechazos por reglas de negocio (HTTP 400): 3,611 peticiones.
  - Usuarios abortados por Artillery (`vusers.failed`): 7,176 usuarios virtuales. 
- **Estudio de Tiempos de Respuesta Sostenidos:** La mediana global (p50) demostró un comportamiento robusto, manteniéndose plana a lo largo de toda la línea de tiempo del test en un rango de entre 85ms y 115ms. Las latencias máximas registradas mostraron picos controlados que se estabilizaron en torno a los 1.5 y 1.8 segundos en los intervalos de máxima concurrencia de transacciones de escritura de reservas.

#### Captura de las gráficas de telemetría procesadas localmente mediante el script en Python:

![Reporte de métricas e intervalos del Soak Test](../images/screenshots-artillery/load-test-phase-2-soak.png)

#### Conclusiones de la prueba de resistencia (Soak Test):
El test de resistencia mostro 2 grandes conclusiones de la infraestructura actual de la aplicación:

1. **Ausencia de Fugas de Memoria (Memory Leaks):** La estabilidad absoluta de la mediana de tiempo de respuesta (p50) y la consistencia en el volumen de peticiones procesadas demuestran que el recolector de basura de Java es capaz de liberar eficientemente la memoria de los objetos de sesión descatalogados. Si existiera una fuga de memoria en el montón de la JVM o una retención indebida de conexiones en HikariCP, los tiempos de respuesta habrían mostrado una pendiente ascendente progresiva debido al *thrashing* de memoria o al agotamiento definitivo de los hilos de Tomcat, comportamiento que queda descartado al analizar las líneas planas de rendimiento.

2. **Causa raíz de los usuarios abortados por la herramienta:** El volumen de usuarios marcados como fallidos por Artillery (`vusers.failed: 7176`) es debio al escenario simulado en el archivo de configuración de Artillery. Al inyectar una carga fija de usuarios que competían secuencialmente por un número restringido de salas en idénticas horas, la inmensa mayoría de las solicitudes de reserva de la Fase 2 del flujo terminaban devolviendo un código 400 legítimo (sala no disponible). 

### Propuestas de optimización y líneas de mejora futuras de esta Fase 2

A partir de los cuellos de botella identificados en los entornos de esfuerzo y resistencia alojados en la nube, se proponen las siguientes optimizaciones de arquitectura de software para futuras versiones del sistema sin alterar la seguridad de los datos:

- **Dispersión Estocástica en Pruebas de Carga:** Para evaluar el rendimiento de la base de datos libre de bloqueos de lógica empresarial, se debe modificar el procesador JavaScript del generador de carga (`processor-multi-room.js`). Reemplazar las franjas horarias estáticas (como el bloque fijo de 10:00 a 12:00) por una asignación dinámica y aleatoria que distribuya las solicitudes a lo largo de toda la jornada universitaria operativa (de 08:00 a 21:00) y amplíe el rango de días. Esto aumentará el espacio de probabilidad transaccional a más de un millón de combinaciones, reduciendo las colisiones lógicas y permitiendo medir de forma pura la latencia de inserción en disco de la instancia RDS.

- **Migración a Bloqueo Optimista (Optimistic Locking):** Para mitigar los picos de latencia en el percentil p99 provocados por la retención de hilos de base de datos bajo alta concurrencia, se propone sustituir los cerrojos pesimistas por un enfoque optimista basado en anotaciones `@Version` en la entidad `Reservation` de Hibernate. De este modo, en lugar de bloquear la base de datos de manera exclusiva mientras se procesa la disponibilidad, el sistema procesará las transacciones en paralelo y rechazará de forma instantánea en el commit las operaciones concurrentes que colisionen, reduciendo el tiempo de espera de los hilos de Tomcat de segundos a escasos milisegundos.

- **Ajuste de Timeouts del Pool de Conexiones:** En el archivo `application.properties`, se debe añadir la directiva `spring.datasource.hikari.connection-timeout=5000`. Limitar el tiempo de espera de obtención de conexiones de Hikari a 5 segundos obligará al servidor a liberar hilos rápidamente bajo escenarios de estrés masivo, evitando la acumulación destructiva de solicitudes en cola en el servidor web.


