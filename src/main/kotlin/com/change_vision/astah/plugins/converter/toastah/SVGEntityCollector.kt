package com.change_vision.astah.plugins.converter.toastah

import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import net.sourceforge.plantuml.activitydiagram.ActivityDiagram
import net.sourceforge.plantuml.classdiagram.ClassDiagram
import net.sourceforge.plantuml.abel.LeafType
import net.sourceforge.plantuml.statediagram.StateDiagram
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.awt.geom.Rectangle2D
import java.io.File
import java.nio.file.Files
import java.util.regex.Pattern
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

object SVGEntityCollector {
    fun collectSvgPosition(reader: SourceStringReader, index: Int): Map<String, Rectangle2D.Float> {
        val tempSvgFile = Files.createTempFile("plantsvg_${index}_", ".svg").toFile()
        tempSvgFile.outputStream().use { os ->
            reader.outputImage(os, index, FileFormatOption(FileFormat.SVG))
        }
        val result = when (val diagram = reader.blocks[index].diagram) {
            is ClassDiagram -> {
                val classBoundaries = collectClassEntityBoundary(tempSvgFile)
                val circleBoundaries = collectCircleElements(tempSvgFile, diagram)
                classBoundaries + circleBoundaries
            }
            is StateDiagram -> {
                val stateNames = diagram.leafs().filter { it.leafType == LeafType.STATE }.map { it.name }
                collectEntityBoundary(tempSvgFile, stateNames)
            }
            is ActivityDiagram -> {
                val activityNames =
                    diagram.leafs().filter { it.leafType == LeafType.ACTIVITY }.map { it.name }
                collectEntityBoundary(tempSvgFile, activityNames)
            }
            else -> emptyMap()
        }
        tempSvgFile.delete()
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
            result["class " + nameAttr] = extractRectangle(rect)
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
         * 状態以外の要素の位置は紐づけが難しいため、種類別に最後の要素の座標のみ扱うものとする。
         * TODO 現状は、ellipseで拾える初期状態・終了状態・ヒストリのみ。他は追々対応する。
         * Pathを追って接続関係を元に調べれなくもないが、一旦保留。
         */
        val otherNodeMap = (0 until ellipses.length).map { ellipses.item(it) }
            .mapNotNull { ellipse ->
                val prevNode = ellipse.previousSibling
                val elementName = when {
                    prevNode?.nodeName == "ellipse" -> "final"
                    ellipse.nextSibling?.nextSibling?.let { it.nodeName == "text" && it.nodeValue == "H" }!! -> "history"
                    else -> "initial"
                }
                Pair(elementName, extractRectangle(ellipse))
            }.toMap()

        return stateMap.plus(otherNodeMap)
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
}
