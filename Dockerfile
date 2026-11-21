FROM clojure:temurin-25-lein

WORKDIR /app

# ライブラリは、イメージには入れない。docker-compose.yml で /root/.m2 にボリュームを割り当てていて、
# そこに make setup（または make clj/install）で入れる。
CMD ["lein", "run"]
