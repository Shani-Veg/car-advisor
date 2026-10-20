# Hugging Face Space (Docker). Builds the advisor from its public GitHub repo, so a new version needs no
# change here: push to GitHub, then Space -> Settings -> Factory rebuild.
# Secrets come from the Space settings as environment variables: GEMINI_API_KEY, ADVISOR_REP_PASSWORD.

FROM eclipse-temurin:17-jdk AS build
RUN apt-get update && apt-get install -y --no-install-recommends git && rm -rf /var/lib/apt/lists/*
WORKDIR /src
RUN git clone --depth 1 https://github.com/Shani-Veg/car-advisor.git .
# The tests run locally (./mvnw test); the Space build only packages.
RUN chmod +x mvnw && ./mvnw -B -q package -DskipTests

FROM eclipse-temurin:17-jre
# Spaces run the container as uid 1000.
WORKDIR /home/user/app
COPY --from=build --chown=1000:1000 /src/target/car-advisor-0.0.1-SNAPSHOT.jar app.jar
COPY --from=build --chown=1000:1000 /src/data data
RUN mkdir -p /tmp/db && chown 1000:1000 /home/user /home/user/app /tmp/db
ENV HOME=/home/user
USER 1000
EXPOSE 7860
CMD ["java", "-Xmx1g", "-jar", "app.jar", "--server.port=7860", "--advisor.data-dir=/home/user/app/data", \
     "--spring.datasource.url=jdbc:h2:file:/tmp/db/car-advisor", "--advisor.public-demo=true"]
