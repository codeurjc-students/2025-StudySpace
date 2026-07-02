## Transición de almacenamiento de ficheros en local a MinIO

En el diseño inicial, la aplicación guardaba las imágenes utilizando la API nativa de Java (java.nio.file) apuntando a una ruta del sistema de archivos local del contenedor (un directorio /uploads).

Configuración y Variables: Se hacía uso de propiedades estáticas en el archivo application.properties como storage.location=/app/uploads.

En lugar de transicionar inmediatamente a un servicio Cloud, se integró MinIO en el entorno de desarrollo local mediante Docker Compose de forma que se pudiera hacer una transición fácil a Amazon S3 en AWS.

Esto permitió integrar el SDK de AWS en el código fuente de Spring Boot (software.amazon.awssdk.services.s3.S3Client) y escribir toda la lógica de subida, descarga y borrado de ficheros como si la aplicación ya estuviera en la nube, pero trabajando en local. 
Permitió desarrollar y probar intensivamente el módulo multimedia sin incurrir en costes de facturación en AWS ni depender de una conexión a internet.

### Adaptación concreta en el código Java (S3Config.java por ese entonces MinIOConfig.java):
Para que el SDK de AWS apuntara a MinIO en lugar de a los servidores reales de Amazon, fue necesario modificar la inicialización del Bean de S3Client, forzando la URL local del contenedor y habilitando el acceso por ruta virtual (Path-Style Access), ya que MinIO no gestiona subdominios DNS para los buckets de forma nativa en local:

```bash
Java
@Bean
public S3Client s3Client() {
    return S3Client.builder()
        .endpointOverride(URI.create("http://localhost:9000")) // Sobrescritura del endpoint Cloud
        .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build()) // Requisito de MinIO
        .region(Region.EU_WEST_1)
        .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create(awsAccessKey, awsSecretKey)))
        .build();
}
```
Configuración en el entorno de desarrollo: Las credenciales se inyectaban mediante variables de entorno estáticas en Docker Compose:

```bash
AWS_ACCESS_KEY_ID=minioadmin

AWS_SECRET_ACCESS_KEY=minioadmin

MINIO_BUCKET=studyspace-images
```


### Despliegue en Producción con Amazon S3
Una vez finalizado el desarrollo con MinIO se realizó la transición hacia Amazon S3.

Gracias a la decisión previa de usar MinIO y el SDK de AWS, el código fuente en Java no tuvo que ser modificado en absoluto. La migración consistió puramente en un cambio de configuración a nivel de infraestructura:

- Se eliminó por completo el contenedor de MinIO de la arquitectura, liberando memoria RAM y recursos de CPU críticos en la instancia EC2 t3.micro. El almacenamiento pasó a delegarse en el servicio gestionado Amazon S3.

- Modificaciones Concretas en la Configuración de Producción:
En el archivo docker-compose.yml final que despliega CloudFormation, se eliminaron las configuraciones específicas de emulación local. Al no proveer un endpointOverride explícito en las propiedades del entorno, el SDK de AWS autodetecta por defecto que debe comunicarse con los endpoints oficiales de Amazon S3 basándose en la región.

- Evolución del mapeo de variables en el entorno de Docker:
La variable encargada de definir el contenedor de objetos se mapeó para que CloudFormation inyectara el nombre del recurso real creado dinámicamente:

```bash
MINIO_BUCKET=${StudySpaceBucket} (donde StudySpaceBucket se resuelve como studyspace-images-774126906341).
```

- Securización mediante Roles IAM (Eliminación de credenciales estáticas):
A diferencia de la fase con MinIO, donde se usaban claves fijas expuestas, en Amazon S3 se implementó una arquitectura de seguridad basada en políticas de IAM. Las variables AWS_ACCESS_KEY_ID y AWS_SECRET_ACCESS_KEY se dejaron completamente vacías en el entorno del contenedor. En su lugar, el servidor EC2 obtuvo un perfil de instancia asociado (EC2InstanceProfile). El SDK de AWS, al inicializarse y ver que no tiene claves estáticas, activa su cadena de proveedores por defecto (DefaultCredentialsProvider Chain) y solicita un token temporal de acceso directamente al servicio de metadatos de la propia máquina virtual de Amazon.