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
package de.xaniox.heavyspleef.core;

import org.bukkit.Material;
import org.bukkit.material.MaterialData;

public class MaterialDataMatcher {

	private static final String DATA_DELIMITER = ":";
	
	private String input;
	private MaterialData result;
	
	private MaterialDataMatcher(String input) {
		this.input = input;
	}
	
	public static MaterialDataMatcher newMatcher(String input) {
		return new MaterialDataMatcher(input);
	}
	
	@SuppressWarnings("deprecation")
	public void match() {
		String[] components = input.split(DATA_DELIMITER);
		
		Material material = matchMaterialByName(components[0]);
		byte data = 0;
		
		if (material == null) {
			try {
				material = Material.getMaterial(components[0]);
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("\"" + components[0] + "\" is not a material");
			}
			
			if (material == null) {
				return;
			}
		}
		
		if (components.length > 1) {
			try {
				data = Byte.parseByte(components[1]);
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("\"" + components[1] + "\" is not a valid data value");
			}
		}
		
		result = new MaterialData(material, data);
	}
	
	private Material matchMaterialByName(String name) {
		name = name.replace("_", "");
		
		for (Material material : Material.values()) {
			String materialName = material.name().replace("_", "");
			
			if (name.equalsIgnoreCase(materialName)) {
				return material;
			}
		}
		
		return null;
	}
	
	public MaterialData result() {
		if (result == null) {
			throw new IllegalStateException("Must call match() first before calling result()");
		}
		
		return result;
	}

}