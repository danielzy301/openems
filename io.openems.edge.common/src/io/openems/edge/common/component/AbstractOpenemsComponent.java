package io.openems.edge.common.component;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.edge.common.channel.Channel;

/**
 * This is the default implementation of the {@link OpenemsComponent} interface.
 * 
 * {@link AbstractOpenemsComponent#activate()} and
 * {@link AbstractOpenemsComponent#deactivate()} methods should be called by the
 * corresponding methods in the OSGi component.
 */
public abstract class AbstractOpenemsComponent implements OpenemsComponent {

	private final static AtomicInteger NEXT_GENERATED_COMPONENT_ID = new AtomicInteger(-1);

	private final Logger log = LoggerFactory.getLogger(AbstractOpenemsComponent.class);

	/**
	 * Holds all Channels by their Channel-ID String representation (in
	 * CaseFormat.UPPER_CAMEL)
	 */
	private final Map<String, Channel<?>> channels = Collections.synchronizedMap(new HashMap<>());

	private String id = null;
	private String servicePid = null;
	private ComponentContext componentContext = null;
	private boolean enabled = true;

	/**
	 * Handles @Activate of implementations. Prints log output.
	 * 
	 * @param id
	 */
	protected void activate(ComponentContext context, String service_pid, String id, boolean enabled) {
		if (id == null || id.trim().equals("")) {
			this.id = "_component" + AbstractOpenemsComponent.NEXT_GENERATED_COMPONENT_ID.incrementAndGet();
		} else {
			this.id = id;
		}
		this.servicePid = service_pid;
		this.enabled = enabled;
		this.componentContext = context;
		if (isEnabled()) {
			this.logMessage("Activate");
		} else {
			this.logMessage("Activate DISABLED");
		}
	}

	/**
	 * Handles @Deactivate of implementations. Prints log output.
	 * 
	 * @param id
	 */
	protected void deactivate() {
		this.logMessage("Deactivate");
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}

	@Override
	public String servicePid() {
		return this.servicePid;
	}

	@Override
	public ComponentContext componentContext() {
		return this.componentContext;
	}

	private void logMessage(String reason) {
		String packageName = this.getClass().getPackage().getName();
		if (packageName.startsWith("io.openems.")) {
			packageName = packageName.substring(11);
		}
		this.logInfo(this.log, reason + " " + this.getClass().getSimpleName() + " [" + packageName + "]");
	}

	@Override
	public String id() {
		return this.id;
	}

	@Override
	public Channel<?> _channel(String channelName) {
		Channel<?> channel = this.channels.get(channelName);
		return channel;
	}

	protected void addChannel(Channel<?> channel) {
		if (channel == null) {
			throw new NullPointerException(
					"Trying to add 'null' Channel. Hint: Check for missing handling of Enum value.");
		}
		this.channels.put(channel.channelId().id(), channel);
	}

	@Override
	public Collection<Channel<?>> channels() {
		return this.channels.values();
	}

	/**
	 * Log an info message including the Component ID.
	 * 
	 * @param log
	 * @param message
	 */
	protected void logInfo(Logger log, String message) {
		log.info("[" + this.id() + "] " + message);
	}

	/**
	 * Log a warn message including the Component ID.
	 * 
	 * @param log
	 * @param message
	 */
	protected void logWarn(Logger log, String message) {
		log.warn("[" + this.id() + "] " + message);
	}

	/**
	 * Log an error message including the Component ID.
	 * 
	 * @param log
	 * @param message
	 */
	protected void logError(Logger log, String message) {
		log.error("[" + this.id() + "] " + message);
	}
}
