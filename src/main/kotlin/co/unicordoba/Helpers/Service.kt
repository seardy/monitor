package co.unicordoba.Helpers

import co.unicordoba.Views.HomeView
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage

class Service : MqttCallback {
    override fun messageArrived(p0: String?, p1: MqttMessage) {
        System.out.println(p1.payload.toString())
    }

    override fun connectionLost(p0: Throwable?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deliveryComplete(p0: IMqttDeliveryToken?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
