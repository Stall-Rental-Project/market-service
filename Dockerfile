FROM openjdk:11

# Set the working directory
WORKDIR /app

COPY /target/market-service.jar market-service.jar

ENTRYPOINT ["java","-Dspring.profiles.active=dev", "-jar", "market-service.jar"]

EXPOSE 9566