FROM gradle:9.6.0-jdk17 AS build
WORKDIR /workspace
COPY . .
RUN gradle --no-daemon :backend:app:installDist

FROM eclipse-temurin:17-jre
RUN useradd --system --create-home --uid 10001 vertice
WORKDIR /opt/vertice
COPY --from=build --chown=vertice:vertice /workspace/backend/app/build/install/app/ ./
USER vertice
ENV HOST=0.0.0.0
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["/opt/vertice/bin/app"]
