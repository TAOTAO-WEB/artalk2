package cn.edu.hdu.artalk2.eventSource;

import java.util.List;

import cn.edu.hdu.artalk2.dto.Message;

/**
 * 消息；监听事件的事件源
 */
public class Messages {

    // 消息列表
    private List<Message> messageList;
    // 监听器（暂时只能有一个）
    private MessageListUpdateListener listUpdateListener;

    public void setListUpdateListener(MessageListUpdateListener listener){
        this.listUpdateListener = listener;
    }

    public List<Message> getMessageList() {
        return messageList;
    }

    /**
     * 设置新的messageList并且触发监听器回调函数
     */
    public void setMessageList(List<Message> messageList) {
        this.messageList = messageList;
        listUpdateListener.onUpdate(messageList);
    }
}
