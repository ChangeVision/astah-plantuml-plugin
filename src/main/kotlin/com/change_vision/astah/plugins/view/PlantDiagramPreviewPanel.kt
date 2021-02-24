package com.change_vision.astah.plugins.view

import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.nio.file.Files
import javax.imageio.ImageIO
import javax.swing.JPanel

class PlantDiagramPreviewPanel : JPanel() {
    var image: BufferedImage? = null
    override fun paint(g: Graphics?) {
        if (image != null) {
            g?.clearRect(0, 0, width, height)
            g?.drawImage(image, 0, 0, this)
        }
    }

    fun updateImage(reader: SourceStringReader) {
        val tempPngFile = Files.createTempFile("plantuml_", ".png").toFile()
        tempPngFile.outputStream().use { os ->
            reader.outputImage(os, 0, FileFormatOption(FileFormat.PNG))
        }
        this.image = ImageIO.read(tempPngFile)
        tempPngFile.delete()

        repaint()
    }
}