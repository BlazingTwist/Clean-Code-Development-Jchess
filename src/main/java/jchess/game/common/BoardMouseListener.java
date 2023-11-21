package jchess.game.common;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BoardMouseListener implements MouseListener {
    private final List<Consumer<MouseEvent>> clickListeners = new ArrayList<>();

    public void addClickListener(Consumer<MouseEvent> listener) {
        clickListeners.add(listener);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // click detection in swing seems spotty at best, so using 'mouseReleased' instead
        for (Consumer<MouseEvent> clickListener : clickListeners) {
            clickListener.accept(e);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
