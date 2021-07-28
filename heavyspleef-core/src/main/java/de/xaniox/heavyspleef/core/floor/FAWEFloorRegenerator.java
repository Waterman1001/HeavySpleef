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
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;

public class FAWEFloorRegenerator implements FloorRegenerator {

    @Override
    public void regenerate(Floor floor, EditSession session, RegenerationCause cause) {
        Clipboard clipboard = floor.getClipboard();
        Region region = clipboard.getRegion();
        World world = region.getWorld();

        if (world == null) {
            throw new IllegalStateException("World of floor " + floor.getName() + " is null!");
        }

        ClipboardHolder holder = new ClipboardHolder(clipboard);

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
