# 🌿 Astah PlantUML Plugin

📘 This README is also available in [日本語](./README_ja.md).

[![GitHub release (latest by date)](https://img.shields.io/github/v/release/ChangeVision/astah-plantuml-plugin)](https://github.com/ChangeVision/astah-plantuml-plugin/releases)
[![GitHub last commit](https://img.shields.io/github/last-commit/ChangeVision/astah-plantuml-plugin)](https://github.com/ChangeVision/astah-plantuml-plugin/commits/)
[![GitHub issues](https://img.shields.io/github/issues/ChangeVision/astah-plantuml-plugin)](https://github.com/ChangeVision/astah-plantuml-plugin/issues)
[![GitHub stars](https://img.shields.io/github/stars/ChangeVision/astah-plantuml-plugin)](https://github.com/ChangeVision/astah-plantuml-plugin/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/ChangeVision/astah-plantuml-plugin)](https://github.com/ChangeVision/astah-plantuml-plugin/network)
[![License: MIT](https://img.shields.io/badge/license-MIT-green.svg)](https://opensource.org/licenses/MIT)

## 📝 Overview

This plugin allows you to convert [PlantUML diagrams](https://plantuml.com/) into [Astah](https://astah.net/products/astah-professional/), and vice versa:

- Import PlantUML diagrams into Astah Professional
- Export Astah diagrams as PlantUML text

## 💻 Requirements

- [Astah Professional](https://astah.net/products/astah-professional/), [Astah UML](https://astah.net/products/astah-uml/) v10.0 or later
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

#### 📥 From PlantUML to Astah (`▲toAstah`)

When importing PlantUML diagrams into Astah, a new diagram is always created.

However, if a blank diagram is already open in the Diagram Editor and its type matches the PlantUML diagram being imported, the elements will be added to the existing open diagram instead of creating a new one.

If the PlantUML code includes multiple diagrams (defined between @startuml and @enduml), each will be imported as a separate diagram in Astah.

#### 📤 From Astah to PlantUML (`▼toPlant`)

When exporting diagrams from Astah to PlantUML, the output is **fully regenerated** every time.
The export does not merge with or update any previously generated PlantUML code.

---

## ✅ Support Status

### 📌 Supported Items

- PlantUML → Astah conversion
- Astah → PlantUML conversion
- PlantUML Editor
  - Continuous validation, error display
  - Zoom in/out
- PlantUML Preview
  - Continuous preview display
  - Zoom in/out
- **Common**
  - Notes (supported notations and anchor targets vary by diagram)
  - Hyperlinks (`[[url]]`, including tooltips and labels)
  - Styles(limited to some diagram element colors are supported)
  - Batch conversion of multiple diagrams (@startuml-@enduml) (ToAstah)
- **Class Diagram (Object Diagram)**
  - Class, Interface, Abstract class, Enumeration(including literal values)
  - Stereotype
  - Attribute, Operation (visibility, static, abstract, initial value)
  - Tagged value (TaggedValue, for class/attribute/operation)
  - Template parameter (generic class)
  - Association, Inheritance, Realization, Dependency, Association label
  - Aggregation, Composition, Navigability
  - Multiplicity with labels
  - Association class
  - Package
  - Object, Link
- **Sequence Diagram**
  - Classifiers: participant, actor, boundary, control, entity  
    *(database, collections, and queue are displayed as participants)*
  - Messages: synchronous, asynchronous, return, Create, Destroy
  - Message labels (arguments, return values, guard conditions)
  - Load class from classifier
  - CombinedFragment (partially supported)
  - Activation bars
- **Statemachine Diagram**
  - Initial state, Final state, State
  - Trigger, Guard
  - Nested states
  - Actions: entry, do, exit
  - Internal transition
  - Stereotype
  - Pseudostates (partially supported)
  - Transition
- **Activity Diagram**
  - Initial node, Final node, Action
  - Control flow
  - Join Node, Fork Node
  - Decision Node, Merge Node
  - Object node
  - Partition (simple cases only)
  - Loop (repeat)
  - Reading legacy syntax (ToAstah)
- **Usecase Diagram**
  - Usecase, Actor
  - Association, Extend, Include, Generalization
  - System Boundary
  - Package
- **Composite Structure Diagram**
  - Component, Class
  - Port, Part
  - Nested structure
  - Connector, Dependency, Realization, Inheritance
- **Component Diagram**
  - Component, Interface (ToAstah only; generated as an Astah class diagram)

---

### 🚧 Unsupported elements

The following are the main unsupported elements (partial list):
- **Common**
  - Styles(without color)
  - Batch export of multiple diagrams (ToPlant)
- **Class Diagram**
  - Namespace (ToPlant)
  - Entity
  - Nested Class
- **Sequence Diagram**
  - Message number
  - Found and Lost Messages (ToPlant)
  - Time Constraint, Duration Constraint
  - InteractionUse (ToAstah)
- **Statemachine Diagram**
  - Region
  - Entry point, Exit point (ToAstah)
  - Terminate
- **Activity Diagram**
  - Exporting legacy syntax (ToPlant)
  - Exporting conditionals with three or more branches (ToPlant)
  - Exporting while and switch syntax (ToPlant)
  - Complex partitions
  - Pin
- **Composite Structure Diagram**
  - Interface (ToPlant)
- **Component Diagram**
  - Exporting with component notation (ToPlant; exported as `class X <<component>>` in a class diagram)

---

## 📄 License

This plugin is distributed under the **[MIT license](./LICENSE)**.

It uses the following open-source libraries:

- [PlantUML](https://plantuml.com/) (bundles `plantuml-mit`, the MIT-licensed build)
- [RSyntaxTextArea](https://github.com/bobbylight/RSyntaxTextArea) ([BSD License](https://github.com/bobbylight/RSyntaxTextArea/blob/master/LICENSE.txt))

---

### About Plugin Behavior and Generated Content

This plugin internally uses temporary files to display and convert diagrams written in PlantUML within Astah.  
However, it does not provide any functionality for users to directly save or export diagrams as image or text files.

According to the [PlantUML FAQ](https://plantuml.com/faq), the content of diagrams generated using PlantUML (whether textual or visual) is considered **the user’s own work** and is **not subject to any license restrictions** . Therefore, it may be used freely.

---

### About PlantUML Licensing

PlantUML is distributed under several licenses, including GPL, LGPL, MIT and Apache. This plugin bundles the MIT-licensed build. For details, see the [PlantUML License Information](https://plantuml.com/license).

