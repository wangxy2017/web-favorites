package com.wxy.web.favorites.websocket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/***
 * <p>
 * Description: 描述
 * </p>
 * @author wangxiaoyuan
 * 2021年10月21日
 */
public class ChannelSupervise {
    private static final ChannelGroup GlobalGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final ConcurrentMap<String, ChannelId> ChannelMap = new ConcurrentHashMap<>();
    private static final ConcurrentMap<ChannelId, String> SidMap = new ConcurrentHashMap<>();

    public static void addChannel(Channel channel, String sid) {
        GlobalGroup.add(channel);
        ChannelMap.put(sid, channel.id());
        SidMap.put(channel.id(), sid);
    }

    public static void removeChannel(Channel channel) {
        GlobalGroup.remove(channel);
        ChannelMap.remove(SidMap.get(channel.id()));
        SidMap.remove(channel.id());
    }

    public static Channel findChannel(String sid) {
        ChannelId channelId = ChannelMap.get(sid);
        if (channelId == null) {
            return null;
        }
        return GlobalGroup.find(channelId);
    }

    public static void send2All(TextWebSocketFrame tws) {
        GlobalGroup.writeAndFlush(tws);
    }
}
