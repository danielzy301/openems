package io.openems.core.utilities.websocket;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.openems.api.exception.ConfigException;
import io.openems.api.exception.NotImplementedException;
import io.openems.api.exception.OpenemsException;
import io.openems.core.Config;
import io.openems.core.Databus;
import io.openems.core.utilities.JsonUtils;

/**
 * Handles a Websocket connection to a browser, femsserver,...
 *
 * @author stefan.feilmeier
 */
public class WebsocketHandler {

	protected final static String DEFAULT_DEVICE_NAME = "fems";

	private static Logger log = LoggerFactory.getLogger(WebsocketHandler.class);

	/**
	 * Holds thingId and channelId, subscribed by this websocket
	 */
	private final HashMultimap<String, String> subscribedChannels = HashMultimap.create();

	/**
	 * Executor for subscriptions task
	 */
	private static ScheduledExecutorService subscriptionExecutor = Executors.newScheduledThreadPool(1);

	/**
	 * Task regularly send subscriped to data
	 */
	private final Runnable subscriptionTask;

	/**
	 * Holds the scheduled subscription task
	 */
	private ScheduledFuture<?> subscriptionFuture = null;

	/**
	 * Holds the databus singleton
	 */
	private final Databus databus;

	/**
	 * Holds the websocket connection
	 */
	protected final WebSocket websocket;

	public WebsocketHandler(WebSocket websocket) {
		this.databus = Databus.getInstance();
		this.websocket = websocket;
		this.subscriptionTask = () -> {
			/*
			 * This task is executed regularly. Sends data to websocket.
			 */
			JsonObject j = new JsonObject();
			JsonObject jCurrentdata = getSubscribedData();
			j.add("currentdata", jCurrentdata);
			this.send(j);
		};
	}

	/**
	 * Message event of websocket. Handles a new message.
	 */
	public void onMessage(JsonObject jMessage) {
		log.info("Websocket message: " + jMessage.toString());
		/*
		 * Subscribe to data
		 */
		if (jMessage.has("subscribe")) {
			subscribe(jMessage.get("subscribe"));
		}
	}

	/**
	 * Handle subscriptions
	 *
	 * @param j
	 */
	private synchronized void subscribe(JsonElement j) {
		try {
			// unsubscribe regular task
			if (subscriptionFuture != null) {
				subscriptionFuture.cancel(true);
			}
			// clear subscriptions
			this.subscribedChannels.clear();
			// add subscriptions
			if (j.isJsonObject()) {
				JsonObject jThings = JsonUtils.getAsJsonObject(j);
				jThings.entrySet().forEach(entry -> {
					try {
						String thing = entry.getKey();
						JsonArray jChannels = JsonUtils.getAsJsonArray(entry.getValue());
						for (JsonElement jChannel : jChannels) {
							String channel = JsonUtils.getAsString(jChannel);
							this.subscribedChannels.put(thing, channel);
						}
					} catch (OpenemsException e) {
						log.error(e.getMessage());
					}
				});
			}
			// schedule task
			if (!this.subscribedChannels.isEmpty()) {
				subscriptionFuture = subscriptionExecutor.scheduleWithFixedDelay(this.subscriptionTask, 0, 3,
						TimeUnit.SECONDS);
			}
		} catch (OpenemsException e) {
			log.error(e.getMessage());
		}
	}

	/**
	 * Gets a json object with all subscribed channels
	 *
	 * @return
	 */
	private JsonObject getSubscribedData() {
		JsonObject jData = new JsonObject();
		subscribedChannels.keys().forEach(thingId -> {
			JsonObject jThingData = new JsonObject();
			subscribedChannels.get(thingId).forEach(channelId -> {
				Optional<?> value = databus.getValue(thingId, channelId);
				JsonElement jValue;
				try {
					jValue = JsonUtils.getAsJsonElement(value.orElse(null));
					jThingData.add(channelId, jValue);
				} catch (NotImplementedException e) {
					log.error(e.getMessage());
				}
			});
			jData.add(thingId, jThingData);
		});
		return jData;
	}

	/**
	 * Sends a message to the websocket
	 *
	 * @param jMessage
	 */
	public boolean send(JsonObject jMessage) {
		try {
			this.websocket.send(jMessage.toString());
			return true;
		} catch (WebsocketNotConnectedException e) {
			return false;
		}
	}

	/**
	 * Sends an initial message to the browser after it was successfully connected
	 */
	public boolean sendConnectionSuccessfulReply() {
		return this.send(this.createConnectionSuccessfulReply());
	}

	/**
	 * Creates an initial message to the browser after it was successfully connected
	 *
	 * <pre>
	 * {
	 *   metadata: {
	 *     devices: [{
	 *       name: {...},
	 *       config: {...}
	 *       online: true
	 *     }],
	 *     backend: "openems"
	 *   }
	 * }
	 * </pre>
	 *
	 * @param handler
	 */
	protected JsonObject createConnectionSuccessfulReply() {
		JsonObject j = new JsonObject();

		// Metadata
		JsonObject jMetadata = new JsonObject();
		try {
			jMetadata.add("config", Config.getInstance().getMetaConfigJson());
		} catch (ConfigException e) {
			log.error(e.getMessage());
		}
		jMetadata.addProperty("backend", "openems");
		j.add("metadata", jMetadata);

		return j;
	}

	/**
	 * Send a notification message/error to the websocket
	 *
	 * @param mesage
	 * @return true if successful, otherwise false
	 */
	// TODO
	// public synchronized boolean sendNotification(NotificationType type, String message) {
	// // log message to syslog
	// switch (type) {
	// case INFO:
	// case SUCCESS:
	// log.info(message);
	// break;
	// case ERROR:
	// log.error(message);
	// break;
	// case WARNING:
	// log.warn(message);
	// break;
	// }
	// // send notification to websocket
	// JsonObject jMessage = new JsonObject();
	// jMessage.addProperty("type", type.name().toLowerCase());
	// jMessage.addProperty("message", message);
	// return send(true, "notification", jMessage);
	// }
}