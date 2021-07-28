/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2016 Matthias Werning
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
package de.xaniox.heavyspleef.migration;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import de.xaniox.heavyspleef.core.floor.Floor;
import de.xaniox.heavyspleef.core.floor.SimpleClipboardFloor;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.persistence.schematic.FloorAccessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FloorMigrator implements Migrator<File, OutputStream> {

	private final FloorAccessor accessor = new FloorAccessor();

	@Override
	public void migrate(File inputSource, OutputStream outputSource, Object cookie) throws MigrationException {

		if (cookie == null || !(cookie instanceof Game)) {
			throw new MigrationException("Cookie must be the instance of a Game");
		}

		ClipboardFormat format = ClipboardFormats.findByFile(inputSource);

		Game game = (Game) cookie;
		Clipboard clipboard;

		try (ClipboardReader reader = format.getReader(new FileInputStream(inputSource))) {
			clipboard = reader.read();
		} catch (IOException e) {
			throw new MigrationException(e);
		}
		
		String fileName = inputSource.getName();
		String floorName = "floor_" + fileName.substring(0, fileName.lastIndexOf('.'));
		
		Region region = clipboard.getRegion();
		World world = new BukkitWorld(game.getWorld());
		region.setWorld(world);
		
		Floor floor = new SimpleClipboardFloor(floorName, clipboard);
		
		try {
			accessor.write(outputSource, floor);
		} catch (IOException e) {
			throw new MigrationException(e);
		}
	}

}