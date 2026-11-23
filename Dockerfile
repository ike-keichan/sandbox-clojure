FROM clojure:temurin-25-lein

# コンテナは、手元のパソコンと同じユーザーで動かす（docker-compose.yml の user を参照）。
# どのユーザーでも動くように、ホームディレクトリを、誰でも書き込める場所に用意する。
RUN mkdir -p /home/app/.m2 \
    && chmod -R 777 /home/app

# - LANG: 文字コードを、UTF-8 にする。決めておかないと、エラーの出力（標準エラー出力）で、日本語が「?」になる。
# - HOME: Leiningen が、設定を置く場所。
# - LEIN_JVM_OPTS、JVM_OPTS: Java に、ホームディレクトリの場所を伝える（user.home）。
#   Java は、HOME ではなく、ユーザーの登録の情報から、ホームディレクトリを調べる。
#   手元のユーザーの番号は、コンテナの中では登録されていないので、ここで直接伝える。
#   伝えないと、ライブラリの置き場所（~/.m2）が決まらない。
#   LEIN_JVM_OPTS は Leiningen 自身に、JVM_OPTS は Leiningen が起動するプログラムに渡される。
#   LEIN_JVM_OPTS の前半の2つは、Leiningen が最初から使う設定（起動を速くする）。
ENV LANG=C.UTF-8 \
    HOME=/home/app \
    LEIN_JVM_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1 -Duser.home=/home/app" \
    JVM_OPTS="-Duser.home=/home/app"

WORKDIR /app

# ライブラリは、イメージには入れない。docker-compose.yml で /home/app/.m2 にボリュームを割り当てていて、
# そこに make setup（または make clj/install）で入れる。
CMD ["lein", "run"]
