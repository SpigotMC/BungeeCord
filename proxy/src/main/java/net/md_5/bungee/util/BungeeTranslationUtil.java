package net.md_5.bungee.util;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;

public class BungeeTranslationUtil
{
    // support only simple translations, no escaping and ignore specialized formatting
    private static final Pattern TRANSLATE_FORMAT = Pattern.compile( "\\{(\\d*)(?:,[^}]+)?}" );

    public static @NotNull BaseComponent getTranslationComponent0(String trans, Object... args)
    {

        Matcher matcher = TRANSLATE_FORMAT.matcher( trans );
        int position = 0;
        int i = 0;
        BaseComponent lastNonArg = null;
        ComponentBuilder builder = new ComponentBuilder();
        while ( matcher.find() )
        {
            // Add the static part of the string
            int pos = matcher.start();
            if ( pos != position )
            {
                builder.appendLegacy( trans.substring( position, pos ) );
                BaseComponent curr = builder.getCurrentComponent();
                if ( lastNonArg != null )
                {
                    // undo eventual formatting of previous argument
                    boolean colorChange = curr.isReset();
                    if ( !Objects.equals( lastNonArg.getColor(), curr.getColor() ) )
                    {
                        colorChange = true;
                        builder.color( lastNonArg.getColor() );
                    }
                    if ( colorChange && lastNonArg.isBold() || lastNonArg.isBold() != curr.isBold() )
                    {
                        builder.bold( lastNonArg.isBold() );
                    }
                    if ( colorChange && lastNonArg.isItalic() || lastNonArg.isItalic() != curr.isItalic() )
                    {
                        builder.italic( lastNonArg.isItalic() );
                    }
                    if ( colorChange && lastNonArg.isUnderlined() || lastNonArg.isUnderlined() != curr.isUnderlined() )
                    {
                        builder.underlined( lastNonArg.isUnderlined() );
                    }
                    if ( colorChange && lastNonArg.isStrikethrough() || lastNonArg.isStrikethrough() != curr.isStrikethrough() )
                    {
                        builder.strikethrough( lastNonArg.isStrikethrough() );
                    }
                    if ( colorChange && lastNonArg.isObfuscated() || lastNonArg.isObfuscated() != curr.isObfuscated() )
                    {
                        builder.obfuscated( lastNonArg.isObfuscated() );
                    }
                }
                lastNonArg = findLastComponent( builder.getCurrentComponent().duplicate() );
            }
            position = matcher.end();

            // find argument index
            String withIndex = matcher.group( 1 );
            Object arg;
            if ( !withIndex.isEmpty() )
            {
                int index = Integer.parseInt( withIndex );
                if ( index >= 0 && index < args.length )
                {
                    arg = args[ index ];
                } else
                {
                    arg = "{" + withIndex + "}";
                }
            } else if ( i < args.length )
            {
                arg = args[ i++ ];
            } else
            {
                arg = "{" + withIndex + "}";
            }
            if ( arg instanceof BaseComponent[] )
            {
                arg = TextComponent.fromArray( (BaseComponent[]) arg );
            }
            if ( arg instanceof BaseComponent )
            {
                BaseComponent argB = (BaseComponent) arg;
                // argument base formatting should use formatting of last non-argument part if no reset is requested
                builder.append( argB, isFirstReset( argB ) ? ComponentBuilder.FormatRetention.NONE : ComponentBuilder.FormatRetention.ALL );
            } else
            {
                builder.append( arg.toString() );
            }
        }
        return builder.build();
    }

    private static BaseComponent findLastComponent(BaseComponent comp)
    {
        BaseComponent last = comp;
        List<BaseComponent> extra = comp.getExtra();
        if ( extra != null && !extra.isEmpty() )
        {
            last = findLastComponent( extra.get( extra.size() - 1 ) );
        }
        return last;
    }

    private static boolean isFirstReset(BaseComponent comp)
    {
        System.out.println( "isFirstReset(comp = " + comp + ")" );
        if ( comp.isReset() )
        {
            return true;
        }
        if ( isSelfEmpty( comp ) )
        {
            List<BaseComponent> extra = comp.getExtra();
            if ( extra != null && !extra.isEmpty() )
            {
                return isFirstReset( extra.get( 0 ) );
            }
        }
        return false;
    }

    private static boolean isSelfEmpty(BaseComponent comp)
    {
        if ( comp instanceof TextComponent )
        {
            return ( (TextComponent) comp ).getText().isEmpty();
        }
        if ( comp instanceof TranslatableComponent )
        {
            return ( (TranslatableComponent) comp ).getTranslate().isEmpty();
        }
        return false;
    }
}
