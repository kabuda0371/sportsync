package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.UserContext;
import com.example.demo.exception.BusinessException;
import com.example.demo.converter.UserConverter;
import com.example.demo.utils.JwtUtil;
import com.example.demo.dto.GoogleLoginDTO;
import com.example.demo.dto.ProfileUpdateDTO;
import com.example.demo.dto.UserLoginDTO;
import com.example.demo.dto.UserRegisterDTO;
import com.example.demo.entity.User;
import com.example.demo.enums.AccountStatusEnum;
import com.example.demo.enums.AuthProviderEnum;
import com.example.demo.enums.UserRoleEnum;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.UserService;
import com.example.demo.vo.UserVO;
import com.example.demo.vo.UserVO;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.example.demo.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.security.LoginRateLimiter;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserConverter userConverter;
    private final LoginRateLimiter loginRateLimiter;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${google.client-id}")
    private String googleClientId;

    @Value("${verify.code.expire-minutes:5}")
    private long codeExpireMinutes;

    @Value("${verify.code.resend-cooldown-seconds:60}")
    private long resendCooldownSeconds;

    private static final String VERIFY_CODE_KEY_PREFIX = "verify:code:";
    private static final String VERIFY_COOLDOWN_KEY_PREFIX = "verify:cooldown:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(UserRegisterDTO registerDTO) {
        log.info("用户开始注册，邮箱: {}", registerDTO.getEmail());
        
        // 高并发防范：直接去除事前 lambdaQuery().exists() 检查
        // 彻底消灭 Check-Then-Act 的竞态条件漏洞
        // 依托数据库层面的 email UNIQUE 唯一索引进行硬拦截在 try catch 中处理

        // 2. 密码加密 (使用 Spring Security的 PasswordEncoder)
        String encryptedPassword = passwordEncoder.encode(registerDTO.getPassword());

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
                .accountStatus(AccountStatusEnum.PENDING.getValue()) // 需要邮箱验证后才能变为 approved
                // constraint users_chk_3: auth_provider in ('local', 'google', 'facebook')
                .authProvider(AuthProviderEnum.LOCAL.getValue()) // 默认本地注册
                .build();
        
        // 检查邮箱是否已存在
        User existingUser = this.lambdaQuery()
                .eq(User::getEmail, registerDTO.getEmail())
                .last("LIMIT 1")
                .one();

        if (existingUser != null) {
            if (AccountStatusEnum.PENDING.getValue().equals(existingUser.getAccountStatus())) {
                // 上次注册未完成邮箱验证，覆盖旧信息允许继续注册
                log.info("发现未验证的旧注册记录，覆盖信息重新发送验证码，邮箱: {}", registerDTO.getEmail());
                existingUser.setPasswordHash(encryptedPassword);
                existingUser.setName(registerDTO.getName());
                existingUser.setDateOfBirth(registerDTO.getDateOfBirth());
                existingUser.setAddress(registerDTO.getAddress());
                this.updateById(existingUser);
                user = existingUser;
            } else {
                throw new BusinessException("Email is already registered!");
            }
        } else {
            try {
                this.save(user);
            } catch (DuplicateKeyException e) {
                log.error("高并发注册冲突，邮箱已被注册: {}", registerDTO.getEmail(), e);
                throw new BusinessException("Email is already registered!");
            }
        }

        log.info("用户注册成功，ID: {}", user.getId());

        // 生成6位数字验证码并存入 Redis
        String verifyCode = generateVerificationCode();
        String redisKey = VERIFY_CODE_KEY_PREFIX + registerDTO.getEmail();
        stringRedisTemplate.opsForValue().set(redisKey, verifyCode, codeExpireMinutes, TimeUnit.MINUTES);

        // 发送验证码邮件
        emailService.sendVerificationCode(registerDTO.getEmail(), verifyCode);
        log.info("注册验证码已发送，邮箱: {}", registerDTO.getEmail());

        UserVO userVO = userConverter.toVO(user);
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
            passwordEncoder.encode(loginDTO.getPassword());
            loginRateLimiter.recordFailedAttempt(email); // 不要放过爆破不存在邮箱的恶意流量
            throw new BusinessException("Invalid email or password!"); // 模糊提示
        }

        // 3. 校验账号状态 (明确区分各类异常状态)
        String accountStatus = user.getAccountStatus();
        if (AccountStatusEnum.SUSPENDED.getValue().equals(accountStatus)) {
            log.warn("登录拦截，账号已被封禁: {}", email);
            throw new BusinessException("Your account has been banned. Please contact customer service!");
        }
        if (AccountStatusEnum.PENDING.getValue().equals(accountStatus)) {
            log.warn("登录拦截，邮箱未验证: {}", email);
            throw new BusinessException("Your email has not been verified yet. Please check your email for the verification code!");
        }
        if (!AccountStatusEnum.APPROVED.getValue().equals(accountStatus)) {
            log.warn("登录失败，未知的账号状态异常: {}，当前状态: {}", email, accountStatus);
            throw new BusinessException("Abnormal account status, unable to login!"); 
        }

        // 4. 断开与数据库的关联依赖（当前类 login() 没有 @Transactional 注解，由于 MyBatis 的 session 机制，在 lambdaQuery 执行完后，底层 Connection 会被及时释放回连接池，只有在有 @Transactional 长事务包裹时，连接才会被一直挂起）
        // 这里进行耗时的 CPU 密集型计算（Bcrypt）是安全的，不会阻塞连接池
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPasswordHash())) {
            loginRateLimiter.recordFailedAttempt(email);
            throw new BusinessException("Invalid email or password!");
        }

        log.info("用户登录成功，ID: {}, 邮箱: {}", user.getId(), email);
        
        // 5. 密码校验成功，证明是合法身份，清空之前的错误记录
        loginRateLimiter.clearLock(email);

        // 6. 返回查找到的用户信息并生成 token
        UserVO userVO = userConverter.toVO(user);
        userVO.setToken(jwtUtil.generateToken(user.getId(), user.getRole()));
        return userVO;
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("User not found!");
        }
        return userConverter.toVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deactivateAccount(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "User not found");
        }
        user.setAccountStatus(AccountStatusEnum.SUSPENDED.getValue());
        this.updateById(user);
        log.info("用户已注销账号，ID: {}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long userId, String status) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "User not found");
        }
        
        // 校验状态值是否合法
        boolean validStatus = false;
        for (AccountStatusEnum enumStatus : AccountStatusEnum.values()) {
            if (enumStatus.getValue().equals(status)) {
                validStatus = true;
                break;
            }
        }
        
        if (!validStatus) {
            throw new BusinessException(400, "Invalid user status");
        }
        
        user.setAccountStatus(status);
        this.updateById(user);
        log.info("管理员更新了用户状态，ID: {}, 新状态: {}", userId, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO  googleLogin(GoogleLoginDTO googleLoginDTO) {
        // 1. 验证 Google ID Token
        GoogleIdToken.Payload payload = verifyGoogleToken(googleLoginDTO.getIdToken());

        String googleId = payload.getSubject(); // Google 用户唯一 ID
        String email = payload.getEmail();
        String name = (String) payload.get("name");

        log.info("Google 登录，googleId: {}, email: {}", googleId, email);

        // 2. 优先通过 socialId + authProvider 查找已绑定的用户
        User user = this.lambdaQuery()
                .eq(User::getSocialId, googleId)
                .eq(User::getAuthProvider, AuthProviderEnum.GOOGLE.getValue())
                .last("LIMIT 1")
                .one();

        if (user == null && email != null) {
            // 3. 如果没有绑定记录，通过邮箱查找是否已有本地注册用户
            user = this.lambdaQuery()
                    .eq(User::getEmail, email)
                    .last("LIMIT 1")
                    .one();

            if (user != null) {
                // 已有本地用户，绑定 Google 账号信息
                user.setAuthProvider(AuthProviderEnum.GOOGLE.getValue());
                user.setSocialId(googleId);
                this.updateById(user);
                log.info("已有本地用户绑定 Google 账号，userId: {}", user.getId());
            }
        }

        if (user == null) {
            // 4. 全新用户，自动注册
            user = User.builder()
                    .email(email)
                    .name(name != null ? name : "Google User")
                    .role(UserRoleEnum.MEMBER.getValue())
                    .accountStatus(AccountStatusEnum.APPROVED.getValue())
                    .authProvider(AuthProviderEnum.GOOGLE.getValue())
                    .socialId(googleId)
                    .build();

            try {
                this.save(user);
            } catch (DuplicateKeyException e) {
                // 并发场景：另一个请求已经用这个邮箱注册了，重新查询
                log.warn("Google 登录并发注册冲突，重新查询用户: {}", email);
                user = this.lambdaQuery()
                        .eq(User::getEmail, email)
                        .last("LIMIT 1")
                        .one();
                if (user == null) {
                    throw new BusinessException("Registration failed, please try again!");
                }
            }
            log.info("Google 新用户注册成功，userId: {}", user.getId());
        }

        // 5. 校验账号状态
        if (AccountStatusEnum.SUSPENDED.getValue().equals(user.getAccountStatus())) {
            throw new BusinessException("Your account has been banned. Please contact customer service!");
        }

        // 6. 生成 JWT Token 并返回
        UserVO userVO = userConverter.toVO(user);
        userVO.setToken(jwtUtil.generateToken(user.getId(), user.getRole()));
        return userVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateProfile(Long userId, ProfileUpdateDTO profileUpdateDTO) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "User not found");
        }
        user.setDateOfBirth(profileUpdateDTO.getDateOfBirth());
        user.setAddress(profileUpdateDTO.getAddress());
        this.updateById(user);
        log.info("用户资料更新成功，userId: {}", userId);
        return userConverter.toVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyEmail(String email, String code) {
        log.info("用户尝试验证邮箱: {}", email);

        // 1. 从 Redis 获取验证码
        String redisKey = VERIFY_CODE_KEY_PREFIX + email;
        String storedCode = stringRedisTemplate.opsForValue().get(redisKey);

        if (storedCode == null) {
            throw new BusinessException("Verification code has expired or does not exist, please request a new one!");
        }

        // 2. 校验验证码是否匹配
        if (!storedCode.equals(code)) {
            throw new BusinessException("Invalid verification code!");
        }

        // 3. 查询用户
        User user = this.lambdaQuery()
                .eq(User::getEmail, email)
                .last("LIMIT 1")
                .one();

        if (user == null) {
            throw new BusinessException("User not found!");
        }

        // 4. 校验用户当前状态是否为 pending
        if (!AccountStatusEnum.PENDING.getValue().equals(user.getAccountStatus())) {
            throw new BusinessException("This email has already been verified!");
        }

        // 5. 更新状态为 approved
        user.setAccountStatus(AccountStatusEnum.APPROVED.getValue());
        this.updateById(user);

        // 6. 验证成功后删除 Redis 中的验证码
        stringRedisTemplate.delete(redisKey);

        log.info("邮箱验证成功，用户ID: {}, 邮箱: {}", user.getId(), email);
    }

    @Override
    public void resendVerificationCode(String email) {
        log.info("用户请求重新发送验证码，邮箱: {}", email);

        // 1. 检查冷却时间
        String cooldownKey = VERIFY_COOLDOWN_KEY_PREFIX + email;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(cooldownKey))) {
            Long ttl = stringRedisTemplate.getExpire(cooldownKey, TimeUnit.SECONDS);
            throw new BusinessException("Please wait " + ttl + " seconds before requesting a new verification code!");
        }

        // 2. 查询用户，确认存在且状态为 pending
        User user = this.lambdaQuery()
                .eq(User::getEmail, email)
                .last("LIMIT 1")
                .one();

        if (user == null) {
            throw new BusinessException("User not found!");
        }

        if (!AccountStatusEnum.PENDING.getValue().equals(user.getAccountStatus())) {
            throw new BusinessException("This email has already been verified!");
        }

        // 3. 生成新的验证码并存入 Redis
        String verifyCode = generateVerificationCode();
        String redisKey = VERIFY_CODE_KEY_PREFIX + email;
        stringRedisTemplate.opsForValue().set(redisKey, verifyCode, codeExpireMinutes, TimeUnit.MINUTES);

        // 4. 设置冷却标记
        stringRedisTemplate.opsForValue().set(cooldownKey, "1", resendCooldownSeconds, TimeUnit.SECONDS);

        // 5. 发送邮件
        emailService.sendVerificationCode(email, verifyCode);
        log.info("验证码重新发送成功，邮箱: {}", email);
    }

    /**
     * 生成6位数字验证码
     */
    private String generateVerificationCode() {
        int code = (int) ((Math.random() * 900000) + 100000);
        return String.valueOf(code);
    }

    /**
     * 验证 Google ID Token 的合法性
     */
    private GoogleIdToken.Payload verifyGoogleToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new BusinessException("Invalid Google ID Token!");
            }
            return idToken.getPayload();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google ID Token 验证失败", e);
            throw new BusinessException("Google authentication failed!");
        }
    }
}
