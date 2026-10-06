package org.easylauncher.mods.elfeatures.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.UUID;

/**
 * UUIDs spelt the way Mojang's services spell them, without dashes — authlib has the same adapter, but 1.6 has no
 * authlib to take it from.
 *
 * <p>Nulls are handled here rather than by {@code nullSafe()}, which the gson of 1.6 doesn't have yet.
 */
public final class UuidTypeAdapter extends TypeAdapter<UUID> {

    private static final String DASHED_GROUPS = "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{12})";

    @Override
    public UUID read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }

        return UUID.fromString(reader.nextString().replaceFirst("^" + DASHED_GROUPS + "$", "$1-$2-$3-$4-$5"));
    }

    @Override
    public void write(JsonWriter writer, UUID value) throws IOException {
        if (value == null) {
            writer.nullValue();
            return;
        }

        writer.value(value.toString().replace("-", ""));
    }

}
