package autoconnect.test;

import com.google.gson.JsonObject;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;

import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.joda.time.DateTime;

public class MqttAsyncClientTest implements MqttCallbackExtended{

	@Override
	public void monitor(Properties stateProps) {
		final String METHOD = "monitor";
		
		Boolean connected = (Boolean) stateProps.get("connected");
		Boolean resting = (Boolean) stateProps.get("resting");
		
		Long lastConnected = (Long) stateProps.get("lastConnected");
		DateTime dtLastConnected = new DateTime(lastConnected.longValue());
		
		Long lastPing = (Long) stateProps.get("lastPing");
		DateTime dtLastPing = new DateTime(lastPing.longValue());
		
		Long lastOutboundActivity = (Long) stateProps.get("lastOutboundActivity");
		DateTime dtLastOutbound = new DateTime(lastOutboundActivity.longValue());
		
		Long lastInboundActivity = (Long) stateProps.get("lastInboundActivity");
		DateTime dtLastInbound = new DateTime(lastInboundActivity.longValue());
		
		debug(METHOD, "connected(" + connected 
				+ ") resting(" + resting
				+ ") lastConnected(" + dtLastConnected
				+ ") lastPing (" + dtLastPing 
				+ ") lastOutboundActivity (" + dtLastOutbound
				+ ") lastInboundActivity (" + dtLastInbound
				+ ")");
	}

	@Override
	public void connectionLost(Throwable cause) {
		final String METHOD = "connectionLost";
		debug(METHOD, null);
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		final String METHOD = "messageArrived";
		debug(METHOD, null);
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		final String METHOD = "deliveryComplete";
		debug(METHOD, null);
	}

	@Override
	public void connectComplete(boolean reconnect, String serverURI) {
		final String METHOD = "connectComplete";		
		debug(METHOD, "Connected to " + serverURI + " Reconnect ? " + reconnect);
	}
	
	private void debug(String method, String message) {
		DateTime dt = new DateTime(new Date());
		if (message != null) {
			System.out.println(method + " " + dt + " " + message);	
		} else {
			System.out.println(method + " " + dt);
		}
	}
	
	
	public static void main(String[] args) {
		
		String org = "6lvecv";
		String deviceType = "Beaglebone";
		String deviceId = "Bone2";
		String token = "WATSON4ME";
		String topic = "iot-2/evt/status/fmt/json";
		
		String serverURI = "tcp://" + org + ".messaging.internetofthings.ibmcloud.com";
		String clientId = "d:" + org + ":" + deviceType + ":" + deviceId;
		
		System.out.println("START TEST");
		
		MqttAsyncClientTest myClient = new MqttAsyncClientTest();
		
		MqttAsyncClient client = null;
		MqttConnectOptions connOpts = null;
		DisconnectedBufferOptions disconnectedBufferOptions = null;
		try {
			client = new MqttAsyncClient(serverURI, clientId);
            connOpts = new MqttConnectOptions();
            connOpts.setKeepAliveInterval(30);
            connOpts.setCleanSession(true);
            connOpts.setAutomaticReconnect(true);
            connOpts.setAutomaticReconnectInterval(10);
            connOpts.setUserName("use-token-auth");
            connOpts.setPassword(token.toCharArray());
            connOpts.setStateMonitorInterval(10);
            
            disconnectedBufferOptions = new DisconnectedBufferOptions();
            disconnectedBufferOptions.setBufferEnabled(true);
            disconnectedBufferOptions.setBufferSize(100);
            disconnectedBufferOptions.setDeleteOldestMessages(true);
            
            client.setCallback(myClient);
            client.setBufferOpts(disconnectedBufferOptions);

		} catch (MqttException e2) {
			e2.printStackTrace();
			System.exit(1);
		}
		
		try {
			client.connect(connOpts).waitForCompletion(5000);
		} catch (MqttException e) {
			// Initial connect must succeed to continue...
			System.out.println("CONNECT FAILED (" + e.getReasonCode() + ") " + e.getMessage() );
			e.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);			
		}
		
		// Send events
		for (int count=1; count <= 10; count++) {
			try {
				Thread.sleep(20 * 1000);
				JsonObject data = new JsonObject();
				DateTime dt = new DateTime(new Date());
				data.addProperty("count", count);
				data.addProperty("localtime", dt.toString());
				System.out.println("Send status event #" + count + ": " + data);
				MqttMessage msg = new MqttMessage(data.toString().getBytes(Charset.forName("UTF-8")));
				client.publish(topic, msg);
			} catch (MqttException e) {
				System.out.println("PUBLISH FAILED (" + e.getReasonCode() + ") " + e.getMessage() );
				e.printStackTrace();
				break;
			} catch (Exception e) {
				System.out.println("PUBLISH FAILED (unexpected exception)" + e.getMessage() );
				e.printStackTrace();
				break;
			}
		}
		

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			//ignore
		}
		
		try {
			client.disconnect();
		} catch (MqttException e) {
			System.out.println("DISCONNECT FAILED (" + e.getReasonCode() + ") " + e.getMessage() );
			e.printStackTrace();
		}
		
		try {
			client.close();
		} catch (MqttException e) {
			System.out.println("CLOSE FAILED (" + e.getReasonCode() + ") " + e.getMessage() );
			e.printStackTrace();
		}
		
		System.out.println("END TEST");
		System.exit(0);

	}


}
