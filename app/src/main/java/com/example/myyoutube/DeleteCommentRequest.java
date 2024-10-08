package com.example.myyoutube;

public class DeleteCommentRequest {
    private int commentId;

    public DeleteCommentRequest(int commentId) {
        this.commentId = commentId;
    }

    public int getCommentId() {
        return commentId;
    }
}

