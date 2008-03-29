package net.srikanths.util.clipboardpingpong;


public class ClipboardPingPong {
  public static void main(String[] args) throws Exception {
    PingPongConfiguration configuration = new PingPongConfiguration();
    configuration.loadProperties();

    ClipboardHelper clipboardHelper = new DefaultClipboardHelper();
    clipboardHelper.setContents(clipboardHelper.getContents());
    ContentsDistributor contentsDistributor = new SocketContentsDistributor(
        configuration.getReceiverAddresses());
    clipboardHelper.addContentsListener(contentsDistributor);
    ContentsReceiver contentsReceiver = new SocketContentsReceiver(
        configuration.getServerPort(), clipboardHelper);
    Thread thread = new Thread(contentsReceiver);
    thread.start();
  }
}
