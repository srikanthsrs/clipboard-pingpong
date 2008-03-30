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
  //
  // *NOTE*: Never use clipboard.setContents() and clipboard.getContents()
  //         as we are experiencing some weird problem about which we do not
  //         have good knowledge.
  //
  // Look at Issue no: 2 for more details.
  //
  private static final int MAX_CLIPBOARD_RETRY = 10;
  private static final int SLEEP_TIME_IN_MILLIS = 20;

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
    Transferable contents = getContentsWrapperForClipboard();
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
    setContentsWrapperForClipboard(stringSelection);
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
    setContentsWrapperForClipboard(getContentsWrapperForClipboard());
  }

  private Transferable getContentsWrapperForClipboard() {
    IllegalStateException thrownException = null;
    for (int i = 0; i < MAX_CLIPBOARD_RETRY; i++) {
      try {
        Transferable contents = clipboard.getContents(this);
        return contents;
      } catch (IllegalStateException illegalStateException) {
        thrownException = illegalStateException;
        try {
          Thread.sleep(SLEEP_TIME_IN_MILLIS);
        } catch (InterruptedException interruptedException) {}
        continue;
      }
    }

    // If we reach this far, then it means we just couldn't get the contents.
    if (thrownException == null) {
      String error = "Could not get clipboard contents.";
      thrownException = new IllegalStateException(error);
    }

    throw thrownException;
  }

  private void setContentsWrapperForClipboard(Transferable contents) {
    IllegalStateException thrownException = null;
    for (int i = 0; i < MAX_CLIPBOARD_RETRY; i++) {
      try {
        clipboard.setContents(contents, this);
        return;
      } catch (IllegalStateException illegalStateException) {
        thrownException = illegalStateException;
        try {
          Thread.sleep(SLEEP_TIME_IN_MILLIS);
        } catch (InterruptedException interruptedException) {}
        continue;
      }
    }

    // If we reach this far, then it means we just couldn't set the contents.
    if (thrownException == null) {
      String error = "Could not set clipboard contents.";
      thrownException = new IllegalStateException(error);
    }

    throw thrownException;
  }
}
