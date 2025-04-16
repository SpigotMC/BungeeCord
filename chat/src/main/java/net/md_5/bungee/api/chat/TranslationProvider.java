package net.md_5.bungee.api.chat;

/**
 * An object capable of being translated by the client in a
 * {@link TranslatableComponent}.
 */
public interface TranslationProvider
{

    /**
     * Get the translation key.
     *
     * @return the translation key
     */
    String getTranslationKey();

    /**
     * Get this translatable object as a {@link TranslatableComponent}.
     *
     * @return the translatable component
     */
    default TranslatableComponent asTranslatableComponent()
    {
        return asTranslatableComponent( (Object[]) null );
    }

    /**
     * Get this translatable object as a {@link TranslatableComponent}.
     *
     * @param with the {@link String Strings} and
     * {@link BaseComponent BaseComponents} to use in the translation
     * @return the translatable component
     */
    default TranslatableComponent asTranslatableComponent(Object... with)
    {
        return new TranslatableComponent( this, with );
    }
}
