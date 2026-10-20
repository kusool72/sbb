package com.mysite.sbb.nettychat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChatServer {
    private final int port = 8081; // Netty는 8081 포트 사용 (Spring Boot와 충돌 방지)
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    @PostConstruct
    public void start() {
        new Thread(() -> {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();

            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChatServerInitializer());

                ChannelFuture future = bootstrap.bind(port).sync();
                serverChannel = future.channel(); // 채널 저장

                log.info("✅ Netty Chat Server started on port: {}", port);

                // `sync()` 제거 → Netty 서버가 비동기적으로 실행됨
            } catch (InterruptedException e) {
                log.error("❌ Netty Chat Server failed", e);
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    @PreDestroy
    public void stop() {
        try {
            if (serverChannel != null) {
                serverChannel.close().sync(); // 서버 채널 닫기
            }
            bossGroup.shutdownGracefully().sync(); // 이벤트 루프 종료
            workerGroup.shutdownGracefully().sync();
            log.info("❌ Netty Chat Server stopped");
        } catch (InterruptedException e) {
            log.error("❌ Netty Server shutdown interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
}
