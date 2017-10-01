package net.md_5.bungee.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Keybind
{

    NONE( "" ),
    KEY_FORWARD( "key.forward" ),
    KEY_LEFT( "key.left" ),
    KEY_BACK( "key.back" ),
    KEY_RIGHT( "key.right" ),
    KEY_JUMP( "key.jump" ),
    KEY_SNEAK( "key.sneak" ),
    KEY_SPRINT( "key.sprint" ),
    KEY_INVENTORY( "key.inventory" ),
    KEY_SWAP_HANDS( "key.swapHands" ),
    KEY_DROP( "key.drop" ),
    KEY_USE( "key.use" ),
    KEY_ATTACK( "key.attack" ),
    KEY_PICK_ITEM( "key.pickItem" ),
    KEY_CHAT( "key.chat" ),
    KEY_PLAYER_LIST( "key.playerlist" ),
    KEY_COMMAND( "key.command" ),
    KEY_SCREENSHOT( "key.screenshot" ),
    KEY_TOGGLE_PERSPECTIVE( "key.togglePerspective" ),
    KEY_SMOOTH_CAMERA( "key.smoothCamera" ),
    KEY_FULLSCREEN( "key.fullscreen" ),
    KEY_SPECTATOR_OUTLINES( "key.spectatorOutlines" ),
    KEY_HOTBAR_1( "key.hotbar.1" ),
    KEY_HOTBAR_2( "key.hotbar.2" ),
    KEY_HOTBAR_3( "key.hotbar.3" ),
    KEY_HOTBAR_4( "key.hotbar.4" ),
    KEY_HOTBAR_5( "key.hotbar.5" ),
    KEY_HOTBAR_6( "key.hotbar.6" ),
    KEY_HOTBAR_7( "key.hotbar.7" ),
    KEY_HOTBAR_8( "key.hotbar.8" ),
    KEY_HOTBAR_9( "key.hotbar.9" ),
    KEY_SAVE_TOOLBAR( "key.saveToolbarActivator" ),
    KEY_LOAD_TOOLBAR( "key.loadToolbarActivator" );

    private String keyCode;

    /**
     * Deserialize the given keycode into a Keybind value
     *
     * @param keycode keycode as string
     * @return the deserialized keycode
     */
    public static Keybind deserialize( String keycode )
    {
        for ( Keybind keyBind : values() ) {
            if ( keyBind.getKeyCode().equals( keycode ) ) {
                return keyBind;
            }
        }

        return Keybind.NONE;
    }

}
