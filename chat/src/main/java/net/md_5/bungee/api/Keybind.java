package net.md_5.bungee.api;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import net.md_5.bungee.api.chat.KeybindComponent;

/**
 * All possible keybind values used by {@link KeybindComponent}.
 */
@RequiredArgsConstructor
public enum Keybind
{
    JUMP( "key.jump", "SPACE" ),
    SNEAK( "key.sneak", "LSHIFT" ),
    SPRINT( "key.sprint", "LCTRL" ),
    LEFT( "key.left", "A" ),
    RIGHT( "key.right", "D" ),
    BACK( "key.back", "S" ),
    FORWARD( "key.forward", "W" ),

    ATTACK( "key.attack", "LMOUSE" ),
    PICK_ITEM( "key.pickItem", "MMOUSE" ),
    USE( "key.use", "RMOUSE" ),

    DROP( "key.drop", "Q" ),
    HOTBAR_1( "key.hotbar.1", "1" ),
    HOTBAR_2( "key.hotbar.2", "2" ),
    HOTBAR_3( "key.hotbar.3", "3" ),
    HOTBAR_4( "key.hotbar.4", "4" ),
    HOTBAR_5( "key.hotbar.5", "5" ),
    HOTBAR_6( "key.hotbar.6", "6" ),
    HOTBAR_7( "key.hotbar.7", "7" ),
    HOTBAR_8( "key.hotbar.8", "8" ),
    HOTBAR_9( "key.hotbar.9", "9" ),
    INVENTORY( "key.inventory", "E" ),
    SWAP_HANDS( "key.swapHands", "F" ),

    LOAD_TOOLBAR_ACTIVATOR( "key.loadToolbarActivator", "X" ),
    SAVE_TOOLBAR_ACTIVATOR( "key.saveToolbarActivator", "C" ),

    PLAYERLIST( "key.playerlist", "TAB" ),
    CHAT( "key.chat", "T" ),
    COMMAND( "key.command", "/" ),

    ADVANCEMENTS( "key.advancements", "L" ),
    SPECTATOR_OUTLINES( "key.spectatorOutlines", "" ),
    SCREENSHOT( "key.screenshot", "F2" ),
    SMOOTH_CAMERA( "key.smoothCamera", "" ),
    FULLSCREEN( "key.fullscreen", "F11" ),
    TOGGLE_PERSPECTIVE( "key.togglePerspective", "F5" ),

    /**
     * A special value indicating a custom internal value.
     */
    CUSTOM( "", "" );

    private static final Map<String, Keybind> BY_VALUE = new HashMap<>();
    /**
     * The internal value to use.
     */
    @Getter
    private final String value;
    /**
     * The default key.
     */
    @Getter
    private final String defaultKey;

    static
    {
        for ( Keybind keybind : values() )
        {
            BY_VALUE.put( keybind.value, keybind );
        }
    }

    /**
     * Get the keybind represented by the specified internal value.
     *
     * @param value the internal value to search for
     * @return the Keybind, or null if none exists for the internal value
     */
    public static Keybind getByValue(String value)
    {
        return BY_VALUE.get( value );
    }
}
