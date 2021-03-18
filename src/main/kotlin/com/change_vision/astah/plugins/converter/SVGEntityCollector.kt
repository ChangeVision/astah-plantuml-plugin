package com.change_vision.astah.plugins.converter

import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import net.sourceforge.plantuml.classdiagram.ClassDiagram
import net.sourceforge.plantuml.cucadiagram.LeafType
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
        val diagram = reader.blocks[index].diagram
        val result = when (diagram) {
            is ClassDiagram -> collectEntityBoundary(tempSvgFile)
            is StateDiagram -> {
                val stateNames = diagram.leafsvalues.filter { it.leafType == LeafType.STATE }.map { it.codeGetName }
                collectStateEntityBoundary(tempSvgFile, stateNames)
            }
            else -> emptyMap()
        }
        tempSvgFile.delete()
        return result
    }

    private fun collectEntityBoundary(svgFile: File): Map<String, Rectangle2D.Float> {
        val commentXpath = XPathFactory.newInstance().newXPath().compile("//comment()")
        val commentPattern = Pattern.compile("""MD5=\[(?<md5>\w+)\]\n(?<code>.*)""")
        val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = builder.parse(svgFile)
        val comments = commentXpath.evaluate(doc, XPathConstants.NODESET) as NodeList
        val entityBoundaryMap = mutableMapOf<String, Rectangle2D.Float>()

        for (i in 0 until comments.length) {
            val commentNode = comments.item(i)
            val comment = commentNode.nodeValue
            val commentMatcher = commentPattern.matcher(comment)
            if (commentMatcher.matches()) {
                val entityCode = commentMatcher.group("code")

                val entityNode = commentNode.nextSibling
                when (entityNode.nodeName) {
                    "rect" -> {
                        entityBoundaryMap[entityCode] = extractRectangle(entityNode)
                    }
                    else -> {
                    }
                }
            }
        }
        return entityBoundaryMap
    }

    fun collectStateEntityBoundary(svgFile: File, stateNames: List<String>): Map<String, Rectangle2D.Float> {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(svgFile)

        val stateMap = stateNames.mapNotNull { state ->
            val stateRect =
                XPathFactory.newInstance().newXPath()
                    .compile("//text[contains(text(),'$state')]/preceding-sibling::rect")
                    .evaluate(doc, XPathConstants.NODESET) as NodeList
            if (stateRect.length == 0) {
                null
            } else {
                val rect = extractRectangle(stateRect.item(0))
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
                    ellipse.nextSibling?.nextSibling?.nodeName == "text" -> "history"
                    else -> "initial"
                }
                Pair(elementName, extractRectangle(ellipse))
            }.toMap()

        return stateMap.plus(otherNodeMap)
    }

    private fun extractRectangle(node: Node): Rectangle2D.Float {
        val attrs = node.attributes
        return when (node.nodeName) {
            "ellipse" -> {
                val cx = attrs.getNamedItem("cx").nodeValue.toFloat()
                val cy = attrs.getNamedItem("cy").nodeValue.toFloat()
                val rx = attrs.getNamedItem("rx").nodeValue.toFloat()
                val ry = attrs.getNamedItem("ry").nodeValue.toFloat()
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

//fun main() {
//    val svg = File("C:\\Users\\shint\\git\\cvlab\\plantuml-plugin\\src\\test\\resources\\svg\\state_004.svg")
//    val states = listOf("State1", "State2", "State3", "Accumulate Enough Data", "ProcessData")
//    SVGEntityCollector.collectStateEntityBoundary(svg, states).forEach { (name, pos) ->
//        println("$name : ${pos.x} - ${pos.y}")
//    }
//}