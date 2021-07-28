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
package de.xaniox.heavyspleef.persistence.xml;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.world.World;
import org.dom4j.Element;

import java.util.List;

public class Polygonal2DRegionXMLCodec implements XMLRegionMetadataCodec<Polygonal2DRegion> {

	@Override
	public void apply(Element applyTo, Polygonal2DRegion region) {
		List<BlockVector2> points = region.getPoints();
		int minY = region.getMinimumY();
		int maxY = region.getMaximumY();
		
		Element pointsElement = applyTo.addElement("points");
		for (BlockVector2 point : points) {
			Element pointElement = pointsElement.addElement("point");
			
			pointElement.addElement("x").addText(String.valueOf(point.getBlockX()));
			pointElement.addElement("z").addText(String.valueOf(point.getBlockZ()));
		}
		
		applyTo.addElement("minY").addText(String.valueOf(minY));
		applyTo.addElement("maxY").addText(String.valueOf(maxY));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Polygonal2DRegion asRegion(Element container) {
		Element pointsElement = container.element("points");
		Element minYElement = container.element("minY");
		Element maxYElement = container.element("maxY");
		
		List<Element> pointElementList = pointsElement.elements("point");
		List<BlockVector2> points = Lists.newArrayList();
		
		for (Element pointElement : pointElementList) {
			int x = Integer.parseInt(pointElement.elementText("x"));
			int z = Integer.parseInt(pointElement.elementText("z"));
			
			BlockVector2 point = BlockVector2.at(x, z);
			points.add(point);
		}
		
		int minY = Integer.parseInt(minYElement.getText());
		int maxY = Integer.parseInt(maxYElement.getText());
		
		return new Polygonal2DRegion((World)null, points, minY, maxY);
	}

}