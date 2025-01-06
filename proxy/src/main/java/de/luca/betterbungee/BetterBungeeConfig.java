package de.luca.betterbungee;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BetterBungeeConfig {

    public static final String CONFIGS_HOSTNAMES_JSON = "configs/hostnames.json";

    private static @Getter Hostnames hostnameconfig = loadConfig(CONFIGS_HOSTNAMES_JSON, Hostnames.class, new Hostnames());

    public static final String CONFIGS_IPFORWARDIPS_JSON = "configs/ipforwardips.json";

    private static @Getter IPForwardIps ipforwardips = loadConfig(CONFIGS_IPFORWARDIPS_JSON, IPForwardIps.class, new IPForwardIps());

    public static final String CONFIGS_CONFIG_JSON = "configs/config.json";

    private static @Getter ConfigJson configJson = loadConfig(CONFIGS_CONFIG_JSON, ConfigJson.class, new ConfigJson());

    public static <T> T loadConfig(String path, Class<T> valueType, T defaultobject) {
        File file = new File(path);
        file.getParentFile().mkdirs();
        if (!file.exists()) {
            saveConfig(path, defaultobject);
            return loadConfig(path, valueType, defaultobject);
        } else {
            try {
                String jsonstring = new String(Files.readAllBytes(file.toPath()));
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.ANY);
                objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                T object = objectMapper.readValue(jsonstring, valueType);
                saveConfig(path, object);
                return object;
            } catch (IOException e) {
                file.delete();
                e.printStackTrace();
            }
        }
        return null;
    }

    public static <T> T saveConfig(String path, T defaultobject) {
        try {
            File file = new File(path);
            FileWriter writer = new FileWriter(file.getAbsolutePath());
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.ANY);
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            String gsonstring = objectMapper.writeValueAsString(defaultobject);
            writer.append(gsonstring);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return defaultobject;
    }

    @NoArgsConstructor
    @Getter @Setter
    public static class Hostnames {
        public void save() {
            saveConfig(CONFIGS_HOSTNAMES_JSON, Hostnames.class);
        }
        //         for minehut use   https://api.minehut.com/mitm/proxy/session/minecraft/hasJoined
        private String defaulturl = "https://sessionserver.mojang.com/session/minecraft/hasJoined";
        private ConcurrentHashMap<String, String> hostnames = (ConcurrentHashMap<String, String>) Stream.of(new String[][] {
                {"hut.mcserver.com", "https://api.minehut.com/mitm/proxy/session/minecraft/hasJoined"},
        }).collect(Collectors.toConcurrentMap(data -> data[0], data -> data[1]));
    }

    @NoArgsConstructor
    @Getter @Setter
    public static class IPForwardIps {
        public void save() {
            saveConfig(CONFIGS_IPFORWARDIPS_JSON, Hostnames.class);
        }
        private List<String> ipforwardips = Arrays.asList("127.0.0.1");
    }

    @NoArgsConstructor
    @Getter @Setter
    public static class ConfigJson {
        public void save() {
            saveConfig(CONFIGS_CONFIG_JSON, ConfigJson.class);
        }
        private boolean allowBungeeConnections = false;
        private LoginCacheSettings loginCacheSettings = new LoginCacheSettings();
    }
    
    @NoArgsConstructor
    @Getter @Setter
    public static class LoginCacheSettings {
        boolean enabled = true;
        int cacheTimeInMinutes = 300;
    }


}
