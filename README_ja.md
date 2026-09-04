# 🌿 astah* PlantUML Plugin

📘 このREADMEの[英語版はこちら](./README.md)です。

[![GitHub release (latest by date)](https://img.shields.io/github/v/release/ChangeVision/astah-plantuml-plugin)](https://github.com/ChangeVision/astah-plantuml-plugin/releases)
[![GitHub last commit](https://img.shields.io/github/last-commit/ChangeVision/astah-plantuml-plugin)](https://github.com/ChangeVision/astah-plantuml-plugin/commits/)
[![GitHub issues](https://img.shields.io/github/issues/ChangeVision/astah-plantuml-plugin)](https://github.com/ChangeVision/astah-plantuml-plugin/issues)
[![GitHub stars](https://img.shields.io/github/stars/ChangeVision/astah-plantuml-plugin)](https://github.com/ChangeVision/astah-plantuml-plugin/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/ChangeVision/astah-plantuml-plugin)](https://github.com/ChangeVision/astah-plantuml-plugin/network)
[![License: MIT](https://img.shields.io/badge/license-MIT-green.svg)](https://opensource.org/licenses/MIT)

## 📝 概要

このプラグインは、[PlantUMLの図](https://plantuml.com/)と[astah](https://astah.change-vision.com)の間で相互変換を行います。

- PlantUMLの図をastah professionalにインポート
- astahの図をPlantUMLのテキストとしてエクスポート

## 💻 対象環境

- [astah* professional](https://astah.change-vision.com/ja/product/astah-professional.html), [astah* UML](https://astah.change-vision.com/ja/product/astah-uml.html) 10.0以上
- [PlantUML動作環境](https://plantuml.com/starting)
   - Windowsは、PlantUMLのライブラリにGraphvizも同梱されるようになったためPlantUML及びGraphvizインストールは不要です。 うまく動作しない場合は、 PlantUMLの[インストール](https://plantuml.com/starting)を試してください。
   - Windows以外の方は、クラス図、ステートマシン図の変換には、[Graphviz](https://plantuml.com/graphviz-dot)のインストールが必要です。

## 📦 インストール方法

1. [Release](https://github.com/ChangeVision/astah-plantuml-plugin/releases) から最新のjarファイルをダウンロードする。
2. astahを起動し、ダウンロードしたjarファイルをドラッグ＆ドロップする。
3. astahを再起動し、新規プロジェクトを作成する。拡張ビュー(astahウィンドウ右下のペイン)に「PlantUML View」が表示されているか確認する。

<img src="https://github.com/ChangeVision/astah-plantuml-plugin/blob/images/img/PlantUML-plugin-for-Astah.png?raw=true" width="600">

## ▶️ 利用方法

プラグインをインストールすると、拡張ビューに「PlantUML View」タブが追加され、左側にPlantUMLエディタ、右側にプレビューが表示されます。

![snapshot](https://github.com/ChangeVision/astah-plantuml-plugin/blob/images/img/snapshot.png?raw=true)

- 「▲toAstah」ボタンでPlantUMLのテキストからastahに図・モデルを生成します
- 「▼toPlant」ボタンで現在のastahの図をPlantUML形式に変換し、プレビューします
- 構文チェックは随時実行され、下部にエラーが表示されます
- Ctrl + マウスホイールでエディタ・プレビューともに拡大・縮小できます

---

### 🔄 変換の仕様と注意事項

#### PlantUMLからastahへの変換(▲toAstah)

PlantUMLからastahに変換する際、図が毎回新しく生成されます。
エディタで開かれている図と、PlantUMLコードから生成される図の種類が一致しており、かつ図要素が存在しない場合には、その図に要素を追加します。

複数のPlantUMLの図(@startumlから@enduml)がコード内にあった場合は、それぞれ別のastahの図として生成されます。

#### astahからPlantUMLへの変換(▼toPlant)

astahからPlantUMLへの変換はすべて再生成され、マージは行いません。

---

## ✅ 対応状況

### 📌 対応項目

- PlantUML → astah変換
- astah → PlantUML変換
- PlantUML エディタ
  - 逐次バリデーション、エラー表示
  - 拡大、縮小
- PlantUML プレビュー
  - 逐次プレビュー表示
  - 拡大、縮小
- 共通
  - ノート(対応する記法・接続先の要素は図によって異なります)
  - ハイパーリンク(`[[url]]`、ツールチップ・ラベル付きを含む)
  - スタイル(一部の図の図要素の色のみ)
  - 複数の図(@startuml〜@enduml)の一括変換(ToAstah)
- クラス図（オブジェクト図）
  - クラス、インタフェース、抽象クラス、列挙型(リテラルの値を含む)
  - ステレオタイプ
  - 属性、操作(可視性、static、abstract、初期値)
  - タグ付き値(TaggedValue、クラス・属性・操作)
  - テンプレートパラメータ(総称クラス)
  - 関連、継承、実現、依存、関連ラベル
  - 集約、コンポジション、誘導可能性
  - 多重度とラベルの同時記述
  - 関連クラス
  - パッケージ
  - オブジェクト、リンク
- シーケンス図
  - 分類子：participant, actor, boundary, control, entity
    (database, collections, queueはparticipantとして表示)
  - メッセージ：同期、非同期、リターン、Create、Destroy
  - メッセージラベル(引数、返り値、ガード条件)
  - 分類子のクラス読み込み
  - 複合フラグメント(一部対応)
  - 活性区間
- ステートマシン図
  - 初期状態、終了状態、状態
  - トリガー、ガード
  - 状態のネスト
  - アクション : entry, do, exit
  - 内部遷移
  - ステレオタイプ
  - 疑似状態(一部対応)
  - 遷移
- アクティビティ図
  - 開始、終了、アクション
  - コントロールフロー
  - ジョインノード、フォークノード
  - デシジョンノード、マージノード
  - オブジェクトノード
  - パーティション（シンプルなケースのみ）
  - ループ(repeat)
  - レガシー構文の読み込み(ToAstah)
- ユースケース図
  - ユースケース、アクター
  - 関連、拡張、包含、汎化
  - システム境界
  - パッケージ
- 合成構造図
  - コンポーネント、クラス
  - ポート、パート
  - ネストした構造
  - コネクタ、依存、実現、継承
- コンポーネント図
  - コンポーネント、インタフェース(ToAstahのみ。astahのクラス図として生成されます)

---

### 🚧 未対応項目

以下は主な未対応要素(一部)です
- 共通
  - スタイル(色以外)
  - 複数の図の一括出力(ToPlant)
- クラス図
  - ネームスペース(ToPlant)
  - エンティティ
  - ネストクラス
- シーケンス図
  - メッセージ番号
  - 出現・消失メッセージ(ToPlant)
  - 時間制約・持続時間制約
  - 相互作用の利用(ToAstah)
- ステートマシン図
  - 領域
  - 入場点、退場点(ToAstah)
  - 終了(terminate)
- アクティビティ図
  - レガシー構文の出力(ToPlant)
  - 3分岐以上の条件分岐の出力(ToPlant)
  - while構文、switch構文の出力(ToPlant)
  - 複雑なパーティション
  - ピン
- 合成構造図
  - インタフェース(ToPlant)
- コンポーネント図
  - コンポーネント記法での出力(ToPlant。クラス図の`class X <<component>>`として出力されます)

## 📄 ライセンス

本プラグインは **[MIT ライセンス](./LICENSE)** のもとで配布されています。

使用しているオープンソースライブラリは以下のとおりです。

- [PlantUML](https://plantuml.com/)（MIT ライセンス版の `plantuml-mit` を同梱しています）
- [RSyntaxTextArea](https://github.com/bobbylight/RSyntaxTextArea)（[BSD ライセンス](https://github.com/bobbylight/RSyntaxTextArea/blob/master/LICENSE.txt)）

---

### プラグインの動作と生成物について

本プラグインでは、PlantUMLで記述された図をastah上で表示・変換する際に、一時ファイルを内部的に使用します。  
ただし、図の画像やテキストファイルをユーザーが直接保存・エクスポートする機能は提供していません。

また、PlantUMLを用いて生成される図の内容（テキストや視覚的表現）は [PlantUML FAQ](https://plantuml.com/faq) に基づき、 **ユーザー自身の成果物** と見なされ、 **ライセンス制約の対象とはならず、自由に利用できます** 。

---

### PlantUMLのライセンスについて

PlantUMLはGPL、LGPL、MIT、Apacheなど複数のライセンスで配布されています。本プラグインはMITライセンス版を同梱しています。詳細は [PlantUML のライセンス案内](https://plantuml.com/license) をご参照ください。

