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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import de.xaniox.heavyspleef.core.HeavySpleef;
import de.xaniox.heavyspleef.core.extension.ExtensionLobbyWall;
import de.xaniox.heavyspleef.core.extension.ExtensionLobbyWall.SignRow.SignRowValidationException;
import de.xaniox.heavyspleef.core.flag.AbstractFlag;
import de.xaniox.heavyspleef.core.floor.Floor;
import de.xaniox.heavyspleef.core.floor.SimpleClipboardFloor;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.flag.defaults.*;
import de.xaniox.heavyspleef.flag.defaults.FlagTeam.TeamColor;
import de.xaniox.heavyspleef.flag.presets.*;
import de.xaniox.heavyspleef.persistence.xml.GameAccessor;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.unsynchronized.*;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

public class GameMigrator implements Migrator<Configuration, File> {

	private static final String FILE_EXTENSION = ".xml";
	private static final int TEAM_FLAG_NOT_SET = -1;
	private static final Map<String, Class<? extends AbstractFlag<?>>> LEGACY_TO_FLAG_MAPPING = Maps.newHashMap();
	
	static {
		LEGACY_TO_FLAG_MAPPING.put("shovels", FlagShovels.class);
		LEGACY_TO_FLAG_MAPPING.put("shears", FlagShears.class);
		LEGACY_TO_FLAG_MAPPING.put("bowspleef", FlagBowspleef.class);
		LEGACY_TO_FLAG_MAPPING.put("splegg", FlagSplegg.class);
		LEGACY_TO_FLAG_MAPPING.put("anticamping", FlagAntiCamping.class);
		LEGACY_TO_FLAG_MAPPING.put("scoreboard", FlagScoreboard.class);
		LEGACY_TO_FLAG_MAPPING.put("win", FlagWinPoint.class);
		LEGACY_TO_FLAG_MAPPING.put("lose", FlagLosePoint.class);
		LEGACY_TO_FLAG_MAPPING.put("lobby", FlagLobby.class);
		LEGACY_TO_FLAG_MAPPING.put("queuelobby", FlagQueueLobby.class);
		LEGACY_TO_FLAG_MAPPING.put("spectate", FlagSpectate.class);
		LEGACY_TO_FLAG_MAPPING.put("spawnpoint", FlagSpawnpoint.class);
		LEGACY_TO_FLAG_MAPPING.put("leavepoint", FlagLeavepoint.class);
		LEGACY_TO_FLAG_MAPPING.put("nextspawnpoint", FlagMultiSpawnpoint.class);
		LEGACY_TO_FLAG_MAPPING.put("itemreward", FlagItemReward.class);
		LEGACY_TO_FLAG_MAPPING.put("minplayers", FlagMinPlayers.class);
		LEGACY_TO_FLAG_MAPPING.put("maxplayers", FlagMaxPlayers.class);
		LEGACY_TO_FLAG_MAPPING.put("autostart", FlagAutostart.class);
		LEGACY_TO_FLAG_MAPPING.put("countdown", FlagCountdown.class);
		LEGACY_TO_FLAG_MAPPING.put("entryfee", FlagEntryFee.class);
		LEGACY_TO_FLAG_MAPPING.put("reward", FlagReward.class);
		LEGACY_TO_FLAG_MAPPING.put("timeout", FlagTimeout.class);
		LEGACY_TO_FLAG_MAPPING.put("regen", FlagRegen.class);
	}
	
	private final OutputFormat outputFormat = OutputFormat.createPrettyPrint();
	private final JDeserialize jdeserialize = new JDeserialize();
	private final SafeGameCreator gameCreator;
	private HeavySpleef heavySpleef;
	private int countMigrated;
	
	public GameMigrator(HeavySpleef heavySpleef) {
		this.heavySpleef = heavySpleef;
		this.gameCreator = new SafeGameCreator(heavySpleef);
	}
	
