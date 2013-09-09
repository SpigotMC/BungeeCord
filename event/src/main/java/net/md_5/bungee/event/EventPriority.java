package net.md_5.bungee.event;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Importance of the {@link EventHandler}. When executing an Event, the handlers
 * are called in order of their Priority.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventPriority
{

    public static final byte LOWEST = -64;
    public static final byte LOW = -32;
    public static final byte NORMAL = 0;
    public static final byte HIGH = 32;
    public static final byte HIGHEST = 64;
}
