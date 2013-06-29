package net.md_5.bungee.api.tab;

/**
 * Represents a custom tab list, which may have slots manipulated.
 */
public interface CustomTabList extends TabListHandler
{

    /**
     * Blank out this tab list and update immediately.
     */
    void clear();

    /**
     * Gets the columns in this list.
     *
     * @return the width of this list
     */
    int getColumns();

    /**
     * Gets the rows in this list.
     *
     * @return the height of this list
     */
    int getRows();

    /**
     * Get the total size of this list.
     *
     * @return {@link #getRows()} * {@link #getColumns()}
     */
    int getSize();

    /**
     * Set the text in the specified slot and update immediately.
     *
     * @param row the row to set
     * @param column the column to set
     * @param text the text to set
     * @return the padded text
     */
    String setSlot(int row, int column, String text);

    /**
     * Set the text in the specified slot.
     *
     * @param row the row to set
     * @param column the column to set
     * @param text the text to set
     * @param update whether or not to invoke {@link #update()} upon completion
     * @return the padded text
     */
    String setSlot(int row, int column, String text, boolean update);

    /**
     * Flush all queued changes to the user.
     */
    void update();
}
