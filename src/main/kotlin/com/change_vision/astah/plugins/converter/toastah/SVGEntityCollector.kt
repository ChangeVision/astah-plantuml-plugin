package com.change_vision.astah.plugins.converter.toastah

import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import net.sourceforge.plantuml.abel.GroupType
import net.sourceforge.plantuml.abel.LeafType
import net.sourceforge.plantuml.activitydiagram.ActivityDiagram
import net.sourceforge.plantuml.classdiagram.ClassDiagram
import net.sourceforge.plantuml.abel.Entity as Entity
import net.sourceforge.plantuml.statediagram.StateDiagram
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.io.File
import java.nio.file.Files
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import kotlin.collections.HashMap
import kotlin.math.abs

object SVGEntityCollector {
    //ジョイン/フォークノードがリンクの始点終点どちらかを調べる
    const val LINK_END_FROM = "from"
    const val LINK_END_TO = "to"
    const val LINK_END_NONE = "none"

    const val SYNCHRO_BAR_NODE_TYPE_FORK = "fork node"
    const val SYNCHRO_BAR_NODE_TYPE_JOIN = "join node"

    const val START_NODE_NAME = "start"
    const val END_NODE_NAME = "end"

    const val RECTANGLE_MARGIN = 0.5f
    const val RECTANGLE_EXPANSION = 2f

    var synchroBarTypeMap = mutableMapOf<Entity,String>()

    fun collectSvgPosition(reader: SourceStringReader, index: Int): Map<String, Rectangle2D.Float> {
        var tempSvgFile : File? = null
        val result = when (val diagram = reader.blocks[index].diagram) {
            is ClassDiagram -> {
                tempSvgFile = Files.createTempFile("plantsvg_${index}_", ".svg").toFile()
                tempSvgFile.outputStream().use { os ->
                    reader.outputImage(os, index, FileFormatOption(FileFormat.SVG))
                }
                val classBoundaries = collectClassEntityBoundary(tempSvgFile)
                val circleBoundaries = collectCircleElements(tempSvgFile, diagram)
                classBoundaries + circleBoundaries
            }
            is StateDiagram -> {
                val stateNames = ArrayList<String>()
                stateNames.addAll(diagram.groupsAndRoot().filter {
                    it.groupType == GroupType.STATE
                }.map {
                    it.name
                })
                stateNames.addAll(diagram.leafs().filter {
                    it.leafType == LeafType.STATE
                }.map {
                    it.name
                })
                tempSvgFile = Files.createTempFile("plantsvg_${index}_", ".svg").toFile()
                tempSvgFile.outputStream().use { os ->
                    reader.outputImage(os, index, FileFormatOption(FileFormat.SVG))
                }
                collectEntityBoundary(tempSvgFile, stateNames)
            }
            is ActivityDiagram -> {
                val activities =
                    diagram.leafs().filter {
                        it.leafType in setOf(LeafType.ACTIVITY, LeafType.SYNCHRO_BAR, LeafType.BRANCH, LeafType.NOTE)
                    }
                tempSvgFile = Files.createTempFile("plantsvg_${index}_", ".svg").toFile()
                tempSvgFile.outputStream().use { os ->
                    reader.outputImage(os, index, FileFormatOption(FileFormat.SVG))
                }
                collectEntityBoundaryForActivity(tempSvgFile, activities)
            }
            else -> emptyMap()
        }
        tempSvgFile!!.delete()
        return result
    }

    private fun collectClassEntityBoundary(svgFile: File): Map<String, Rectangle2D.Float> {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(svgFile)
        val result = mutableMapOf<String, Rectangle2D.Float>()
        val gNodes = doc.getElementsByTagName("g")

        for (i in 0 until gNodes.length) {
            val g = gNodes.item(i)
            val classAttr = g.attributes?.getNamedItem("class")?.nodeValue ?: continue
            if (classAttr != "entity") continue

            val nameAttr = g.attributes?.getNamedItem("data-entity")?.nodeValue ?: continue
            val rect = (0 until g.childNodes.length)
                .map { g.childNodes.item(it) }
                .firstOrNull { it.nodeName == "rect" } ?: continue
            // 以前は抽出したキーの接頭辞にclassがついていたため、付けておく
            result["class $nameAttr"] = extractRectangle(rect)
        }

        return result
    }

