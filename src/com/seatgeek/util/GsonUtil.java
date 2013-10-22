package com.seatgeek.util;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class GsonUtil {
	private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
	
	/** Empty private constructor to prevent instance creation.*/
	private GsonUtil() {  }
	
	/** Creates a new {@link Gson} instance with preset parameters.*/
	public static Gson newGson() {
		return new GsonBuilder()
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
			.setDateFormat(DATE_PATTERN)
			.create();
	}
}
