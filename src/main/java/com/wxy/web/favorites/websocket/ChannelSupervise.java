package com.wxy.web.favorites.websocket;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.CacheObj;
import cn.hutool.cache.impl.TimedCache;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/***
 * <p>
 * Description: 描述
 * </p>
 * @author wangxiaoyuan
 * 2021年10月21日
 */
public class ChannelSupervise {
    private static final ChannelGroup GLOBAL_GROUP;
    private static final TimedCache<String, Channel> CHANNEL_CACHE;

    static {
        GLOBAL_GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        CHANNEL_CACHE = CacheUtil.newTimedCache(TimeUnit.MINUTES.toMillis(5));
        CHANNEL_CACHE.setListener((k, v) -> {
            if (v != null) {
                GLOBAL_GROUP.remove(v);
            }
        });
    }

    public static void addChannel(Channel channel, String sid) {
        GLOBAL_GROUP.add(channel);
        CHANNEL_CACHE.put(sid, channel);
    }

    public static void removeChannel(Channel channel) {
        Iterator<CacheObj<String, Channel>> iterator = CHANNEL_CACHE.cacheObjIterator();
        while (iterator.hasNext()) {
            CacheObj<String, Channel> next = iterator.next();
            if (Objects.equals(next.getValue(), channel)) {
                CHANNEL_CACHE.remove(next.getKey());
                break;
            }
        }
    }

    public static Channel findChannel(String sid) {
        return CHANNEL_CACHE.get(sid, false);
    }

    public static void send2All(TextWebSocketFrame tws) {
        GLOBAL_GROUP.writeAndFlush(tws);
    }
}
