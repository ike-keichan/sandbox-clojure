# sandbox-clojure

Clojure（Ring、Compojure、MySQL）の技術検証と、プロジェクトのひな形を兼ねたリポジトリ。

Clojure で Web API を作るときの土台を、すぐ動く状態でまとめています。新しいプロジェクトを始めるときのひな形として使うほか、ライブラリの新しい版や、別の構成を試す場所としても使います。

## Description

- [Branch](#branch)
- [Tool Versions](#tool-versions)
- [Getting Started](#getting-started)
- [Command](#command)
- [Documents](#documents)

---

## Branch

目的の違う内容を、ブランチで分けています。

| ブランチ | 内容 | 使いみち |
| --- | --- | --- |
| [`main`](https://github.com/ike-keichan/sandbox-clojure) | 最小の形。API は、動作の確認用の `GET /api/hello` だけ | 新しいプロジェクトの出発点にします |
| [`example/task-api`](https://github.com/ike-keichan/sandbox-clojure/tree/example/task-api) | `main` に、タスクを管理する API を足した見本 | 機能の足し方の手本にします |

`README.md` と、`readme/` の番号つきの文書は、どのブランチでも同じ内容です。`example/` で始まるブランチには、そのブランチの説明（`readme/EXAMPLE_*.md`）が入っています。

---

## Tool Versions

| 役割 | 使っているもの | バージョン |
| --- | --- | --- |
| 言語 | Clojure | 1.12 |
| 実行環境 | Java（Eclipse Temurin） | 25（長期サポート版） |
| ビルドの道具 | Leiningen | 2.13 |
| HTTP の処理 | Ring（サーバーは Jetty） | 1.15 |
| ルーティング | Compojure | 1.7 |
| データベース | MySQL | 8.4（長期サポート版） |
| データベースの操作 | next.jdbc | 1.4 |
| コードの検査 | clj-kondo | 2026.10 |
| コードの整形 | cljfmt | 0.16 |
| テスト | clojure.test、ring-mock | |

Java、Clojure、MySQL は、Docker のコンテナの中で動きます。手元に必要なのは、Docker Desktop、make、Git だけです。

正確なバージョンは、[project.clj](./project.clj)、[Dockerfile](./Dockerfile)、[docker-compose.yml](./docker-compose.yml) に記録しています。

---

## Getting Started

環境構築の手順は、[こちら](./readme/01_GETTING_STARTED.md)。

手順を終えると、<http://localhost:3000/api/hello> で API を呼べるようになります。

---

## Command

主要なコマンドは、以下のとおりです。それ以外のコマンドの詳細は、[こちら](./readme/02_COMMAND.md)。

| コマンド | 内容 |
| --- | --- |
| `make setup` | 環境を作ります（初回だけ） |
| `make up` | コンテナを起動します |
| `make upd` | コンテナを、バックグラウンドで起動します |
| `make stop` | コンテナを止めます |
| `make clj/test` | テストを実行します |
| `make clj/check` | コードを検査します |
| `make clj/fix` | 書式を、自動で直します |
| `make db/migrate` | テーブルを作り直します（データは消えます） |

---

## Documents

| 文書 | 内容 |
| --- | --- |
| [Getting Started](./readme/01_GETTING_STARTED.md) | 環境構築の手順。必要なソフトウェアの用意から、動作の確認、うまくいかないときの対処まで |
| [Command](./readme/02_COMMAND.md) | すべてのコマンドの説明 |
| [Architecture](./readme/03_ARCHITECTURE.md) | コンテナの構成、リクエストの流れ、プログラムの層、ディレクトリ構成、テストの仕組み、設定 |
| [Template](./readme/04_TEMPLATE.md) | ひな形として使う手順。自分のリポジトリの作り方、名前の変え方、機能の足し方 |
| [Development](./readme/05_DEVELOPMENT.md) | このリポジトリ自体を更新するときの決まり。ブランチ、作業の流れ、依存の更新、見本のブランチの保ち方 |
| [CLAUDE.md](./CLAUDE.md) | コードと文書の書き方の細かい決まり。Claude Code に作業を頼むときにも読み込まれます |
