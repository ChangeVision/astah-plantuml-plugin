# 🌿 Astah PlantUML Plugin

📘 This README is also available in [日本語](./README_ja.md).

[![GitHub release (latest by date)](https://img.shields.io/github/v/release/ChangeVision/astah-plantuml-plugin)](https://github.com/ChangeVision/astah-plantuml-plugin/releases)
[![GitHub last commit](https://img.shields.io/github/last-commit/ChangeVision/astah-plantuml-plugin)](https://github.com/ChangeVision/astah-plantuml-plugin/commits/)
[![GitHub issues](https://img.shields.io/github/issues/ChangeVision/astah-plantuml-plugin)](https://github.com/ChangeVision/astah-plantuml-plugin/issues)
[![GitHub stars](https://img.shields.io/github/stars/ChangeVision/astah-plantuml-plugin)](https://github.com/ChangeVision/astah-plantuml-plugin/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/ChangeVision/astah-plantuml-plugin)](https://github.com/ChangeVision/astah-plantuml-plugin/network)
[![License: GPL v3](https://img.shields.io/badge/license-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

## 📝 Overview

This plugin allows you to convert [PlantUML diagrams](https://plantuml.com/) into [Astah](https://astah.net/products/astah-professional/), and vice versa:

- Import PlantUML diagrams into Astah Professional
- Export Astah diagrams as PlantUML text

## 💻 Requirements

- [Astah Professional](https://astah.net/products/astah-professional/) v9.0 or later
- [PlantUML](https://plantuml.com/) runtime environment
  - On **Windows**, no separate installation of PlantUML or Graphviz is needed because Graphviz is bundled with PlantUML.
  - On **non-Windows OS**, to generate class diagrams or statemachine diagrams, [Graphviz](https://plantuml.com/graphviz-dot) must be installed manually.

If the plugin doesn't work, try [installing PlantUML manually](https://plantuml.com/starting).

## 📦 How to install

1. Download the latest `.jar` file from [Releases](https://github.com/ChangeVision/astah-plantuml-plugin/releases)
2. Launch Astah and drag & drop the `.jar` file into Astah's window
3. Restart Astah and create a new project. If you see a "PlantUML View" tab in the bottom-right pane, installation is complete.

<img src="https://github.com/ChangeVision/astah-plantuml-plugin/blob/images/img/PlantUML-plugin-for-Astah.png?raw=true" width="600">

## ▶️ How to use

After installing the plugin, the "PlantUML View" tab will appear with a PlantUML editor on the left and a preview pane on the right.

<img src="https://github.com/ChangeVision/astah-plantuml-plugin/blob/images/img/PlantUML-View-Pane-Closeup.png?raw=true" width="600">

- `▲toAstah` generates diagrams and models in Astah from PlantUML text
- `▼toPlant` exports the current Astah diagram to PlantUML format with preview
- Syntax checking runs continuously and displays errors at the bottom
- Ctrl + scroll zooms both the editor and the preview

---

### 🔄 Conversion Specifications

#### 📥 From PlantUML to Astah

- On the first conversion, a new diagram will be created in Astah.
- From the second time onward, new elements will be **merged** into the existing diagram. Existing elements will be **left unchanged**, and deleted items in the PlantUML text will be **ignored**.
- Only **new classes** are added; updating attributes or operations in existing classes will not be reflected.
- If multiple diagrams are described (multiple `@startuml` ~ `@enduml` blocks), each one will be imported as a separate diagram. However, if the order or type of diagrams is changed afterward, updates may not work correctly.

#### 📤 From Astah to PlantUML

- The output will be **fully regenerated** every time; no merge is performed.

---

## ✅ Supported diagrams and model elements

- **Class Diagram**
  - Class, Interface
  - Attribute, Operation
  - Association, Inheritance, Dependency, Association label
- **Sequence Diagram**
  - Classifiers: participant, actor, boundary, control, entity  
    *(database, collections, and queue are displayed as participant)*
  - Messages: synchronous, asynchronous, return, and labels
  - Load class from classifier
- **Statemachine Diagram**
  - Initial state, Final state, State
  - Transition
- **Activity Diagram (legacy)**
  - Initial node, Final node, Action
  - Control flow

---

## 🚧 Unsupported elements

These features are currently not supported but may be added in the future:

- **Common**
    - Notes
    - Styles
- **Class Diagram**
    - Package, Namespace
    - Stereotype
    - Entity
    - Nested Class
    - Multiplicity with labels
- **Sequence Diagram**
    - Message order (when exporting from Astah)
    - Grouping: alt/else, opt, loop, par, break, critical, group
    - Activation bars
- **Statemachine Diagram**
    - Entry/Exit/Do actions, Trigger, Guard, Action
    - Nested states
    - Fork, Join, Decision, Merge
    - Unstable order (when exporting from Astah)
- **Activity Diagram**
    - New syntax
    - Object node
    - Partition
    - Unstable order (when exporting from Astah)

---

## 📄 License

This plugin includes the following open-source libraries:

- [PlantUML](https://plantuml.com/) ([GPLv3 License](https://www.gnu.org/licenses/gpl-3.0.html))
- [RSyntaxTextArea](https://github.com/bobbylight/RSyntaxTextArea) ([BSD License](https://github.com/bobbylight/RSyntaxTextArea/blob/master/LICENSE.txt))

Therefore, this plugin inherits the GPLv3 license.

If you would like to use this plugin under the MIT license, please consider obtaining a [PlantUML commercial license](https://plantuml.com/purchase), or contact us for more details.

> For more on PlantUML's licensing, see [https://plantuml.com/license](https://plantuml.com/license)
