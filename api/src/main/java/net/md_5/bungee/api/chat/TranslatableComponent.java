package net.md_5.bungee.api.chat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Getter
@Setter
@NoArgsConstructor
public class TranslatableComponent extends BaseComponent
{
    public final ResourceBundle locales = ResourceBundle.getBundle( "en_US" );

    private String translate;
    private List<BaseComponent> with;

    public TranslatableComponent(String translate, Object... with)
    {
        setTranslate( translate );
        this.with = new ArrayList<>();
        for ( Object w : with )
        {
            if ( w instanceof String )
            {
                this.with.add( new TextComponent( (String) w ) );
            } else
            {
                this.with.add( (BaseComponent) w );
            }
        }
    }

    public void setWith(List<BaseComponent> components)
    {
        for ( BaseComponent component : components )
        {
            component.parent = this;
        }
        with = components;
    }

    @Override
    protected void toPlainText(StringBuilder builder)
    {
        String[] parts = translate.split( "((?<=%s)|(?=%s))" );
        int i = 0;
        for ( String part : parts )
        {
            if ( part.equals( "%s" ) )
            {
                with.get( i ).toPlainText( builder );
                i++;
            } else
            {
                builder.append( part );
            }
        }
        super.toPlainText( builder );
    }

    @Override
    protected void toLegacyText(StringBuilder builder)
    {
        String[] parts = locales.getString( translate ).split( "((?<=%s)|(?=%s))" );
        int i = 0;
        for ( String part : parts )
        {
            if ( part.equals( "%s" ) )
            {
                with.get( i ).toLegacyText( builder );
                i++;
            } else
            {
                builder.append( getColor() );
                if ( isBold() ) builder.append( ChatColor.BOLD );
                if ( isItalic() ) builder.append( ChatColor.ITALIC );
                if ( isUnderlined() ) builder.append( ChatColor.UNDERLINE );
                if ( isStrikethrough() ) builder.append( ChatColor.STRIKETHROUGH );
                if ( isObfuscated() ) builder.append( ChatColor.MAGIC );
                builder.append( part );
            }
        }
        super.toLegacyText( builder );
    }

    @Override
    public String toString()
    {
        return String.format( "TranslatableComponent{translate=%s, with=%s, %s}", translate, with, super.toString() );
    }
}
