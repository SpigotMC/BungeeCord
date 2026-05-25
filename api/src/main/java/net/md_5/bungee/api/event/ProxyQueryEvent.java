package net.md_5.bungee.api.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.QueryResponse;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.plugin.Event;

/**
 * Called when a query is received
 */
@Data
@AllArgsConstructor
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class ProxyQueryEvent extends Event
{

    /**
     * If the query is extended or not
     */
    private final boolean extendedQuery;

    /**
     * The listener from the query is call
     */
    private final ListenerInfo listener;

    /**
     * The query response
     */
    private QueryResponse response;

}
