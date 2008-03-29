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
  }

  public void addContentsListener(ContentsListener listener) {
    contentsListeners.add(listener);
  }

  public void removeContentsListener(ContentsListener listener) {
    contentsListeners.remove(listener);
  }

  public String getContents() {
    Transferable contents = clipboard.getContents(this);
    if (contents == null) {
      return "";
    }

    try {
      return (String) contents.getTransferData(DataFlavor.stringFlavor);
    } catch (UnsupportedFlavorException e) {
      return "";
    } catch (IOException e) {
      return "";
    }
  }

  public void setContents(String contents) {
    StringSelection stringSelection = new StringSelection(contents);
    clipboard.setContents(stringSelection, this);
  }

  public void lostOwnership(Clipboard clipboard, Transferable contents) {
    ContentsListener[] listeners;

    synchronized (contentsListeners) {
      listeners = new ContentsListener[contentsListeners.size()];
      listeners = (ContentsListener[]) contentsListeners.toArray(listeners);
    }

    for (int i = 0; i < listeners.length; i++) {
      ContentsListener listener = listeners[i];
      listener.newContentsReceived(getContents());
    }
  }
}
