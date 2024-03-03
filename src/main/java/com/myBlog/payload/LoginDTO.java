package com.myBlog.payload;

import lombok.Data;

@Data
public class LoginDTO {
    private String usernameOrEmail; //username or email
    private String password;
}