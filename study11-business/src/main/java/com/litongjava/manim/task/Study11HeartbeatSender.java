package com.litongjava.manim.task;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import com.jfinal.kit.Kv;
import com.litongjava.enhance.buffer.GlobalScheduler;
import com.litongjava.tio.boot.server.TioBootServer;
import com.litongjava.tio.core.ChannelContext;
import com.litongjava.tio.core.Tio;
import com.litongjava.tio.http.common.sse.SsePacket;
import com.litongjava.tio.utils.json.FastJson2Utils;
import com.litongjava.tio.utils.lock.SetWithLock;

public class Study11HeartbeatSender {

  // 15s 周期（可外部注入）
  private final long periodSeconds;
  private final byte[] heartbeatPayload; // 复用 JSON 序列化结果，减少分配

  public Study11HeartbeatSender() {
    this(15);
  }

  public Study11HeartbeatSender(long periodSeconds) {
    this.periodSeconds = periodSeconds;
    this.heartbeatPayload = FastJson2Utils.toJSONBytes(Kv.by("type", "keep alive"));
  }

  /** 启动周期任务 */
  public void start() {
    // 固定速率调度：尽量保证每 15s 一个心跳
    GlobalScheduler.scheduleAtFixedRate(this::tick, 0, periodSeconds, TimeUnit.SECONDS);
  }

  /** 每个周期发送一次心跳 */
  private void tick() {
    SetWithLock<ChannelContext> setWithLock = Tio.getAll(TioBootServer.me().getServerTioConfig());
    if (setWithLock == null)
      return;

    // 正确使用 SetWithLock 提供的锁来保护遍历
    Lock lock = setWithLock.getLock().readLock();
    lock.lock();
    try {
      Set<ChannelContext> channels = setWithLock.getObj();
      for (ChannelContext ctx : channels) {
        Object type = ctx.getAttribute("type");
        if ("SSE".equals(type)) {
          // 为每个发送构造包；payload 复用
          SsePacket packet = new SsePacket("heartbeat", heartbeatPayload);
          try {
            Tio.send(ctx, packet);
          } catch (Exception sendEx) {
            // 单个连接失败不要影响整体周期
            // 这里可换成你的日志框架
            System.err.println("Heartbeat send failed: " + sendEx.getMessage());
          }
        }
      }
    } finally {
      lock.unlock();
    }
  }
}