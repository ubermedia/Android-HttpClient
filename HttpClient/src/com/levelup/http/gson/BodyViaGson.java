package com.levelup.http.gson;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.levelup.http.parser.BodyTransformChain;

/**
 * An {@link XferTransformViaGson} class that has debug enabled for alpha/beta builds
 *
 * Created by Steve Lhomme on 26/06/2014.
 */
public class BodyViaGson<T> extends BodyTransformChain<T> {
	public BodyViaGson(TypeToken<T> typeToken) {
		super(new XferTransformViaGson<T>(typeToken));
	}

	public BodyViaGson(Type type) {
		super(new XferTransformViaGson<T>(type));
	}

	public BodyViaGson(Gson gson, TypeToken<T> typeToken) {
		super(new XferTransformViaGson<T>(gson, typeToken));
	}

	public BodyViaGson(Gson gson, Type type) {
		super(new XferTransformViaGson<T>(gson, type));
	}

	public BodyViaGson<T> enableDebugData(boolean enable) {
		((XferTransformViaGson) transforms[1]).enableDebugData(enable);
		return this;
	}
}