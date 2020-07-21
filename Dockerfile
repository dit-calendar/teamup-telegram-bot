######## Buil app
FROM openjdk:11 AS BUILD_IMAGE
ENV APP_HOME=/root/dev/myapp/
RUN mkdir -p $APP_HOME/src/main/java
WORKDIR $APP_HOME

# download dependencies
COPY build.gradle.kts gradlew gradlew.bat $APP_HOME
COPY gradle $APP_HOME/gradle
RUN ./gradlew build
####### build jar
COPY src .
RUN ./gradlew build


########## Run app
FROM openjdk:11-jre-slim
COPY --from=BUILD_IMAGE /root/dev/myapp/build/libs/teamup-telegram-bot-*-all.jar /teamup-telegram-bot.jar
EXPOSE 8080
CMD ["java","-jar","teamup-telegram-bot.jar"]