FROM openjdk:15.0.2-jdk-slim
LABEL org.opencontainers.image.source="https://github.com/tricketynet/BungeeCord"

WORKDIR /usr/app
COPY bootstrap/target/BungeeCord.jar bungeecord.jar
COPY config.yml .

EXPOSE 25577
CMD java -Dlog4j2.formatMsgNoLookups=true -jar bungeecord.jar