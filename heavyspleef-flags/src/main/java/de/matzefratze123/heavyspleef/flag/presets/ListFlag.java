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
package de.matzefratze123.heavyspleef.flag.presets;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.dom4j.Element;

import de.matzefratze123.heavyspleef.core.flag.AbstractFlag;
import de.matzefratze123.heavyspleef.core.flag.InputParseException;

public abstract class ListFlag<T> extends AbstractFlag<List<T>>{

	private ListInputParser<T> listParser;
	
	@Override
	public List<T> parseInput(Player player, String input) throws InputParseException {
		//Lazy initialization
		if (listParser == null) {
			listParser = createParser();
		}
		
		return listParser.parse(input);
	}
	
	public void add(T e) {
		getValue().add(e);
	}
	
	public void remove(T e) {
		getValue().remove(e);
	}
	
	public T get(int index) {
		return getValue().get(index);
	}
	
	public T remove(int index) {
		return getValue().remove(index);
	}
	
	public boolean contains(Object obj) {
		return getValue().contains(obj);
	}
	
	public int size() {
		return getValue().size();
	}
	
	private void clear() {
		getValue().clear();
	}
	
	@Override
	public void marshal(Element element) {
		for (T item : getValue()) {
			Element itemElement = element.addElement("item");
			marshalListItem(itemElement, item);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void unmarshal(Element element) {
		if (getValue() == null) {
			setValue(new ArrayList<T>());
		} else {
			clear();
		}
		
		List<Element> itemElementsList = element.elements("item");
		for (Element itemElement : itemElementsList) {
			T item = unmarshalListItem(itemElement);
			add(item);
		}
	}
	
	public abstract void marshalListItem(Element element, T item);
	
	public abstract T unmarshalListItem(Element element);
	
	public abstract ListInputParser<T> createParser();
	
}
