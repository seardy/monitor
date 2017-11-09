package co.unicordoba.Views


import co.unicordoba.Helpers.parseFileToQueue
import javafx.animation.Animation
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCombination
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.shape.SVGPath
import javafx.util.Duration
import tornadofx.View
import tornadofx.times
import java.text.SimpleDateFormat
import java.util.*


class HomeView : View() {
    override val root : HBox by fxml("/Views/home.fxml")

    // Properties
    private val image: ImageView by fxid("image")
    private val box: VBox by fxid("box")
    private val timer: Label by fxid("timer")
    private val finger: SVGPath by fxid("fingerprintSvg")
    private val titulo: Label by fxid("titulo")


    init {

        val queue = parseFileToQueue()

        titulo.prefWidthProperty().bind(box.widthProperty())

        //root.prefHeightProperty().bind(primaryStage.maxHeightProperty())
        image.fitWidthProperty().bind(root.widthProperty() * 0.70)
        image.fitHeightProperty().bind(root.heightProperty())
        val clockImage = Timeline(KeyFrame(Duration.ZERO, EventHandler {
                image.image = Image(queue.peek())
                queue.add(queue.poll());
        }),  KeyFrame(Duration.minutes(1.00))
        )

        clockImage.cycleCount = Animation.INDEFINITE
        clockImage.play()

        val clock = Timeline(KeyFrame(Duration.ZERO, EventHandler {
            val cal = Calendar.getInstance()
            val sdf = SimpleDateFormat("hh:mm a")
            sdf.timeZone = TimeZone.getTimeZone("America/Bogota")
            val str = sdf.format(cal.time)

            timer.text = str.toLowerCase()
        }),
                KeyFrame(Duration.seconds(1.0))
        )
        clock.cycleCount = Animation.INDEFINITE
        clock.play()

        /*
        primaryStage.scene.accelerators.put(
                KeyCombination.keyCombination("CTRL+C"),
                Runnable { Platform.exit() }
        )
        */

    }
}