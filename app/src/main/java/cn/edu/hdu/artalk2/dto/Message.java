package cn.edu.hdu.artalk2.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {

    private String msId;
    private String userId;
    private int likeCount;
    private int DislikeCount;
    private int commentCount;
    private int mstype;
    private String time;
    private String avatar;

    public Message() {
    }

    public Message(String msId, String userId, int likeCount, int dislikeCount, int commentCount, int mstype, String time, String avatar) {
        this.msId = msId;
        this.userId = userId;
        this.likeCount = likeCount;
        this.DislikeCount = dislikeCount;
        this.commentCount = commentCount;
        this.mstype = mstype;
        this.time = time;
        this.avatar = avatar;
    }

    public String getMsId() {
        return msId;
    }

    public void setMsId(String msId) {
        this.msId = msId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getDislikeCount() {
        return DislikeCount;
    }

    public void setDislikeCount(int dislikeCount) {
        this.DislikeCount = dislikeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getMstype() {
        return mstype;
    }

    public void setMstype(int mstype) {
        this.mstype = mstype;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
