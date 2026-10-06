# ItemSlotMachine

額縁をリールにしたスロットマシンを設置できる Bukkit 用プラグイン。スピンごとにジャックポットが積み上がる。
**フォーク元**: [DarkBlade12/ItemSlotMachine](https://github.com/DarkBlade12/ItemSlotMachine)(作者 DarkBlade12)
リポジトリ: https://github.com/gorogoro-space/ItemSlotMachine(GPL-3.0、公開リポジトリ)

## 作業の進め方(必ず守ること)

- **設計が確定するまで実装しない。** 機能追加や仕様変更は、まず設計案(何を・なぜ・どう変えるか、影響範囲)を提示し、承認を得てからコードを書く。
- 判断が必要な点は、選択肢を示して質問する。勝手に決めない。
- やり取りは日本語で行う。
- 変更は必要最小限にする。頼まれていないリファクタリングや機能追加はしない(フォークなので、元のコードとの差分を小さく保つ意味もある)。
- 作業後は、変更・追加・削除したファイルの一覧と変更内容を報告する。
- 仕様を変えたら README.md と CLAUDE.md も合わせて更新する。
- `git push` の前には必ず確認を取る。コミットは意味のある単位で分ける。
- リポジトリへの初回の `git push` の前には、「GitHub で Watch 設定(Custom → Issues と Pull requests)はしましたか?」と日本語で確認する。自動 Watch は廃止されており、設定しないと issue や PR の通知が届かない。
- コミットメッセージや PR に `Co-Authored-By: Claude` などの署名を付けない。`.claude/` は Git に入れない(`.git/info/exclude` で除外する)。
- 実装中に設計の抜けや穴に気づいたら、黙って対処せず報告して相談する。
- プルリクエストをチェックするときは、次の観点で確認して報告する。
  - 変更概要(何を・なぜ変えているか)
  - 脆弱性(権限チェックの漏れ、入力値の検証、パスの扱いなど)
  - 安全面(データの破損・消失、再読み込み時の後始末、他プラグインの妨げなど)
  - 性能(TPS など)の低下(高頻度イベントでの重い処理、メインスレッドでの同期 I/O、定期タスクの追加など。「最重要の設計方針」に沿っているか)

## フォークとしての注意

- 既存コードのコメント・ログ・識別子は英語。既存の英語コメントは書き換えない。追加するコメントは日本語でよい。
- プレイヤー向けメッセージは `src/main/resources/messages_<タグ>.json` にある(同梱は `en-US`・`de-DE`・`ja-JP`。同梱する言語は `ItemSlotMachine.java` の `MessageManager` に `Locale` を渡して登録する)。文言を追加・変更するときは、同梱の全言語ファイルにキーを足す。サーバーに既にある言語ファイルは上書きしない。足りないキーは jar 内の同じ言語、無ければ `en-US` から補う。
- パッケージ名 `com.darkblade12.itemslotmachine` などフォーク元の表記は、頼まれない限り変えない。`plugin.yml` は `authors: [DarkBlade12, kubotan, tash087]`、`website` はこのリポジトリの URL にしてある。
- ライセンスは GPL-3.0 なので、フォーク元の著作権表示を消さない。

## 環境

- **Paper 1.21.11 / Java 21 が必須**。依存は `paper-api:1.21.11-R0.1-SNAPSHOT`(compileOnly)
- ビルド: `gradlew.bat clean build`(Windows)。**JDK 21 で実行する**(`JAVA_HOME` を `C:\Program Files\Java\jdk-21` にする。既定の `java` は JDK 27)。「ビルドして」と言われたら常にクリーンビルドする。成果物は `build/libs/`
  - IntelliJ の「アーティファクトのビルド」はクラスファイルが入らないことがある。必ず Gradle でビルドする
- その他の依存: VaultAPI 1.7(compileOnly、jitpack)のみ。jar に同梱するライブラリはない
- バージョンは `gradle.properties` の `version`。`plugin.yml` の `${version}` に `processResources` で埋め込む。`api-version: '1.21.11'`
- commons-lang などの外部ユーティリティは使わず、Java 標準で書く(Paper 1.21.11 に commons-lang 2 はない)。`capitalize` と `unescapeJava`、`ChatColor` の代わりの `translateAlternateColorCodes`・`stripColor` は `util/MessageUtils` にある
- 削除予定(`forRemoval`)の API は使わない。レジストリの要素は `RegistryAccess.registryAccess().getRegistry(RegistryKey.…)` で引く(`Registry.BANNER_PATTERN` や `PatternType.getKey()` なども非推奨)。非推奨(削除予定ではない)の API も使わない(2.0.2 で `-Xlint:all` の警告を 0 件にした)
- softdepend: Vault, Multiverse-Core, Multiworld, PlotMe, MyWorlds, Essentials, CapsuleToy
- パッケージ名は**すべて小文字**のままにする。大文字が混ざると plugin.yml の main と一致せず起動しない
- 動作確認はサーバーを再起動して行う。PlugManX での読み込みは権限やコマンドの登録が不完全になることがある

## 最重要の設計方針

### TPS に影響させない
- 高頻度イベント(PlayerInteractEvent、PlayerMoveEvent、EntityChangeBlockEvent など)は、安い判定(ワールドや座標の整数比較など)を先に行い、対象外なら即座に抜ける
- BlockPhysicsEvent、VehicleMoveEvent など発生頻度が極端に高いイベントは使わない
- メインスレッドでファイルや DB の同期 I/O をしない(起動時・リロード時に一度だけ行う小さなファイルの読み込みは除く)。未読み込みチャンクを判定のために読み込まない
- 設定ファイルは起動時・リロード時に一度だけ解析して保持する。Material などの集合は EnumSet
- 定期タスクは最小限にし、追加するときは頻度と理由を設計案に書く

### 権限
- `plugin.yml` に登録しているのは `itemslotmachine.slot.use`(`default: true`、全員が遊べる)だけ。ほかの権限は未登録のため、Bukkit では OP 専用になる
- 権限は `Permission.java` で親子関係(ワイルドカード)をコードで判定している。権限を追加・変更したら README の「コマンド」「権限」の表も更新する

### 外部と通信しない
- 統計情報の送信(bStats など)や更新チェックなど、外部への通信は入れない(フォーク元にあった bStats と CurseForge の更新チェックは削除済み)

### データの保存
- プラグインフォルダ以外には何も書き込まない
- スロットマシン・デザイン・統計・コインショップの保存形式を変えるときは、既存データの読み込み(移行)を必ず考える。サーバー上に既存のデータがある前提
- 共有ジャックポットは `money-pots/<グループ名>.json`（`{"money": 数値}`）。グループ名は `[A-Za-z0-9_-]{1,32}`。ファイルが無いときは、同じグループで一番高い機械の金額で作る。ファイルがあるときはそれを正本にし、各機械の json に残っている金額は使わない。壊れたファイルは上書きしない（そのグループは機械ごとのポットのまま）
- スロットマシン設定に `triple-pays-pot`、`anticipate`、`symbol-types` の重み、`money-pot.group` が無い既存ファイルは、これまでと同じ動き（三つ揃いはポット全取り、絵柄は等確率、焦らしなし、ポットは機械ごと）
- `template.yml` はファイルが無いときだけ jar から書き出す。既にある雛形は更新しない。新しい配当で機械を建てるには、サーバーを止めて `plugins/ItemSlotMachine/template.yml` を差し替える
- 設定が読めない機械は読み込まず、その間は保護しない。`/slot reload` は読めなかった機械名と理由を実行者に出す。引数なしの `reload()` は、config の再読み込みに成功したら true を返す。読めなかったファイルは `/slot reload <名前>` で読み直せる
- コンボの `capsule-tickets` は 1 から 64。当たったときに CapsuleToy の `createCodedTicket(String)` を反射で呼び、`capsule-ticket-name`（省略時は `infernal`）専用のコード付き券を 1 枚ずつ作る。コンパイル依存にはしない。CapsuleToy が無い、または券を作れないときは券を渡さずログだけ残し、お金の払いは続ける。`actions` の文字列としては書けない。名前の無い `createCodedTicket()` は呼ばない
- アイテム(ポットの中身など)は `util/ItemStackAdapter` で JSON にする。ポーションの種類・追加効果・旗の模様は名前空間付きのキー(`minecraft:swiftness` など)で書き、フォーク元の古い形式(`basePotionData`、列挙名)も読めるようにしてある
- アイテムの名前・説明文・本のページは Component 版 API で扱い、文字列との変換は必ず `MessageUtils.toItemComponent`/`fromItemComponent` を使う(斜体を外す。旧 String 版 API と見た目をそろえるため)。看板は `toSignComponent`/`fromSignComponent`(Paper の看板の String 版 API と同じ変換)。作り方が変わるとアイテムが `isSimilar` で一致しなくなる(2.0.2 で名前の内部構造が変わり、2.0.1 以前のコインは `CoinManager.isCoin` で名前・説明文の文字列を比べて判定している。杖はもらい直しが必要)

### 他プラグインとの関係
- 経済は Vault 経由。Vault がない環境でも起動できる状態を保つ
- 既存の機能(例: GSit の座る操作、看板の click_event によるテレポートなど)を妨げないこと。イベントをキャンセルする範囲は必要最小限にする

## 過去にハマった点(他プロジェクトでの経験)

- `config.getString(path, "")` のように既定値を渡すと、jar 内 config.yml の既定値が参照されない。既定値なしで取得して null を判定すること
- plugin.yml で `default: true` にした権限でも、登録されないと Bukkit は「OP のみ」として扱う。全員向けの機能を権限で縛らない
- 乗客などのエンティティを `remove()` すると内部で降車イベント(`EntityDismountEvent`)が出る。これをキャンセルすると、消えたエンティティが乗ったまま残り、毎 tick 降ろそうとして重くなる

## ファイル構成(src/main/java/com/darkblade12/itemslotmachine/)

- `ItemSlotMachine.java` — メインクラス
- `Settings.java` / `Setting.java` / `Permission.java` — 全体設定と権限ノード
- `plugin/` — プラグインの基盤(`PluginBase`、`Manager`、メッセージ、コマンド基盤、設定基盤、`hook/VaultHook`)
- `command/` — `/slot`(`/sm`)、`/design`(`/sd`)、`/coin`(`/sc`)、`/statistic`(`/stat`)の各サブコマンド
- `slotmachine/` — スロットマシン本体、個別設定、当たりの組み合わせ(`combo/`)、共有ジャックポット(`MoneyPotGroup`)
- `design/` — デザイン(スロットマシンの形)の作成・管理
- `coin/` — コインとコインショップ
- `statistic/` — スロットマシン・プレイヤーの統計
- `reference/` — 向きに依存しない相対座標(デザインの配置用)
- `nameable/`、`util/` — 共通部品
- `src/main/resources/` — `plugin.yml`、`config.yml`、`template.yml`(スロットマシン個別設定の雛形)、`design_default.json`、`messages_*.json`
