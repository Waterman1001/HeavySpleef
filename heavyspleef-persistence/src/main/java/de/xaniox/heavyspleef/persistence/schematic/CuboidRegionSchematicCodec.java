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
package de.xaniox.heavyspleef.persistence.schematic;

import com.google.common.collect.Lists;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;

import java.util.List;
import java.util.Map;

public class CuboidRegionSchematicCodec implements SchematicRegionMetadataCodec<CuboidRegion> {

	@Override
	public void apply(Map<String, Tag> tags, CuboidRegion region) {
		BlockVector3 pos1 = region.getPos1();
		BlockVector3 pos2 = region.getPos2();
		
		List<IntTag> pos1List = Lists.newArrayList();
		pos1List.add(new IntTag(pos1.getBlockX()));
		pos1List.add(new IntTag(pos1.getBlockY()));
		pos1List.add(new IntTag(pos1.getBlockZ()));
		
		ListTag pos1Tag = new ListTag(IntTag.class, pos1List);
		
		List<IntTag> pos2List = Lists.newArrayList();
		pos2List.add(new IntTag(pos2.getBlockX()));
		pos2List.add(new IntTag(pos2.getBlockY()));
		pos2List.add(new IntTag(pos2.getBlockZ()));
		
		ListTag pos2Tag = new ListTag(IntTag.class, pos2List);
		
		tags.put("pos1", pos1Tag);
		tags.put("pos2", pos2Tag);
	}

	@Override
	public CuboidRegion asRegion(Map<String, Tag> tags) {
		ListTag pos1Tag = (ListTag) tags.get("pos1");
		ListTag pos2Tag = (ListTag) tags.get("pos2");
		
		int pos1X = pos1Tag.getInt(0);
		int pos1Y = pos1Tag.getInt(1);
		int pos1Z = pos1Tag.getInt(2);
		
		int pos2X = pos2Tag.getInt(0);
		int pos2Y = pos2Tag.getInt(1);
		int pos2Z = pos2Tag.getInt(2);

		BlockVector3 pos1 = BlockVector3.at(pos1X, pos1Y, pos1Z);
		BlockVector3 pos2 = BlockVector3.at(pos2X, pos2Y, pos2Z);
		
		return new CuboidRegion(pos1, pos2);
	}
	
}