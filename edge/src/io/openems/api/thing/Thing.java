/*******************************************************************************
 * OpenEMS - Open Source Energy Management System
 * Copyright (c) 2016, 2017 FENECON GmbH and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *   FENECON GmbH - initial API and implementation and initial documentation
 *******************************************************************************/
package io.openems.api.thing;

import io.openems.api.channel.ReadChannel;
import io.openems.api.channel.thingstate.ThingState;
import io.openems.api.channel.thingstate.ThingStateChannels;
import io.openems.api.doc.ChannelInfo;

public interface Thing {

	public String id();

	public default String getAlias() {
		return id();
	}

	public default void addListener(ThingChannelsUpdatedListener listener) {

	}

	public default void removeListener(ThingChannelsUpdatedListener listener) {

	}

	public default void init() {

	}

	@ChannelInfo(type = ThingState.class)
	public ThingStateChannels getStateChannel();

	@SuppressWarnings("unchecked")
	public default ReadChannel<Boolean>[] getFaultChannels(){
		ThingStateChannels stateChannel = getStateChannel();
		return stateChannel.getFaultChannels().toArray(new ReadChannel[stateChannel.getFaultChannels().size()]);
	}

	@SuppressWarnings("unchecked")
	public default ReadChannel<Boolean>[] getWarningChannels(){
		ThingStateChannels stateChannel = getStateChannel();
		return stateChannel.getWarningChannels().toArray(new ReadChannel[stateChannel.getWarningChannels().size()]);
	}
}
