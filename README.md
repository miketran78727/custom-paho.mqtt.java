# custom-paho.mqtt.java
This private build of the Java Client include the following enhancements:
* Setting fixed interval of reconnect attempts
* Monitoring client state support

To use this custom Java client, the application must implement **MqttCallbackExtended** interface. The **monitor()** method will be called at a fixed interval specified by the application at via connect options.

See [sample test](https://github.com/miketran78727/custom-paho.mqtt.java/blob/master/MqttAsyncClientTest.java) for usage
