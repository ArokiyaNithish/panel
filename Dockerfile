FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY target/portal-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 10000
CMD ["sh", "-c", "java -jar app.jar --server.port=$PORT"]
