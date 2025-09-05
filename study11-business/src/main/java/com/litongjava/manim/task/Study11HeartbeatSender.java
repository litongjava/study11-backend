package com.litongjava.manim.task;

import java.util.Set;

import com.jfinal.kit.Kv;
import com.litongjava.tio.boot.server.TioBootServer;
import com.litongjava.tio.core.ChannelContext;
import com.litongjava.tio.core.Tio;
import com.litongjava.tio.http.common.sse.SsePacket;
import com.litongjava.tio.utils.json.FastJson2Utils;
import com.litongjava.tio.utils.lock.SetWithLock;

public class Study11HeartbeatSender implements Runnable {
  private final String message = "keep alive";

  @Override
  public void run() {
    while (true) {
      // Get the set of all currently connected channels
      SetWithLock<ChannelContext> setWithLock = Tio.getAll(TioBootServer.me().getServerTioConfig());
      Set<ChannelContext> connectedChannels = setWithLock.getObj();

      // Iterate through each connected channel
      for (ChannelContext channelContext : connectedChannels) {
        // Check if the connection is of SSE type
        Object type = channelContext.getAttribute("type");
        if (type != null && "SSE".equals(type)) {
          // Prepare the heartbeat data as a JSON byte array
          byte[] jsonBytes = FastJson2Utils.toJSONBytes(Kv.by("type", message));

          // Create an SSE packet with a defined event type (e.g., "main")
          SsePacket ssePacket = new SsePacket("heartbeat", jsonBytes);

          // Send the heartbeat packet to the client using the channel context
          Tio.send(channelContext, ssePacket);
        }
      }

      // Pause for 3 seconds between heartbeat cycles
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        // Restore the interrupted status and break out of the loop if interrupted
        Thread.currentThread().interrupt();
        break;
      }
    }
  }
}
