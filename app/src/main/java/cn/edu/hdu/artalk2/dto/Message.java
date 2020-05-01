package cn.edu.hdu.artalk2.dto;

public class Message {

    private String msId;
    private int type;
    private int userId;
    private int likeCount;
    private int commentCount;

    public Message(String msId, int type, int userId, int likeCount, int commentCount) {
        this.msId = msId;
        this.type = type;
        this.userId = userId;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
    }

    public Message() {
    }

    public String getMsId() {
        return msId;
    }

    public void setMsId(String msId) {
        this.msId = msId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }
}
