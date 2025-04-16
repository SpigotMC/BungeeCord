package net.md_5.bungee.api.chat.hover.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;

@Data
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class Entity extends Content
{

    /**
     * Namespaced entity ID.
     *
     * Will use 'minecraft:pig' if null.
     */
    private String type;
    /**
     * Entity UUID in hyphenated hexadecimal format.
     *
     * Should be valid UUID. TODO : validate?
     */
    @NonNull
    private String id;
    /**
     * Name to display as the entity.
     *
     * This is optional and will be hidden if null.
     */
    private BaseComponent name;

    @Override
    public HoverEvent.Action requiredAction()
    {
        return HoverEvent.Action.SHOW_ENTITY;
    }
}
