FROM maven:3.5-jdk-8 AS build  
COPY src /usr/src/app/src  
COPY pom.xml /usr/src/app  
RUN mvn -f /usr/src/app/pom.xml clean package

FROM gcr.io/distroless/java  
COPY --from=build /usr/src/app/target/ircBot-jar-with-dependencies.jar /usr/app/ircBot-jar-with-dependencies.jar

ENTRYPOINT ["java","-jar","/usr/app/ircBot-jar-with-dependencies.jar","/config/config.txt"]
