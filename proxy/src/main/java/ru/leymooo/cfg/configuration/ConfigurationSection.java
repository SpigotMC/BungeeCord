package ru.leymooo.cfg.configuration;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a section of a {@link ru.leymooo.cfg.configuration.Configuration}
 */
public interface ConfigurationSection {

    /**
     * Gets a set containing all keys in this section.
     * <p>
     * If deep is set to true, then this will contain all the keys within any
     * child {@link ru.leymooo.cfg.configuration.ConfigurationSection}s (and their children, etc). These
     * will be in a valid path notation for you to use.
     * <p>
     * If deep is set to false, then this will contain only the keys of any
     * direct children, and not their own children.
     *
     * @param deep Whether or not to get a deep list, as opposed to a shallow
     *             list.
     * @return Set of keys contained within this ConfigurationSection.
     */
    Set<String> getKeys(boolean deep);

    /**
     * Gets a Map containing all keys and their values for this section.
     * <p>
     * If deep is set to true, then this will contain all the keys and values
     * within any child {@link ru.leymooo.cfg.configuration.ConfigurationSection}s (and their children,
     * etc). These keys will be in a valid path notation for you to use.
     * <p>
     * If deep is set to false, then this will contain only the keys and
     * values of any direct children, and not their own children.
     *
     * @param deep Whether or not to get a deep list, as opposed to a shallow
     *             list.
     * @return Map of keys and values of this section.
     */
    Map<String, Object> getValues(boolean deep);

    /**
     * Checks if this {@link ru.leymooo.cfg.configuration.ConfigurationSection} contains the given path.
     * <p>
     * If the value for the requested path does not exist but a default value
     * has been specified, this will return true.
     *
     * @param path Path to check for existence.
     * @return True if this section contains the requested path, either via
     * default or being set.
     * @throws IllegalArgumentException Thrown when path is null.
     */
    boolean contains(String path);

    /**
     * Checks if this {@link ru.leymooo.cfg.configuration.ConfigurationSection} has a value set for the
     * given path.
     * <p>
     * If the value for the requested path does not exist but a default value
     * has been specified, this will still return false.
     *
     * @param path Path to check for existence.
     * @return True if this section contains the requested path, regardless of
     * having a default.
     * @throws IllegalArgumentException Thrown when path is null.
     */
    boolean isSet(String path);

    /**
     * Gets the path of this {@link ru.leymooo.cfg.configuration.ConfigurationSection} from its root {@link
     * ru.leymooo.cfg.configuration.Configuration}
     * <p>
     * For any {@link ru.leymooo.cfg.configuration.Configuration} themselves, this will return an empty
     * string.
     * <p>
     * If the section is no longer contained within its root for any reason,
     * such as being replaced with a different value, this may return null.
     * <p>
     * To retrieve the single name of this section, that is, the final part of
     * the path returned by this method, you may use {@link #getName()}.
     *
     * @return Path of this section relative to its root
     */
    String getCurrentPath();

    /**
     * Gets the name of this individual {@link ru.leymooo.cfg.configuration.ConfigurationSection}, in the
     * path.
     * <p>
     * This will always be the final part of {@link #getCurrentPath()}, unless
     * the section is orphaned.
     *
     * @return Name of this section
     */
    String getName();

    /**
     * Gets the root {@link ru.leymooo.cfg.configuration.Configuration} that contains this {@link
     * ru.leymooo.cfg.configuration.ConfigurationSection}
     * <p>
     * For any {@link ru.leymooo.cfg.configuration.Configuration} themselves, this will return its own
     * object.
     * <p>
     * If the section is no longer contained within its root for any reason,
     * such as being replaced with a different value, this may return null.
     *
     * @return Root configuration containing this section.
     */
    Configuration getRoot();

    /**
     * Gets the parent {@link ru.leymooo.cfg.configuration.ConfigurationSection} that directly contains
     * this {@link ru.leymooo.cfg.configuration.ConfigurationSection}.
     * <p>
     * For any {@link ru.leymooo.cfg.configuration.Configuration} themselves, this will return null.
     * <p>
     * If the section is no longer contained within its parent for any reason,
     * such as being replaced with a different value, this may return null.
     *
     * @return Parent section containing this section.
     */
    ru.leymooo.cfg.configuration.ConfigurationSection getParent();

    /**
     * Gets the requested Object by path.
     * <p>
     * If the Object does not exist but a default value has been specified,
     * this will return the default value. If the Object does not exist and no
     * default value was specified, this will return null.
     *
     * @param path Path of the Object to get.
     * @return Requested Object.
     */
    Object get(String path);

