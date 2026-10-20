package com.mysite.sbb.nettychat;

import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChatServerHandler extends SimpleChannelInboundHandler<String> {

    // 연결된 모든 클라이언트를 관리하는 ChannelGroup
    private static final ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        Channel incoming = ctx.channel();
        clients.add(incoming); // 클라이언트 채널 추가
        log.info("✅ 클라이언트 연결됨: {}", incoming.remoteAddress());

        // 클라이언트에게 입장 메시지 전송
        broadcastMessage("📢 [알림] " + incoming.remoteAddress() + " 님이 입장했습니다.");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        // 메시지가 수신될 때 호출됨
        Channel incoming = ctx.channel();
        log.info("📩 받은 메시지: [{}]: {}", incoming.remoteAddress(), msg);

        // 받은 메시지를 모든 클라이언트에게 전송
        broadcastMessage("[사용자 " + incoming.remoteAddress() + "]: " + msg);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        Channel leaving = ctx.channel();
        clients.remove(leaving); // 클라이언트 목록에서 제거
        log.info("❌ 클라이언트 연결 종료: {}", leaving.remoteAddress());

        // 클라이언트에게 퇴장 메시지 전송
        broadcastMessage("📢 [알림] " + leaving.remoteAddress() + " 님이 퇴장했습니다.");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("⚠️ 예외 발생: {}", cause.getMessage());
        ctx.close();
    }

    // 모든 클라이언트에게 메시지를 브로드캐스트
    private void broadcastMessage(String message) {
        for (Channel ch : clients) {
            ch.writeAndFlush(message + "\n"); // 각 클라이언트에게 메시지 전송
        }
    }
}
