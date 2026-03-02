# 🌿 astah* PlantUML Plugin

📘 This README is also available in [日本語](./README_ja.md).

[![GitHub release (latest by date)](https://img.shields.io/github/v/release/ChangeVision/astah-plantuml-plugin)](https://github.com/ChangeVision/astah-plantuml-plugin/releases)
[![GitHub last commit](https://img.shields.io/github/last-commit/ChangeVision/astah-plantuml-plugin)](https://github.com/ChangeVision/astah-plantuml-plugin/commits/)
[![GitHub issues](https://img.shields.io/github/issues/ChangeVision/astah-plantuml-plugin)](https://github.com/ChangeVision/astah-plantuml-plugin/issues)
[![GitHub stars](https://img.shields.io/github/stars/ChangeVision/astah-plantuml-plugin)](https://github.com/ChangeVision/astah-plantuml-plugin/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/ChangeVision/astah-plantuml-plugin)](https://github.com/ChangeVision/astah-plantuml-plugin/network)
[![License: GPL v3](https://img.shields.io/badge/license-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

## 📝 Overview

This plugin converts models between [Astah](https://astah.net/products/astah-professional/) and [PlantUML](https://plantuml.com/).

## 💻 Requirements

- [Astah Professional](https://astah.net/products/astah-professional/), [Astah UML](https://astah.net/products/astah-uml/) v10.0 or later
- [PlantUML](https://plantuml.com/) runtime environment
  - On **Windows**, no separate installation of PlantUML or Graphviz is needed because Graphviz is bundled with PlantUML. If the plugin doesn't work, try [installing PlantUML manually](https://plantuml.com/starting).
  - On **non-Windows OS**, to generate class diagrams or statemachine diagrams, [Graphviz](https://plantuml.com/graphviz-dot) must be installed manually.

## 📦 How to install

1. Download the latest `.jar` file from [Releases](https://github.com/ChangeVision/astah-plantuml-plugin/releases)
2. Launch Astah and drag & drop the `.jar` file into Astah's window
3. Restart Astah and create a new project. If you see a "PlantUML View" tab in the extension view (bottom-right pane), installation is complete.

## ▶️ How to use

A "PlantUML View" tab will be added to the extension view.
![PlantUML View screenshot](https://github.com/ChangeVision/astah-plantuml-plugin/blob/images/img/snapshot.png?raw=true)
The left side is the PlantUML editor, and the right side is the preview. The `▲toAstah` button converts PlantUML to Astah, and the `▼toPlant` button converts Astah to PlantUML.
The editor content is continuously validated, and the preview on the right is automatically updated. Syntax check results are displayed at the bottom. Both the editor and preview support zoom in/out with Ctrl + mouse wheel.

---

### 🔄 Conversion Specifications

#### From PlantUML to Astah (`▲toAstah`)

When importing PlantUML diagrams into Astah, a new diagram is always created.

However, if a blank diagram is already open in the Diagram Editor and its type matches the PlantUML diagram being imported, the elements will be added to the existing open diagram instead of creating a new one.

If the PlantUML code includes multiple diagrams (defined between @startuml and @enduml), each will be imported as a separate diagram in Astah.

#### From Astah to PlantUML (`▼toPlant`)

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
  - Notes (only some diagrams are supported)
  - Styles (limited to element colors in some diagrams)
- **Class Diagram (Object Diagram)**
  - Class, Interface
  - Stereotype
  - Attribute, Operation
  - Association, Inheritance, Dependency, Association label
  - Multiplicity with labels
  - Package
  - Object, Link
- **Sequence Diagram**
  - Classifiers: participant, actor, boundary, control, entity  
    *(database, collections, and queue are displayed as participants)*
  - Messages: synchronous, asynchronous, return, Create, Destroy
  - Message labels
  - Load class from classifier
  - Combined Fragment (partially supported)
- **Statemachine Diagram**
  - Initial state, Final state, State
  - Trigger, Guard
  - Nested states
  - Actions: entry, do, exit
  - Pseudostates (partially supported)
  - Transition
- **Activity Diagram**
  - Initial node, Final node, Action
  - Control flow
  - Join Node, Fork Node
  - Decision Node
  - Object Node
  - Partition (simple cases only)
- **Usecase Diagram**
  - Usecase, Actor
  - Association, Extend, Include

---

### 🚧 Unsupported elements

The following are the main unsupported elements (partial list):
- **Common**
  - Styles (except for colors)
- **Class Diagram**
  - Namespace (ToPlant)
  - Entity
  - Nested Class
- **Sequence Diagram**
  - Message number
  - Activation bars
  - Found and Lost Messages
  - Time Constraint, Duration Constraint
  - InteractionUse
- **Statemachine Diagram**
  - Region
- **Activity Diagram**
  - Legacy syntax
  - Complex partitions
  - Pin
- **Usecase Diagram**
  - System Boundary
  - Package
- **Composite Structure Diagram**

---

## 📄 License

This plugin uses the following open-source libraries and is distributed under the **GPLv3 license**:

- [PlantUML](https://plantuml.com/) ([GPLv3 License](https://www.gnu.org/licenses/gpl-3.0.html))
- [RSyntaxTextArea](https://github.com/bobbylight/RSyntaxTextArea) ([BSD License](https://github.com/bobbylight/RSyntaxTextArea/blob/master/LICENSE.txt))

---

### About Plugin Behavior and Generated Content

This plugin internally uses temporary files to display and convert diagrams written in PlantUML within Astah.  
However, it does not provide any functionality for users to directly save or export diagrams as image or text files.

According to the [PlantUML FAQ](https://plantuml.com/faq), the content of diagrams generated using PlantUML (whether textual or visual) is considered **the user’s own work** and is **not subject to the GPL or other license restrictions** . Therefore, it may be used freely.

---

### About PlantUML Licensing Options

If you wish to use PlantUML under a license other than GPL (e.g., MIT or LGPL), please refer to the [PlantUML License Information](https://plantuml.com/license).

