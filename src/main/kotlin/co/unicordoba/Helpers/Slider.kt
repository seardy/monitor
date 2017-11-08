package co.unicordoba.Helpers

import java.io.File
import java.util.*

fun parseFileToQueue() : Queue<String> {
    val queue : Queue<String> = LinkedList<String>()
    File("/scal/Images.txt").useLines { lines -> lines.forEach { queue.add(it) } }
    return queue
}