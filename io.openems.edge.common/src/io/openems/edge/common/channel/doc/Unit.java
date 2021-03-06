package io.openems.edge.common.channel.doc;

import io.openems.common.types.OpenemsType;

public enum Unit {
	/*
	 * Generic
	 */

	/**
	 * No Unit
	 */
	NONE(""),
	/**
	 * Percentage [%], 0-100
	 */
	PERCENT("%"),
	/**
	 * On or Off
	 */
	ON_OFF(""),

	/*
	 * Power
	 */

	/**
	 * Unit of Active Power [W]
	 */
	WATT("W"),
	/**
	 * Unit of Active Power [mW]
	 */
	MILLIWATT("W", WATT, -3),
	/*
	 * Unit of Reactive Power [var]
	 */
	VOLT_AMPERE_REACTIVE("var"),
	/*
	 * Unit of Apparent Power [VA]
	 */
	VOLT_AMPERE("VA"),

	/*
	 * Voltage
	 */

	/**
	 * Unit of Voltage [V]
	 */
	VOLT("V"),
	/**
	 * Unit of Voltage [mV]
	 */
	MILLIVOLT("mV", VOLT, -3),

	/*
	 * Current
	 */

	/**
	 * Unit of Current [A]
	 */
	AMPERE("A"),
	/**
	 * Unit of Current [mA]
	 */
	MILLIAMPERE("mA", AMPERE, -3),

	/*
	 * Energy
	 */

	/**
	 * Unit of Energy [Wh]
	 */
	WATT_HOURS("Wh"),

	/*
	 * Frequency
	 */

	/**
	 * Unit of Frequency [Hz]
	 */
	HERTZ("Hz"),
	/**
	 * Unit of Frequency [mHz]
	 */
	MILLIHERTZ("mHz", HERTZ, -3),

	/*
	 * Temperature
	 */
	/**
	 * Unit of Temperature [�C]
	 */
	DEGREE_CELCIUS("�C"),

	/*
	 * Time
	 */
	/**
	 * Unit of Time in Seconds [s]
	 */
	SECONDS("sec");

	private final Unit baseUnit;
	private final int scaleFactor;
	private final String symbol;

	private Unit(String symbol) {
		this(symbol, null, 0);
	}

	private Unit(String symbol, Unit baseUnit, int scaleFactor) {
		this.symbol = symbol;
		this.baseUnit = baseUnit;
		this.scaleFactor = scaleFactor;
	}

	public Unit getBaseUnit() {
		return baseUnit;
	}

	public int getAsBaseUnit(int value) {
		return (int) (value * Math.pow(10, this.scaleFactor));
	}

	public String getSymbol() {
		return symbol;
	}

	public String format(Object value, OpenemsType type) {
		switch (this) {
		case NONE:
			return value.toString();
		case AMPERE:
		case DEGREE_CELCIUS:
		case HERTZ:
		case MILLIAMPERE:
		case MILLIHERTZ:
		case MILLIVOLT:
		case PERCENT:
		case VOLT:
		case VOLT_AMPERE:
		case VOLT_AMPERE_REACTIVE:
		case WATT:
		case WATT_HOURS:
		case SECONDS:
			return value + " " + this.symbol;
		case ON_OFF:
			boolean booleanValue = (Boolean) value;
			return booleanValue ? "ON" : "OFF";
		default:
			break;
		}
		return "FORMAT_ERROR"; // should never happen, if 'switch' is complete
	}

	public String formatAsBaseUnit(Object value, OpenemsType type) {
		if (this.baseUnit != null) {
			switch (type) {
			case SHORT:
			case INTEGER:
			case LONG:
			case FLOAT:
			case DOUBLE:
				return this.baseUnit.formatAsBaseUnit(this.getAsBaseUnit((int) value), type);
			case BOOLEAN:
			case STRING:
				return this.baseUnit.formatAsBaseUnit(value, type);
			}
		} else {
			this.format(value, type);
		}
		return "FORMAT_ERROR"; // should never happen, if 'switch' is complete
	}
}
