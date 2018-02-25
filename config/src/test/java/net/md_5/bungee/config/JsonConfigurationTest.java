package net.md_5.bungee.config;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;

public class JsonConfigurationTest {

    private static final ConfigurationProvider CONFIGURATION_PROVIDER = ConfigurationProvider.getProvider(JsonConfiguration.class);
    private static final String TEST_DOCUMENT = "{\n" +
            "  \"array\": [\n" +
            "    1.0,\n" +
            "    2.0,\n" +
            "    3.0\n" +
            "  ],\n" +
            "  \"boolean\": true,\n" +
            "  \"null\": null,\n" +
            "  \"number\": 123.0,\n" +
            "  \"object\": {\n" +
            "    \"a\": \"b\",\n" +
            "    \"c\": 1.0,\n" +
            "    \"e\": 2.1\n" +
            "  },\n" +
            "  \"string\": \"Hello World\"\n" +
            "}";

    private Configuration configuration;

    @Before
    public void setUp() {
        configuration = CONFIGURATION_PROVIDER.load(TEST_DOCUMENT);
    }

    @Test
    public void testArray() {
        List<Integer> array = configuration.getIntList("array");

        assertEquals(3, array.size());
        assertEquals(1, array.get(0).intValue());
        assertEquals(2, array.get(1).intValue());
        assertEquals(3, array.get(2).intValue());
    }

    @Test
    public void testBoolean() {
        boolean configurationBoolean = configuration.getBoolean("boolean");

        assertTrue(configurationBoolean);
    }

    @Test
    public void testNull() {
        Object aNull = configuration.get("null");

        assertNull(aNull);
    }

    @Test
    public void testString() {
        String string = configuration.getString("string");

        assertEquals("Hello World", string);
    }

    @Test
    public void testObject() {
        Object object = configuration.get("object");

        assertEquals(Configuration.class, object.getClass());

        Configuration configuration = (Configuration) object;

        assertEquals("b", configuration.getString("a"));
        assertEquals(1, configuration.getShort("c"));
        assertEquals(2.1F, configuration.getFloat("e"), 0.0000001);
    }

    @Test
    public void testSave() {
        try {
            File file = new File("test.json");
            CONFIGURATION_PROVIDER.save(configuration, file);

            byte[] bytes = Files.readAllBytes(Paths.get("test.json"));

            assertArrayEquals(TEST_DOCUMENT.getBytes(), bytes);

            file.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}