    /**
     * クラス図内のcircle/楕円要素を検出する
     * @param svgFile SVGファイル
     * @param diagram ClassDiagram
     * @return 要素コードと位置情報のマップ
     */
    private fun collectCircleElements(svgFile: File, diagram: ClassDiagram): Map<String, Rectangle2D.Float> {
        val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = builder.parse(svgFile)
        val entityBoundaryMap = mutableMapOf<String, Rectangle2D.Float>()

        // サークル/楕円要素を検索
        val ellipses = XPathFactory.newInstance().newXPath()
            .compile("//ellipse | //circle")
            .evaluate(doc, XPathConstants.NODESET) as NodeList


        // Circleタイプの要素をモデルから抽出
        val circleElements = diagram.leafs().filter {
            it.leafType == LeafType.CIRCLE ||
                    it.leafType == LeafType.DESCRIPTION
        }

        val circleNames = circleElements.map { it.name }

        // SVG内の各ellipse/circle要素を調査
        for (i in 0 until ellipses.length) {
            val ellipseNode = ellipses.item(i)

            // 次の兄弟ノードがtext要素かチェック
            var nextSibling = ellipseNode.nextSibling
            while (nextSibling != null) {
                if (nextSibling.nodeName == "text") {
                    // textノードの内容からインターフェース名を抽出
                    val textContent = nextSibling.textContent.trim()

                    // インターフェース名と一致するか確認
                    for (circleName in circleNames) {
                        if (textContent.contains(circleName)) {
                            val entityCode = circleName
                            entityBoundaryMap[entityCode] = extractRectangle(ellipseNode)
                            break
                        }
                    }
                    break
                }
                nextSibling = nextSibling.nextSibling
            }
        }

        return entityBoundaryMap
    }

