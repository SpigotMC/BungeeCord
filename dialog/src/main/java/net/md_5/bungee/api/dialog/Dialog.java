package net.md_5.bungee.api.dialog;

import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a dialog GUI.
 */
public interface Dialog
{

    /**
     * Gets the dialog base which contains the dialog title and other options
     * common to all types of dialogs.
     *
     * @return mutable reference to the dialog base
     */
    DialogBase getBase();

    /**
     * Sets the dialog base.
     * <br>
     * For internal use only as this is mandatory and should be specified in the
     * constructor.
     *
     * @param base the new dialog base
     */
    @ApiStatus.Internal
    void setBase(DialogBase base);
}
