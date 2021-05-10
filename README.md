# astah* PlantUML Plugin

## 概要

[astah](https://astah.change-vision.com)と[PlantUML](https://plantuml.com)のモデルの相互変換を行うプラグインです。

## 対象環境

- Astah* Professional 6.7
- PlantUML動作環境
  (WindowsはPlantUMLのライブラリにGraphvizも同梱されるようになったためPlantUML及びGraphvizインストールは不要です。 うまく動作しない場合は、 PlantUMLの[インストール]()
  を試してみてください。）

## インストール方法

1. [Release](https://github.com/ChangeVision/astah-plantuml-plugin/releases) から最新のjarファイルをダウンロードする。
2. astahを起動し、jarファイルをDrag & Dropする。
3. astahを再起動し、新規プロジェクトを作成する。拡張ビューに「PlantUML View」が表示されているか確認する。

## 利用方法

拡張ビューにPlantUML Viewというタブが追加されます。
![snapshot](https://raw.githubusercontent.com/ChangeVision/astah-plantuml-plugin/images/img/snapshot.png?token=AAK45APPR25EN2DB2NW3P53AUG7A2)
左側がPlantUMLのエディタ、右側がプレビュー、「▲toAstah」ボタンでPlantUMLからastahに変換、「▼toPlant」ボタンでastahからPlantUMLに変換できます。
エディタの内容は常時評価されプレビューに表示されます。また下部にシンタックスチェックの結果が表示されています。 エディタもプレビューもCtrl+マウスホイールで拡大・縮小できます。

## 対応状況

### 対応項目

- PlantUML → Astah変換
- Astah → PlantUML変換
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
    - 分類子のクラス読み込み -ステートマシン図
    - 初期状態、終了状態、状態
    - 遷移
    - アクティビティ図
    - 開始、終了、アクション
    - コントロールフロー

### 未対応項目

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
- アクティビティ図
    - オブジェクトノード
    - パーティション

## License

利用しているPlantUMLライブラリがGPL2で提供されているためGPL2を継承しています。 MITで利用する方法もあるので、MITで利用したい場合はお問い合わせください。