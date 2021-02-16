# Package stage
FROM openjdk:11
ARG JAR_FILE=./target/*.jar
ARG APP_PATH=/home/app
ARG FULL_APP_PATH=${APP_PATH}/mycloud-api.jar
COPY ${JAR_FILE} ${FULL_APP_PATH}
VOLUME /var/lib/myCloud/
EXPOSE 8080
ENTRYPOINT ["java","-jar","/home/app/mycloud-api.jar"]