	public int getCountMigrated() {
		return countMigrated;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void migrate(Configuration inputSource, File outputFolder, Object cookie) throws MigrationException {
		if (cookie == null || !(cookie instanceof List<?>)) {
			throw new MigrationException("Cookie must be a game of lists");
		}
		
		countMigrated = 0;
		
		List<Game> gameList = (List<Game>) cookie;
		Set<String> gameNames = inputSource.getKeys(false);	
		
		for (String name : gameNames) {
			ConfigurationSection section = inputSource.getConfigurationSection(name);
			
			File xmlFile = new File(outputFolder, name + FILE_EXTENSION);
			if (xmlFile.exists()) {
				//Rename this game as there is already a file
				xmlFile = new File(outputFolder, name + "_1" + FILE_EXTENSION);
			}
			
			XMLWriter writer = null;
			
			try {
				xmlFile.createNewFile();
				
				GameAccessor accessor = new GameAccessor(heavySpleef);
				Document document = DocumentHelper.createDocument();
				Element rootElement = document.addElement("game");
				
				Game game = migrateGame(section, rootElement);
				if (game == null) {
					continue;
				}
				
				accessor.write(game, rootElement);
				gameList.add(game);
				
				OutputStream out = new FileOutputStream(xmlFile);
				writer = new XMLWriter(out, outputFormat);
				writer.write(document);
				++countMigrated;
			} catch (IOException e) {
				throw new MigrationException(e);
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException e) {}
				}
			}
		}
	}
	
	private Game migrateGame(ConfigurationSection in, Element element) throws MigrationException {
		String name = in.getName();
		World world;
		String type = in.getString("type");
		
		if ("CUBOID".equalsIgnoreCase(type)) {
			world = legacyStringToLocation(in.getString("first")).getWorld();
		} else if ("CYLINDER".equalsIgnoreCase(type)){
			world = legacyStringToLocation(in.getString("center")).getWorld();
		} else {
			heavySpleef.getLogger().warning("Cannot convert game '" + name + "': Unknown game type");
			return null;
		}
		
		//Create the game safely without calling the constructor
		Game game = gameCreator.createSafeGame(name, world);
		
		ConfigurationSection floorsSection = in.getConfigurationSection("floors");
		
		if (floorsSection != null) {
			for (String floorKey : floorsSection.getKeys(false)) {
				ConfigurationSection floorSection = floorsSection.getConfigurationSection(floorKey);
				String id = "floor_" + floorSection.getString("id");
				
				String shape = floorSection.getString("shape");
				Region region;
				
				if (shape.equals("CUBOID")) {
					BlockVector3 first = legacyStringToVector(floorSection.getString("first"));
					BlockVector3 second = legacyStringToVector(floorSection.getString("second"));
					
					region = new CuboidRegion(first, second);
				} else if (shape.equals("CYLINDER")) {
					//TODO: Add cylinder floor support for older versions of HeavySpleef?
					continue;
				} else {
					//Unknown floor type
					continue;
				}
				
				Clipboard clipboard = new BlockArrayClipboard(region);
				Floor floor = new SimpleClipboardFloor(id, clipboard);
				
				game.addFloor(floor);
			}
		}
		
		ConfigurationSection losezonesSection = in.getConfigurationSection("losezones");
		
		if (losezonesSection != null) {
			for (String losezoneKey : losezonesSection.getKeys(false)) {
				ConfigurationSection losezoneSection = losezonesSection.getConfigurationSection(losezoneKey);
				String id = "deathzone_" + losezoneSection.getString("id");
				
				BlockVector3 first = legacyStringToVector(losezoneSection.getString("first"));
				BlockVector3 second = legacyStringToVector(losezoneSection.getString("second"));
				Region region = new CuboidRegion(first, second);
				
				game.addDeathzone(id, region);
			}
		}
		
		ConfigurationSection flagsSection = in.getConfigurationSection("flags");
		boolean enableTeamGames = false;
		
		if (flagsSection != null) {
			for (String flagKey : flagsSection.getKeys(false)) {
				ConfigurationSection flagSection = flagsSection.getConfigurationSection(flagKey);
				String legacyValueString = flagSection.getString("value");
				
				if (flagKey.equals("team")) {
					//The team flag is the only flag that must be handled seperately
					enableTeamGames = extractFlagValue(legacyValueString, Boolean.class, null).booleanValue();
					continue;
				}
				
				AbstractFlag<?> flag = getFlag(flagKey, legacyValueString);
				if (flag == null) {
					heavySpleef.getLogger()
							.log(Level.WARNING,
									"Cannot migrate flag \"" + flagKey + "\" for game \"" + game.getName() + "\""
											+ " as this flag is no longer available in this version");
					continue;
				}
				
				game.addFlag(flag);
			}
		}
		
		ConfigurationSection teamsSection = in.getConfigurationSection("teams");
		List<TeamColor> colors = Lists.newArrayList();
		int minPlayers = TEAM_FLAG_NOT_SET;
		int maxPlayers = TEAM_FLAG_NOT_SET;
		
		if (teamsSection != null) {
			for (String teamString : teamsSection.getKeys(false)) {
				ConfigurationSection teamSection = teamsSection.getConfigurationSection(teamString);
				String color = teamSection.getString("color");
				int sectionMinPlayers = teamSection.getInt("min-players", TEAM_FLAG_NOT_SET);
				int sectionMaxPlayers = teamSection.getInt("max-players", TEAM_FLAG_NOT_SET);
				
				if (minPlayers == TEAM_FLAG_NOT_SET && sectionMinPlayers != TEAM_FLAG_NOT_SET) {
					minPlayers = sectionMinPlayers;
				}
				
				if (maxPlayers == TEAM_FLAG_NOT_SET && sectionMaxPlayers != TEAM_FLAG_NOT_SET) {
					maxPlayers = sectionMaxPlayers;
				}
				
				color = color.toUpperCase();
				TeamColor teamColor = TeamColor.valueOf(color);
				
				colors.add(teamColor);
			}
		}
		
		//We need at least two teams
		if (enableTeamGames && colors.size() > 1) {
			FlagTeam teamFlag = new FlagTeam();
			teamFlag.setValue(colors);
			game.addFlag(teamFlag);
			
			if (minPlayers != TEAM_FLAG_NOT_SET) {
				FlagMinTeamSize minTeamSizeFlag = new FlagMinTeamSize();
				minTeamSizeFlag.setValue(minPlayers);
				game.addFlag(minTeamSizeFlag);
			}
			
			if (maxPlayers != TEAM_FLAG_NOT_SET) {
				FlagMaxTeamSize maxTeamSizeFlag = new FlagMaxTeamSize();
				maxTeamSizeFlag.setValue(maxPlayers);
				game.addFlag(maxTeamSizeFlag);
			}
		}
		
		ConfigurationSection signwallsSection = in.getConfigurationSection("signwalls");
		
		if (signwallsSection != null) {
			for (String signWallId : signwallsSection.getKeys(false)) {
				ConfigurationSection signwallSection = signwallsSection.getConfigurationSection(signWallId);
				
				Location first = legacyStringToLocation(signwallSection.getString("first"));
				Location second = legacyStringToLocation(signwallSection.getString("second"));
				
				ExtensionLobbyWall wall;
				
				try {
					wall = new ExtensionLobbyWall(first.getWorld(), first.toVector(), second.toVector());
				} catch (SignRowValidationException e) {
					throw new MigrationException(e);
				}
				
				game.addExtension(wall);
			}
		}
		
		return game;
	}
	
	private Location legacyStringToLocation(String legacyString) {
		String[] components = legacyString.split(",");
		
		World world = Bukkit.getWorld(components[0]);
		double x = Double.parseDouble(components[1]);
		double y = Double.parseDouble(components[2]);
		double z = Double.parseDouble(components[3]);
		float pitch = 0f;
		float yaw = 0f;
		
		if (components.length > 4) {
			pitch = Float.parseFloat(components[4]);
			yaw = Float.parseFloat(components[5]);
		}
		
		return new Location(world, x, y, z, yaw, pitch);
	}
	
	private BlockVector3 legacyStringToVector(String legacyString) {
		Location location = legacyStringToLocation(legacyString);
		return BukkitAdapter.asBlockVector(location);
	}
	
	@SuppressWarnings("unchecked")
	private <T> AbstractFlag<?> getFlag(String legacyFlagName, String legacyValue) throws MigrationException {
		Class<? extends AbstractFlag<T>> flagClazz = (Class<? extends AbstractFlag<T>>) LEGACY_TO_FLAG_MAPPING.get(legacyFlagName);
		if (flagClazz == null) {
			//This flag could not be found so just return null
			return null;
		}
		
		T result = null;
		
		if (BooleanFlag.class.isAssignableFrom(flagClazz)) {
			result = (T) extractFlagValue(legacyValue, Boolean.class, null);
		} else if (IntegerFlag.class.isAssignableFrom(flagClazz)) {
			result = (T) extractFlagValue(legacyValue, Integer.class, null);
		} else if (DoubleFlag.class.isAssignableFrom(flagClazz)) {
			result = (T) extractFlagValue(legacyValue, Double.class, null);
		} else if (ItemStackFlag.class.isAssignableFrom(flagClazz)) {
			result = (T) extractFlagValue(legacyValue, ItemStack.class, null);
		} else if (ItemStackListFlag.class.isAssignableFrom(flagClazz)) {
			result = (T) extractFlagValue(legacyValue, List.class, ItemStack.class);
		} else if (LocationFlag.class.isAssignableFrom(flagClazz)) {
			result = (T) extractFlagValue(legacyValue, Location.class, null);
		} else if (LocationListFlag.class.isAssignableFrom(flagClazz)) {
			result = (T) extractFlagValue(legacyValue, List.class, Location.class);
		}
		
		AbstractFlag<T> flag;
		
		try {
			flag = flagClazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new MigrationException("Cannot find no-args constructor for flag \"" + flagClazz.getName() + "\"");
		}
		
		flag.setValue(result);
		return flag;
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	private <T, K> T extractFlagValue(String legacyFlagString, Class<T> expected, Class<K> expectedGenericClass) throws MigrationException {
		String[] components = legacyFlagString.split(":");
		Validate.isTrue(components.length > 1, "Invalid legacy flag value string \"" + legacyFlagString + "\"");
		String valueString = components[1];
		
		T value = null;
		
		if (expected == Boolean.class) {
			value = (T)(Boolean) Boolean.parseBoolean(valueString);
		} else if (expected == Integer.class) {
			value = (T)(Integer)Integer.parseInt(valueString);
		} else if (expected == Double.class) {
			value = (T)(Double)Double.parseDouble(valueString);
		} else if (expected == Location.class) {
			value = (T)legacyStringToLocation(valueString);
		} else if (expected == ItemStack.class) {
			String[] itemStackComponents = valueString.split("-");
			
			Material id = Material.getMaterial(itemStackComponents[0]);
			byte data = 0;
			int amount = 1;
			
			if (itemStackComponents.length > 1) {
				data = Byte.parseByte(itemStackComponents[1]);
				
				if (itemStackComponents.length > 2) {
					amount = Integer.parseInt(itemStackComponents[2]);
				}
			}
			
			MaterialData materialData = new MaterialData(id, data);
			value = (T) materialData.toItemStack(amount);
		} else if (expected == List.class) {
			String[] listComponents = valueString.split(";");
			List<K> resultList = Lists.newArrayList();
			
			for (int i = 0; i < listComponents.length; i++) {
				String base64SerializedString = listComponents[i];
				byte[] serializedBytes = Base64Coder.decode(base64SerializedString);
				
				Map<String, Object> fields;
				
				try {
					fields = decodeSerializedObject(serializedBytes);
				} catch (IOException e) {
					throw new MigrationException(e);
				}
				
				K result = null;
				
				if (expectedGenericClass == ItemStack.class) {
					EnumObject materialEnumObject = (EnumObject) fields.get("material");
					
					Material material = Material.getMaterial(materialEnumObject.value.value);
					byte data = (byte) fields.get("data");
					int amount = (int) fields.get("amount");
					
					Object displayNameObj = fields.get("displayName");
					String displayName = null;
					
					if (displayNameObj != null) {
						displayName = ((StringObject) displayNameObj).value;
					}
					
					List<String> lore = null;
					//The lore is a type of an instance as it is an ArrayList
					Instance arrayListInstance = (Instance) fields.get("lore");
					
					if (arrayListInstance != null) {
						lore = Lists.newArrayList();
						Map<Field, Object> arrayListFields = arrayListInstance.fielddata.values().iterator().next();
						
						for (Entry<Field, Object> entry : arrayListFields.entrySet()) {
							Field field = entry.getKey();
							Object fieldValue = entry.getValue();
							
							//Let's hope oracle doesn't change the array's field name in the future
							if (fieldValue instanceof ArrayObject && field.name.equals("a")) {
								ArrayObject array = (ArrayObject) fieldValue;
								ArrayCollection collection = array.data;
								
								for (Object collectionValue : collection) {
									String loreLine = ((StringObject) collectionValue).value;
									String[] parts = loreLine.split("\\n");
									
									for (String part : parts) {
										lore.add(part);
									}
								}
							}
						}
					}
					
					MaterialData materialData = new MaterialData(material, data);
					ItemStack stack = materialData.toItemStack(amount);
					ItemMeta meta = stack.getItemMeta();
					
					if (displayName != null) {
						meta.setDisplayName(displayName);
					}
					
					if (lore != null) {
						meta.setLore(lore);
					}
					
					stack.setItemMeta(meta);
					result = (K) stack;
				} else if (expectedGenericClass == Location.class) {
					World world = Bukkit.getWorld(((StringObject) fields.get("world")).value);
					double x = (double) fields.get("x");
					double y = (double) fields.get("y");
					double z = (double) fields.get("z");
					
					float pitch = (float) fields.get("pitch");
					float yaw = (float) fields.get("yaw");
					
					Location location = new Location(world, x, y, z, yaw, pitch);
					result = (K) location;
				}
				
				resultList.add(result);
			}
			
			value = (T) resultList;
		}
		
		return value;
	}
	
	private Map<String, Object> decodeSerializedObject(byte[] serialized) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
		DataInputStream dis = new DataInputStream(bais);
		
		jdeserialize.run(dis, false);
		List<Content> contents = jdeserialize.getContent();
		
		if (contents.size() == 0) {
			throw new IOException("No content in serialized byte array (contents.size() == 0)");
		}
		
		Content content = contents.get(0);
		if (!(content instanceof Instance)) {
			throw new IOException("Byte array is not a serialized instance");
		}
		
		Instance instance = (Instance) content;
		Map<ClassDescription, Map<Field, Object>> fieldData = instance.fielddata;
		
		if (fieldData.size() == 0) {
			throw new IOException("Instance does not contain any field data");
		}
		
		Entry<ClassDescription, Map<Field, Object>> entry = fieldData.entrySet().iterator().next();
		Map<Field, Object> fields = entry.getValue();
		Map<String, Object> fieldsResult = Maps.newHashMap();
		
		for (Entry<Field, Object> fieldEntry : fields.entrySet()) {
			fieldsResult.put(fieldEntry.getKey().name, fieldEntry.getValue());
		}
		
		return fieldsResult;
	}

}