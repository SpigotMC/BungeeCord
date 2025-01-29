package net.md_5.bungee.api.chat.hover.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.jetbrains.annotations.ApiStatus;

@Data
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class Entity extends Content
{

    /**
     * Required for backwards compatibility.
     *
     * @param type the type of the entity, for example 'minecraft:pig'
     * @param id for example '6cb1b229-ce5c-4179-af8d-eea185c25963'
     * @param name the name of the entity
     */
    public Entity(String type, @NonNull String id, BaseComponent name)
    {
        this( type, id, name, false );
    }

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

    /**
     * True if this entity is for 1.21.5 or later
     */
    @ApiStatus.Internal
    private boolean v1_21_5;

    @Override
    public HoverEvent.Action requiredAction()
    {
        return HoverEvent.Action.SHOW_ENTITY;
    }
}
