package com.change_vision.astah.plugins.view

import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Image
import java.awt.Point
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import java.awt.image.BufferedImage
import java.awt.image.ImageObserver
import java.nio.file.Files
import javax.imageio.ImageIO
import javax.swing.*

class PlantDiagramPreviewPanel : JPanel() {
    private val plantImageLabel = JLabel().also {
        it.autoscrolls = true
    }
    private var zoom = 100
    private val minZoom = 10
    private val maxZoom = 1000

    private val mouseAdapter = object : MouseAdapter() {
        var origin: Point? = null
        override fun mousePressed(e: MouseEvent?) {
            if (e != null) {
                origin = Point(e.point)
            }
        }

        override fun mouseClicked(e: MouseEvent?) {
            if (e?.button == MouseEvent.BUTTON2) {
                zoom = 100
                updateScaledImage(image!!, 100)
            }
        }
        
        override fun mouseDragged(e: MouseEvent?) {
            if (origin == null || e == null) return
            val viewPort: JViewport = SwingUtilities
                .getAncestorOfClass(JViewport::class.java, plantImageLabel) as? JViewport ?: return
            val view = viewPort.viewRect
            view.x += origin!!.x - e.x
            view.y += origin!!.y - e.y
            plantImageLabel.scrollRectToVisible(view)
        }

        override fun mouseWheelMoved(e: MouseWheelEvent?) {
            if (e == null) return
            if (e.isControlDown) {
                val rotation = e.wheelRotation
                zoom -= rotation * 10
                zoom = when {
                    zoom < minZoom -> minZoom
                    zoom > maxZoom -> maxZoom
                    else -> zoom
                }
                updateScaledImage(image!!, zoom)
            }

            super.mouseWheelMoved(e)
        }
    }

    init {
        layout = BorderLayout()
        plantImageLabel.addMouseListener(mouseAdapter)
        plantImageLabel.addMouseMotionListener(mouseAdapter)
        plantImageLabel.addMouseWheelListener(mouseAdapter)
        val scrollPane = JScrollPane(plantImageLabel)
        add(scrollPane)
    }

    private val observer = ImageObserver { _, _, x, y, _, _ ->
        plantImageLabel.preferredSize = Dimension(x, y)
        true
    }

    private var image: BufferedImage? = null
    fun updateImage(reader: SourceStringReader) {
        val tempPngFile = Files.createTempFile("plantuml_", ".png").toFile()
        tempPngFile.outputStream().use { os ->
            reader.outputImage(os, 0, FileFormatOption(FileFormat.PNG))
        }
        image = ImageIO.read(tempPngFile)
        updateScaledImage(image!!, zoom)
        tempPngFile.delete()
    }

    private fun updateScaledImage(image: BufferedImage, scale: Int) {
        val imageIcon = ImageIcon(scaleImage(image, scale))
        imageIcon.imageObserver = observer
        plantImageLabel.icon = imageIcon
        repaint()
    }

    private fun scaleImage(image: BufferedImage, scale: Int) =
        when (zoom) {
            100 -> image
            else -> image.getScaledInstance(
                (image.width * (scale / 100f)).toInt(),
                (image.height * (scale / 100f)).toInt(),
                Image.SCALE_DEFAULT
            )
        }
}