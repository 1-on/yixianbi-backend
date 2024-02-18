package com.yixian.yixianbi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yixian.yixianbi.constant.JwtClaimsConstant;
import com.yixian.yixianbi.constant.MessageConstant;
import com.yixian.yixianbi.context.BaseContext;
import com.yixian.yixianbi.exception.BaseException;
import com.yixian.yixianbi.mapper.UserMapper;
import com.yixian.yixianbi.model.entity.User;
import com.yixian.yixianbi.model.enums.UserRoleEnum;
import com.yixian.yixianbi.model.vo.LoginUserVO;
import com.yixian.yixianbi.properties.JwtProperties;
import com.yixian.yixianbi.service.UserService;
import com.yixian.yixianbi.utils.JwtUtil;
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
//        // 1.校验参数
//        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
//            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
//        }
//        // 账号过短
//        if (userAccount.length() < 4) {
//            throw new BaseException(MessageConstant.ACCOUNT_TOO_SHORT);
//        }
//        // 密码过短
//        if (userPassword.length() < 8 || checkPassword.length() < 8) {
//            throw new BaseException(MessageConstant.PASSWORD_TOO_SHORT);
//        }
//        // 两次密码不一致
//        if (!userPassword.equals(checkPassword)) {
//            throw new BaseException(MessageConstant.PASSWORD_INCONSISTENCY);
//        }
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

        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        loginUserVO.setToken(token);

        return loginUserVO;
    }

    @Override
    public User getLoginUser() {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(MessageConstant.NOT_LOGIN);
        }
        User currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BaseException(MessageConstant.NOT_LOGIN);
        }
        return currentUser;
    }

    @Override
    public LoginUserVO getLoginUserVo(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public boolean isAdmin() {
        Long userId = BaseContext.getCurrentId();
        User user = this.getById(userId);
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }
}




