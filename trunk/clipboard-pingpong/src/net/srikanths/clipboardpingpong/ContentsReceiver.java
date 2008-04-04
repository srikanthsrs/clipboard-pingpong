package net.srikanths.clipboardpingpong;

public interface ContentsReceiver extends Runnable, ShutdownListener {
  public void setClipboardHelper(ClipboardHelper helper);

  public void fireSystemClipboardChange(String contents);
}
