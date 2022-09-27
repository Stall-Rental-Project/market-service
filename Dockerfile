FROM openjdk:11

# Set the working directory
WORKDIR /app

COPY /target/market-service.jar market-service.jar

ENTRYPOINT ["java", "-jar", "market-service.jar"]

EXPOSE 6566