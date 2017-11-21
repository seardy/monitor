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
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Paint
import javafx.scene.shape.SVGPath
import javafx.util.Duration
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import tornadofx.*
import java.text.SimpleDateFormat
import java.util.*


class HomeView : View() {

    // Properties
    override val root : HBox by fxml("/Views/home.fxml")
    private val image: ImageView by fxid("image")
    private val box: VBox by fxid("box")
    private val finger: SVGPath by fxid("fingerprintSvg")
    private val timer: Label by fxid("timer")
    private val status: Label by fxid("status")
    private val name: Label by fxid("name")
    private val operation: Label by fxid("operationTitle")
    // Images Queue
    private val queue = parseFileToQueue()

    init {
        // Responsive ui layout
        image.fitWidthProperty().bind(root.widthProperty() * 0.70)
        image.fitHeightProperty().bind(root.heightProperty())
        timer.prefWidthProperty().bind(box.widthProperty())

        //  Image slider
        val clockImage = Timeline(KeyFrame(Duration.minutes(1.00), EventHandler {
            image.image = queue.element()
            queue.add(queue.element())
            queue.remove()
        }),  KeyFrame(Duration.ZERO)
        )
        // Clock countdown
        val clock = Timeline(KeyFrame(Duration.seconds(1.00), EventHandler {
            val cal = Calendar.getInstance()
            val sdf = SimpleDateFormat("hh:mm a")
            sdf.timeZone = TimeZone.getTimeZone("America/Bogota")
            val str = sdf.format(cal.time)

            timer.text = str.toLowerCase()
        }),
                KeyFrame(Duration.ZERO)
        )

        clockImage.cycleCount = Animation.INDEFINITE
        clockImage.play()
        clock.cycleCount = Animation.INDEFINITE
        clock.play()

        val client = MqttClient("tcp://localhost:1883", "Monitor")
        client.connect()
        client.subscribe("search/waiting", {
            topic, message -> onWaitingFinger(String(message.payload))
        })
        client.subscribe("search/found", {
            topic, message -> onFingerFound(String(message.payload), client)
        })
        client.subscribe("search/processing", {
            topic, message -> onFingerProcessing()
        })
        client.subscribe("search/notFound", {
            _, _ -> onNotFound(client)
        })
        client.subscribe("search/error", {
            _, _ -> onError(client)
        })
        // Enroll events
        client.subscribe("enroll/abort", {
            _, message -> Platform.runLater {
            defaultMode()
        }
        })
        client.subscribe("enroll/begin", {
            _, message -> run {
            val json = loadJsonObject(String(message.payload))
            Platform.runLater {
                this.operation.text = "Operación de registro"
                this.name.text = json.getString("nombre")
            }
        }
        })
        client.subscribe("enroll/waiting", {
            _, message -> onWaitingFinger(String(message.payload))
        })
        client.subscribe("enroll/processing", {
            _, _ -> onFingerProcessing()
        })
        client.subscribe("enroll/exist", {
            topic , message ->
            run {
                Platform.runLater {
                    this.status.text = String(message.payload)
                    box.style {
                        backgroundColor += c("#f44336")
                    }
                    // Wait to back to normal mode
                    Timeline(KeyFrame(Duration.seconds(2.50), EventHandler {
                        defaultMode()
                        client.publish("search/finished", MqttMessage())
                    }),
                            KeyFrame(Duration.ZERO)
                    ).play()
                }
            }
        })
        client.subscribe("enroll/distint", {
            topic , message ->
            run {
                Platform.runLater {
                    this.status.text = String(message.payload)
                    box.style {
                        backgroundColor += c("#f44336")
                    }
                    // Wait to back to normal mode
                    Timeline(KeyFrame(Duration.seconds(2.50), EventHandler {
                        defaultMode()
                        client.publish("search/finished", MqttMessage())
                    }),
                            KeyFrame(Duration.ZERO)
                    ).play()
                }
            }
        })
        client.subscribe("enroll/successful", {
            topic , message ->
            run {
                Platform.runLater {
                    this.status.text = String(message.payload)
                    box.style {
                        backgroundColor += c("#4caf50")
                    }
                    // Wait to back to normal mode
                    Timeline(KeyFrame(Duration.seconds(2.50), EventHandler {
                        defaultMode()
                        client.publish("search/finished", MqttMessage())
                    }),
                            KeyFrame(Duration.ZERO)
                    ).play()
                }
            }
        })
        client.publish("search/finished", MqttMessage())
    }

    private fun onError(client: MqttClient) {
        Platform.runLater {
            finger.style {
                fill = Paint.valueOf("white")
            }
            box.style {
                backgroundColor += c("#f44336")
            }
            status.text = "No tiene horario para hoy"
            // Wait 5 seconds
            Timeline(KeyFrame(Duration.seconds(2.50), EventHandler {
                defaultMode()
                client.publish("search/finished", MqttMessage())
            }),
                    KeyFrame(Duration.ZERO)
            ).play()
        }
    }
    private fun onWaitingFinger(value: String) {
        Platform.runLater {
            this.status.text = value
        }
    }
    private fun onFingerProcessing(){
        Platform.runLater {
            finger.style {
                fill = Paint.valueOf("black")
            }
        }
    }
    private fun onFingerFound(json: String, client: MqttClient){
        val obj = loadJsonObject(json)
        val name = obj.getString("name")
        val atiempo = obj.getBoolean("atiempo")

        Platform.runLater {
            finger.style {
                fill = Paint.valueOf("white")
            }
            if(!atiempo)
                box.style {
                    backgroundColor += c("#ffa000")
                }
            else {
                box.style {
                    backgroundColor += c("#4caf50")
                }
            }
            this.operation.text = name
            if (atiempo)
                status.text = "A tiempo"
            else
                status.text =  "A destiempo"

            val clock = Timeline(KeyFrame(Duration.seconds(2.50), EventHandler {
                defaultMode()
                client.publish("search/finished", MqttMessage())
            }),
                    KeyFrame(Duration.ZERO)
            )
            clock.play()

        }
    }
    private fun onNotFound(client: MqttClient) {
        Platform.runLater {
            box.style {
                backgroundColor += c("#f44336")
            }
            finger.style {
                fill = Paint.valueOf("white")
            }
            name.text = "Lectura incorrecta o"
            status.text = "no se encuentra en el sistema"
            // Wait 5 seconds
            Timeline(KeyFrame(Duration.seconds(2.50), EventHandler {
                defaultMode()
                client.publish("search/finished", MqttMessage())
            }),
                    KeyFrame(Duration.ZERO)
            ).play()

        }
    }
    private fun defaultMode(){
        finger.style {
            fill = Paint.valueOf("white")
        }
        box.style {
            backgroundColor += c("#ff7043")
        }
        name.text = ""
        status.text = ""
        operation.text = ""
    }

}