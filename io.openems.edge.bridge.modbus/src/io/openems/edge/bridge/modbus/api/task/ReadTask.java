package io.openems.edge.bridge.modbus.api.task;

import com.ghgande.j2mod.modbus.ModbusException;

import io.openems.common.exceptions.OpenemsException;
import io.openems.edge.bridge.modbus.AbstractModbusBridge;
import io.openems.edge.bridge.modbus.api.element.AbstractModbusElement;
import io.openems.edge.bridge.modbus.api.element.ModbusElement;

/**
 * A Modbus 'ReadTask' is holding references to one or more Modbus
 * {@link AbstractModbusElement} which have register addresses in the same
 * range. The ReadTask handles the execution (query) on this range. @{link
 * WriteTask} inherits from ReadTask.
 * 
 * @author stefan.feilmeier
 */
public interface ReadTask {

	/**
	 * Sets the modbus unit id
	 * 
	 * @param unitId
	 */
	public void setUnitId(int unitId);

	/**
	 * Gets the ModbusElements
	 * 
	 * @return
	 */
	public ModbusElement<?>[] getElements();

	/**
	 * Gets the start modbus register address
	 * 
	 * @return
	 */
	public int getStartAddress();

	/**
	 * Gets the Priority of this ReadTask. The higher the priority, the more often
	 * the task is executed. (HIGH = execute in every cycle).
	 * 
	 * @return
	 */
	public Priority getPriority();

	/**
	 * Sends a query for this Task to the Modbus device
	 * 
	 * @param bridge
	 * @param unitId
	 * @throws ModbusException
	 */
	public abstract void executeQuery(AbstractModbusBridge bridge) throws OpenemsException;
}