    private fun collectEntityBoundary(svgFile: File, stateNames: List<String>): Map<String, Rectangle2D.Float> {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(svgFile)

        val stateMap = stateNames.mapNotNull { state ->
            val stateRect =
                XPathFactory.newInstance().newXPath()
                    .compile("//text[text() = '$state']/preceding-sibling::rect")
                    .evaluate(doc, XPathConstants.NODESET) as NodeList
            if (stateRect.length == 0) {
                null
            } else {
                val rect = extractRectangle(stateRect.item(stateRect.length - 1))
                Pair(state, rect)
            }
        }.toMap()
        val ellipses = XPathFactory.newInstance().newXPath()
            .compile("//ellipse[not(@fill='none')]")
            .evaluate(doc, XPathConstants.NODESET) as NodeList

        /*
         * ellipseから初期状態・終了状態・ヒストリを拾う
         * 位置については PlantUML 上での位置をなるべく再現するようにした
         */
        val otherNodeMap = HashMap<String, Rectangle2D.Float>()
        var ellipseIndex = 0
        while (ellipseIndex < ellipses.length) {
            val ellipse = ellipses.item(ellipseIndex)
            if (ellipse == null) {
                ellipseIndex++
                continue
            }
            val nextEllipse = ellipses.item(ellipseIndex + 1)
            val prevNode = ellipse.previousSibling
            val nextNode = ellipse.nextSibling
            var elementName = ""
            when {
                prevNode?.nodeName == "ellipse" -> {
                    val cx = ellipse.attributes?.getNamedItem("cx")?.nodeValue?.toFloat()
                    val cy = ellipse.attributes?.getNamedItem("cy")?.nodeValue?.toFloat()
                    val prevCx = prevNode.attributes?.getNamedItem("cx")?.nodeValue?.toFloat()
                    val prevCy = prevNode.attributes?.getNamedItem("cy")?.nodeValue?.toFloat()
                    val rx = ellipse.attributes?.getNamedItem("rx")?.nodeValue?.toFloat()
                    val ry = ellipse.attributes?.getNamedItem("ry")?.nodeValue?.toFloat()
                    val prevRx = prevNode.attributes?.getNamedItem("rx")?.nodeValue?.toFloat()
                    val prevRy = prevNode.attributes?.getNamedItem("ry")?.nodeValue?.toFloat()
                    if (cx == prevCx && cy == prevCy && ((rx?.minus(prevRx!!))?.let { abs(it) } == 5.0f) && (ry?.minus(prevRy!!)?.let { abs(it) } == 5.0f)) {
                        elementName = "final"
                        ellipseIndex++
                    } else if (nextNode.nextSibling?.let { it.nodeName == "text" && it.nodeValue == "H" }!!) {
                        elementName = "history"
                        ellipseIndex++
                    } else if (nextNode.nextSibling?.let { it.nodeName == "text" && it.nodeValue == "H*" }!!) {
                        elementName = "deepHistory"
                        ellipseIndex++
                    } else {
                        if (nextNode.nodeName == "ellipse") {
                            if (nextNode.equals(nextEllipse)) {
                                val nextCx = nextNode.attributes?.getNamedItem("cx")?.nodeValue?.toFloat()
                                val nextCy = nextNode.attributes?.getNamedItem("cy")?.nodeValue?.toFloat()
                                val nextRx = nextNode.attributes?.getNamedItem("rx")?.nodeValue?.toFloat()
                                val nextRy = nextNode.attributes?.getNamedItem("ry")?.nodeValue?.toFloat()
                                if (cx == nextCx && cy == nextCy
                                    && nextRx?.minus(rx!!)?.let { abs(it) } == 5.0f && nextRy?.minus(ry!!)?.let { abs(it) } == 5.0f) {
                                    ellipseIndex++
                                } else if (nextNode.nextSibling?.let { it.nodeName == "text" && it.firstChild?.nodeValue == "H" }!!) {
                                    elementName = "history"
                                    ellipseIndex++
                                } else if (nextNode.nextSibling?.let { it.nodeName == "text" && it.firstChild?.nodeValue == "H*" }!!) {
                                    elementName = "deepHistory"
                                    ellipseIndex++
                                } else {
                                    elementName = "initial"
                                    ellipseIndex++
                                }
                            } else {
                                elementName = "initial"
                                ellipseIndex++
                            }
                        } else {
                            elementName = "initial"
                            ellipseIndex++
                        }
                    }
                }
                nextNode.nodeName == "ellipse" -> {
                    if (nextNode.equals(nextEllipse)) {
                        val cx = ellipse.attributes?.getNamedItem("cx")?.nodeValue?.toFloat()
                        val cy = ellipse.attributes?.getNamedItem("cy")?.nodeValue?.toFloat()
                        val nextCx = nextNode.attributes?.getNamedItem("cx")?.nodeValue?.toFloat()
                        val nextCy = nextNode.attributes?.getNamedItem("cy")?.nodeValue?.toFloat()
                        val rx = ellipse.attributes?.getNamedItem("rx")?.nodeValue?.toFloat()
                        val ry = ellipse.attributes?.getNamedItem("ry")?.nodeValue?.toFloat()
                        val nextRx = nextNode.attributes?.getNamedItem("rx")?.nodeValue?.toFloat()
                        val nextRy = nextNode.attributes?.getNamedItem("ry")?.nodeValue?.toFloat()
                        if (cx == nextCx && cy == nextCy
                            && nextRx?.minus(rx!!)?.let { abs(it) } == 5.0f && nextRy?.minus(ry!!)?.let { abs(it) } == 5.0f) {
//                            elementName = "final"
                            ellipseIndex++
                        } else if (nextNode.nextSibling?.let { it.nodeName == "text" && it.firstChild?.nodeValue == "H" }!!) {
                            elementName = "history"
                            ellipseIndex++
                        } else if (nextNode.nextSibling?.let { it.nodeName == "text" && it.firstChild?.nodeValue == "H*" }!!) {
                            elementName = "deepHistory"
                            ellipseIndex++
                        } else {
                            elementName = "initial"
                            ellipseIndex++
                        }
                    } else {
                        elementName = "initial"
                        ellipseIndex++
                    }
                }
                nextNode.let { it.nodeName == "text" && it.firstChild?.nodeValue == "H" } -> {
                    elementName = "history"
                    ellipseIndex++
                }
                nextNode.let { it.nodeName == "text" && it.firstChild?.nodeValue == "H*" } -> {
                    elementName = "deepHistory"
                    ellipseIndex++
                }
                else -> {
                    elementName = "initial"
                    ellipseIndex++
                }
            }
            if (elementName.isNotEmpty()) {
                otherNodeMap[elementName] = extractRectangle(ellipse)
            }
        }


        return stateMap.plus(otherNodeMap)
    }

