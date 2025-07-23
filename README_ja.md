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

- [astah* professional](https://astah.change-vision.com/ja/product/astah-professional.html) 9.0以上
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

### 🔄 変換の仕様と注意事項

PlantUMLからastahに変換する際、初回は新しく図が作成されます。 2回目以降の変換では、既存の図にマージされます。既存の要素は変更されず、新しく追加された要素のみが反映されます。削除は反映されずスキップされます。
現状は追加はクラス単位なので、属性や操作を追記しても反映されません。

複数のPlantUMLの図(@startumlから@enduml)あった場合は、それぞれ別のastahの図として生成されますが、 2回目以降に順序や図の種類が変わった場合は、正しく動作しません。

astahからPlantUMLへの変換はすべて再生成され、マージは行いません。

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
  - グループ化：alt/else, opt, loop, par, break, critical, group
- ステートマシン図
  - 初期状態、終了状態、状態
  - トリガー、ガード
  - 状態のネスト
  - フォーク、ジョイン、デシジョン、マージ
  - 遷移
- アクティビティ図(レガシー版)
  - 開始、終了、アクション
  - コントロールフロー
  - ジョインノード、フォークノード
  - デシジョンノード
- ユースケース図
  - ユースケース、アクター
  - 関連、拡張、包含

### 🚧 未対応項目

- 共通
    - ノート(シーケンス図、ステートマシン図)
    - スタイル(色以外)
- クラス図
    - パッケージ、ネームスペース
    - エンティティ
    - ネストクラス
- シーケンス図
    - 活性区間
- ステートマシン図
    - アクション : entry, do, exit
- アクティビティ図
    - 新しい構文
    - オブジェクトノード
    - パーティション
    - astahからplantへ生成時の順序が不定

## 📄 ライセンス

本プラグインは、以下のオープンソースライブラリを使用しており、**GPLv3 ライセンス**のもとで配布されています。

- [PlantUML](https://plantuml.com/)（[GPLv3 ライセンス](https://www.gnu.org/licenses/gpl-3.0.html)）
- [RSyntaxTextArea](https://github.com/bobbylight/RSyntaxTextArea)（[BSD ライセンス](https://github.com/bobbylight/RSyntaxTextArea/blob/master/LICENSE.txt)）

---

### プラグインの動作と生成物について

本プラグインでは、PlantUMLで記述された図をAstah上で表示・変換する際に、一時ファイルを内部的に使用します。  
ただし、図の画像やテキストファイルをユーザーが直接保存・エクスポートする機能は提供していません。

また、PlantUMLを用いて生成される図の内容（テキストや視覚的表現）は [PlantUML FAQ](https://plantuml.com/faq) に基づき、**ユーザーの所有物**とされ、**GPLv3などのライセンス制約の対象とはならず、自由にご利用いただけます**。
---

### PlantUMLのライセンス選択について

GPLライセンス以外（MIT、LGPLなど）でPlantUMLをご利用されたい場合は、[PlantUML のライセンス案内](https://plantuml.com/license)をご参照ください。

