FROM clojure:temurin-26-lein

WORKDIR /app

COPY project.clj /app/

RUN lein deps

CMD ["lein", "run"]
