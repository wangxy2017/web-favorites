package com.wxy.web.favorites.websocket;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.concurrent.TimeUnit;

/***
 * <p>
 * Description: 描述
 * </p>
 * @author wangxiaoyuan
 * 2021年10月21日
 */
public class ChannelSupervise {
    private static final ChannelGroup GLOBAL_GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final Cache<String, ChannelId> CHANNEL_MAP = CacheBuilder.newBuilder()
            .maximumSize(1000) // 设置缓存的最大容量
            .expireAfterWrite(5, TimeUnit.MINUTES) // 设置缓存在写入五分钟后失效
            .concurrencyLevel(10) // 设置并发级别为10
            .recordStats() // 开启缓存统计
            .build();

    public static void addChannel(Channel channel, String sid) {
        GLOBAL_GROUP.add(channel);
        CHANNEL_MAP.put(sid, channel.id());
    }

    public static void removeChannel(Channel channel) {
        GLOBAL_GROUP.remove(channel);
    }

    public static Channel findChannel(String sid) {
        ChannelId channelId = CHANNEL_MAP.getIfPresent(sid);
        return channelId == null ? null : GLOBAL_GROUP.find(channelId);
    }

    public static void send2All(TextWebSocketFrame tws) {
        GLOBAL_GROUP.writeAndFlush(tws);
    }
}
