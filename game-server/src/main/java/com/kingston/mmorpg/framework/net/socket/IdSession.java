package com.kingston.mmorpg.framework.net.socket;

import com.kingston.mmorpg.framework.net.command.IDispatch;
import com.kingston.mmorpg.framework.net.socket.message.Message;
import com.kingston.mmorpg.framework.net.socket.netty.NettySession;
import com.kingston.mmorpg.game.database.user.entity.PlayerEnt;
import io.netty.channel.Channel;

/**
 * 玩家登录session，不与任何nio框架绑定
 *
 * @author kingston
 * @see NettySession
 * @see Channel
 */
public interface IdSession {


    void sendPacket(Message packet);

    long getOwnerId();

    /**
     * 更新属性值
     *
     * @param key
     * @param value
     * @return
     */
    Object setAttribute(String key, Object value);

    /**
     * 获取属性值
     *
     * @param key
     * @return
     */
    Object getAttribute(String key);

    IDispatch getDispatcher();

    void bindDispatcher(IDispatch dispatcher);

    PlayerEnt getPlayer();

    void setPlayer(PlayerEnt player);

}