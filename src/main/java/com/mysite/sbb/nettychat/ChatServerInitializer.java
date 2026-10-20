package com.mysite.sbb.nettychat;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class ChatServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) {
        // 2바이트 길이를 읽고 메시지 본문을 처리하는 디코더
        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 0, 2, 0, 2));  // 길이 정보는 첫 2바이트
        ch.pipeline().addLast(new StringDecoder()); // 문자열 디코더
        ch.pipeline().addLast(new StringEncoder()); // 문자열 인코더
        ch.pipeline().addLast(new ChatServerHandler()); // 클라이언트 처리 핸들러
    }
}