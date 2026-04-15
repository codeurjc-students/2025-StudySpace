## Especificación técnica de Casos de Uso

Con el objetivo de definir el comportamiento exacto del sistema, a continuación se detallan los casos de uso principales. Para cada uno se especifican los actores involucrados, las precondiciones necesarias, el flujo principal de eventos y las postcondiciones resultantes, siendo fieles a la lógica de negocio implementada en el código.

### CU-01: Autenticación, registro y gestión de perfil

- **Actores:** Usuario Anónimo, Usuario Registrado.
- **Descripción:** Gestión del ciclo de vida de la cuenta del usuario y su seguridad.
- **Precondiciones:** Ninguna para el registro. Para el perfil, el usuario debe tener una sesión válida.
- **Flujo principal:**
  1. El usuario anónimo se registra introduciendo nombre, correo y contraseña segura.
  2. El sistema cifra la contraseña y guarda el usuario en base de datos.
  3. El sistema le envia un **enlace de confirmación que contiene un token dinámico** al correo electronico del usuario, que debe usar para activar su cuenta y asi poder iniciar sesion.
  4. El usuario inicia sesión; el _backend_ valida las credenciales y devuelve un _token_ JWT en formato _cookie_ segura.
  5. El usuario accede a su perfil, donde puede actualizar sus datos o subir una imagen de avatar (almacenada en el sistema de ficheros del servidor).
  6. Si olvida la contraseña, puede solicitar un reseteo, recibiendo un enlace temporal por correo.
- **Postcondiciones:** El usuario queda autenticado y autorizado para interactuar con los recursos protegidos.

### CU-02: Búsqueda y filtrado avanzado de infraestructura

- **Actores:** Todos los usuarios.
- **Descripción:** Localización de aulas cruzando requisitos técnicos y de aforo.
- **Precondiciones:** El sistema debe tener aulas y _software_ registrados en la base de datos.
- **Flujo principal:**
  1. El usuario introduce parámetros de búsqueda en la interfaz (capacidad mínima, campus, texto libre para buscar coincidencias de _software_ o nombre de aula).
  2. El _frontend_ envía la petición paginada al _backend_.
  3. El _backend_ procesa la consulta mediante _Hibernate Search (Lucene)_, realizando una búsqueda difusa y filtrando aulas inactivas si el usuario no es administrador.
  4. Se devuelve el listado de aulas paginado y ordenado.
- **Postcondiciones:** La vista principal se actualiza mostrando las tarjetas de las aulas que cumplen los criterios.

### CU-03: Formalización de reservas y búsqueda inteligente

- **Actores:** Usuario Registrado, Administrador.
- **Descripción:** Proceso de reserva de un espacio y sistema de recomendación ante conflictos.
- **Precondiciones:** Usuario autenticado. Aula seleccionada en estado "Activo".
- **Flujo principal:**
  1. El usuario selecciona una fecha, una franja horaria (máximo 3 horas continuas) y redacta el motivo de la reserva.
  2. El sistema valida en tiempo real que no haya solapamientos con otras reservas.
  3. Si está disponible, la reserva se guarda en la base de datos.
  4. **Flujo alternativo (Conflicto):** Si el tramo está ocupado, el usuario pulsa en "Búsqueda Inteligente". El algoritmo rastrea aulas con igual o mayor capacidad en el mismo campus y devuelve sugerencias. Si encuentra disponibilidad exacta, la etiqueta como _Exact Match_; de lo contrario, sugiere ligeros desplazamientos horarios (_Alternative_). El usuario selecciona una opción y el formulario se autocompleta.
- **Postcondiciones:** La reserva queda vinculada al usuario. Se genera un archivo `.ics` adjunto en el correo final para añadir a _Google Calendar_ u _Outlook_.

### CU-04: Gestión administrativa de usuarios e inventario

- **Actores:** Administrador.
- **Descripción:** Administración total (CRUD) del catálogo de aulas, software y moderación de cuentas de usuario.
- **Precondiciones:** El usuario debe tener el rol `ROLE_ADMIN`.
- **Flujo principal:**
  1. El administrador navega a los paneles de gestión correspondientes (Aulas, Software o Usuarios).
  2. **Gestión de Aulas/Software:** Puede crear nuevos registros, editar características o borrarlos de la base de datos.
  3. **Desactivación de Aulas:** Al desactivar un aula (por obras o incidencias), el sistema bloquea nuevas reservas. Al eliminarla permanentemente, el sistema pide un **motivo obligatorio** mediante un diálogo, cancela en cascada todas las reservas futuras y notifica a los usuarios afectados por correo adjuntando el motivo.
  4. **Gestión de Usuarios:** El administrador puede buscar usuarios, promoverlos al rol de _Admin_, o bloquear sus cuentas (_Toggle Block_) para impedirles el inicio de sesión.
- **Postcondiciones:** El catálogo público y los permisos de acceso se actualizan en tiempo real reflejando los cambios del administrador.

### CU-05: Moderación administrativa de reservas de terceros

- **Actores:** Administrador.
- **Descripción:** Capacidad de intervenir en el sistema de reservas para solucionar conflictos logísticos o usos indebidos.
- **Precondiciones:** El usuario debe tener el rol `ROLE_ADMIN`.
- **Flujo principal:**
  1. El administrador accede al historial de reservas de un usuario específico a través del panel de gestión.
  2. Si detecta una anomalía, selecciona la opción de "Cancelar".
  3. El sistema lanza un _prompt_ exigiendo que el administrador escriba una **justificación de la cancelación**.
  4. La reserva pasa a estado cancelado en la base de datos.
- **Postcondiciones:** El tramo horario vuelve a estar libre para otros estudiantes. El usuario penalizado recibe un correo automático informándole de la cancelación junto con la justificación redactada por el administrador.

### CU-06: Generación de analítica y mapas de calor

- **Actores:** Todos los usuarios (Analítica específica) / Administrador (Analítica global).
- **Descripción:** Visualización estadística del uso de los espacios mediante gráficas interactivas.
- **Precondiciones:** Existencia de reservas en el sistema.
- **Flujo principal:**
  1. El cliente realiza una petición al _endpoint_ de estadísticas (específico de un aula o global).
  2. El _backend_ agrupa los datos de ocupación por franjas horarias de 30 minutos y calcula porcentajes de uso de aulas y de software asociado.
  3. El _frontend_ renderiza la información utilizando _Chart.js_ (diagramas de anillo y barras) y dibuja el mapa de eventos utilizando _FullCalendar_.
- **Postcondiciones:** El usuario puede analizar visualmente los "mapas de calor" de tráfico y el administrador puede utilizar las métricas globales para la toma de decisiones logísticas.
