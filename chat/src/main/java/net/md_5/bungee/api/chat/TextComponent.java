package net.md_5.bungee.api.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;

import java.util.*;
import java.util.regex.Matcher;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TextComponent extends BaseComponent
{


    /**
     * Converts the old formatting system that used
     * {@link net.md_5.bungee.api.ChatColor#COLOR_CHAR} into the new json based
     * system.
     *
     * @param message the text to convert
     * @return the components needed to print the message to the client
     */
    /**
     * Converts the old formatting system that used
     * {@link net.md_5.bungee.api.ChatColor#COLOR_CHAR} into the new json based
     * system.
     *
     * @param message the text to convert
     * @return the components needed to print the message to the client
     */
    public static BaseComponent[] fromLegacyText(String message){
        return fromLegacyText(message,Arrays.asList(MessageProcessor.URL_MESSAGEPROCESSOR));
    }

    /**
     * Converts the old formatting system that used
     * {@link net.md_5.bungee.api.ChatColor#COLOR_CHAR} into the new json based
     * system.
     *
     * @param message the text to convert
     * @return the components needed to print the message to the client
     */
    /**
     * Converts the old formatting system that used
     * {@link net.md_5.bungee.api.ChatColor#COLOR_CHAR} into the new json based
     * system.
     *
     * @param message the text to convert
     * @param messageProcessors list of message processors
     * @return the components needed to print the message to the client
     */
    public static BaseComponent[] fromLegacyText(String message,List<MessageProcessor> messageProcessors)
    {
        ArrayList<BaseComponent> components = new ArrayList<BaseComponent>();
        StringBuilder builder = new StringBuilder();
        TextComponent component = new TextComponent();
        HashMap<Matcher,MessageProcessor> matchers = new HashMap<Matcher, MessageProcessor>();
        for (MessageProcessor messageProcessor : messageProcessors) {
            matchers.put(messageProcessor.getPattern().matcher(message), messageProcessor);
        }
mainloop:
        for ( int i = 0; i < message.length(); i++ )
        {
            char c = message.charAt( i );
            if ( c == ChatColor.COLOR_CHAR )
            {
                i++;
                c = message.charAt( i );
                if ( c >= 'A' && c <= 'Z' )
                {
                    c += 32;
                }
                ChatColor format = ChatColor.getByChar( c );
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
                    components.add( old );
                }
                switch ( format )
                {
                    case BOLD:
                        component.setBold( true );
                        break;
                    case ITALIC:
                        component.setItalic( true );
                        break;
                    case UNDERLINE:
                        component.setUnderlined( true );
                        break;
                    case STRIKETHROUGH:
                        component.setStrikethrough( true );
                        break;
                    case MAGIC:
                        component.setObfuscated( true );
                        break;
                    case RESET:
                        format = ChatColor.WHITE;
                    default:
                        component = new TextComponent();
                        component.setColor( format );
                        break;
                }
                continue;
            }
            int pos = message.indexOf( ' ', i );
            if ( pos == -1 )
            {
                pos = message.length();
            }
            for (Map.Entry<Matcher, MessageProcessor> entry : matchers.entrySet()) {
                Matcher matcher = entry.getKey();
                MessageProcessor messageProcessor = entry.getValue();
                if ( matcher.region( i, pos ).find() )
                {

                    if ( builder.length() > 0 )
                    {
                        TextComponent old = component;
                        component = new TextComponent( old );
                        old.setText( builder.toString() );
                        builder = new StringBuilder();
                        components.add( old );
                    }

                    TextComponent old = component;
                    String matchedMessage = message.substring( i, pos );
                    component = messageProcessor.process(matchedMessage, old);
                    components.add( component );
                    i += pos - i - 1;
                    component = old;
                    continue mainloop;
                }
            }
            builder.append( c );
        }
        if ( builder.length() > 0 )
        {
            component.setText( builder.toString() );
            components.add( component );
        }

        // The client will crash if the array is empty
        if ( components.isEmpty() )
        {
            components.add( new TextComponent( "" ) );
        }

        return components.toArray( new BaseComponent[ components.size() ] );
    }

    /**
     * The text of the component that will be displayed to the client
     */
    private String text;

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
        setText( "" );
        setExtra( Arrays.asList( extras ) );
    }

    /**
     * Creates a duplicate of this TextComponent.
     *
     * @return the duplicate of this TextComponent.
     */
    @Override
    public BaseComponent duplicate()
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
        builder.append( getColor() );
        if ( isBold() )
        {
            builder.append( ChatColor.BOLD );
        }
        if ( isItalic() )
        {
            builder.append( ChatColor.ITALIC );
        }
        if ( isUnderlined() )
        {
            builder.append( ChatColor.UNDERLINE );
        }
        if ( isStrikethrough() )
        {
            builder.append( ChatColor.STRIKETHROUGH );
        }
        if ( isObfuscated() )
        {
            builder.append( ChatColor.MAGIC );
        }
        builder.append( text );
        super.toLegacyText( builder );
    }

    @Override
    public String toString()
    {
        return String.format( "TextComponent{text=%s, %s}", text, super.toString() );
    }
}
