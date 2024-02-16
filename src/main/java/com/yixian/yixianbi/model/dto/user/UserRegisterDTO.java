package com.yixian.yixianbi.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterDTO implements Serializable {

    private static final long serialVersionUID = 208720879779972292L;

    private String userAccount;

    private String userPassword;

    private String checkPassword;
}
