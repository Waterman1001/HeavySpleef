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
package de.xaniox.heavyspleef.core.floor;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class SimpleClipboardFloor implements Floor {
	
	private String name;
	private Clipboard floorClipboard;
	
	public SimpleClipboardFloor(String name, Clipboard clipboard) {
		this.name = name;
		this.floorClipboard = clipboard;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public Clipboard getClipboard() {
		return floorClipboard;
	}
	
	@Override
	public Region getRegion() {
		return floorClipboard.getRegion();
	}

	@Override
	public boolean contains(Block block) {
		return contains(block.getLocation());
	}
	
	@Override
	public boolean contains(Location location) {
		BlockVector3 pt = BukkitAdapter.asBlockVector(location);
		return floorClipboard.getRegion().contains(pt);
	}

	@Override
	@Deprecated
	public void generate(EditSession session) {
		Region region = floorClipboard.getRegion();
		
		ClipboardHolder holder = new ClipboardHolder(floorClipboard);
		
		Operation pasteOperation = holder.createPaste(session)
				.to(region.getMinimumPoint())
				.ignoreAirBlocks(true)
				.build();
		
		try {
			Operations.complete(pasteOperation);
		} catch (WorldEditException e) {
			throw new RuntimeException(e);
		}
	}
	
}