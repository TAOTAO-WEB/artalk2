package cn.edu.hdu.artalk2.eventSource;

import java.util.List;

import cn.edu.hdu.artalk2.dto.Message;

/**
 * 消息更新监接口
 */
public interface MessageListUpdateListener {
    void onUpdate(List<Message> list);
}
