# Astah PlantUML Plugin

This Plugin allows you to convert [PlantUML diagrams](https://plantuml.com/) into [Astah](https://astah.net/products/astah-professional/) and vice versa.

## Requirements

- [Astah Professional](https://astah.net/products/astah-professional/) v6.7 or later
- Environment that PlantUML works
  (If you are on Windows, no installation for PlantUML and Graphiviz is needed since PlantUML library contains Graphviz. If this plug-in does not work, please [install PlantUML locally](https://plantuml.com/starting).)

## How to install

1. Download the latest version of .jar file from [Release](https://github.com/ChangeVision/astah-plantuml-plugin/releases)
2. Launch Astah and drag the plantuml-plugin-x.x.x.jar file onto Astah's window
3. Restart Astah and go to [File] - [New].  If you see [PlantUML view] tab on the right-bottom pane, that means installation is complete.
 
   <img src="https://github.com/ChangeVision/astah-plantuml-plugin/blob/images/img/PlantUML-plugin-for-Astah.png?raw=true" width="600">
   
## How to use

This is the view you get after installing this plug-in which is a PlantUML editor on the left and its preview on the right.
<img src="https://github.com/ChangeVision/astah-plantuml-plugin/blob/images/img/PlantUML-View-Pane-Closeup.png?raw=true" width="600">
- 「▲toAstah」 button generates a diagram and also creates model in Astah based on PlantUML editor
- 「▼toPlant」 button converts Astah diagram to PlantUML text with preview
- Syntax validator check is always running and it shows syntax errors at the bottom if there are any
- Ctrl + scroll wheel works for zooming both on the editor and preview




### Current conversion rules

#### From PlantUML to Astah
- When you push from PlantUML to Astah for the first time, it creates a new diagram and renders model elements. From the second time around, updates will be merged to the existing diagram you already created instead of creating a new diagram.
- Updates of attributes/operations in the existing class will not affect
- Deleting model information from the text will not affect
- Creating multiple diagrams (more than one set of @startuml - @enduml) works. However, updates will not work if you change the order of the diagrams or diagram types.


#### From Astah to PlantUML
Sending Astah diagram to PlantUML always regenerates the existing text and preview.


#### Existing Issue
Installing this plug-in disables the [Astah's Script Editor](https://astah.net/product-plugins/script-editor/) - it will not let you type in the Script Editor or any other plugins which uses RSyntaxTextArea.
This issue is planned to be fixed in June 2021.

## Supported Diagram types and model elements

- Class Diagram
  - Class, Interface
  - Attribute, Operation
  - Association, Inheritance, Dependency, Association label
- Sequence Diagram
  - Classifier： participant, actor, boundary, control, entity
    (database, collections an queue will be shown as participant)
  - Message： Synchronous, Asynchronous and return with the label
  - Classifier's class 
- Statemachine Diagram
  - Initial state, Final state and state
  - Transition
- Activity Diagram
  - InitialNode, FlowFinalNode, Action
  - ControlFlow

## Unsupported models elements
Currently, following models won't be converted. This may change in the future update.

- Common model elements
    - Note
    - Style
- Class Diagram
    - Package, Namespace
    - Stereotype
    - Entity
    - Nested Class
    - Muptiplicity and other additional information to assocciations asides the association label
- Sequence Diagram
    - Order of Messages (Only when sending to PlantUML from Astah)
    - Groupi ：alt/else, opt, loop, par, break, critical, group
    - Activation
- Statemachine Diagram
    - action : entry, do, exit. trigger, guard, action
    - Nested states
    - Fork, Join, Decision, Merge
    - Order of display is not stable (Only when sending to PlantUML from Astah)
- Activity Diagram
    - Object Node
    - Partition
    - Order of display is not stable (Only when sending to PlantUML from Astah)

## License

This plug-in is a free software program; you can redistribute it and/or modify it under the terms of the GNU General Public License version2. (The PlantUML library this plug-in use is licensed under GPL2.)
If you'd like to use this under MIT, please contact us.
