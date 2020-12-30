package com.bigdistributor.tasks.serializers;

import com.bigdistributor.io.JsonSerializerDeserializer;
import com.google.gson.*;
import mpicbg.spim.data.sequence.ViewId;

import java.lang.reflect.Type;

public class ViewIdJsonSerializer implements JsonSerializerDeserializer<ViewId> {

	@Override
	public ViewId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		return new Gson().fromJson(json, ViewIdSerializable.class).toViewId();
	}

	@Override
	public JsonElement serialize(ViewId src, Type type, JsonSerializationContext context) {
		ViewIdSerializable vs = new ViewIdSerializable(src);
		JsonElement x = new GsonBuilder().create().toJsonTree(vs);
		return x;
	}

}
