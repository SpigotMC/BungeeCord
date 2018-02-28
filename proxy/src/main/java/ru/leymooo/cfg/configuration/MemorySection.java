package ru.leymooo.cfg.configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A type of {@link ru.leymooo.cfg.configuration.ConfigurationSection} that is stored in memory.
 */
public class MemorySection implements ru.leymooo.cfg.configuration.ConfigurationSection {

    protected final Map<String, Object> map = new LinkedHashMap<>();
    private final Configuration root;
    private final ru.leymooo.cfg.configuration.ConfigurationSection parent;
    private final String path;
    private final String fullPath;

    /**
     * Creates an empty MemorySection for use as a root {@link ru.leymooo.cfg.configuration.Configuration}
     * section.
     * <p>
     * Note that calling this without being yourself a {@link ru.leymooo.cfg.configuration.Configuration}
     * will throw an exception!
     *
     * @throws IllegalStateException Thrown if this is not a {@link
     *                               ru.leymooo.cfg.configuration.Configuration} root.
     */
    protected MemorySection() {
        if (!(this instanceof Configuration)) {
            throw new IllegalStateException("Cannot construct a root MemorySection when not a Configuration");
        }

        this.path = "";
        this.fullPath = "";
        this.parent = null;
        this.root = (Configuration) this;
    }

    /**
     * Creates an empty MemorySection with the specified parent and path.
     *
     * @param parent Parent section that contains this own section.
     * @param path   Path that you may access this section from via the root
     *               {@link ru.leymooo.cfg.configuration.Configuration}.
     * @throws IllegalArgumentException Thrown is parent or path is null, or
     *                                  if parent contains no root Configuration.
     */
    protected MemorySection(ru.leymooo.cfg.configuration.ConfigurationSection parent, String path) {
        if (parent == null) {
            throw new NullPointerException("Parent may not be null");
        }
        if (path == null) {
            throw new NullPointerException("Path may not be null");
        }

        this.path = path;
        this.parent = parent;
        this.root = parent.getRoot();

        if (this.root == null) {
            throw new NullPointerException("Path may not be orphaned");
        }

        this.fullPath = createPath(parent, path);
    }

