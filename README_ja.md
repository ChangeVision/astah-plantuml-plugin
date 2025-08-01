# 🌿 astah* PlantUML Plugin

📘 このREADMEの[英語版はこちら](./README.md)です。

[![GitHub release (latest by date)](https://img.shields.io/github/v/release/ChangeVision/astah-plantuml-plugin)](https://github.com/ChangeVision/astah-plantuml-plugin/releases)
[![GitHub last commit](https://img.shields.io/github/last-commit/ChangeVision/astah-plantuml-plugin)](https://github.com/ChangeVision/astah-plantuml-plugin/commits/)
[![GitHub issues](https://img.shields.io/github/issues/ChangeVision/astah-plantuml-plugin)](https://github.com/ChangeVision/astah-plantuml-plugin/issues)
[![GitHub stars](https://img.shields.io/github/stars/ChangeVision/astah-plantuml-plugin)](https://github.com/ChangeVision/astah-plantuml-plugin/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/ChangeVision/astah-plantuml-plugin)](https://github.com/ChangeVision/astah-plantuml-plugin/network)
[![License: GPL v3](https://img.shields.io/badge/license-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

## 📝 概要

[astah](https://astah.change-vision.com)と[PlantUML](https://plantuml.com)のモデルの相互変換を行うプラグインです。

## 💻 対象環境

- [astah* professional](https://astah.change-vision.com/ja/product/astah-professional.html), [astah* UML](https://astah.change-vision.com/ja/product/astah-uml.html) 10.0以上
- [PlantUML動作環境](https://plantuml.com/starting)
   - Windowsは、PlantUMLのライブラリにGraphvizも同梱されるようになったためPlantUML及びGraphvizインストールは不要です。 うまく動作しない場合は、 PlantUMLの[インストール](https://plantuml.com/starting)を試してください。
   - Windows以外の方は、クラス図、ステートマシン図の変換には、[Graphviz](https://plantuml.com/graphviz-dot)のインストールが必要です。

## 📦 インストール方法

1. [Release](https://github.com/ChangeVision/astah-plantuml-plugin/releases) から最新のjarファイルをダウンロードする。
2. astahを起動し、ダウンロードしたjarファイルをドラッグ＆ドロップする。
3. astahを再起動し、新規プロジェクトを作成する。拡張ビュー(astahウィンドウ右下のペイン)に「PlantUML View」が表示されているか確認する。

## ▶️ 利用方法

拡張ビューにPlantUML Viewというタブが追加されます。
![snapshot](https://github.com/ChangeVision/astah-plantuml-plugin/blob/images/img/snapshot.png?raw=true)
左側がPlantUMLのエディタ、右側がプレビュー、「▲toAstah」ボタンでPlantUMLからastahに変換、「▼toPlant」ボタンでastahからPlantUMLに変換できます。
エディタの内容は随時評価され、右側のプレビューに自動反映されます。下部には構文チェックの結果が表示されます。 エディタもプレビューもCtrl+マウスホイールで拡大・縮小できます。

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
  - ノート(一部の図のみ)
  - スタイル(一部の図の図要素の色のみ)
- クラス図
  - クラス、インタフェース
  - ステレオタイプ
  - 属性、操作
  - 関連、継承、依存、関連ラベル
  - 多重度とラベルの同時記述
- シーケンス図
  - 分類子：participant, actor, boundary, control, entity
    (database, collections, queueはparticipantとして表示)
  - メッセージ：同期、非同期、リターン、Create、Destroy
  - メッセージラベル
  - 分類子のクラス読み込み
  - グループ化：alt/else, opt, loop, par, break, critical
- ステートマシン図
  - 初期状態、終了状態、状態
  - トリガー、ガード
  - 状態のネスト
  - 遷移
- アクティビティ図(レガシー版)
  - 開始、終了、アクション
  - コントロールフロー
  - ジョインノード、フォークノード
  - デシジョンノード
- ユースケース図
  - ユースケース、アクター
  - 関連、拡張、包含

---

### 🚧 未対応項目

以下は主な未対応要素(一部)です
- 共通
    - ノート(シーケンス図、ステートマシン図)
    - スタイル(色以外)
- クラス図
    - パッケージ、ネームスペース
    - エンティティ
    - ネストクラス
- シーケンス図
    - メッセージ番号
    - 活性区間
    - 出現・消失メッセージ
    - 時間制約・持続時間制約
    - 相互作用の利用(ToPlant)
    - 複合フラグメント(ToAstah)(一部対応)
- ステートマシン図
    - 疑似状態(一部対応)
    - アクション : entry, do, exit
    - 領域
    - 入れ子が関係する遷移(toAstah)
- アクティビティ図
    - 新しい構文
    - オブジェクトノード
    - パーティション
    - astahからplantへ生成時の順序が不定
- ユースケース図
    - システム境界
    - パッケージ

## 📄 ライセンス

本プラグインは、以下のオープンソースライブラリを使用しており、**GPLv3 ライセンス**のもとで配布されています。

- [PlantUML](https://plantuml.com/)（[GPLv3 ライセンス](https://www.gnu.org/licenses/gpl-3.0.html)）
- [RSyntaxTextArea](https://github.com/bobbylight/RSyntaxTextArea)（[BSD ライセンス](https://github.com/bobbylight/RSyntaxTextArea/blob/master/LICENSE.txt)）

---

### プラグインの動作と生成物について

本プラグインでは、PlantUMLで記述された図をAstah上で表示・変換する際に、一時ファイルを内部的に使用します。  
ただし、図の画像やテキストファイルをユーザーが直接保存・エクスポートする機能は提供していません。

また、PlantUMLを用いて生成される図の内容（テキストや視覚的表現）は [PlantUML FAQ](https://plantuml.com/faq) に基づき、 **ユーザー自身の成果物** と見なされ、 **GPLやその他のライセンス制約の対象とはならず、自由に利用できます** 。

---

### PlantUMLのライセンス選択について

GPLライセンス以外（MIT、LGPLなど）でPlantUMLをご利用されたい場合は、[PlantUML のライセンス案内](https://plantuml.com/license)をご参照ください。

