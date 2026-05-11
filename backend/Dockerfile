FROM openjdk:11-jdk-slim
WORKDIR /app
COPY target/ecommerce-1.0.0.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-Xmx256m -Xms128m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