    public static double toDouble(Object obj, double def) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        if (obj instanceof String) {
            try {
                return Double.parseDouble((String) obj);
            } catch (NumberFormatException ignored) {
            }
        } else if (obj instanceof List) {
            List<?> val = (List<?>) obj;
            if (!val.isEmpty()) {
                return toDouble(val.get(0), def);
            }
        }
        return def;
    }

    public static int toInt(Object obj, int def) {
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        if (obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch (NumberFormatException ignored) {
            }
        } else if (obj instanceof List) {
            List<?> val = (List<?>) obj;
            if (!val.isEmpty()) {
                return toInt(val.get(0), def);
            }
        }
        return def;
    }

    public static long toLong(Object obj, long def) {
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException ignored) {
            }
        } else if (obj instanceof List) {
            List<?> val = (List<?>) obj;
            if (!val.isEmpty()) {
                return toLong(val.get(0), def);
            }
        }
        return def;
    }

    /**
     * Creates a full path to the given {@link ru.leymooo.cfg.configuration.ConfigurationSection} from its
     * root {@link ru.leymooo.cfg.configuration.Configuration}.
     * <p>
     * You may use this method for any given {@link ru.leymooo.cfg.configuration.ConfigurationSection}, not
     * only {@link ru.leymooo.cfg.configuration.MemorySection}.
     *
     * @param section Section to create a path for.
     * @param key     Name of the specified section.
     * @return Full path of the section from its root.
     */
    public static String createPath(ru.leymooo.cfg.configuration.ConfigurationSection section, String key) {
        return createPath(section, key, (section == null) ? null : section.getRoot());
    }

    /**
     * Creates a relative path to the given {@link ru.leymooo.cfg.configuration.ConfigurationSection} from
     * the given relative section.
     * <p>
     * You may use this method for any given {@link ru.leymooo.cfg.configuration.ConfigurationSection}, not
     * only {@link ru.leymooo.cfg.configuration.MemorySection}.
     *
     * @param section    Section to create a path for.
     * @param key        Name of the specified section.
     * @param relativeTo Section to create the path relative to.
     * @return Full path of the section from its root.
     */
    public static String createPath(ru.leymooo.cfg.configuration.ConfigurationSection section, String key, ru.leymooo.cfg.configuration.ConfigurationSection relativeTo) {
        if (section == null) {
            throw new NullPointerException("Cannot create path without a section");
        }
        Configuration root = section.getRoot();
        if (root == null) {
            throw new IllegalStateException("Cannot create path without a root");
        }
        char separator = root.options().pathSeparator();

        StringBuilder builder = new StringBuilder();
        for (ru.leymooo.cfg.configuration.ConfigurationSection parent = section; (parent != null) && (parent != relativeTo); parent = parent.getParent()) {
            if (builder.length() > 0) {
                builder.insert(0, separator);
            }

            builder.insert(0, parent.getName());
        }

        if ((key != null) && !key.isEmpty()) {
            if (builder.length() > 0) {
                builder.append(separator);
            }

            builder.append(key);
        }

        return builder.toString();
    }

    @Override
    public Set<String> getKeys(boolean deep) {
        Set<String> result = new LinkedHashSet<>();

        Configuration root = getRoot();
        if ((root != null) && root.options().copyDefaults()) {
            ru.leymooo.cfg.configuration.ConfigurationSection defaults = getDefaultSection();

            if (defaults != null) {
                result.addAll(defaults.getKeys(deep));
            }
        }

        mapChildrenKeys(result, this, deep);

        return result;
    }

    @Override
    public Map<String, Object> getValues(boolean deep) {
        Map<String, Object> result = new LinkedHashMap<>();

        Configuration root = getRoot();
        if ((root != null) && root.options().copyDefaults()) {
            ru.leymooo.cfg.configuration.ConfigurationSection defaults = getDefaultSection();

            if (defaults != null) {
                result.putAll(defaults.getValues(deep));
            }
        }

        mapChildrenValues(result, this, deep);

        return result;
    }

    @Override
    public boolean contains(String path) {
        return get(path) != null;
    }

    @Override
    public boolean isSet(String path) {
        Configuration root = getRoot();
        if (root == null) {
            return false;
        }
        if (root.options().copyDefaults()) {
            return contains(path);
        }
        return get(path, null) != null;
    }

    @Override
    public String getCurrentPath() {
        return this.fullPath;
    }

    @Override
    public String getName() {
        return this.path;
    }

    @Override
    public Configuration getRoot() {
        return this.root;
    }

    @Override
    public ru.leymooo.cfg.configuration.ConfigurationSection getParent() {
        return this.parent;
    }

    @Override
    public void addDefault(String path, Object value) {
        if (path == null) {
            throw new NullPointerException("Path cannot be null");
        }

        Configuration root = getRoot();
        if (root == null) {
            throw new IllegalStateException("Cannot add default without root");
        }
        if (root == this) {
            throw new UnsupportedOperationException("Unsupported addDefault(String, Object) implementation");
        }
        root.addDefault(createPath(this, path), value);
    }

    @Override
    public ru.leymooo.cfg.configuration.ConfigurationSection getDefaultSection() {
        Configuration root = getRoot();
        Configuration defaults = root == null ? null : root.getDefaults();

        if (defaults != null) {
            if (defaults.isConfigurationSection(getCurrentPath())) {
                return defaults.getConfigurationSection(getCurrentPath());
            }
        }

        return null;
    }

    @Override
    public void set(String path, Object value) {
        if (path == null) {
            throw new NullPointerException("Cannot set to an empty path");
        }

        Configuration root = getRoot();
        if (root == null) {
            throw new IllegalStateException("Cannot use section without a root");
        }

        char separator = root.options().pathSeparator();
        // i1 is the leading (higher) index
        // i2 is the trailing (lower) index
        int i1 = -1, i2;
        ru.leymooo.cfg.configuration.ConfigurationSection section = this;
        while ((i1 = path.indexOf(separator, i2 = i1 + 1)) != -1) {
            String node = path.substring(i2, i1);
            ru.leymooo.cfg.configuration.ConfigurationSection subSection = section.getConfigurationSection(node);
            if (subSection == null) {
                section = section.createSection(node);
            } else {
                section = subSection;
            }
        }

        String key = path.substring(i2);
        if (section == this) {
            if (value == null) {
                this.map.remove(key);
            } else {
                this.map.put(key, value);
            }
        } else {
            section.set(key, value);
        }
    }

    @Override
    public Object get(String path) {
        return get(path, getDefault(path));
    }

    @Override
    public Object get(String path, Object def) {
        if (path == null) {
            throw new NullPointerException("Path cannot be null");
        }

        if (path.isEmpty()) {
            return this;
        }

        Configuration root = getRoot();
        if (root == null) {
            throw new IllegalStateException("Cannot access section without a root");
        }

        char separator = root.options().pathSeparator();
        // i1 is the leading (higher) index
        // i2 is the trailing (lower) index
        int i1 = -1;
        int i2;
        ru.leymooo.cfg.configuration.ConfigurationSection section = this;
        while ((i1 = path.indexOf(separator, i2 = i1 + 1)) != -1) {
            section = section.getConfigurationSection(path.substring(i2, i1));
            if (section == null) {
                return def;
            }
        }

        String key = path.substring(i2);
        if (section == this) {
            Object result = this.map.get(key);
            if (result == null) {
                return def;
            } else {
                return result;
            }
        }
        return section.get(key, def);
    }

    @Override
    public ru.leymooo.cfg.configuration.ConfigurationSection createSection(String path) {
        if (path == null) {
            throw new NullPointerException("Cannot create section at empty path");
        }
        Configuration root = getRoot();
        if (root == null) {
            throw new IllegalStateException("Cannot create section without a root");
        }

        char separator = root.options().pathSeparator();
        // i1 is the leading (higher) index
        // i2 is the trailing (lower) index
        int i1 = -1, i2;
        ru.leymooo.cfg.configuration.ConfigurationSection section = this;
        while ((i1 = path.indexOf(separator, i2 = i1 + 1)) != -1) {
            String node = path.substring(i2, i1);
            ru.leymooo.cfg.configuration.ConfigurationSection subSection = section.getConfigurationSection(node);
            if (subSection == null) {
                section = section.createSection(node);
            } else {
                section = subSection;
            }
        }

        String key = path.substring(i2);
        if (section == this) {
            ru.leymooo.cfg.configuration.ConfigurationSection result = new ru.leymooo.cfg.configuration.MemorySection(this, key);
            this.map.put(key, result);
            return result;
        }
        return section.createSection(key);
    }

    @Override
    public ru.leymooo.cfg.configuration.ConfigurationSection createSection(String path, Map<?, ?> map) {
        ru.leymooo.cfg.configuration.ConfigurationSection section = createSection(path);

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                section.createSection(entry.getKey().toString(), (Map<?, ?>) entry.getValue());
            } else {
                section.set(entry.getKey().toString(), entry.getValue());
            }
        }

        return section;
    }

    // Primitives
    @Override
    public String getString(String path) {
        Object def = getDefault(path);
        return getString(path, def != null ? def.toString() : null);
    }

    @Override
    public String getString(String path, String def) {
        Object val = get(path, def);
        if (val != null) {
            return val.toString();
        } else {
            return def;
        }
    }

    @Override
    public boolean isString(String path) {
        Object val = get(path);
        return val instanceof String;
    }

    @Override
    public int getInt(String path) {
        Object def = getDefault(path);
        return getInt(path, toInt(def, 0));
    }

    @Override
    public int getInt(String path, int def) {
        Object val = get(path, def);
        return toInt(val, def);
    }

    @Override
    public boolean isInt(String path) {
        Object val = get(path);
        return val instanceof Integer;
    }

    @Override
    public boolean getBoolean(String path) {
        Object def = getDefault(path);
        if (def instanceof Boolean) {
            return getBoolean(path, (Boolean) def);
        } else {
            return getBoolean(path, false);
        }
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        Object val = get(path, def);
        if (val instanceof Boolean) {
            return (Boolean) val;
        } else {
            return def;
        }
    }

    @Override
    public boolean isBoolean(String path) {
        Object val = get(path);
        return val instanceof Boolean;
    }

    @Override
    public double getDouble(String path) {
        Object def = getDefault(path);
        return getDouble(path, toDouble(def, 0));
    }

    @Override
    public double getDouble(String path, double def) {
        Object val = get(path, def);
        return toDouble(val, def);
    }

    @Override
    public boolean isDouble(String path) {
        Object val = get(path);
        return val instanceof Double;
    }

    @Override
    public long getLong(String path) {
        Object def = getDefault(path);
        return getLong(path, toLong(def, 0));
    }

    @Override
    public long getLong(String path, long def) {
        Object val = get(path, def);
        return toLong(val, def);
    }

    @Override
    public boolean isLong(String path) {
        Object val = get(path);
        return val instanceof Long;
    }

    // Java
    @Override
    public List<?> getList(String path) {
        Object def = getDefault(path);
        return getList(path, def instanceof List ? (List<?>) def : null);
    }

    @Override
    public List<?> getList(String path, List<?> def) {
        Object val = get(path, def);
        return (List<?>) ((val instanceof List) ? val : def);
    }

    @Override
    public boolean isList(String path) {
        Object val = get(path);
        return val instanceof List;
    }

    @Override
    public List<String> getStringList(String path) {
        final List<?> list = getList(path);

        if (list == null) {
            return new ArrayList<>(0);
        }

        final List<String> result = new ArrayList<>();

        for (final Object object : list) {
            if ((object instanceof String) || (isPrimitiveWrapper(object))) {
                result.add(String.valueOf(object));
            }
        }

        return result;
    }

    @Override
    public List<Integer> getIntegerList(String path) {
        List<?> list = getList(path);

        List<Integer> result = new ArrayList<>();

        for (Object object : list) {
            if (object instanceof Integer) {
                result.add((Integer) object);
            } else if (object instanceof String) {
                try {
                    result.add(Integer.valueOf((String) object));
                } catch (NumberFormatException ignored) {
                }
            } else if (object instanceof Character) {
                result.add((int) (Character) object);
            } else if (object instanceof Number) {
                result.add(((Number) object).intValue());
            }
        }

        return result;
    }

    @Override
    public List<Boolean> getBooleanList(String path) {
        List<?> list = getList(path);

        List<Boolean> result = new ArrayList<>();

        for (Object object : list) {
            if (object instanceof Boolean) {
                result.add((Boolean) object);
            } else if (object instanceof String) {
                if (Boolean.TRUE.toString().equals(object)) {
                    result.add(true);
                } else if (Boolean.FALSE.toString().equals(object)) {
                    result.add(false);
                }
            }
        }

        return result;
    }

    @Override
    public List<Double> getDoubleList(String path) {
        List<?> list = getList(path);

        List<Double> result = new ArrayList<>();

        for (Object object : list) {
            if (object instanceof Double) {
                result.add((Double) object);
            } else if (object instanceof String) {
                try {
                    result.add(Double.valueOf((String) object));
                } catch (NumberFormatException ignored) {
                }
            } else if (object instanceof Character) {
                result.add((double) (Character) object);
            } else if (object instanceof Number) {
                result.add(((Number) object).doubleValue());
            }
        }

        return result;
    }

    @Override
    public List<Float> getFloatList(String path) {
        List<?> list = getList(path);

        List<Float> result = new ArrayList<>();

        for (Object object : list) {
            if (object instanceof Float) {
                result.add((Float) object);
            } else if (object instanceof String) {
                try {
                    result.add(Float.valueOf((String) object));
                } catch (NumberFormatException ignored) {
                }
            } else if (object instanceof Character) {
                result.add((float) (Character) object);
            } else if (object instanceof Number) {
                result.add(((Number) object).floatValue());
            }
        }

        return result;
    }

    @Override
    public List<Long> getLongList(String path) {
        List<?> list = getList(path);

        List<Long> result = new ArrayList<>();

        for (Object object : list) {
            if (object instanceof Long) {
                result.add((Long) object);
            } else if (object instanceof String) {
                try {
                    result.add(Long.valueOf((String) object));
                } catch (NumberFormatException ignored) {
                }
            } else if (object instanceof Character) {
                result.add((long) (Character) object);
            } else if (object instanceof Number) {
                result.add(((Number) object).longValue());
            }
        }

        return result;
    }

    @Override
    public List<Byte> getByteList(String path) {
        List<?> list = getList(path);

        List<Byte> result = new ArrayList<>();

        for (Object object : list) {
            if (object instanceof Byte) {
                result.add((Byte) object);
            } else if (object instanceof String) {
                try {
                    result.add(Byte.valueOf((String) object));
                } catch (NumberFormatException ignored) {
                }
            } else if (object instanceof Character) {
                result.add((byte) ((Character) object).charValue());
            } else if (object instanceof Number) {
                result.add(((Number) object).byteValue());
            }
        }

        return result;
    }

    @Override
    public List<Character> getCharacterList(String path) {
        List<?> list = getList(path);

        List<Character> result = new ArrayList<>();

        for (Object object : list) {
            if (object instanceof Character) {
                result.add((Character) object);
            } else if (object instanceof String) {
                String str = (String) object;

                if (str.length() == 1) {
                    result.add(str.charAt(0));
                }
            } else if (object instanceof Number) {
                result.add((char) ((Number) object).intValue());
            }
        }

        return result;
    }

    @Override
    public List<Short> getShortList(String path) {
        List<?> list = getList(path);

        List<Short> result = new ArrayList<>();

        for (Object object : list) {
            if (object instanceof Short) {
                result.add((Short) object);
            } else if (object instanceof String) {
                try {
                    result.add(Short.valueOf((String) object));
                } catch (NumberFormatException ignored) {
                }
            } else if (object instanceof Character) {
                result.add((short) ((Character) object).charValue());
            } else if (object instanceof Number) {
                result.add(((Number) object).shortValue());
            }
        }

        return result;
    }

    @Override
    public List<Map<?, ?>> getMapList(String path) {
        List<?> list = getList(path);
        List<Map<?, ?>> result = new ArrayList<>();

        for (Object object : list) {
            if (object instanceof Map) {
                result.add((Map<?, ?>) object);
            }
        }

        return result;
    }

    @Override
    public ru.leymooo.cfg.configuration.ConfigurationSection getConfigurationSection(String path) {
        Object val = get(path, null);
        if (val != null) {
            return (val instanceof ru.leymooo.cfg.configuration.ConfigurationSection) ? (ru.leymooo.cfg.configuration.ConfigurationSection) val : null;
        }

        val = get(path, getDefault(path));
        return (val instanceof ru.leymooo.cfg.configuration.ConfigurationSection) ? createSection(path) : null;
    }

    @Override
    public boolean isConfigurationSection(String path) {
        Object val = get(path);
        return val instanceof ru.leymooo.cfg.configuration.ConfigurationSection;
    }

    protected boolean isPrimitiveWrapper(Object input) {
        return (input instanceof Integer)
                || (input instanceof Boolean)
                || (input instanceof Character)
                || (input instanceof Byte)
                || (input instanceof Short)
                || (input instanceof Double)
                || (input instanceof Long)
                || (input instanceof Float);
    }

    protected Object getDefault(String path) {
        if (path == null) {
            throw new NullPointerException("Path may not be null");
        }

        Configuration root = getRoot();
        Configuration defaults = root == null ? null : root.getDefaults();
        return (defaults == null) ? null : defaults.get(createPath(this, path));
    }

    protected void mapChildrenKeys(Set<String> output, ru.leymooo.cfg.configuration.ConfigurationSection section, boolean deep) {
        if (section instanceof ru.leymooo.cfg.configuration.MemorySection) {
            ru.leymooo.cfg.configuration.MemorySection sec = (ru.leymooo.cfg.configuration.MemorySection) section;

            for (Map.Entry<String, Object> entry : sec.map.entrySet()) {
                output.add(createPath(section, entry.getKey(), this));

                if (deep && (entry.getValue() instanceof ru.leymooo.cfg.configuration.ConfigurationSection)) {
                    ru.leymooo.cfg.configuration.ConfigurationSection subsection = (ru.leymooo.cfg.configuration.ConfigurationSection) entry.getValue();
                    mapChildrenKeys(output, subsection, deep);
                }
            }
        } else {
            Set<String> keys = section.getKeys(deep);

            for (String key : keys) {
                output.add(createPath(section, key, this));
            }
        }
    }

    protected void mapChildrenValues(Map<String, Object> output, ru.leymooo.cfg.configuration.ConfigurationSection section, boolean deep) {
        if (section instanceof ru.leymooo.cfg.configuration.MemorySection) {
            ru.leymooo.cfg.configuration.MemorySection sec = (ru.leymooo.cfg.configuration.MemorySection) section;

            for (Map.Entry<String, Object> entry : sec.map.entrySet()) {
                output.put(createPath(section, entry.getKey(), this), entry.getValue());

                if (entry.getValue() instanceof ru.leymooo.cfg.configuration.ConfigurationSection) {
                    if (deep) {
                        mapChildrenValues(output, (ConfigurationSection) entry.getValue(), deep);
                    }
                }
            }
        } else {
            Map<String, Object> values = section.getValues(deep);

            for (Map.Entry<String, Object> entry : values.entrySet()) {
                output.put(createPath(section, entry.getKey(), this), entry.getValue());
            }
        }
    }

    @Override
    public String toString() {
        Configuration root = getRoot();
        return getClass().getSimpleName() + "[path='" + getCurrentPath() + "', root='" + (root == null ? null : root.getClass().getSimpleName()) +
                "']";
    }
}
