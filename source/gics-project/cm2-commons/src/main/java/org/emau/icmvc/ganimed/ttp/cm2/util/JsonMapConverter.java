package org.emau.icmvc.ganimed.ttp.cm2.util;

/*-
 * ###license-information-start###
 * E-PIX - Enterprise Patient Identifier Cross-referencing
 * __
 * Copyright (C) 2014 - 2022 Trusted Third Party of the University Medicine Greifswald -
 * 							kontakt-ths@uni-greifswald.de
 * 
 * 							concept and implementation
 * 							l.geidel, c.hampf
 * 							web client
 * 							a.blumentritt, m.bialke, f.m.moser
 * 							fhir-api
 * 							m.bialke
 * 							docker
 * 							r. schuldt
 * 
 * 							The gICS was developed by the University Medicine Greifswald and published
 *  							in 2014 as part of the research project "MOSAIC" (funded by the DFG HO 1937/2-1).
 *  
 * 							Selected functionalities of gICS were developed as
 * 							part of the following research projects:
 * 							- MAGIC (funded by the DFG HO 1937/5-1)
 * 							- MIRACUM (funded by the German Federal Ministry of Education and Research 01ZZ1801M)
 * 							- NUM-CODEX (funded by the German Federal Ministry of Education and Research 01KX2021)
 * 
 * 							please cite our publications
 * 							https://doi.org/10.1186/s12967-020-02457-y
 * 							http://dx.doi.org/10.3414/ME14-01-0133
 * 							http://dx.doi.org/10.1186/s12967-015-0545-6
 * 							http://dx.doi.org/10.3205/17gmds146
 * __
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ###license-information-end###
 */

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.type.CollectionType;

/**
 * A simple JSON converter for maps with compleX Java objects as keys.
 * The map will be serialized as a JSON array of the map entries with "key" and "value" as entry keys.
 *
 * @param <K>
 *            the type of the keys in the map
 * @param <V>
 *            the type of the values in the map
 * @author moser
 */
public class JsonMapConverter<K, V>
{
	protected final Class<K> keyType;
	protected final Class<V> valueType;
	protected final ObjectMapper mapper;
	private final ObjectReader reader;
	private final ObjectWriter writer;

	/**
	 * Creates a JsonMapConverter with the default mapper.
	 *
	 * @param keyType
	 *            the class of the key type
	 * @param valueType
	 *            the class of the value type
	 */
	public JsonMapConverter(Class<K> keyType, Class<V> valueType)
	{
		this(keyType, valueType, null);
	}

	/**
	 * Creates a JsonMapConverter with a custom mapper.
	 *
	 * @param keyType
	 *            the class of the key type
	 * @param valueType
	 *            the class of the value type
	 * @param mapper
	 *            the custom mapper
	 */
	public JsonMapConverter(Class<K> keyType, Class<V> valueType, ObjectMapper mapper)
	{
		this.keyType = keyType;
		this.valueType = valueType;
		this.mapper = mapper != null ? mapper : JsonConverter.createDefaultObjectMapper();
		CollectionType type = this.mapper.getTypeFactory().constructCollectionType(List.class, MapEntry.class);
		this.reader = this.mapper.readerFor(type);
		this.writer = this.mapper.writerFor(type);
	}

	public Class<K> getKeyType()
	{
		return keyType;
	}

	public Class<V> getValueType()
	{
		return valueType;
	}

	public ObjectMapper getMapper()
	{
		return mapper;
	}

	public ObjectReader getReader()
	{
		return reader;
	}

	public ObjectWriter getWriter()
	{
		return writer;
	}

	/**
	 * Returns the JSON representation for the given map.
	 *
	 * @param map
	 *            the map to convert
	 * @return the JSON representation for the given map
	 * @throws JsonProcessingException
	 *             if the map cannot be converted to JSON
	 */
	public String toJson(Map<K, V> map) throws JsonProcessingException
	{
		List<MapEntry> entries = map.entrySet().stream().map(e -> new MapEntry(e.getKey(), e.getValue())).collect(Collectors.toList());
		return writer.writeValueAsString(entries);
	}

	/**
	 * Returns the map for the given JSON representation.
	 *
	 * @param json
	 *            the JSON representation to convert
	 * @return the map for the given JSON representation
	 * @throws IOException
	 *             if the JSON string cannot be converted into a map
	 */
	public Map<K, V> fromJson(String json) throws IOException
	{
		List<MapEntry> entries = reader.readValue(json);
		Map<K, V> map = new HashMap<>();
		for (MapEntry e : entries)
		{
			map.put(reader.readValue(mapper.writeValueAsString(e.key), keyType),
					reader.readValue(mapper.writeValueAsString(e.value), valueType));
		}
		return map;
	}

	/**
	 * A JsonSerializer for maps which can be used to annotate map instance fields with {@link JsonSerialize}
	 * to convert them as {@link #fromJson(String)} does.
	 *
	 * @param <K>
	 *            the type of the keys in the map
	 * @param <V>
	 *            the type of the values in the map
	 */
	public static class Serializer<K, V> extends JsonSerializer<Map<K, V>>
	{
		private final JsonMapConverter<K, V> c;

		public Serializer(JsonMapConverter<K, V> c)
		{
			this.c = c;
		}

		@Override
		public void serialize(Map<K, V> value, JsonGenerator gen, SerializerProvider provider) throws IOException
		{
			gen.writeTree(c.getMapper().readTree(c.toJson(value)));
		}
	}

	/**
	 * A JsonDeserializer for maps which can be used to annotate map instance fields with {@link JsonDeserialize}
	 * to convert maps as {@link #toJson(Map)} does.
	 *
	 * @param <K>
	 *            the type of the keys in the map
	 * @param <V>
	 *            the type of the values in the map
	 */
	public static class Deserializer<K, V> extends JsonDeserializer<Map<K, V>>
	{
		private final JsonMapConverter<K, V> c;

		public Deserializer(JsonMapConverter<K, V> c)
		{
			this.c = c;
		}

		@Override
		public Map<K, V> deserialize(JsonParser p, DeserializationContext ctx) throws IOException
		{
			return c.fromJson(c.getMapper().writeValueAsString(p.getCodec().readTree(p)));
		}
	}

	static class MapEntry
	{
		Object key;
		Object value;

		public MapEntry()
		{}

		MapEntry(Object key, Object value)
		{
			this.key = key;
			this.value = value;
		}
	}
}
