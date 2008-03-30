package net.srikanths.clipboardpingpong;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

public class DefaultClipboardHelper implements ClipboardHelper {
  private Clipboard clipboard;
  private List contentsListeners;

  public DefaultClipboardHelper() {
    clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    contentsListeners = new Vector();
    takeClipboardOwnership();
  }

  public void addContentsListener(ContentsListener listener) {
    contentsListeners.add(listener);
  }

  public void removeContentsListener(ContentsListener listener) {
    contentsListeners.remove(listener);
  }

  public String getContents() throws UnsupportedFlavorException {
    Transferable contents = clipboard.getContents(this);
    if (contents == null) {
      return null;
    }

    try {
      return (String) contents.getTransferData(DataFlavor.stringFlavor);
    } catch (IOException e) {
      return null;
    }
  }

  public void setContents(String contents) {
    if (null == contents) {
      contents = "";
    }

    StringSelection stringSelection = new StringSelection(contents);
    clipboard.setContents(stringSelection, this);
  }

  public void lostOwnership(Clipboard clipboard, Transferable contents) {
    // Change the owner to this clipboard helper so that we get notified
    // of the future clipboard copies. It's a hack, this should be replaced
    // when a better solution is found.
    takeClipboardOwnership();

    String newContents;

    try {
      newContents = getContents();
    } catch (UnsupportedFlavorException e) {
      // Contents is not String, no business anymore, lets get out.
      return;
    }

    ContentsListener[] listeners;

    synchronized (contentsListeners) {
      listeners = new ContentsListener[contentsListeners.size()];
      listeners = (ContentsListener[]) contentsListeners.toArray(listeners);
    }

    for (int i = 0; i < listeners.length; i++) {
      ContentsListener listener = listeners[i];
      listener.newContentsReceived(newContents);
    }
  }

  private void takeClipboardOwnership() {
    clipboard.setContents(clipboard.getContents(this), this);
  }
}
