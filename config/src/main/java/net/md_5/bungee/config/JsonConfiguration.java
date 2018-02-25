package net.md_5.bungee.config;

import com.google.common.collect.Maps;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonConfiguration extends ConfigurationProvider {

    /**
     * Needed to prettify the json after the {@link JsonWriter} wrote wrong indentations in
     * {@link #save(Configuration, Writer)}.
     */
    private static final JsonParser JSON_PARSER = new JsonParser();

    /**
     * The gson instance used for serialization.
     */
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Configuration.class, new TypeAdapter<Configuration>() {
                @Override
                public void write(JsonWriter jsonWriter, Configuration configuration) throws IOException {
                    jsonWriter.jsonValue(GSON.toJson(configuration.self));
                }

                @Override
                public Configuration read(JsonReader jsonReader) {
                    return null;
                }
            })
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    @Override
    public void save(Configuration config, File file) throws IOException {
        try (FileWriter fileWriter = new FileWriter(file)) {
            save(config, fileWriter);
        }
    }

    @Override
    public void save(Configuration config, Writer writer) {
        String toJson = GSON.toJson(config);
        JsonObject jsonObject = JSON_PARSER.parse(toJson).getAsJsonObject();
        GSON.toJson(jsonObject, writer);
    }

    @Override
    public Configuration load(File file) throws IOException {
        return load(file, null);
    }

    @Override
    public Configuration load(File file, Configuration defaults) throws IOException {
        try (FileReader fileReader = new FileReader(file)) {
            return load(fileReader, defaults);
        }
    }

    @Override
    public Configuration load(Reader reader) {
        return load(reader, null);
    }

    @Override
    public Configuration load(Reader reader, Configuration defaults) {
        Map map = GSON.fromJson(reader, LinkedHashMap.class);

        if (map == null) {
            map = Maps.newLinkedHashMap();
        }

        return new Configuration(map, defaults);
    }

    @Override
    public Configuration load(InputStream is) {
        return load(is, null);
    }

    @Override
    public Configuration load(InputStream is, Configuration defaults) {
        try (InputStreamReader inputStreamReader = new InputStreamReader(is)) {
            return load(inputStreamReader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new Configuration(Maps.newLinkedHashMap(), null);
    }

    @Override
    public Configuration load(String string) {
        return load(string, null);
    }

    @Override
    public Configuration load(String string, Configuration defaults) {
        Map map = GSON.fromJson(string, LinkedHashMap.class);

        if (map == null) {
            map = Maps.newLinkedHashMap();
        }

        return new Configuration(map, defaults);
    }
}
