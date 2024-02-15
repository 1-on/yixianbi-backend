package com.yixian.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yixian.springbootinit.constant.JwtClaimsConstant;
import com.yixian.springbootinit.constant.MessageConstant;
import com.yixian.springbootinit.exception.BaseException;
import com.yixian.springbootinit.model.enums.UserRoleEnum;
import com.yixian.springbootinit.model.vo.LoginUserVO;
import com.yixian.springbootinit.properties.JwtProperties;
import com.yixian.springbootinit.service.UserService;
import com.yixian.springbootinit.model.entity.User;
import com.yixian.springbootinit.mapper.UserMapper;
import com.yixian.springbootinit.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author jiangfei
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2024-02-08 18:44:01
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 盐值，用以混淆密码
     */
    public static final String SALT = "yixian";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1.校验参数
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        // 账号过短
        if (userAccount.length() < 4) {
            throw new BaseException(MessageConstant.ACCOUNT_TOO_SHORT);
        }
        // 密码过短
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BaseException(MessageConstant.PASSWORD_TOO_SHORT);
        }
        // 两次密码不一致
        if (!userPassword.equals(checkPassword)) {
            throw new BaseException(MessageConstant.PASSWORD_INCONSISTENCY);
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        Long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BaseException(MessageConstant.ACCOUNT_DUPLICATION);
        }
        // 2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BaseException(MessageConstant.REGISTER_FAILED);
        }
        return user.getId();
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword) {
        // 1.校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        if (userAccount.length() < 4) {
            throw new BaseException(MessageConstant.ACCOUNT_ERROR);
        }
        if (userPassword.length() < 8) {
            throw new BaseException(MessageConstant.PASSWORD_ERROR);
        }
        // 2.处理异常情况 (账号不存在、密码不正确、账号被封禁)
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BaseException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        // 2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        if (!Objects.equals(user.getUserPassword(), encryptPassword)) {
            throw new BaseException(MessageConstant.PASSWORD_ERROR);
        }
        if (Objects.equals(user.getUserRole(), UserRoleEnum.BAN.getValue())) {
            throw new BaseException(MessageConstant.ACCOUNT_BAN);
        }
        // 3.生成JWT令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getSecretKey(), jwtProperties.getTtl(), claims);

        LoginUserVO loginUserVO = LoginUserVO.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .userAvatar(user.getUserAvatar())
                .userProfile(user.getUserProfile())
                .userRole(user.getUserRole())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .token(token)
                .build();

        return loginUserVO;

    }
}




