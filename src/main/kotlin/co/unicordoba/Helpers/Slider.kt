package co.unicordoba.Helpers

import javafx.scene.image.Image
import java.io.File
import java.util.*

fun parseFileToQueue(): Queue<Image> {
    val queue : Queue<Image> = LinkedList<Image>()
    File("/scal/Images.txt").useLines { lines -> lines.forEach { queue.add(Image(it)) } }
    return queue
}