package ru.leymooo.cfg.configuration.serialization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applies to a {@link ru.leymooo.cfg.configuration.serialization.ConfigurationSerializable} that will delegate all
 * deserialization to another {@link ru.leymooo.cfg.configuration.serialization.ConfigurationSerializable}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DelegateDeserialization {
    /**
     * Which class should be used as a delegate for this classes
     * deserialization
     *
     * @return Delegate class
     */
    Class<? extends ConfigurationSerializable> value();
}
