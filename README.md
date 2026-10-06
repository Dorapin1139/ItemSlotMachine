# ItemSlotMachine
[![Paper 1.21.11](https://img.shields.io/badge/Paper-1.21.11-brightgreen.svg)](https://fill-ui.papermc.io/projects/paper/version/1.21.11)
[![GitHub release](https://img.shields.io/github/release/gorogoro-space/ItemSlotMachine.svg)](https://github.com/gorogoro-space/ItemSlotMachine/releases)
[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/gorogoro-space/ItemSlotMachine/issues)
[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://github.com/gorogoro-space/ItemSlotMachine/blob/main/LICENSE.txt)

額縁をリールにしたスロットマシンをサーバーに設置できる Paper 用プラグインです。
スピンのたびにジャックポットが積み上がっていきます。

**このリポジトリはフォークです。** 元のソースコードは [DarkBlade12](https://github.com/DarkBlade12/) さんの [ItemSlotMachine](https://github.com/DarkBlade12/ItemSlotMachine/tree/master) です。
フォーク元からの主な変更点は次のとおりです。

- Paper 1.21.11 / Java 21 に対応し、ビルドを Maven から Gradle に変更
- bStats による統計情報の送信と、CurseForge への更新チェックを削除(外部と通信しません)
- 日本語のメッセージ(`ja-JP`)を同梱

## 主な機能

- スロットマシンをいくつでも設置・管理できる
- スロットマシンの形(デザイン)を自分で作れる(標準のデザインを内蔵)
- スロットマシンごとに、絵柄・当選確率・ポット・効果音・当たりの組み合わせなどを設定できる
- スロットマシンとプレイヤーの統計を表示できる
- 看板でコインショップを作れる
- プラグインのメッセージをすべて変更できる

# Requirements
- Paper 1.21.11
- Java 21
- [Vault](https://www.spigotmc.org/resources/vault.34315/) と経済プラグイン(任意)。ジャックポット、コインの購入(`/coin buy`、コインショップ)に必要です

## ビルド

JDK 21 が必要です。Gradle はラッパー(`gradlew`)が自動でダウンロードするので、別途インストールする必要はありません。

```
gradlew.bat clean build
```

(macOS / Linux では `./gradlew clean build`)

## IntelliJ IDEA でのビルド手順

本プロジェクトはビルドツールに Gradle を使用しています。
IntelliJ IDEA 上で正しくプラグイン（JARファイル）を生成するには、以下の手順を実行してください。
「ビルド → アーティファクトのビルド」はクラスファイルが入らないことがあるので使わないでください。

### 🛠 ビルド手順

1. IntelliJ IDEA の画面右端にある **「Gradle」タブ** をクリックして開きます。
2. プロジェクト名（ItemSlotMachine）を展開し、 **`Tasks`** ツリーを開きます。
3. リスト内にある **`clean`** をダブルクリックして実行します（古いビルドキャッシュを削除します）。
4. 続けてリスト内にある **`build`** をダブルクリックして実行します。

### 📦 生成されたファイルの場所
ビルドが成功すると、プロジェクトのルート直下に `build/libs` フォルダが作成（または更新）され、その中に中身の詰まった正しい JAR ファイルが生成されます。

* **生成先:** `build/libs/ItemSlotMachine-2.0.6.jar`

この JAR ファイルを Minecraft サーバーの `plugins` フォルダに配置してください。

## 使い方

### スロットマシンを設置する

1. `/slot build default` を実行すると、立っている位置と向いている方向に合わせて、標準のデザインでスロットマシンが建ちます(名前を省略すると `slot1` のように自動で付きます)。
2. 設置すると、`plugins/ItemSlotMachine/slot machines/<名前>.yml` にそのスロットマシンの設定ファイルができます。必要に応じて編集し、`/slot reload <名前>` で反映します。

### 遊ぶ

1. コインを手に入れます(コインショップの看板、`/coin buy`、`/coin give` など)。コインは `config.yml` の `coin.type`(既定は金塊)に、専用の名前と説明文が付いたアイテムです。
   - 2.0.1 以前に手に入れたコインもそのまま使えます。ただし、2.0.2 以降に手に入れたコインとはインベントリで重なりません(アイテム名の内部の形式が変わったため)。
2. コインを手に持って、スロットマシンのジュークボックスを右クリックするとスピンが始まります。
   - コインは手に持っているスタックからだけ減ります。手に持っている枚数が `coin-amount` に足りないと、ほかのスロットにコインがあっても回せません。
3. `reel-stop` を 0 にしたスロットマシンでは、リールの額縁を左クリックして止めます。
4. 当たりの組み合わせがそろうと、ポットの中身などがもらえます。

### デザインを作る

1. `/design wand` で範囲選択用の杖をもらいます(2.0.1 以前にもらった杖は反応しないので、もらい直してください)。
2. 杖で左クリックした場所が 1 つ目、右クリックした場所が 2 つ目の角になります。範囲内には、**額縁 3 つ**(リール)、**看板**(ポットの表示)、**ジュークボックス**(スロット本体)が必要です。
3. `/design create [名前]` で、選択した範囲をデザインとして保存します。

### コインショップを作る

看板の 1 行目に `[CoinShop]` と書くと、コインショップの看板になります(権限 `itemslotmachine.shop.create` が必要)。

- 看板を見ながらホットバーをスクロールすると、買う枚数を 1 枚ずつ(スニーク中は 10 枚ずつ)変えられます(1〜100 枚)。
- 看板を右クリックすると購入します。購入には権限は不要ですが、Vault が必要です。

## 設定ファイル

設定ファイルは `plugins/ItemSlotMachine/` にできます。

| ファイル | 内容 |
|---|---|
| `config.yml` | プラグイン全体の設定 |
| `template.yml` | スロットマシンを新しく設置するときに使う設定の雛形 |
| `slot machines/<名前>.yml` | スロットマシンごとの設定(`template.yml` の写し) |
| `money-pots/<グループ名>.json` | 共有しているジャックポット。`money-pot.group` を付けた機械だけ使う |
| `designs/<名前>.json` | 作成したデザイン。標準のデザイン `default` はプラグインに内蔵 |
| `statistics/slot machine/`、`statistics/player/` | スロットマシンとプレイヤーの統計 |
| `messages_<言語タグ>.json` | メッセージ。`&` で色を付けられます |

`config.yml` は `/slot reload` で、スロットマシンの設定は `/slot reload <名前>` で、デザインは `/design reload [名前]` で再読み込みします。

### config.yml

| 項目 | 既定値 | 説明 |
|---|---|---|
| `debug-mode-enabled` | `false` | `true` にすると、エラーの詳細(スタックトレース)をコンソールに出します |
| `language-tag` | `'en-US'` | 使うメッセージファイル(`messages_<タグ>.json`)。同梱は `en-US`(英語)、`de-DE`(ドイツ語)、`ja-JP`(日本語) |
| `design.name-pattern` | `'design{0}'` | `/design create` で名前を省略したときの名前。`{0}` に番号が入ります |
| `design.space-check.enabled` | `true` | スロットマシンを建てる範囲にブロックがあるとき、建てるのを止めます。`false` にすると範囲内のブロックを**すべて置き換えます** |
| `design.space-check.ignored-types` | `['snow', 'short_grass', ...]` | 上のチェックで無視するブロック(雪や草など) |
| `slot-machine.name-pattern` | `'slot{0}'` | `/slot build` で名前を省略したときの名前。`{0}` に番号が入ります |
| `slot-machine.use-limit` | `1` | 1 人が同時に使えるスロットマシンの数。1 未満にすると無制限 |
| `coin.type` | `'gold_nugget'` | コインにするアイテム |
| `coin.use-common-item` | `false` | `true` にすると、専用の名前や説明文がない普通のアイテムもコインとして使えます |
| `coin.price` | `100.0` | コイン 1 枚の値段(Vault の通貨) |

### スロットマシンの設定(template.yml / slot machines/&lt;名前&gt;.yml)

| 項目 | 既定値 | 説明 |
|---|---|---|
| `coin-amount` | `1` | 1 回のスピンに必要なコインの枚数(1 以上) |
| `symbol-types` | `['apple-5', 'melon_slice-8', ...]` | リール(額縁)に出る絵柄。2 種類以上。`apple-5` の `5` は出やすさ。重みが全部同じ、または省略したときは等確率。三つ揃いの内訳は、重みを 3 回かけた割合 |
| `allow-creative` | `true` | クリエイティブモードでも遊べるか。遊べる場合、コインは不要です |
| `launch-fireworks` | `true` | 当たったときに花火を打ち上げるか |
| `individual-permission` | `false` | `true` にすると、遊ぶのに `itemslotmachine.slot.use.<名前>` が必要になります(`false` なら `itemslotmachine.slot.use`) |
| `reel-stop` | `20` | リールが自動で止まるまでの回転数。0 にすると、プレイヤーがリールを左クリックして止めます |
| `reel-delay` | `[0, 5, 10]` | 止めたあと、各リールが余分に回る回数 |
| `anticipate` | `true` | 決まった出目の左二つが同じ絵柄なら、最後のリールだけ余分に回す。確率は変わらない。キーが無い既存ファイルは `false` |
| `anticipate-spins` | `8` | 最後のリールを余分に回す回数。0〜40。省略時は 8 で、約 2 秒です |
| `winning-chance` | `8` | 何かが三つ揃う確率(%)。0 にすると完全にランダム。どの絵柄が揃うかは `symbol-types` の重み |
| `triple-pays-pot` | `false` | `false` だと、下のコンボに書いたものだけを渡す。キーが無い既存ファイルは `true` で、三つ揃いはジャックポットとアイテムのポットを両方渡す |
| `lock-time` | (無効) | 止まったあと、遊んだ人以外が使えない秒数。0 またはコメントアウトで無効 |
| `win-commands` | `[]` | 当たりのたびにコンソールが実行するコマンド。雛形では何も実行しません。`<user>`、`<money>`、`<currency>`、`<item_amount>`、`<items>`、`<slot_machine>` が使えます |
| `sounds.spin` / `win` / `lose` | | 回転中・当たり・はずれの効果音。書式は `<効果音名>-<音量>-<ピッチ>[-<true/false>]`。最後を `false` にすると遊んでいる人にだけ聞こえます(省略時は周りの人にも聞こえます) |
| `money-pot.enabled` | `true` | ジャックポットを使うか(Vault が必要)。ジャックポットとアイテムのポットのどちらかは有効にしてください |
| `money-pot.default` | `10000.0` | ジャックポットの初期額(当たって空になったあともこの額に戻ります)。金リンゴだけが受け取ります |
| `money-pot.raise` | `50.0` | 1 回のスピンでジャックポットに足される額 |
| `money-pot.house-cut` | `0.0` | 払い出すときに胴元が取る割合(%)。0 またはコメントアウトで無効。雛形は 0 なので、コンボの金額がそのまま手取り |
| `money-pot.group` | (なし) | 同じ名前の機械で、ジャックポットを一つにする。アイテムのポットは機械ごとのまま。英数字、`_`、`-` だけで 32 文字まで。省略すると、その機械だけのジャックポット |
| `item-pot.enabled` | `false` | アイテムのポットを使うか。雛形は無効で、景品のアイテムはコンボの券だけ |
| `item-pot.default` | `[]` | ポットの初期の中身。書式は `<アイテム名>[-<個数>]`。`coin` と書くとコインになります |
| `item-pot.raise` | `[]` | 1 回のスピンでポットに足されるアイテム |
| `combos.<名前>.pattern` | | 当たりになる絵柄の並び(3 つ)。`*` はどの絵柄にも一致します |
| `combos.<名前>.money` | | この並びのときに渡す定額。ジャックポットからは引かない。ジャックポットの `house-cut` は、この定額と割合払いにも掛かる |
| `combos.<名前>.items` | | この並びのときに渡すアイテム。書式は `<アイテム名>[-<個数>]`。ポットからは引かない |
| `capsule-ticket-name` | `infernal` | 券を使えるカプセルトイの登録名。設置するときに看板の 2 行目へ書いた名前です。英数字と `_` だけ。省略したときは `infernal`。別の看板では使えず、番号は残る |
| `combos.<名前>.capsule-tickets` | | この並びのときに渡すコード付き券の枚数。1 から 64。当たったときに 1 枚ずつ作る。`actions` には書かない |
| `combos.<名前>.actions` | | 当たったときの動作。`money`、`items`、`capsule-tickets` のあとに実行する。どれか一つは必要 |

当たりの動作(`actions`)には次のものがあります。

| 動作 | 内容 |
|---|---|
| `MULTIPLY_MONEY_POT:<倍率>` / `RAISE_MONEY_POT:<額>` / `PAY_OUT_MONEY_POT` | ジャックポットを倍にする / 増やす / 払い出す |
| `PAY_OUT_MONEY_POT_FRACTION:<割合>` | ジャックポットのその割合だけ渡して、残りはジャックポットに残す。`0` より大きく `1` 以下。`1` は 0 にするだけで、初期額には戻さない |
| `MULTIPLY_ITEM_POT:<倍率>` / `RAISE_ITEM_POT:<アイテム>` / `PAY_OUT_ITEM_POT` | アイテムのポットを倍にする / 増やす / 払い出す |
| `PAY_OUT_MONEY:<額>` / `PAY_OUT_ITEMS:<アイテム>` | ジャックポットやアイテムのポットとは別にお金 / アイテムを渡す |
| `EXECUTE_COMMAND:<コマンド>` | コンソールからコマンドを実行する。`<user>`、`<money>`、`<currency>`、`<item_amount>`、`<items>`、`<slot_machine>` が使えます |

雛形の配当は次のとおりです。コイン 1 枚の値段は 100 で、お金の `house-cut` は 0 なので、定額はそのまま手取りです。何かが揃う確率は 8% で、約 13 回に 1 回です。どの絵柄が揃うかは、重みを 3 回かけた割合で分けます（ベリー 10³=1000、メロン 8³=512、ニンジン 7³=343、リンゴ 5³=125、金リンゴ 4³=64、合計 2044）。アイテムは CapsuleToy のコード付き券です。当たったときに 1 枚ずつ作り、それぞれ別の番号が付きます。名前と説明の 2 行は、CapsuleToy の `ticket-display-name` と `ticket-lore1`、`ticket-lore2` を使います。3 行目が番号、4 行目が `capsule-ticket-name` です。その名前の看板でしか使えません。

| 揃い | 重み | 当たりのうち | およそ | 手取り | 券 |
|---|---:|---:|---:|---:|---|
| ベリー | 10 | 48.9% | 26 回に 1 回 | 200 | なし |
| メロン | 8 | 25.0% | 50 回に 1 回 | 800 | なし |
| ニンジン | 7 | 16.8% | 74 回に 1 回 | 2000 | 1 枚 |
| リンゴ | 5 | 6.1% | 204 回に 1 回 | 5000 | 2 枚 |
| 金リンゴ | 4 | 3.1% | 399 回に 1 回 | ジャックポット | 3 枚 |

金リンゴが揃ったときだけ、ジャックポットをもらえます。最初は 10000 で、1 回回すごとに 50 増えます。およそ 400 回で 3 万前後です。差し引く割合は 0 なので、看板の金額がそのままもらえます。ほかの揃いはジャックポットを減らしません。アイテムのポットは使いません。券は重なりません。持ち物に入りきらない券は、1 枚ずつ足元に落ちます。CapsuleToy が無いときは券を渡さず、記録に残します。

花火とレベルアップの音は、上の表の当たりが出たときだけです。左の二つが同じ絵柄なら、最後のリールが約 2 秒長く回ります。三つ目が違うはずれでも、左が揃っていれば長く回ります。チャットは回した人だけに出て、「おめでとうございます。○○を獲得しました。」と表示します。

一人が金リンゴまで回し続けた場合、コイン代 100 に対して戻るお金はおよそ 1.5 倍です。券は別で、およそ 33 回に 1 枚です。

既にサーバーにある `plugins/ItemSlotMachine/template.yml` は、プラグインを更新しても書き換わりません。このファイルが無いときだけ、jar の中の雛形が書き出されます。新しい配当で機械を建てるときは、サーバーを止めて、jar の中の `template.yml` を `plugins/ItemSlotMachine/template.yml` に差し替えてください。差し替えたあとに建てた機械が、上の表になります。

設置済みの機械の yml も書き換わりません。この表にするときは、その機械の yml を雛形に合わせて書き、`/slot reload <名前>` をします。項目が無い既存ファイルは、三つ揃いでジャックポットとアイテムのポットを両方渡し、絵柄は同じ確率、最後のリールを長く回す演出なしのままです。重みを書かない機械の抽選も変わりません。

`symbol-types` に同じ絵柄を重ねるなど、設定が読めない機械は読み込まれません。その間は保護が無く、ブロックを壊したり額縁のアイテムを取ったりできます。`/slot reload` をすると、読めなかった機械の名前と理由をゲーム内に出します。ファイルを直してから、もう一度 `/slot reload` か `/slot reload <名前>` をすると読み込めます。

共有ジャックポットは、最初にファイルが無いとき、同じグループの機械のうち一番高い金額で `money-pots/<名前>.json` を作ります。ファイルがあるときは、そちらが金額の正本です。各機械の json に残っている金額は使いません。差し引く割合、初期額、1 回で増える額は、当たった機械、または回した機械の設定を使います。`/slot money` は、その機械が属する共有ジャックポットを操作します。

## コマンド

**コマンドの権限はすべて既定で OP だけが持っています(`plugin.yml` に登録していない権限は、Bukkit では OP 専用になるため)。**
OP 以外のプレイヤーに使わせるには、LuckPerms などの権限プラグインで権限を与えてください。
どのコマンドも `help [ページ]` は権限なしで使えます(例: `/slot help`)。

### /slot(別名 /slotmachine、/sm)

| コマンド | 説明 | 権限(既定は OP のみ) |
|---|---|---|
| `/slot build <デザイン> [名前]` | 立っている位置にスロットマシンを建てる(プレイヤーのみ) | `itemslotmachine.command.slot.build` |
| `/slot remove <名前>` | スロットマシンを撤去する | `itemslotmachine.command.slot.remove` |
| `/slot list` | スロットマシンの一覧と、回転中かどうかを表示する | `itemslotmachine.command.slot.list` |
| `/slot tp <名前>` | スロットマシンの近くにテレポートする(プレイヤーのみ) | `itemslotmachine.command.slot.tp` |
| `/slot rebuild <名前>` | スロットマシンを建て直す | `itemslotmachine.command.slot.rebuild` |
| `/slot move <名前> <ブロック数>` | 向いている方向へスロットマシンを動かす(プレイヤーのみ) | `itemslotmachine.command.slot.move` |
| `/slot stop <名前>` | 回転中のスロットマシンを止める | `itemslotmachine.command.slot.stop` |
| `/slot money <名前> <clear/deposit/withdraw/set> [default/額]` | ジャックポットを空にする / 入れる / 引き出す / 設定する | `itemslotmachine.command.slot.money` |
| `/slot item <名前> <clear/add/set> [default/hand/アイテム]` | アイテムのポットを空にする / 追加する / 設定する。`hand` は手に持っているアイテム | `itemslotmachine.command.slot.item` |
| `/slot reload [名前]` | 名前なしでプラグイン全体、名前ありでそのスロットマシンの設定を再読み込みする。読めなかった機械があれば名前と理由を表示する | `itemslotmachine.command.slot.reload` |

### /design(別名 /slotdesign、/sd)

| コマンド | 説明 | 権限(既定は OP のみ) |
|---|---|---|
| `/design wand` | 範囲選択用の杖をもらう(プレイヤーのみ) | `itemslotmachine.command.design.wand` |
| `/design create [名前]` | 杖で選んだ範囲をデザインとして保存する(プレイヤーのみ) | `itemslotmachine.command.design.create` |
| `/design remove <名前>` | デザインを削除する(`default` は削除できません) | `itemslotmachine.command.design.remove` |
| `/design list` | デザインの一覧を表示する | `itemslotmachine.command.design.list` |
| `/design invert <名前>` | リール(額縁)の順番を逆にする(`default` は不可) | `itemslotmachine.command.design.invert` |
| `/design reload [名前]` | デザインを再読み込みする | `itemslotmachine.command.design.reload` |

### /coin(別名 /slotcoin、/sc)

| コマンド | 説明 | 権限(既定は OP のみ) |
|---|---|---|
| `/coin buy <枚数>` | コインを買う(プレイヤーのみ、Vault が必要) | `itemslotmachine.command.coin.buy` |
| `/coin give <プレイヤー> <枚数>` | コインを渡す | `itemslotmachine.command.coin.give` |

### /statistic(別名 /slotstatistic、/stat、/stats)

| コマンド | 説明 | 権限(既定は OP のみ) |
|---|---|---|
| `/statistic show <slot/player> <名前>` | スロットマシンまたはプレイヤーの統計を表示する | `itemslotmachine.command.statistic.show` |
| `/statistic top <slot/player> <項目>` | 項目ごとのランキングを表示する | `itemslotmachine.command.statistic.top` |
| `/statistic reset <slot/player> <名前>` | 統計をリセットする | `itemslotmachine.command.statistic.reset` |

`top` の項目は `total spins`、`won spins`、`lost spins`、`spent coins`、`won money`、`won items` です(スロットマシンで使えるのは最初の 3 つ)。どの言語でもこの英語名で指定でき、`language-tag` が `ja-JP` のときは `総スピン数`、`当たり回数`、`はずれ回数`、`使ったコイン`、`獲得したお金`、`獲得したアイテム` でも指定できます。

## 権限

コマンド以外の権限です。`itemslotmachine.slot.use` だけは全員が持っており、それ以外は既定では OP だけが持っています。

| 権限 | 内容 |
|---|---|
| `itemslotmachine.slot.use` | スロットマシンで遊ぶ。**既定で全員が持っています**(`plugin.yml` で `default: true`)。遊ばせたくないプレイヤーやグループには、権限プラグインで false にしてください |
| `itemslotmachine.slot.use.<名前>` | `individual-permission: true` のスロットマシンで遊ぶ(既定は OP のみ) |
| `itemslotmachine.slot.modify.<名前>` | スロットマシンのブロックや額縁を壊す・変える(持っていない人からは保護されます)。ポットを表示する看板は、文字が自動で書き換わるので、この権限があっても編集や染色はできません |
| `itemslotmachine.slot.inspect` | コイン以外のアイテム(ブロック以外)や素手でジュークボックスを右クリックし、スロットマシンの名前を確認する |
| `itemslotmachine.shop.create` | コインショップの看板を作る |

まとめて与えるには、次のワイルドカードが使えます。

| 権限 | 含まれるもの |
|---|---|
| `itemslotmachine.*` | すべて |
| `itemslotmachine.command.*` | すべてのコマンド |
| `itemslotmachine.command.slot.*` / `design.*` / `coin.*` / `statistic.*` | それぞれのコマンドすべて |
| `itemslotmachine.slot.*` | 遊ぶ・変更・確認のすべて |
| `itemslotmachine.slot.use.*` | すべてのスロットマシンで遊ぶ |
| `itemslotmachine.slot.modify.*` | すべてのスロットマシンを変更する |

## 開発(IntelliJ IDEA で Claude Code を使う)

このリポジトリには、AI コーディングツール [Claude Code](https://docs.claude.com/ja/docs/claude-code/overview) 向けの作業ルールを書いた `CLAUDE.md` があります。IntelliJ IDEA で使う手順は次のとおりです。

1. Claude の有料プラン(Pro / Max)か、Anthropic API のアカウントを用意します。
2. Windows では、先に [Git for Windows](https://git-scm.com/downloads/win) をインストールします。
3. Claude Code 本体をインストールします。Windows では PowerShell で次を実行します。
   ```
   irm https://claude.ai/install.ps1 | iex
   ```
4. IntelliJ IDEA の「設定 → プラグイン → Marketplace」で「Claude Code」を検索してインストールし、IDE を再起動します。検索結果には Anthropic 以外が作った似た名前のプラグインも表示されます。プラグイン名の下に書かれた提供元が「Anthropic」になっているものを選んでください。
5. プロジェクトを開いて、ターミナルで `claude` を実行します(`Ctrl+Esc` でも起動できます)。初回はブラウザでログインします。

起動すると `CLAUDE.md` が自動で読み込まれ、このプロジェクトの設計方針に沿って作業します。

## ライセンス

GPL-3.0(`LICENSE.txt`)。フォーク元の著作権は DarkBlade12 さんにあります。

## kubotan へのメモ

リポジトリを新規作成したら、**必ず Watch を設定すること**(忘れない!)。
GitHub の自動 Watch 機能は 2025 年 5 月に廃止されたため、設定しないと他の人が立てた issue や PR の通知が届かない。

1. リポジトリのページ右上の **「Watch」** を押す
2. **「Custom」** を選び、**Issues** と **Pull requests** にチェックを入れる
