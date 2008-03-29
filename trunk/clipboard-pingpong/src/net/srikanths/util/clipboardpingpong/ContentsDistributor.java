package net.srikanths.util.clipboardpingpong;

public interface ContentsDistributor extends ContentsListener {
  public void distribute(String contents);
}