    private fun collectEntityBoundaryForActivity(svgFile: File, activities: List<Entity>):
            Map<String, Rectangle2D.Float> {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(svgFile)
        val xpath = XPathFactory.newInstance().newXPath()

        //アクションの座標取得
        val actionMap = activities.filter { it.leafType == LeafType.ACTIVITY }.mapNotNull { activity ->
            val action = activity.name
            val display = activity.display.firstOrNull()?.removeSuffix(" ")
            val actionRectangles = xpath
                .compile("//text[contains(text(),'$display')]/preceding-sibling::rect")
                .evaluate(doc, XPathConstants.NODESET) as NodeList

            if (actionRectangles.length == 0) return@mapNotNull null

            val rect = extractRectangle(actionRectangles.item(actionRectangles.length - 1))
            action to rect
        }.toMap()

        //デシジョンノード
        val decisionMergeNodeMap = activities.filter { it.leafType == LeafType.BRANCH }.mapNotNull { decisionMergeNode ->
            val uid = decisionMergeNode.uid
            val nodeRectangles = xpath
                .compile("//g[@class='entity' and @data-uid='$uid']")
                .evaluate(doc, XPathConstants.NODESET) as NodeList

            if (nodeRectangles.length == 0) return@mapNotNull null

            val polygonNode =nodeRectangles.item(0).childNodes.item(0)
            if(polygonNode.nodeName != "polygon" || polygonNode !is Element)return@mapNotNull null
            val points = getPolygonPoints(polygonNode)

            val minPoints = Point2D.Float(points.minOf { it.x }, points.minOf { it.y })
            val maxPoints = Point2D.Float(points.maxOf { it.x }, points.maxOf { it.y })

            decisionMergeNode.name to Rectangle2D.Float(
                minPoints.x,
                minPoints.y,
                maxPoints.x - minPoints.x,
                maxPoints.y - minPoints.y
            )

        }.toMap()

        //ノート
        val noteMap = activities.filter { it.leafType == LeafType.NOTE }.mapNotNull { note ->
            val uid = note.uid
            val nodeRectangles = xpath
                .compile("//g[@class='entity' and @data-uid='$uid']")
                .evaluate(doc, XPathConstants.NODESET) as NodeList

            if (nodeRectangles.length == 0) return@mapNotNull null

            val pathNode = nodeRectangles.item(0).childNodes.item(0)
            if(pathNode.nodeName != "path" || pathNode !is Element)return@mapNotNull null

            val points = extractPathPoints(pathNode.getAttribute("d"))
            val minPoints = Point2D.Float (points.minOf { it.x },points.minOf { it.y })

            note.name to Rectangle2D.Float( minPoints.x,minPoints.y,0f,0f)
        }.toMap()

        //ジョイン/フォークノード
        val syncBarNodes = mutableMapOf<String, Rectangle2D.Float>()
        val lineEndPoints = mutableMapOf<String, List<Point2D>>()

        val synchroBars = activities.filter { it.leafType == LeafType.SYNCHRO_BAR }

        for (leaf in synchroBars) {
            val name = leaf.name
            val lineNumber = leaf.location.position
            val barLink = getLinksFromLineNumber(lineNumber, xpath, doc)

            if (barLink.length ==  0 ) continue
            val barNode = barLink.item(0)

            synchroBarTypeMap[leaf] = checkSynchroBarType(name, xpath, doc)

            val barPos = checkLinkEnd(barNode, name)

            val points = (0 until barNode.childNodes.length)
                .mapNotNull { index ->
                    val element = barNode.childNodes.item(index)
                    if (element !is Element) return@mapNotNull null

                    when (barPos) {
                        LINK_END_FROM -> if (element.nodeName == "path") {
                            extractPathPoints(element.getAttribute("d"))
                        } else null
                        LINK_END_TO -> if (element.nodeName == "polygon") {
                            getPolygonPoints(element)
                        } else null
                        else -> null
                    }
                }
                .flatten()

            if (points.isNotEmpty()) {
                lineEndPoints[name] = points
            }
        }

        val rectList = (xpath.compile("//rect[not(following-sibling::*[1][self::text])]")
            .evaluate(doc, XPathConstants.NODESET) as NodeList).let { nodeList ->
            (0 until nodeList.length).map { extractRectangle(nodeList.item(it)) }
        }

        for ((name, points) in lineEndPoints) {
            for (rect in rectList) {
                val expandedRect = Rectangle2D.Float(rect.x - RECTANGLE_MARGIN, rect.y - RECTANGLE_MARGIN,
                    rect.width + RECTANGLE_EXPANSION, rect.height + RECTANGLE_EXPANSION)
                if (points.any { expandedRect.contains(it) }) {
                    syncBarNodes[name] = rect
                }
            }
        }

        return actionMap + decisionMergeNodeMap + syncBarNodes + getEllipseRectangles(xpath , doc) + noteMap
    }

