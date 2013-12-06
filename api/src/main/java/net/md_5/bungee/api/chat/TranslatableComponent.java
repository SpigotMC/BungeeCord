package net.md_5.bungee.api.chat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TranslatableComponent extends BaseComponent
{

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
        //TODO
        super.toPlainText( builder );
    }

    @Override
    protected void toLegacyText(StringBuilder builder)
    {
        //TODO
        super.toLegacyText( builder );
    }

    @Override
    public String toString()
    {
        return String.format( "TranslatableComponent{translate=%s, with=%s, %s}", translate, with, super.toString() );
    }
}
