/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2015 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;
import java.util.Set;

import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.presets.BooleanFlag;

@Flag(name = "allow-fly", parent = FlagSpectate.class)
public class FlagAllowSpectateFly extends BooleanFlag {
	
	@Override
	public void onFlagRemove(Game game) {
		FlagSpectate flag = (FlagSpectate) getParent();
		Set<SpleefPlayer> spectators = flag.getSpectators();
		
		for (SpleefPlayer player : spectators) {
			Player bukkitPlayer = player.getBukkitPlayer();
			bukkitPlayer.setAllowFlight(false);
			bukkitPlayer.setFlying(false);
		}
	}
	
	@Override
	public void onFlagAdd(Game game) {
		FlagSpectate flag = (FlagSpectate) getParent();
		Set<SpleefPlayer> spectators = flag.getSpectators();
		
		for (SpleefPlayer player : spectators) {
			player.getBukkitPlayer().setAllowFlight(getValue());
		}
	}
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Enables the ability to fly while spectating");
	}
	
	/* Spectate leave is handled by restoring the player state in parent flag */
	public void onSpectateEnter(SpleefPlayer spleefPlayer) {		
		boolean value = getValue();
		Player player = spleefPlayer.getBukkitPlayer();
		
		player.setAllowFlight(value);
		player.setFlying(value);
	}

}
