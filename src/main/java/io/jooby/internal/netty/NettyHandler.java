package io.jooby.internal.netty;

import io.jooby.Context;
import io.jooby.Err;
import io.jooby.Route;
import io.jooby.Router;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import static io.netty.handler.codec.http.HttpUtil.isKeepAlive;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.Executor;

@ChannelHandler.Sharable
public class NettyHandler extends ChannelInboundHandlerAdapter {
  private static final AttributeKey<String> PATH = AttributeKey.valueOf("path");
  private final DefaultEventExecutorGroup executor;
  private final Router router;

  public NettyHandler(DefaultEventExecutorGroup executor, Router router) {
    this.executor = executor;
    this.router = router;
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
    ctx.flush();
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws InterruptedException {
    if (msg instanceof HttpRequest) {
      HttpRequest req = (HttpRequest) msg;
      String path = Route.normalize(req.uri());
      ctx.channel().attr(PATH).set(path);
      Route.Chain chain = router.chain(req.method().name(), path);
      NettyContext context = new NettyContext(ctx, req, isKeepAlive(req), path);
      try {
        chain.next(context);
      } catch (Context.Dispatched dispatched) {
        dispatch(dispatched.executor, ctx, context, chain);
      }
    }
  }

  private void dispatch(Executor executor, ChannelHandlerContext ctx, Context context,
      Route.Chain chain) {
    Executor exec = executor == null ? this.executor : executor;
    exec.execute(() -> chain.next(context));
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    String path = ctx.channel().attr(PATH).get();
    ctx.close();
    LoggerFactory.getLogger(Err.class)
        .error("execution of {} resulted in unexpected exception", path, cause);
  }
}
