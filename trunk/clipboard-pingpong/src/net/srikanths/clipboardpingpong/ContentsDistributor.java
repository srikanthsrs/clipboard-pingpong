package net.srikanths.clipboardpingpong;

public interface ContentsDistributor extends ContentsListener {
  public void distribute(String contents);
}
