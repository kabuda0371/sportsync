package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.UserContext;
import com.example.demo.exception.BusinessException;
import com.example.demo.converter.UserConverter;
import com.example.demo.utils.JwtUtil;
import com.example.demo.dto.UserLoginDTO;
import com.example.demo.dto.UserRegisterDTO;
import com.example.demo.entity.User;
import com.example.demo.enums.AccountStatusEnum;
import com.example.demo.enums.AuthProviderEnum;
import com.example.demo.enums.UserRoleEnum;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.UserService;
import com.example.demo.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.security.LoginRateLimiter;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserConverter userConverter;
    private final LoginRateLimiter loginRateLimiter;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(UserRegisterDTO registerDTO) {
        log.info("用户开始注册，邮箱: {}", registerDTO.getEmail());
        
        // 高并发防范：直接去除事前 lambdaQuery().exists() 检查
        // 彻底消灭 Check-Then-Act 的竞态条件漏洞
        // 依托数据库层面的 email UNIQUE 唯一索引进行硬拦截在 try catch 中处理

        // 2. 密码加密 (使用 jbcrypt)
        String encryptedPassword = BCrypt.hashpw(registerDTO.getPassword(), BCrypt.gensalt());

        // 3. 构建用户实体并保存 (使用 Builder 模式)
        User user = User.builder()
                .email(registerDTO.getEmail())
                .passwordHash(encryptedPassword) // 密码密文存入 passwordHash 字段
                .name(registerDTO.getName())
                .dateOfBirth(registerDTO.getDateOfBirth())
                .address(registerDTO.getAddress())
                // constraint users_chk_1: role in ('member', 'staff', 'admin')
                .role(UserRoleEnum.MEMBER.getValue()) // 默认分配普通会员角色
                // constraint users_chk_2: account_status in ('pending', 'approved', 'suspended')
                .accountStatus(AccountStatusEnum.APPROVED.getValue()) // 默认账号状态为已批准
                // constraint users_chk_3: auth_provider in ('local', 'google', 'facebook')
                .authProvider(AuthProviderEnum.LOCAL.getValue()) // 默认本地注册
                .build();
        
        try {
            // 使用 MyBatis-Plus 的 save 方法保存到数据库
            this.save(user);
        } catch (DuplicateKeyException e) {
            log.error("高并发注册冲突，邮箱已被注册: {}", registerDTO.getEmail(), e);
            throw new BusinessException("该邮箱已被注册！");
        }

        log.info("用户注册成功，ID: {}", user.getId());
        UserVO userVO = userConverter.toVO(user);
        userVO.setToken(jwtUtil.generateToken(user.getId()));
        return userVO;
    }

    @Override
    public UserVO login(UserLoginDTO loginDTO) {
        String email = loginDTO.getEmail();
        log.info("用户尝试登录，邮箱: {}", email);

        // 0. 防爆破前置拦截：检查账号是否仍在封锁期内 (委派给专门的组件)
        loginRateLimiter.checkLockout(email);

        // 1. 根据邮箱查询用户（使用 last("LIMIT 1") 兜底，防止脏数据导致报 500 错误）
        User user = this.lambdaQuery()
                .eq(User::getEmail, email)
                .last("LIMIT 1")
                .one();

        // 2. 校验用户是否存在
        if (user == null) {
            // 防止计时攻击 (Timing Attack)：即使账号不存在，也进行一次耗时的哈希计算，抹平响应时间差异
            BCrypt.hashpw(loginDTO.getPassword(), BCrypt.gensalt());
            loginRateLimiter.recordFailedAttempt(email); // 不要放过爆破不存在邮箱的恶意流量
            throw new BusinessException("邮箱或密码错误！"); // 模糊提示
        }

        // 3. 校验账号状态 (明确区分各类异常状态)
        String accountStatus = user.getAccountStatus();
        if (AccountStatusEnum.SUSPENDED.getValue().equals(accountStatus)) {
            log.warn("登录拦截，账号已被封禁: {}", email);
            throw new BusinessException("您的账号已被封禁，如有疑问请联系客服！");
        }
        if (AccountStatusEnum.PENDING.getValue().equals(accountStatus)) {
            log.warn("登录拦截，账号审核中: {}", email);
            throw new BusinessException("您的账号还在审核中，请耐心等待！"); 
        }
        if (!AccountStatusEnum.APPROVED.getValue().equals(accountStatus)) {
            log.warn("登录失败，未知的账号状态异常: {}，当前状态: {}", email, accountStatus);
            throw new BusinessException("账号状态异常，无法登录！"); 
        }

        // 4. 断开与数据库的关联依赖（当前类 login() 没有 @Transactional 注解，由于 MyBatis 的 session 机制，在 lambdaQuery 执行完后，底层 Connection 会被及时释放回连接池，只有在有 @Transactional 长事务包裹时，连接才会被一直挂起）
        // 这里进行耗时的 CPU 密集型计算（Bcrypt）是安全的，不会阻塞连接池
        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPasswordHash())) {
            loginRateLimiter.recordFailedAttempt(email);
            throw new BusinessException("邮箱或密码错误！");
        }

        log.info("用户登录成功，ID: {}, 邮箱: {}", user.getId(), email);
        
        // 5. 密码校验成功，证明是合法身份，清空之前的错误记录
        loginRateLimiter.clearLock(email);

        // 6. 返回查找到的用户信息并生成 token
        UserVO userVO = userConverter.toVO(user);
        userVO.setToken(jwtUtil.generateToken(user.getId()));
        return userVO;
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在！");
        }
        return userConverter.toVO(user);
    }
}
