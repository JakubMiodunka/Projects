package pl.jakubmiodunka.gui.panels.interfaces;

/**
 * Interface related with external refreshing the content of panels.
 * All panels, that contains data imported from database should implement this interface.
 *
 * @author Jakub Miodunka
 */
public interface RefreshablePanel {
    /**
     * Should import currently displayed data from the repository and refresh the content displayed on the panel.
     */
    void refresh();
}
