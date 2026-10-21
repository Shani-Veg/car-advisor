# Render.com web service (free plan: 512 MB, shared CPU). Built from this repo by render.yaml; every push to main
# deploys again. Secrets come from the Render dashboard as environment variables: GEMINI_API_KEY, ADVISOR_REP_PASSWORD,
# and for permanent storage SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD.

FROM eclipse-temurin:17-jdk AS build
WORKDIR /src
COPY . .
# The tests run locally (./mvnw test); the deploy build only packages.
RUN chmod +x mvnw && ./mvnw -B -q package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /src/target/car-advisor-0.0.1-SNAPSHOT.jar app.jar
COPY --from=build /src/data data
# Small heap and a single-threaded GC to fit 512 MB; C1 only, for a faster start on a shared CPU.
# The database: PostgreSQL when SPRING_DATASOURCE_URL is set in the Render dashboard, so cases and rep questions
# survive restarts. Without it, an H2 file in /tmp, and the public demo starts clean after every restart.
ENV JAVA_TOOL_OPTIONS="-Xmx256m -Xss512k -XX:MaxMetaspaceSize=150m -XX:ReservedCodeCacheSize=48m -XX:+UseSerialGC -XX:TieredStopAtLevel=1"
EXPOSE 10000
CMD ["sh", "-c", "exec java -jar app.jar --server.port=${PORT:-10000} --server.tomcat.threads.max=20 --advisor.data-dir=/app/data --spring.datasource.url=${SPRING_DATASOURCE_URL:-jdbc:h2:file:/tmp/db/car-advisor} --advisor.public-demo=true"]
