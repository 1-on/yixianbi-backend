package com.yixian.springbootinit.controller;

import com.yixian.springbootinit.common.Result;
import com.yixian.springbootinit.constant.MessageConstant;
import com.yixian.springbootinit.exception.BaseException;
import com.yixian.springbootinit.model.dto.user.UserLoginDTO;
import com.yixian.springbootinit.model.dto.user.UserRegisterDTO;
import com.yixian.springbootinit.model.vo.LoginUserVO;
import com.yixian.springbootinit.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
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
        return "ok";
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


}
