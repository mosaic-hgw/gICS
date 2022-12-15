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
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * A simple JSON converter for plain Java objects.
 *
 * @param <T> the type of objects to convert
 * @author moser
 */
public class JsonConverter<T>
{
	/**
	 * The default mapper which converts w.r.t. to any declared non-transient instance field ignoring null-values.
	 *
	 * @return the default mapper
	 */
	public static ObjectMapper createDefaultObjectMapper()
	{
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
				.withFieldVisibility(JsonAutoDetect.Visibility.ANY)
				.withGetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withSetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"));
		return mapper;
	}

	protected final Class<T> type;
	protected final ObjectMapper mapper;

	/**
	 * Creates a converter for the given type and the default mapper.
	 *
	 * @param type the type of objects to convert
	 */
	public JsonConverter(Class<T> type)
	{
		this(type, null);
	}

	/**
	 * Creates a converter for the given type with a custom mapper.
	 *
	 * @param type the type of objects to convert
	 */
	public JsonConverter(Class<T> type, ObjectMapper mapper)
	{
		this.type = type;
		this.mapper = mapper != null ? mapper : createDefaultObjectMapper();
	}

	public ObjectMapper getMapper()
	{
		return mapper;
	}

	/**
	 * Returns the JSON representation for the given object.
	 *
	 * @param object the object to convert
	 * @return the JSON representation for the given object
	 * @throws JsonProcessingException if the object cannot be converted to JSON
	 */
	public String toJson(T object) throws JsonProcessingException
	{
		return mapper.writerFor(type).writeValueAsString(object);
	}

	/**
	 * Returns the object for the given JSON representation.
	 *
	 * @param json the JSON representation to convert
	 * @return the object for the given JSON representation
	 * @throws IOException if the JSON string cannot be converted into an object
	 */
	public T fromJson(String json) throws IOException
	{
		return mapper.readerFor(type).readValue(json, type);
	}

	/**
	 * A JsonSerializer which can be used to annotate instance fields with {@link JsonSerialize}
	 * to convert them as {@link #fromJson(String)} does.
	 *
	 * @param <T> the type of the objects to convert
	 */
	public static class Serializer<T> extends JsonSerializer<T>
	{
		private final JsonConverter<T> c;

		public Serializer(JsonConverter<T> c)
		{
			this.c = c;
		}

		@Override
		public void serialize(T value, JsonGenerator gen, SerializerProvider provider) throws IOException {
			gen.writeTree(c.getMapper().readTree(c.toJson(value)));
		}
	}

	/**
	 * A JsonDeserializer which can be used to annotate instance fields with {@link JsonDeserialize}
	 * to convert maps as {@link #toJson(T)} does.
	 *
	 * @param <T> the type of the objects to convert
	 */
	public static class Deserializer<T> extends JsonDeserializer<T>
	{
		private final JsonConverter<T> c;

		public Deserializer(JsonConverter<T> c)
		{
			this.c = c;
		}

		@Override
		public T deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
			return c.fromJson(c.getMapper().writeValueAsString(p.getCodec().readTree(p)));
		}
	}
}
