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
- クラス図
  - クラス、インタフェース
  - 属性、操作
  - 関連、継承、依存、関連ラベル
- シーケンス図
  - 分類子：participant, actor, boundary, control, entity
    (database, collections, queueはparticipantとして表示)
  - メッセージ：同期、非同期、リターン。メッセージラベル
  - 分類子のクラス読み込み 
- ステートマシン図
  - 初期状態、終了状態、状態
  - 遷移
- アクティビティ図(レガシー版)
  - 開始、終了、アクション
  - コントロールフロー

### 🚧 未対応項目

- 共通
    - ノート
    - スタイル
- クラス図
    - パッケージ、ネームスペース
    - ステレオタイプ
    - エンティティ
    - ネストクラス
    - 多重度とラベルの同時記述
- シーケンス図
    - astahからplantへ生成時のメッセージ順序が不定（追加順）
    - グループ化：alt/else, opt, loop, par, break, critical, group
    - 活性区間
- ステートマシン図
    - action : entry, do, exit. trigger, guard, action
    - 状態のネスト
    - フォーク、ジョイン、デシジョン、マージ
    - astahからplantへ生成時の順序が不定
- アクティビティ図
    - 新しい構文
    - オブジェクトノード
    - パーティション
    - astahからplantへ生成時の順序が不定

## 📄 License

本プラグインには、以下のオープンソースライブラリが含まれています：

- [PlantUML](https://plantuml.com/)（[GPLv3 ライセンス](https://www.gnu.org/licenses/gpl-3.0.html)）
- [RSyntaxTextArea](https://github.com/bobbylight/RSyntaxTextArea)（[BSD ライセンス](https://github.com/bobbylight/RSyntaxTextArea/blob/master/LICENSE.txt)）

このため、本プラグインは GPLv3 ライセンスを継承しています。

MIT ライセンスでの利用をご希望の場合は、PlantUML の [商用ライセンス](https://plantuml.com/purchase) を取得いただくか、別途ご相談ください。

> PlantUML のライセンスに関する詳細は、[https://plantuml.com/license](https://plantuml.com/license) をご参照ください。