    /**
     * Gets the requested Object by path, returning a default value if not
     * found.
     * <p>
     * If the Object does not exist then the specified default value will
     * returned regardless of if a default has been identified in the root
     * {@link ru.leymooo.cfg.configuration.Configuration}.
     *
     * @param path Path of the Object to get.
     * @param def  The default value to return if the path is not found.
     * @return Requested Object.
     */
    Object get(String path, Object def);

    /**
     * Sets the specified path to the given value.
     * <p>
     * If value is null, the entry will be removed. Any existing entry will be
     * replaced, regardless of what the new value is.
     * <p>
     * Some implementations may have limitations on what you may store. See
     * their individual javadoc for details. No implementations should allow
     * you to store {@link ru.leymooo.cfg.configuration.Configuration}s or {@link ru.leymooo.cfg.configuration.ConfigurationSection}s,
     * please use {@link #createSection(String)} for that.
     *
     * @param path  Path of the object to set.
     * @param value New value to set the path to.
     */
    void set(String path, Object value);

    /**
     * Creates an empty {@link ru.leymooo.cfg.configuration.ConfigurationSection} at the specified path.
     * <p>
     * Any value that was previously set at this path will be overwritten. If
     * the previous value was itself a {@link ru.leymooo.cfg.configuration.ConfigurationSection}, it will
     * be orphaned.
     *
     * @param path Path to create the section at.
     * @return Newly created section
     */
    ru.leymooo.cfg.configuration.ConfigurationSection createSection(String path);

    /**
     * Creates a {@link ru.leymooo.cfg.configuration.ConfigurationSection} at the specified path, with
     * specified values.
     * <p>
     * Any value that was previously set at this path will be overwritten. If
     * the previous value was itself a {@link ru.leymooo.cfg.configuration.ConfigurationSection}, it will
     * be orphaned.
     *
     * @param path Path to create the section at.
     * @param map  The values to used.
     * @return Newly created section
     */
    ru.leymooo.cfg.configuration.ConfigurationSection createSection(String path, Map<?, ?> map);

    // Primitives

    /**
     * Gets the requested String by path.
     * <p>
     * If the String does not exist but a default value has been specified,
     * this will return the default value. If the String does not exist and no
     * default value was specified, this will return null.
     *
     * @param path Path of the String to get.
     * @return Requested String.
     */
    String getString(String path);

    /**
     * Gets the requested String by path, returning a default value if not
     * found.
     * <p>
     * If the String does not exist then the specified default value will
     * returned regardless of if a default has been identified in the root
     * {@link ru.leymooo.cfg.configuration.Configuration}.
     *
     * @param path Path of the String to get.
     * @param def  The default value to return if the path is not found or is
     *             not a String.
     * @return Requested String.
     */
    String getString(String path, String def);

    /**
     * Checks if the specified path is a String.
     * <p>
     * <p> If the path exists but is not a String, this will return false. If the
     * path does not exist, this will return false. If the path does not exist
     * but a default value has been specified, this will check if that default
     * value is a String and return appropriately.</p>
     *
     * @param path Path of the String to check.
     * @return Whether or not the specified path is a String.
     */
    boolean isString(String path);

    /**
     * Gets the requested int by path.
     * <p>
     * <p>If the int does not exist but a default value has been specified, this
     * will return the default value. If the int does not exist and no default
     * value was specified, this will return 0.</p>
     *
     * @param path Path of the int to get.
     * @return Requested int.
     */
    int getInt(String path);

    /**
     * Gets the requested int by path, returning a default value if not found.
     * <p>
     * <p>If the int does not exist then the specified default value will
     * returned regardless of if a default has been identified in the root
     * {@link ru.leymooo.cfg.configuration.Configuration}.</p>
     *
     * @param path Path of the int to get.
     * @param def  The default value to return if the path is not found or is
     *             not an int.
     * @return Requested int.
     */
    int getInt(String path, int def);

    /**
     * Checks if the specified path is an int.
     * <p>
     * <p>If the path exists but is not a int, this will return false. If the
     * path does not exist, this will return false. If the path does not exist
     * but a default value has been specified, this will check if that default
     * value is a int and return appropriately.</p>
     *
     * @param path Path of the int to check.
     * @return Whether or not the specified path is an int.
     */
    boolean isInt(String path);

    /**
     * Gets the requested boolean by path.
     * <p>
     * If the boolean does not exist but a default value has been specified,
     * this will return the default value. If the boolean does not exist and
     * no default value was specified, this will return false.
     *
     * @param path Path of the boolean to get.
     * @return Requested boolean.
     */
    boolean getBoolean(String path);

