package net.md_5.bungee.api.chat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public final class TextComponent extends BaseComponent
{

    private static final Pattern url = Pattern.compile( "^(?:(https?)://)?([-\\w_\\.]{2,}\\.[a-z]{2,4})(/\\S*)?$" );

    /**
     * Converts the old formatting system that used
     * {@link net.md_5.bungee.api.ChatColor#COLOR_CHAR} into the new json based
     * system.
     *
     * @param message the text to convert
     * @return the components needed to print the message to the client
     */
    public static BaseComponent fromLegacy(String message)
    {
        return fromLegacy( message, ChatColor.WHITE );
    }

    /**
     * Converts the old formatting system that used
     * {@link net.md_5.bungee.api.ChatColor#COLOR_CHAR} into the new json based
     * system.
     *
     * @param message the text to convert
     * @param defaultColor color to use when no formatting is to be applied
     * (i.e. after ChatColor.RESET).
     * @return the components needed to print the message to the client
     */
    public static BaseComponent fromLegacy(String message, ChatColor defaultColor)
    {
        ComponentBuilder componentBuilder = new ComponentBuilder();
        populateComponentStructure( message, defaultColor, componentBuilder::append );
        return componentBuilder.build();
    }

    /**
     * Converts the old formatting system that used
     * {@link net.md_5.bungee.api.ChatColor#COLOR_CHAR} into the new json based
     * system.
     *
     * @param message the text to convert
     * @return the components needed to print the message to the client
     * @deprecated {@link #fromLegacy(String)} is preferred as it will
     * consolidate all components into a single BaseComponent with extra
     * contents as opposed to an array of components which is non-standard and
     * may result in unexpected behavior.
     */
    @Deprecated
    public static BaseComponent[] fromLegacyText(String message)
    {
        return fromLegacyText( message, ChatColor.WHITE );
    }

    /**
     * Converts the old formatting system that used
     * {@link net.md_5.bungee.api.ChatColor#COLOR_CHAR} into the new json based
     * system.
     *
     * @param message the text to convert
     * @param defaultColor color to use when no formatting is to be applied
     * (i.e. after ChatColor.RESET).
     * @return the components needed to print the message to the client
     * @deprecated {@link #fromLegacy(String, ChatColor)} is preferred as it
     * will consolidate all components into a single BaseComponent with extra
     * contents as opposed to an array of components which is non-standard and
     * may result in unexpected behavior.
     */
    @Deprecated
    public static BaseComponent[] fromLegacyText(String message, ChatColor defaultColor)
    {
        ArrayList<BaseComponent> components = new ArrayList<>();
        populateComponentStructure( message, defaultColor, components::add );
        return components.toArray( new BaseComponent[ 0 ] );
    }

    private static void populateComponentStructure(String message, ChatColor defaultColor, Consumer<BaseComponent> appender)
    {
        StringBuilder builder = new StringBuilder();
        TextComponent component = new TextComponent();
        Matcher matcher = url.matcher( message );

        for ( int i = 0; i < message.length(); i++ )
        {
            char c = message.charAt( i );
            if ( c == ChatColor.COLOR_CHAR )
            {
                if ( ++i >= message.length() )
                {
                    break;
                }
                c = message.charAt( i );
                if ( c >= 'A' && c <= 'Z' )
                {
                    c += 32;
                }
                ChatColor format;
                if ( c == 'x' && i + 12 < message.length() )
                {
                    StringBuilder hex = new StringBuilder( "#" );
                    for ( int j = 0; j < 6; j++ )
                    {
                        hex.append( message.charAt( i + 2 + ( j * 2 ) ) );
                    }
                    try
                    {
                        format = ChatColor.of( hex.toString() );
                    } catch ( IllegalArgumentException ex )
                    {
                        format = null;
                    }

                    i += 12;
                } else
                {
                    format = ChatColor.getByChar( c );
                }
                if ( format == null )
                {
                    continue;
                }
                if ( builder.length() > 0 )
                {
                    TextComponent old = component;
                    component = new TextComponent( old );
                    old.setText( builder.toString() );
                    builder = new StringBuilder();
                    appender.accept( old );
                }
                if ( format == ChatColor.BOLD )
                {
                    component.setBold( true );
                } else if ( format == ChatColor.ITALIC )
                {
                    component.setItalic( true );
                } else if ( format == ChatColor.UNDERLINE )
                {
                    component.setUnderlined( true );
                } else if ( format == ChatColor.STRIKETHROUGH )
                {
                    component.setStrikethrough( true );
                } else if ( format == ChatColor.MAGIC )
                {
                    component.setObfuscated( true );
                } else
                {
                    if ( format == ChatColor.RESET )
                    {
                        format = defaultColor;
                    }
                    component = new TextComponent();
                    component.setColor( format );
                    component.setReset( true );
                }
                continue;
            }
            int pos = message.indexOf( ' ', i );
            if ( pos == -1 )
            {
                pos = message.length();
            }
            if ( matcher.region( i, pos ).find() )
            { //Web link handling

                if ( builder.length() > 0 )
                {
                    TextComponent old = component;
                    component = new TextComponent( old );
                    old.setText( builder.toString() );
                    builder = new StringBuilder();
                    appender.accept( old );
                }

                TextComponent old = component;
                component = new TextComponent( old );
                String urlString = message.substring( i, pos );
                component.setText( urlString );
                component.setClickEvent( new ClickEvent( ClickEvent.Action.OPEN_URL,
                        urlString.startsWith( "http" ) ? urlString : "http://" + urlString ) );
                appender.accept( component );
                i += pos - i - 1;
                component = old;
                continue;
            }
            builder.append( c );
        }

        component.setText( builder.toString() );
        appender.accept( component );
    }

    /**
     * Internal compatibility method to transform an array of components to a
     * single component.
     *
     * @param components array
     * @return single component
     */
    public static BaseComponent fromArray(BaseComponent... components)
    {
        if ( components == null )
        {
            return null;
        }

        if ( components.length == 1 )
        {
            return components[0];
        }

        return new TextComponent( components );
    }

    /**
     * The text of the component that will be displayed to the client
     */
    private String text;

    /**
     * Creates a TextComponent with blank text.
     */
    public TextComponent()
    {
        this.text = "";
    }

    /**
     * Creates a TextComponent with formatting and text from the passed
     * component
     *
     * @param textComponent the component to copy from
     */
    public TextComponent(TextComponent textComponent)
    {
        super( textComponent );
        setText( textComponent.getText() );
    }

    /**
     * Creates a TextComponent with blank text and the extras set to the passed
     * array
     *
     * @param extras the extras to set
     */
    public TextComponent(BaseComponent... extras)
    {
        this();
        if ( extras.length == 0 )
        {
            return;
        }
        setExtra( new ArrayList<BaseComponent>( Arrays.asList( extras ) ) );
    }

    /**
     * Creates a duplicate of this TextComponent.
     *
     * @return the duplicate of this TextComponent.
     */
    @Override
    public TextComponent duplicate()
    {
        return new TextComponent( this );
    }

    @Override
    protected void toPlainText(StringBuilder builder)
    {
        builder.append( text );
        super.toPlainText( builder );
    }

    @Override
    protected void toLegacyText(StringBuilder builder)
    {
        addFormat( builder );
        builder.append( text );
        super.toLegacyText( builder );
    }

    @Override
    public String toString()
    {
        return "TextComponent{text=" + text + ", " + super.toString() + '}';
    }
}
