package com.yixian.springbootinit.service;

import com.yixian.springbootinit.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yixian.springbootinit.model.vo.LoginUserVO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author jiangfei
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2024-02-08 18:44:01
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param checkPassword 确认密码
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);


    /**
     * 用户登录
     *
     * @param userAccount  账号
     * @param userPassword 密码
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword);

}
