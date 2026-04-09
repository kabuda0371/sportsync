package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.converter.UserConverter;
import com.example.demo.dto.PartnerProfileDTO;
import com.example.demo.dto.PartnerRequestDTO;
import com.example.demo.entity.PartnerRequest;
import com.example.demo.entity.User;
import com.example.demo.enums.AccountStatusEnum;
import com.example.demo.enums.AvailabilityEnum;
import com.example.demo.enums.PartnerRequestStatusEnum;
import com.example.demo.enums.SkillLevelEnum;
import com.example.demo.enums.SportEnum;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.PartnerRequestMapper;
import com.example.demo.service.PartnerMatchingService;
import com.example.demo.service.UserService;
import com.example.demo.vo.PartnerMatchVO;
import com.example.demo.vo.PartnerRequestVO;
import com.example.demo.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartnerMatchingServiceImpl extends ServiceImpl<PartnerRequestMapper, PartnerRequest> implements PartnerMatchingService {

    private final UserService userService;
    private final UserConverter userConverter;
    private final com.example.demo.mapper.UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updatePartnerProfile(Long userId, PartnerProfileDTO dto) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "User not found");
        }

        for (String sport : dto.getPreferredSport()) {
            if (!SportEnum.isValid(sport)) {
                throw new BusinessException(400, "Invalid sport: " + sport + ". Must be one of: badminton, football, swimming, tennis");
            }
        }
        if (!SkillLevelEnum.isValid(dto.getSkillLevel())) {
            throw new BusinessException(400, "Invalid skill level. Must be one of: beginner, intermediate, advanced");
        }
        for (String avail : dto.getAvailability()) {
            if (!AvailabilityEnum.isValid(avail)) {
                throw new BusinessException(400, "Invalid availability: " + avail + ". Must be one of: weekday_morning, weekday_afternoon, weekday_evening, weekend_morning, weekend_afternoon, weekend_evening");
            }
        }

        user.setPartnerMatchingEnabled(dto.getPartnerMatchingEnabled());
        user.setPreferredSport(String.join(",", dto.getPreferredSport()));
        user.setSkillLevel(dto.getSkillLevel());
        user.setAvailability(String.join(",", dto.getAvailability()));
        user.setPartnerBio(dto.getPartnerBio());
        userService.updateById(user);

        log.info("用户更新伙伴匹配简介，userId: {}, sport: {}, level: {}, availability: {}",
                userId, dto.getPreferredSport(), dto.getSkillLevel(), dto.getAvailability());

        return userConverter.toVO(user);
    }

    @Override
    public IPage<PartnerMatchVO> getMatches(Long userId, int page, int size, String skillLevel, String availability, String sport) {
        User currentUser = userService.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(404, "User not found");
        }

        if (!Boolean.TRUE.equals(currentUser.getPartnerMatchingEnabled())) {
            throw new BusinessException(400, "Please enable partner matching and complete your activity profile first");
        }

        if (currentUser.getPreferredSport() == null) {
            throw new BusinessException(400, "Please complete your activity profile first");
        }

        // 只排除 PENDING 和 ACCEPTED 状态的请求，REJECTED 的允许重新匹配
        List<Long> excludeIds = this.lambdaQuery()
                .ne(PartnerRequest::getStatus, PartnerRequestStatusEnum.REJECTED.getValue())
                .and(w -> w
                        .eq(PartnerRequest::getRequesterId, userId)
                        .or()
                        .eq(PartnerRequest::getTargetId, userId))
                .list()
                .stream()
                .map(req -> req.getRequesterId().equals(userId) ? req.getTargetId() : req.getRequesterId())
                .collect(Collectors.toList());
        excludeIds.add(userId);

        // 按指定运动搜索，或使用用户自己的偏好运动
        if (sport != null && !SportEnum.isValid(sport)) {
            throw new BusinessException(400, "Invalid sport parameter. Must be one of: badminton, football, swimming, tennis");
        }
        List<String> searchSports = (sport != null)
                ? List.of(sport)
                : Arrays.asList(currentUser.getPreferredSport().split(","));
        List<String> currentAvailabilities = Arrays.asList(currentUser.getAvailability().split(","));

        // 使用 SQL 层排序和分页
        Page<User> userPage = new Page<>(page, size);
        IPage<User> matchedPage = userMapper.selectMatchedPartners(
                userPage, excludeIds, searchSports, skillLevel, availability,
                currentUser.getSkillLevel(), currentAvailabilities);

        List<PartnerMatchVO> voList = matchedPage.getRecords().stream()
                .map(user -> PartnerMatchVO.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .preferredSport(user.getPreferredSport() != null ? Arrays.asList(user.getPreferredSport().split(",")) : List.of())
                        .skillLevel(user.getSkillLevel())
                        .availability(user.getAvailability() != null ? Arrays.asList(user.getAvailability().split(",")) : List.of())
                        .partnerBio(user.getPartnerBio())
                        .build())
                .collect(Collectors.toList());

        Page<PartnerMatchVO> resultPage = new Page<>(page, size);
        resultPage.setTotal(matchedPage.getTotal());
        resultPage.setRecords(voList);
        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PartnerRequestVO sendRequest(Long userId, PartnerRequestDTO dto) {
        if (userId.equals(dto.getTargetId())) {
            throw new BusinessException(400, "Cannot send partner request to yourself");
        }

        User currentUser = userService.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(404, "User not found");
        }
        if (!Boolean.TRUE.equals(currentUser.getPartnerMatchingEnabled())) {
            throw new BusinessException(400, "Please enable partner matching first");
        }

        User targetUser = userService.getById(dto.getTargetId());
        if (targetUser == null) {
            throw new BusinessException(404, "Target user not found");
        }
        if (!Boolean.TRUE.equals(targetUser.getPartnerMatchingEnabled())) {
            throw new BusinessException(400, "Target user has not enabled partner matching");
        }

        // 检查是否已经存在 PENDING 或 ACCEPTED 的请求（双向检查），REJECTED 允许重新发送
        long existingCount = this.lambdaQuery()
                .ne(PartnerRequest::getStatus, PartnerRequestStatusEnum.REJECTED.getValue())
                .and(w -> w
                        .and(inner -> inner
                                .eq(PartnerRequest::getRequesterId, userId)
                                .eq(PartnerRequest::getTargetId, dto.getTargetId()))
                        .or(inner -> inner
                                .eq(PartnerRequest::getRequesterId, dto.getTargetId())
                                .eq(PartnerRequest::getTargetId, userId)))
                .count();
        if (existingCount > 0) {
            throw new BusinessException(409, "A partner request already exists between you and this user");
        }

        // 检查 7 天内是否有被拒绝的请求（双向冷却期）
        LocalDateTime cooldownCutoff = LocalDateTime.now().minusDays(7);
        long recentRejectedCount = this.lambdaQuery()
                .eq(PartnerRequest::getStatus, PartnerRequestStatusEnum.REJECTED.getValue())
                .ge(PartnerRequest::getUpdatedAt, cooldownCutoff)
                .and(w -> w
                        .and(inner -> inner
                                .eq(PartnerRequest::getRequesterId, userId)
                                .eq(PartnerRequest::getTargetId, dto.getTargetId()))
                        .or(inner -> inner
                                .eq(PartnerRequest::getRequesterId, dto.getTargetId())
                                .eq(PartnerRequest::getTargetId, userId)))
                .count();
        if (recentRejectedCount > 0) {
            throw new BusinessException(429, "A partner request between you and this user was recently rejected. Please wait 7 days before sending a new request.");
        }

        PartnerRequest request = PartnerRequest.builder()
                .requesterId(userId)
                .targetId(dto.getTargetId())
                .status(PartnerRequestStatusEnum.PENDING.getValue())
                .message(dto.getMessage())
                .build();

        this.save(request);

        log.info("伙伴配对请求已发送，requesterId: {}, targetId: {}", userId, dto.getTargetId());

        return buildRequestVO(request, currentUser.getName(), targetUser.getName());
    }

    @Override
    public IPage<PartnerRequestVO> getReceivedRequests(Long userId, int page, int size) {
        Page<PartnerRequest> requestPage = new Page<>(page, size);
        this.lambdaQuery()
                .eq(PartnerRequest::getTargetId, userId)
                .eq(PartnerRequest::getStatus, PartnerRequestStatusEnum.PENDING.getValue())
                .orderByDesc(PartnerRequest::getCreatedAt)
                .page(requestPage);

        return convertRequestPage(requestPage);
    }

    @Override
    public IPage<PartnerRequestVO> getSentRequests(Long userId, int page, int size) {
        Page<PartnerRequest> requestPage = new Page<>(page, size);
        this.lambdaQuery()
                .eq(PartnerRequest::getRequesterId, userId)
                .orderByDesc(PartnerRequest::getCreatedAt)
                .page(requestPage);

        return convertRequestPage(requestPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PartnerRequestVO handleRequest(Long userId, Long requestId, String status) {
        if (!PartnerRequestStatusEnum.ACCEPTED.getValue().equals(status)
                && !PartnerRequestStatusEnum.REJECTED.getValue().equals(status)) {
            throw new BusinessException(400, "Invalid status. Must be 'accepted' or 'rejected'");
        }

        PartnerRequest request = this.getById(requestId);
        if (request == null) {
            throw new BusinessException(404, "Partner request not found");
        }

        if (!request.getTargetId().equals(userId)) {
            throw new BusinessException(403, "You can only handle requests sent to you");
        }

        if (!PartnerRequestStatusEnum.PENDING.getValue().equals(request.getStatus())) {
            throw new BusinessException(400, "This request has already been processed");
        }

        request.setStatus(status);
        this.updateById(request);

        User requester = userService.getById(request.getRequesterId());
        User target = userService.getById(request.getTargetId());

        log.info("伙伴配对请求已处理，requestId: {}, status: {}", requestId, status);

        return buildRequestVO(request,
                requester != null ? requester.getName() : "Unknown",
                target != null ? target.getName() : "Unknown");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelRequest(Long userId, Long requestId) {
        PartnerRequest request = this.getById(requestId);
        if (request == null) {
            throw new BusinessException(404, "Partner request not found");
        }

        if (!request.getRequesterId().equals(userId)) {
            throw new BusinessException(403, "You can only cancel requests you sent");
        }

        if (!PartnerRequestStatusEnum.PENDING.getValue().equals(request.getStatus())) {
            throw new BusinessException(400, "Only pending requests can be cancelled");
        }

        this.removeById(requestId);

        log.info("伙伴配对请求已取消，requestId: {}, userId: {}", requestId, userId);
    }

    @Override
    public IPage<PartnerMatchVO> getMyPartners(Long userId, int page, int size) {
        List<PartnerRequest> acceptedRequests = this.lambdaQuery()
                .eq(PartnerRequest::getStatus, PartnerRequestStatusEnum.ACCEPTED.getValue())
                .and(w -> w
                        .eq(PartnerRequest::getRequesterId, userId)
                        .or()
                        .eq(PartnerRequest::getTargetId, userId))
                .list();

        if (acceptedRequests.isEmpty()) {
            Page<PartnerMatchVO> emptyPage = new Page<>(page, size);
            emptyPage.setTotal(0);
            emptyPage.setRecords(List.of());
            return emptyPage;
        }

        List<Long> partnerIds = acceptedRequests.stream()
                .map(req -> req.getRequesterId().equals(userId) ? req.getTargetId() : req.getRequesterId())
                .distinct()
                .collect(Collectors.toList());

        List<User> partners = userService.listByIds(partnerIds);
        List<PartnerMatchVO> voList = partners.stream()
                .map(user -> PartnerMatchVO.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .preferredSport(user.getPreferredSport() != null ? Arrays.asList(user.getPreferredSport().split(",")) : List.of())
                        .skillLevel(user.getSkillLevel())
                        .availability(user.getAvailability() != null ? Arrays.asList(user.getAvailability().split(",")) : List.of())
                        .partnerBio(user.getPartnerBio())
                        .build())
                .collect(Collectors.toList());

        Page<PartnerMatchVO> resultPage = new Page<>(page, size);
        int start = (int) ((page - 1) * size);
        int end = Math.min(start + size, voList.size());
        resultPage.setRecords(start < voList.size() ? voList.subList(start, end) : List.of());
        resultPage.setTotal(voList.size());
        return resultPage;
    }

    /**
     * 将 PartnerRequest 分页结果转换为 PartnerRequestVO 分页结果（批量查询用户，避免 N+1）
     */
    private IPage<PartnerRequestVO> convertRequestPage(Page<PartnerRequest> requestPage) {
        List<PartnerRequest> requests = requestPage.getRecords();
        if (requests.isEmpty()) {
            Page<PartnerRequestVO> emptyPage = new Page<>(requestPage.getCurrent(), requestPage.getSize());
            emptyPage.setTotal(requestPage.getTotal());
            emptyPage.setRecords(List.of());
            return emptyPage;
        }

        // 批量查询所有相关用户，避免 N+1
        Set<Long> userIds = requests.stream()
                .flatMap(r -> Stream.of(r.getRequesterId(), r.getTargetId()))
                .collect(Collectors.toSet());
        Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<PartnerRequestVO> voList = requests.stream()
                .map(req -> {
                    User requester = userMap.get(req.getRequesterId());
                    User target = userMap.get(req.getTargetId());
                    return buildRequestVO(req,
                            requester != null ? requester.getName() : "Unknown",
                            target != null ? target.getName() : "Unknown");
                })
                .collect(Collectors.toList());

        Page<PartnerRequestVO> voPage = new Page<>(requestPage.getCurrent(), requestPage.getSize());
        voPage.setTotal(requestPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    private PartnerRequestVO buildRequestVO(PartnerRequest request, String requesterName, String targetName) {
        return PartnerRequestVO.builder()
                .id(request.getId())
                .requesterId(request.getRequesterId())
                .requesterName(requesterName)
                .targetId(request.getTargetId())
                .targetName(targetName)
                .status(request.getStatus())
                .message(request.getMessage())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