    /**
     * Gets the requested boolean by path, returning a default value if not
     * found.
     * <p>
     * If the boolean does not exist then the specified default value will
     * returned regardless of if a default has been identified in the root
     * {@link ru.leymooo.cfg.configuration.Configuration}.
     *
     * @param path Path of the boolean to get.
     * @param def  The default value to return if the path is not found or is
     *             not a boolean.
     * @return Requested boolean.
     */
    boolean getBoolean(String path, boolean def);

    /**
     * Checks if the specified path is a boolean.
     * <p>
     * If the path exists but is not a boolean, this will return false. If the
     * path does not exist, this will return false. If the path does not exist
     * but a default value has been specified, this will check if that default
     * value is a boolean and return appropriately.
     *
     * @param path Path of the boolean to check.
     * @return Whether or not the specified path is a boolean.
     */
    boolean isBoolean(String path);

    /**
     * Gets the requested double by path.
     * <p>
     * If the double does not exist but a default value has been specified,
     * this will return the default value. If the double does not exist and no
     * default value was specified, this will return 0.
     *
     * @param path Path of the double to get.
     * @return Requested double.
     */
    double getDouble(String path);

    /**
     * Gets the requested double by path, returning a default value if not
     * found.
     * <p>
     * If the double does not exist then the specified default value will
     * returned regardless of if a default has been identified in the root
     * {@link ru.leymooo.cfg.configuration.Configuration}.
     *
     * @param path Path of the double to get.
     * @param def  The default value to return if the path is not found or is
     *             not a double.
     * @return Requested double.
     */
    double getDouble(String path, double def);

    /**
     * Checks if the specified path is a double.
     * <p>
     * If the path exists but is not a double, this will return false. If the
     * path does not exist, this will return false. If the path does not exist
     * but a default value has been specified, this will check if that default
     * value is a double and return appropriately.
     *
     * @param path Path of the double to check.
     * @return Whether or not the specified path is a double.
     */
    boolean isDouble(String path);

    /**
     * Gets the requested long by path.
     * <p>
     * If the long does not exist but a default value has been specified, this
     * will return the default value. If the long does not exist and no
     * default value was specified, this will return 0.
     *
     * @param path Path of the long to get.
     * @return Requested long.
     */
    long getLong(String path);

    /**
     * Gets the requested long by path, returning a default value if not
     * found.
     * <p>
     * If the long does not exist then the specified default value will
     * returned regardless of if a default has been identified in the root
     * {@link ru.leymooo.cfg.configuration.Configuration}.
     *
     * @param path Path of the long to get.
     * @param def  The default value to return if the path is not found or is
     *             not a long.
     * @return Requested long.
     */
    long getLong(String path, long def);

    /**
     * Checks if the specified path is a long.
     * <p>
     * If the path exists but is not a long, this will return false. If the
     * path does not exist, this will return false. If the path does not exist
     * but a default value has been specified, this will check if that default
     * value is a long and return appropriately.
     *
     * @param path Path of the long to check.
     * @return Whether or not the specified path is a long.
     */
    boolean isLong(String path);

    // Java

    /**
     * Gets the requested List by path.
     * <p>
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return null.
     *
     * @param path Path of the List to get.
     * @return Requested List.
     */
    List<?> getList(String path);

    /**
     * Gets the requested List by path, returning a default value if not
     * found.
     * <p>
     * If the List does not exist then the specified default value will
     * returned regardless of if a default has been identified in the root
     * {@link ru.leymooo.cfg.configuration.Configuration}.
     *
     * @param path Path of the List to get.
     * @param def  The default value to return if the path is not found or is
     *             not a List.
     * @return Requested List.
     */
    List<?> getList(String path, List<?> def);

    /**
     * Checks if the specified path is a List.
     * <p>
     * If the path exists but is not a List, this will return false. If the
     * path does not exist, this will return false. If the path does not exist
     * but a default value has been specified, this will check if that default
     * value is a List and return appropriately.
     *
     * @param path Path of the List to check.
     * @return Whether or not the specified path is a List.
     */
    boolean isList(String path);

    /**
     * Gets the requested List of String by path.
     * <p>
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return an empty List.
     * <p>
     * This method will attempt to cast any values into a String if possible,
     * but may miss any values out if they are not compatible.
     *
     * @param path Path of the List to get.
     * @return Requested List of String.
     */
    List<String> getStringList(String path);

    /**
     * Gets the requested List of Integer by path.
     * <p>
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return an empty List.
     * <p>
     * This method will attempt to cast any values into a Integer if possible,
     * but may miss any values out if they are not compatible.
     *
     * @param path Path of the List to get.
     * @return Requested List of Integer.
     */
    List<Integer> getIntegerList(String path);

    /**
     * Gets the requested List of Boolean by path.
     * <p>
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return an empty List.
     * <p>
     * This method will attempt to cast any values into a Boolean if possible,
     * but may miss any values out if they are not compatible.
     *
     * @param path Path of the List to get.
     * @return Requested List of Boolean.
     */
    List<Boolean> getBooleanList(String path);

