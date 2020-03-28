package ru.leymooo.botfilter.discard;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorStream
{

    public void error(String message)
    {
        System.err.print( message );
    }
}
