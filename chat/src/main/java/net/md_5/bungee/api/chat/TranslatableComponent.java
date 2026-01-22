package net.md_5.bungee.api.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.md_5.bungee.chat.TranslationRegistry;

@Getter
@ToString
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public final class TranslatableComponent extends BaseComponent
{

    private static final Pattern FORMAT = Pattern.compile( "%(?:(\\d+)\\$)?([A-Za-z%]|$)" );

    /**
     * The key into the Minecraft locale files to use for the translation. The
     * text depends on the client's locale setting. The console is always en_US
     */
    private String translate;
    /**
     * The components to substitute into the translation
     */
    private List<BaseComponent> with;
    /**
     * The fallback text, if the translation is not found
     */
    private String fallback;

    /**
     * Creates a translatable component from the original to clone it.
     *
     * @param original the original for the new translatable component.
     */
    public TranslatableComponent(TranslatableComponent original)
    {
        super( original );
        setTranslate( original.getTranslate() );
        setFallback( original.getFallback() );

        if ( original.getWith() != null )
        {
            List<BaseComponent> temp = new ArrayList<>();
            for ( BaseComponent baseComponent : original.getWith() )
            {
                temp.add( baseComponent.duplicate() );
            }
            setWith( temp );
        }
    }

    /**
     * Creates a translatable component with the passed substitutions
     *
     * @param translate the translation key
     * @param with the {@link java.lang.String}s and
     * {@link net.md_5.bungee.api.chat.BaseComponent}s to use into the
     * translation
     * @see #translate
     * @see #setWith(java.util.List)
     */
    public TranslatableComponent(String translate, Object... with)
    {
        setTranslate( translate );
        if ( with != null && with.length != 0 )
        {
            List<BaseComponent> temp = new ArrayList<BaseComponent>();
            for ( Object w : with )
            {
                if ( w instanceof BaseComponent )
                {
                    temp.add( (BaseComponent) w );
                } else
                {
                    temp.add( new TextComponent( String.valueOf( w ) ) );
                }
            }
            setWith( temp );
        }
    }

    /**
     * Creates a translatable component with the passed substitutions
     *
     * @param translatable the translatable object
     * @param with the {@link java.lang.String}s and
     * {@link net.md_5.bungee.api.chat.BaseComponent}s to use into the
     * translation
     * @see #translate
     * @see #setWith(java.util.List)
     */
    public TranslatableComponent(TranslationProvider translatable, Object... with)
    {
        this( translatable.getTranslationKey(), with );
    }

    /**
     * Creates a duplicate of this TranslatableComponent.
     *
     * @return the duplicate of this TranslatableComponent.
     */
    @Override
    public TranslatableComponent duplicate()
    {
        return new TranslatableComponent( this );
    }

    /**
     * Sets The key into the Minecraft locale files to use for the translation.
     * The text depends on the client's locale setting. The console is always
     * en_US.
     *
     * @param translate new translation key
     * @return this
     */
    public TranslatableComponent setTranslate(String translate)
    {
        this.translate = translate;
        return this;
    }

    /**
     * Sets the translation substitutions to be used in this component. Removes
     * any previously set substitutions
     *
     * @param components the components to substitute
     * @return this
     */
    public TranslatableComponent setWith(List<BaseComponent> components)
    {
        for ( BaseComponent component : components )
        {
            component.parent = this;
        }
        with = components;
        return this;
    }

    /**
     * Sets the fallback text, if the translation is not found.
     *
     * @param fallback new fallback text
     * @return this
     */
    public TranslatableComponent setFallback(String fallback)
    {
        this.fallback = fallback;
        return this;
    }

    /**
     * Adds a text substitution to the component. The text will inherit this
     * component's formatting
     *
     * @param text the text to substitute
     * @return this
     */
    public TranslatableComponent addWith(String text)
    {
        return addWith( new TextComponent( text ) );
    }

    /**
     * Adds a component substitution to the component. The text will inherit
     * this component's formatting
     *
     * @param component the component to substitute
     * @return this
     */
    public TranslatableComponent addWith(BaseComponent component)
    {
        if ( with == null )
        {
            with = new ArrayList<BaseComponent>();
        }
        component.parent = this;
        with.add( component );
        return this;
    }

    @Override
    protected void toPlainText(StringVisitor builder)
    {
        convert( builder, false );
        super.toPlainText( builder );
    }

    @Override
    protected void toLegacyText(StringVisitor builder)
    {
        convert( builder, true );
        super.toLegacyText( builder );
    }

    private void convert(StringVisitor builder, boolean applyFormat)
    {
        String trans = TranslationRegistry.INSTANCE.translate( translate );

        if ( trans.equals( translate ) && fallback != null )
        {
            trans = fallback;
        }

        Matcher matcher = FORMAT.matcher( trans );
        int position = 0;
        int i = 0;
        while ( matcher.find( position ) )
        {
            int pos = matcher.start();
            if ( pos != position )
            {
                if ( applyFormat )
                {
                    addFormat( builder );
                }
                builder.append( trans.substring( position, pos ) );
            }
            position = matcher.end();

            String formatCode = matcher.group( 2 );
            switch ( formatCode.charAt( 0 ) )
            {
                case 's':
                case 'd':
                    String withIndex = matcher.group( 1 );

                    BaseComponent withComponent = with.get( withIndex != null ? Integer.parseInt( withIndex ) - 1 : i++ );
                    if ( applyFormat )
                    {
                        withComponent.toLegacyText( builder );
                    } else
                    {
                        withComponent.toPlainText( builder );
                    }
                    break;
                case '%':
                    if ( applyFormat )
                    {
                        addFormat( builder );
                    }
                    builder.append( '%' );
                    break;
            }
        }
        if ( trans.length() != position )
        {
            if ( applyFormat )
            {
                addFormat( builder );
            }
            builder.append( trans.substring( position ) );
        }
    }
}
