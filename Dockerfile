FROM maven:3-amazoncorretto-21 as build
COPY src/main/java src/main/java
COPY src/main/resources src/main/resources
COPY src/test src/test
COPY pom.xml pom.xml
RUN mvn package -f pom.xml

FROM amazoncorretto:21
COPY --from=build /target/jchess.jar /app/jchess.jar
COPY --from=build /target/lib /app/lib
CMD ["java", "-jar", "/app/jchess.jar"]