    private fun getEllipseRectangles(xpath : XPath, doc : Document): Map<String, Rectangle2D.Float>{
        val ellipseNodes = xpath
            .compile("//ellipse[not(@fill='none')]")
            .evaluate(doc, XPathConstants.NODESET) as NodeList

        /*
         * 状態以外の要素の位置は紐づけが難しいため、種類別に最後の要素の座標のみ扱うものとする。
         * TODO 現状は、ellipseで拾える初期状態・終了状態・ヒストリのみ。他は追々対応する。
         * Pathを追って接続関係を元に調べれなくもないが、一旦保留。
         */
        return (0 until ellipseNodes.length)
            .mapNotNull { i ->
                val ellipse = ellipseNodes.item(i)
                val prevNode = ellipse.previousSibling
                val nextTextNode = ellipse.nextSibling?.nextSibling

                val elementName = when {
                    prevNode?.nodeName == "ellipse" -> END_NODE_NAME
                    nextTextNode?.nodeName == "text" && nextTextNode.nodeValue == "H" -> "history"
                    else -> START_NODE_NAME
                }

                elementName to extractRectangle(ellipse)
            }.toMap()
    }

    private fun extractRectangle(node: Node): Rectangle2D.Float {
        val attrs = node.attributes
        return when (node.nodeName) {
            "ellipse", "circle" -> {
                val cx = attrs.getNamedItem("cx").nodeValue.toFloat()
                val cy = attrs.getNamedItem("cy").nodeValue.toFloat()
                val rx = attrs.getNamedItem("rx")?.nodeValue?.toFloat() ?:
                       attrs.getNamedItem("r")?.nodeValue?.toFloat() ?: 10f
                val ry = attrs.getNamedItem("ry")?.nodeValue?.toFloat() ?:
                       attrs.getNamedItem("r")?.nodeValue?.toFloat() ?: 10f
                Rectangle2D.Float(cx - rx, cy - ry, rx * 2, ry * 2)
            }
            else -> {
                val x = attrs.getNamedItem("x").nodeValue.toFloat()
                val y = attrs.getNamedItem("y").nodeValue.toFloat()
                val w = attrs.getNamedItem("width").nodeValue.toFloat()
                val h = attrs.getNamedItem("height").nodeValue.toFloat()
                Rectangle2D.Float(x, y, w, h)
            }
        }
    }

