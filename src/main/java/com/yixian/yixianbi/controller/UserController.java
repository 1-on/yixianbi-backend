package com.yixian.yixianbi.controller;

import com.yixian.yixianbi.common.Result;
import com.yixian.yixianbi.constant.MessageConstant;
import com.yixian.yixianbi.exception.BaseException;
import com.yixian.yixianbi.model.dto.user.UserLoginDTO;
import com.yixian.yixianbi.model.dto.user.UserRegisterDTO;
import com.yixian.yixianbi.model.entity.User;
import com.yixian.yixianbi.model.vo.LoginUserVO;
import com.yixian.yixianbi.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/ok")
    public String ok() {
        return "ok成功";
    }

    /**
     * 用户注册
     *
     * @param userRegisterDTO 账号、密码、确认密码
     * @return
     */
    @PostMapping("/register")
    public Result<Long> userRegister(@RequestBody UserRegisterDTO userRegisterDTO) {
        if (userRegisterDTO == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        String userAccount = userRegisterDTO.getUserAccount();
        String userPassword = userRegisterDTO.getUserPassword();
        String checkPassword = userRegisterDTO.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return Result.success(result);
    }


    @PostMapping("/login")
    public Result<LoginUserVO> userLogin(@RequestBody UserLoginDTO userLoginDTO) {
        if (userLoginDTO == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        String userAccount = userLoginDTO.getUserAccount();
        String userPassword = userLoginDTO.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword);
        return Result.success(loginUserVO);
    }

    @GetMapping("/get/login")
    public Result<LoginUserVO> getLoginUser() {
        User user = userService.getLoginUser();
        return Result.success(userService.getLoginUserVo(user));
    }


}
