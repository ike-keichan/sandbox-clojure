# Example: table-formatter（表を整形するプログラムの見本）

`example/table-formatter` ブランチの説明です。このブランチは、`main`（最小の形）に、Markdown の表を整形するプログラムを足したものです。API ではなく、コマンドとして実行するプログラムを、同じ開発環境で動かす見本として使います。

リポジトリ全体の説明は [README](../README.md)、各ファイルの役割は [Architecture](./03_ARCHITECTURE.md) を参照してください。

## プログラムの内容

列の幅がそろっていない Markdown の表を受け取り、幅をそろえた表を出力します。

整形の前：

```
| header1 | header2 |
| ------- | --- |
| 1  | 2          |
| 3     | 4 |
```

整形のあと：

```
| header1 | header2 |
| ------- | ------- |
| 1       | 2       |
| 3       | 4       |
```

決まりは、次のとおりです。

- 要素の前後の空白は、取り除きます。
- すべての列を、同じ幅にします。幅は、表の中で、いちばん長い要素の長さです。
- 区切りの行（`---`）は、幅いっぱいの `-` にします。
- 空の要素は、空白で埋めます。

## 試す

環境構築は、[Getting Started](./01_GETTING_STARTED.md) の手順で行います。`git clone` のあとに、このブランチに切り替えてください。

```zsh
$ git switch example/table-formatter
$ make setup
```

`make table/format` で、見本の表（[samples/table.md](../samples/table.md)）を整形します。コンテナを起動しておく必要は、ありません。

```zsh
$ make table/format
| header1 | header2 |
| ------- | ------- |
| 1       | 2       |
| 3       | 4       |
```

※ 実際には、表の前に、実行したコマンドと、コンテナの作成の記録（`Container ... Created`）も表示されます。

自分のファイルを整形するときは、`FILE` で指定します。

```zsh
$ make table/format FILE=<ファイルの場所>
```

表として読めない入力（`|` を1つも含まない、など）を渡すと、エラーを表示して、失敗で終わります。

```zsh
$ make table/format FILE=<表ではないファイル>
エラー：入力フォーマットが違うよ(#°Д°)

make: *** [table/format] Error 1
```

テストは、`main` と同じコマンドで実行します（コンテナを起動した状態で実行します）。

```zsh
$ make upd
$ make clj/test

Ran 7 tests containing 22 assertions.
0 failures, 0 errors.
```

## `main` に足したファイル

| ファイル | 内容 |
| --- | --- |
| `src/table_formatter/core.clj` | プログラムの本体 |
| `test/table_formatter/core_test.clj` | テスト |
| `samples/table.md` | 見本の入力 |
| `samples/table.expected.md` | 見本の入力を整形した結果。自動テストで、実際の出力と比べます |
| `readme/EXAMPLE_TABLE_FORMATTER.md` | この文書 |

## `main` から書き換えたファイル

| ファイル | 書き換えた内容 |
| --- | --- |
| `Makefile` | 最後に、`table/format` を足しました |
| `.github/workflows/test.yml` | プログラムを実行して、出力を `samples/table.expected.md` と比べる手順を足しました |

`main` の API（`GET /api/hello`）と、データベースは、そのまま残しています。このプログラムは、どちらも使いません。

## プログラムの構成

| 関数 | 内容 |
| --- | --- |
| `format-element` | 要素を1つ受け取り、幅をそろえた文字列にします |
| `format-table` | 表の全体を受け取り、整形した文字列を返します |
| `-main` | 入力を読み込み、`format-table` の結果を表示します |

処理の流れは、次のとおりです。

1. 入力の文字列を、`|` で分けます。行の境目は、改行だけの要素になります。
2. 要素の前後の空白を取り除き、いちばん長い要素の長さを調べます。
3. 要素ごとに、幅をそろえた文字列にして、つなげます。

入力は、2つの方法で渡せます。

| 渡し方 | 例 |
| --- | --- |
| 標準入力（`make table/format` が使っている方法） | `lein run -m table-formatter.core < samples/table.md` |
| 引数 | `lein run -m table-formatter.core "| a | b |"` |

`project.clj` の `:main` は、`main` と同じ `api.core`（API のサーバー）のままです。このプログラムは、`-m table-formatter.core` で、名前空間を指定して実行します。

## 注意点

- **幅は、文字の数で数えています。** 日本語のような、画面での幅が広い文字を含む表は、文字の数はそろいますが、見た目はそろいません。
- **すべての列が、同じ幅になります。** 列ごとに幅を変える作りには、していません。
- **このプログラムは、結果を `println` で表示します。** `CLAUDE.md` の「確認用の表示（`println`）を、コミットに残さない」は、API のプログラムについての決まりです。結果を表示することが目的のプログラムなので、ここでは使っています。
- **テストは、関数を直接呼ぶ形で書いています。** `CLAUDE.md` の「テストは、API を呼ぶ形で書く」も、API についての決まりです。
