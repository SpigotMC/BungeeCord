package net.md_5.bungee.event;

import java.lang.reflect.Method;
import java.util.Arrays;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PrioritySortingHandler
{

    /**
     * Sorts the Methods based on the given EventHandler Annotation
     *
     * @param methods listener
     * @return sorted listener
     */
    public static EventMethod[] sort(Method[] methods)
    {
        return sort( convert( methods ) );
    }

    public static EventMethod[] sort(EventMethod[] methods)
    {
        Arrays.sort( methods );
        return methods;
    }

    /**
     * Converts the EventMethods to Methods again
     *
     * @param methods
     * @return
     */
    @Deprecated
    private static Method[] convert(EventMethod[] methods)
    {
        Method[] tmp = new Method[ methods.length ];
        for ( int i = 0; i < methods.length; i++ )
        {
            tmp[i] = methods[i].getMethod();
        }
        return tmp;
    }

    /**
     * Converts Methods to EventMethods for sorting
     *
     * @param methods
     * @return
     */
    private static EventMethod[] convert(Method[] methods)
    {
        EventMethod[] tmp = new EventMethod[ methods.length ];
        for ( int i = 0; i < methods.length; i++ )
        {
            tmp[i] = new EventMethod( methods[i] );
        }
        return tmp;
    }
}