    /**
     * Gets the requested List of Double by path.
     * <p>
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return an empty List.
     * <p>
     * This method will attempt to cast any values into a Double if possible,
     * but may miss any values out if they are not compatible.
     *
     * @param path Path of the List to get.
     * @return Requested List of Double.
     */
    List<Double> getDoubleList(String path);

    /**
     * Gets the requested List of Float by path.
     * <p>
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return an empty List.
     * <p>
     * This method will attempt to cast any values into a Float if possible,
     * but may miss any values out if they are not compatible.
     *
     * @param path Path of the List to get.
     * @return Requested List of Float.
     */
    List<Float> getFloatList(String path);

    /**
     * Gets the requested List of Long by path.
     * <p>
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return an empty List.
     * <p>
     * This method will attempt to cast any values into a Long if possible,
     * but may miss any values out if they are not compatible.
     *
     * @param path Path of the List to get.
     * @return Requested List of Long.
     */
    List<Long> getLongList(String path);

    /**
     * Gets the requested List of Byte by path.
     * <p>
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return an empty List.
     * <p>
     * This method will attempt to cast any values into a Byte if possible,
     * but may miss any values out if they are not compatible.
     *
     * @param path Path of the List to get.
     * @return Requested List of Byte.
     */
    List<Byte> getByteList(String path);

    /**
     * Gets the requested List of Character by path.
     * <p>
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return an empty List.
     * <p>
     * This method will attempt to cast any values into a Character if
     * possible, but may miss any values out if they are not compatible.
     *
     * @param path Path of the List to get.
     * @return Requested List of Character.
     */
    List<Character> getCharacterList(String path);

    /**
     * Gets the requested List of Short by path.
     * <p>
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return an empty List.
     * <p>
     * This method will attempt to cast any values into a Short if possible,
     * but may miss any values out if they are not compatible.
     *
     * @param path Path of the List to get.
     * @return Requested List of Short.
     */
    List<Short> getShortList(String path);

    /**
     * Gets the requested List of Maps by path.
     * <p>
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return an empty List.
     * <p>
     * This method will attempt to cast any values into a Map if possible, but
     * may miss any values out if they are not compatible.
     *
     * @param path Path of the List to get.
     * @return Requested List of Maps.
     */
    List<Map<?, ?>> getMapList(String path);

    /**
     * Gets the requested ConfigurationSection by path.
     * <p>
     * If the ConfigurationSection does not exist but a default value has been
     * specified, this will return the default value. If the
     * ConfigurationSection does not exist and no default value was specified,
     * this will return null.
     *
     * @param path Path of the ConfigurationSection to get.
     * @return Requested ConfigurationSection.
     */
    ru.leymooo.cfg.configuration.ConfigurationSection getConfigurationSection(String path);

    /**
     * Checks if the specified path is a ConfigurationSection.
     * <p>
     * If the path exists but is not a ConfigurationSection, this will return
     * false. If the path does not exist, this will return false. If the path
     * does not exist but a default value has been specified, this will check
     * if that default value is a ConfigurationSection and return
     * appropriately.
     *
     * @param path Path of the ConfigurationSection to check.
     * @return Whether or not the specified path is a ConfigurationSection.
     */
    boolean isConfigurationSection(String path);

    /**
     * Gets the equivalent {@link ru.leymooo.cfg.configuration.ConfigurationSection} from the default
     * {@link ru.leymooo.cfg.configuration.Configuration} defined in {@link #getRoot()}.
     * <p>
     * If the root contains no defaults, or the defaults doesn't contain a
     * value for this path, or the value at this path is not a {@link
     * ru.leymooo.cfg.configuration.ConfigurationSection} then this will return null.
     *
     * @return Equivalent section in root configuration
     */
    ru.leymooo.cfg.configuration.ConfigurationSection getDefaultSection();

    /**
     * Sets the default value in the root at the given path as provided.
     * <p>
     * If no source {@link ru.leymooo.cfg.configuration.Configuration} was provided as a default
     * collection, then a new {@link ru.leymooo.cfg.configuration.MemoryConfiguration} will be created to
     * hold the new default value.
     * <p>
     * If value is null, the value will be removed from the default
     * Configuration source.
     * <p>
     * If the value as returned by {@link #getDefaultSection()} is null, then
     * this will create a new section at the path, replacing anything that may
     * have existed there previously.
     *
     * @param path  Path of the value to set.
     * @param value Value to set the default to.
     * @throws IllegalArgumentException Thrown if path is null.
     */
    void addDefault(String path, Object value);
}
