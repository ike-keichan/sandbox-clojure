FROM clojure:openjdk-17-lein

WORKDIR /app

COPY project.clj /app/
COPY src /app/src

RUN lein deps;

CMD ["lein", "run"]
