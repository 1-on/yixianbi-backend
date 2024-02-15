package com.yixian.springbootinit.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginDTO implements Serializable {


    private static final long serialVersionUID = -6950229156987513979L;

    private String userAccount;

    private String userPassword;
}
