FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 4575
ENTRYPOINT ["java","-jar","/app/app.jar"]