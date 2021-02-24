package com.change_vision.astah.plugins.converter

import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
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
        val tempSvgFile = Files.createTempFile("plantsvg_", ".svg").toFile()
        tempSvgFile.outputStream().use { os ->
            reader.outputImage(os, index, FileFormatOption(FileFormat.SVG))
        }
        val result = collectEntityBoundary(tempSvgFile)
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
                        val h = entityNode.attributes.getNamedItem("height").nodeValue.toFloat()
                        val w = entityNode.attributes.getNamedItem("width").nodeValue.toFloat()
                        val x = entityNode.attributes.getNamedItem("x").nodeValue.toFloat()
                        val y = entityNode.attributes.getNamedItem("y").nodeValue.toFloat()
                        entityBoundaryMap[entityCode] = Rectangle2D.Float(x, y, w, h)
                    }
                    else -> {
                    }
                }
            }
        }
        return entityBoundaryMap
    }
}