package com.bigdistributor.tasks.serializers;

import com.google.gson.*;
import mpicbg.spim.data.sequence.ViewId;
import net.preibisch.bigdistributor.io.serializers.JsonSerializerDeserializer;

import java.lang.reflect.Type;

public class ViewIdJsonSerializer implements JsonSerializerDeserializer<ViewId> {

	@Override
	public ViewId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		return new Gson().fromJson(json, com.bigdistributor.tasks.bigdistributor.serializers.ViewIdSerializable.class).toViewId();
	}

	@Override
	public JsonElement serialize(ViewId src, Type type, JsonSerializationContext context) {
		com.bigdistributor.tasks.bigdistributor.serializers.ViewIdSerializable vs = new com.bigdistributor.tasks.bigdistributor.serializers.ViewIdSerializable(src);
		JsonElement x = new GsonBuilder().create().toJsonTree(vs);
		return x;
	}

}
