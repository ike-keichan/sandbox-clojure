FROM clojure:temurin-25-lein

WORKDIR /app

COPY project.clj /app/

RUN lein deps

CMD ["lein", "run"]
