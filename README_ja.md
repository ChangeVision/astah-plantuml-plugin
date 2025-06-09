# astah* PlantUML Plugin

## 概要

[astah](https://astah.change-vision.com)と[PlantUML](https://plantuml.com)のモデルの相互変換を行うプラグインです。

## 対象環境

- [astah* professional](https://astah.change-vision.com/ja/product/astah-professional.html) 9.0以上
- [PlantUML動作環境](https://plantuml.com/starting)
   - Windowsは、PlantUMLのライブラリにGraphvizも同梱されるようになったためPlantUML及びGraphvizインストールは不要です。 うまく動作しない場合は、 PlantUMLの[インストール](https://plantuml.com/starting)を試してください。
   - Windows以外の方は、クラス図、ステートマシン図の変換には、[Graphviz](https://plantuml.com/graphviz-dot)のインストールが必要です。

## インストール方法

1. [Release](https://github.com/ChangeVision/astah-plantuml-plugin/releases) から最新のjarファイルをダウンロードする。
2. astahを起動し、jarファイルをDrag & Dropする。
3. astahを再起動し、新規プロジェクトを作成する。拡張ビュー(astahウィンドウ右下のペイン)に「PlantUML View」が表示されているか確認する。

## 利用方法

拡張ビューにPlantUML Viewというタブが追加されます。
![snapshot](https://github.com/ChangeVision/astah-plantuml-plugin/blob/images/img/snapshot.png?raw=true)
左側がPlantUMLのエディタ、右側がプレビュー、「▲toAstah」ボタンでPlantUMLからastahに変換、「▼toPlant」ボタンでastahからPlantUMLに変換できます。
エディタの内容は常時評価されプレビューに表示されます。また下部にシンタックスチェックの結果が表示されています。 エディタもプレビューもCtrl+マウスホイールで拡大・縮小できます。

### 変換の仕様と注意事項

PlantUMLからastahに変換する際、初回は新しく図が作成されます。 2回目以降は既存の図にマージされます。既存の要素はスキップされ、新しい要素のみ追加されます。削除は反映されずスキップされます。
現状は追加はクラス単位なので、属性や操作を追記しても反映されません。

複数のPlantUMLの図(@startumlから@enduml)あった場合は、それぞれ別のastahの図として生成されますが、 2回目以降に順序や図の種類が変わった場合は、正しく動作しません。

astahからPlantUMLへの変換はすべて再生成され、マージは行いません。

#### 競合
[スクリプトエディタプラグイン](https://astah.change-vision.com/ja/feature/script-plugin.html)と同時に使用した場合、テキストが入力できなくなる不具合があります。
（RSyntaxTextAreaを用いているプラグイン同士は同様の不具合が発生する可能性があります。）

## 対応状況

### 対応項目

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
    - astahからplantへ生成時の順序が不定
- アクティビティ図
    - 新しい構文
    - オブジェクトノード
    - パーティション
    - astahからplantへ生成時の順序が不定

## License

利用しているPlantUMLライブラリがGPL2で提供されているためGPL2を継承しています。 MITで利用する方法もあるので、MITで利用したい場合はお問い合わせください。
