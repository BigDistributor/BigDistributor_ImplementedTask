package net.preibisch.bigdistributor.serializers;

import com.google.gson.*;
import mpicbg.spim.data.sequence.ViewId;
import net.preibisch.distribution.io.serializers.JsonSerializerDeserializer;

import java.lang.reflect.Type;

public class ViewIdJsonSerializer implements JsonSerializerDeserializer<ViewId> {

	@Override
	public ViewId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		return new Gson().fromJson(json, bigdistributor.serializers.ViewIdSerializable.class).toViewId();
	}

	@Override
	public JsonElement serialize(ViewId src, Type type, JsonSerializationContext context) {
		bigdistributor.serializers.ViewIdSerializable vs = new bigdistributor.serializers.ViewIdSerializable(src);
		JsonElement x = new GsonBuilder().create().toJsonTree(vs);
		return x;
	}

}
