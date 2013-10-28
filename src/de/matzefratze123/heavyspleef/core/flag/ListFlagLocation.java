package de.matzefratze123.heavyspleef.core.flag;

import java.io.Serializable;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.HeavySpleef;

public class ListFlagLocation extends ListFlag<ListFlagLocation.SerializeableLocation> {

	public ListFlagLocation(String name, List<SerializeableLocation> defaulte) {
		super(name, defaulte);
	}

	public void putElement(Player player, String input, List<SerializeableLocation> list) {
		Location location = player.getLocation();
		SerializeableLocation sLocation = new SerializeableLocation(location);
		
		list.add(sLocation);
		
		for (SerializeableLocation l : list) {
			System.out.println(l.x + "," + l.y + "," + l.z);
		}
		
		System.out.println(list.size());
	}

	@Override
	public String toInfo(Object value) {
		return getName() + ": LIST";
	}

	@Override
	public String getHelp() {
		return HeavySpleef.PREFIX + " /spleef flag <name> " + getName();
	}
	
	public static class SerializeableLocation implements Serializable {

		/* Serial Version UID */
		private static final long serialVersionUID = 6983776452848943576L;
		
		private double x, y, z;
		private String world;
		
		private float pitch;
		private float yaw;
		
		private transient Location holding;
		
		public SerializeableLocation(Location holding) {
			setBukkitLocation(holding);
		}

		public double getX() {
			return x;
		}

		public void setX(double x) {
			this.x = x;
		}

		public double getY() {
			return y;
		}

		public void setY(double y) {
			this.y = y;
		}

		public double getZ() {
			return z;
		}

		public void setZ(double z) {
			this.z = z;
		}

		public String getWorld() {
			return world;
		}

		public void setWorld(String world) {
			this.world = world;
		}

		public float getPitch() {
			return pitch;
		}

		public void setPitch(float pitch) {
			this.pitch = pitch;
		}

		public float getYaw() {
			return yaw;
		}

		public void setYaw(float yaw) {
			this.yaw = yaw;
		}

		public Location getBukkitLocation() {
			if (holding == null) {
				holding = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
			}
			
			return holding;
		}

		public void setBukkitLocation(Location holding) {
			this.holding = holding;
			
			setX(holding.getX());
			setY(holding.getY());
			setZ(holding.getZ());
			
			setWorld(holding.getWorld().getName());
			setPitch(holding.getPitch());
			setYaw(holding.getYaw());
		}
		
	}
	
}
