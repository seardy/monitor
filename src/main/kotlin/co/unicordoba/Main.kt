package co.unicordoba

import co.unicordoba.Views.HomeView
import javafx.application.Application
import javafx.stage.Stage
import tornadofx.App

class MainApp : App(HomeView::class) {
    override fun start(stage: Stage) {
        super.start(stage)
        stage.isFullScreen = true
    }
}

fun main(args: Array<String>) {
    Application.launch(MainApp::class.java, *args)
}