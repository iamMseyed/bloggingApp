package com.myBlog.payload;

import lombok.Data;

@Data
public class CommentDTO {
    private long id;
    private String text;
    private String email;
}