    fun extractPathPoints(d: String): List<Point2D.Float> {
        val commands = Regex("[A-Za-z]").findAll(d).map { it.value }.toList()
        val parts = d.split(Regex("[A-Za-z]")).map { it.trim() }.filter { it.isNotEmpty() }

        val result = mutableListOf<Point2D.Float>()

        for ((i, cmd) in commands.withIndex()) {
            val nums = parts.getOrNull(i)?.split(",", " ")?.filter { it.isNotBlank() } ?: continue
            val floats = nums.mapNotNull { it.toFloatOrNull() }

            when (cmd) {
                "M", "L" -> {
                    for (j in floats.indices step 2) {
                        if (j + 1 < floats.size) {
                            result.add(Point2D.Float(floats[j], floats[j + 1]))
                        }
                    }
                }
                "C" -> {
                    for (j in floats.indices step 6) {
                        if (j + 5 < floats.size) {
                            result.add(Point2D.Float(floats[j+4], floats[j + 5]))
                        }
                    }
                }
            }
        }

        return result
    }

    fun checkLinkEnd(linkNode : Node, nodeName : String):String{
        if(linkNode !is Element) return LINK_END_NONE

        val from = linkNode.getAttribute("data-entity-1")
        val to = linkNode.getAttribute("data-entity-2")

        return when (nodeName) {
            from -> LINK_END_FROM
            to -> LINK_END_TO
            else -> LINK_END_NONE
        }
    }

    fun checkSynchroBarType(name : String,xpath: XPath,doc : Document):String{
        val linkList = getLinkNodeList(xpath,doc)
        var countFrom = 0
        var countTo = 0
        for(index in 0 until linkList.length){
            val link = linkList.item(index)
            if(link !is Element) continue
            when(checkLinkEnd(link,name)){
                LINK_END_FROM -> countFrom++
                LINK_END_TO -> countTo++
            }
        }

        return if(countFrom <= countTo) SYNCHRO_BAR_NODE_TYPE_JOIN
        else SYNCHRO_BAR_NODE_TYPE_FORK
    }

    fun getLinkNodeList(xpath: XPath,doc : Document):NodeList{
        return xpath.compile("//g[@class='link']")
            .evaluate(doc, XPathConstants.NODESET) as NodeList
    }

    fun getLinksFromLineNumber(lineNumber:Int,xpath: XPath,doc : Document):NodeList{
        return xpath.compile("//g[@class='link' and @data-source-line='$lineNumber']")
            .evaluate(doc, XPathConstants.NODESET) as NodeList
    }

    fun getPolygonPoints(element : Element) : List<Point2D.Float> {
         return element.getAttribute("points")
            .split(',')
            .map { it.trim() }
            .chunked(2)
            .mapNotNull { chunk ->
                if (chunk.size == 2) {
                    val x = chunk[0].toFloatOrNull()
                    val y = chunk[1].toFloatOrNull()
                    if (x != null && y != null) Point2D.Float(x, y) else null
                } else null
            }
    }

    fun isFork(entity: Entity): Boolean{
        return synchroBarTypeMap[entity] == SYNCHRO_BAR_NODE_TYPE_FORK
    }
}
