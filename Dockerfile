# Usa una imagen base ligera de OpenJDK 17 sobre Alpine Linux
FROM openjdk:17-alpine

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo JAR de tu aplicación al contenedor
COPY target/vg-ms-payment-0.0.1-SNAPSHOT.jar app.jar

# Expone el puerto 8088 (el puerto donde corre tu aplicación)
EXPOSE 8088

# Comando principal para ejecutar la aplicación
CMD ["java", "-jar", "app.jar"]
