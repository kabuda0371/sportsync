package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.dto.BookingRequestDTO;
import com.example.demo.dto.BookingStatusUpdateDTO;
import com.example.demo.entity.Booking;
import com.example.demo.entity.BookingInvitation;
import com.example.demo.entity.Facility;
import com.example.demo.exception.BusinessException;
import com.example.demo.entity.PartnerRequest;
import com.example.demo.mapper.BookingInvitationMapper;
import com.example.demo.mapper.BookingMapper;
import com.example.demo.mapper.PartnerRequestMapper;
import com.example.demo.service.BookingService;
import com.example.demo.service.FacilityService;
import com.example.demo.service.UserService;
import com.example.demo.vo.BookingVO;
import com.example.demo.vo.InvitationMemberStatusVO;
import com.example.demo.entity.User;
import com.example.demo.enums.AccountStatusEnum;
import com.example.demo.enums.BookingStatusEnum;
import com.example.demo.enums.InvitationStatusEnum;
import com.example.demo.enums.UserRoleEnum;
import com.example.demo.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl extends ServiceImpl<BookingMapper, Booking> implements BookingService {

    @Autowired
    private FacilityService facilityService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PartnerRequestMapper partnerRequestMapper;

    @Autowired
    private BookingInvitationMapper bookingInvitationMapper;

    /**
     * Parse comma-separated partner IDs string to List<Long>
     */
    private List<Long> parsePartnerIds(String partnerIds) {
        if (partnerIds == null || partnerIds.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(partnerIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    /**
     * Convert List<Long> to comma-separated string
     */
    private String joinPartnerIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        return ids.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private void notifyAcceptedPartners(Booking booking, String message) {
        List<Long> pIds = parsePartnerIds(booking.getPartnerIds());
        for (Long pid : pIds) {
            notificationService.sendNotification(pid, booking.getId(), message);
        }
    }

    private void notifyActiveInvitees(Long bookingId, String message) {
        List<BookingInvitation> active = bookingInvitationMapper.selectList(
                new LambdaQueryWrapper<BookingInvitation>()
                        .eq(BookingInvitation::getBookingId, bookingId)
                        .in(BookingInvitation::getStatus,
                                InvitationStatusEnum.PENDING.getValue(),
                                InvitationStatusEnum.ACCEPTED.getValue()));
        for (BookingInvitation inv : active) {
            notificationService.sendNotification(inv.getInviteeId(), bookingId, message);
        }
    }

    private void requireAcceptedPartnership(Long userId, Long partnerId) {
        long acceptedCount = partnerRequestMapper.selectCount(
                new LambdaQueryWrapper<PartnerRequest>()
                        .eq(PartnerRequest::getStatus, "accepted")
                        .and(w -> w
                                .and(inner -> inner
                                        .eq(PartnerRequest::getRequesterId, userId)
                                        .eq(PartnerRequest::getTargetId, partnerId))
                                .or(inner -> inner
                                        .eq(PartnerRequest::getRequesterId, partnerId)
                                        .eq(PartnerRequest::getTargetId, userId)))
        );
        if (acceptedCount == 0) {
            throw new BusinessException(400, "No accepted partner relationship exists with user (ID: " + partnerId + ")");
        }
    }

    private String getConflictDisplayName(Long userId) {
        User user = userService.getById(userId);
        return user != null && user.getName() != null && !user.getName().isBlank()
                ? user.getName()
                : "User " + userId;
    }

    private String buildUserConflictMessage(Long userId) {
        return getConflictDisplayName(userId)
                + " already has another booking during this time slot. Please choose a different time slot.";
    }

    private void lockAndCheckUserConflict(Long userId, LocalDate date,
                                          java.time.LocalTime startTime, java.time.LocalTime endTime,
                                          String roleHint) {
        long ownConflict = baseMapper.countUserOwnConflictForUpdate(userId, date, startTime, endTime);
        long invitedConflict = baseMapper.countUserInvitedConflictForUpdate(userId, date, startTime, endTime);
        if (ownConflict + invitedConflict > 0) {
            throw new BusinessException(409, buildUserConflictMessage(userId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookingVO createBooking(Long userId, BookingRequestDTO requestDTO) {
        // Validate facility exists
        Facility facility = facilityService.getById(requestDTO.getFacilityId());
        if (facility == null) {
            throw new BusinessException(404, "Facility not found");
        }

        // Validate user account status
        User user = userService.getById(userId);
        if (user == null || !AccountStatusEnum.APPROVED.getValue().equalsIgnoreCase(user.getAccountStatus())) {
            throw new BusinessException(403, "Account not approved, unable to book facilities");
        }

        // Validate time
        if (requestDTO.getStartTime().isAfter(requestDTO.getEndTime())) {
            throw new BusinessException(400, "Start time must be earlier than end time");
        }

        long facilityConflict = baseMapper.countConflictForUpdate(
                requestDTO.getFacilityId(),
                requestDTO.getBookingDate(),
                requestDTO.getStartTime(),
                requestDTO.getEndTime()
        );
        if (facilityConflict > 0) {
            throw new BusinessException(409, "Time slot is already occupied");
        }

        lockAndCheckUserConflict(userId, requestDTO.getBookingDate(),
                requestDTO.getStartTime(), requestDTO.getEndTime(), "Requester");

        List<Long> partnerIdList = requestDTO.getPartnerIds();
        List<Long> sortedUniquePartners = Collections.emptyList();
        if (partnerIdList != null && !partnerIdList.isEmpty()) {
            sortedUniquePartners = partnerIdList.stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .filter(id -> !id.equals(userId))
                    .sorted()
                    .collect(Collectors.toList());

            for (Long partnerId : sortedUniquePartners) {
                User partner = userService.getById(partnerId);
                if (partner == null) {
                    throw new BusinessException(404, "Partner user not found (ID: " + partnerId + ")");
                }
                requireAcceptedPartnership(userId, partnerId);

                lockAndCheckUserConflict(partnerId, requestDTO.getBookingDate(),
                        requestDTO.getStartTime(), requestDTO.getEndTime(), "Partner");
            }
        }

        boolean hasPartners = !sortedUniquePartners.isEmpty();
        String initialStatus = hasPartners
                ? BookingStatusEnum.AWAITING_PARTNER.getValue()
                : BookingStatusEnum.PENDING.getValue();

        Booking booking = Booking.builder()
                .userId(userId)
                .facilityId(requestDTO.getFacilityId())
                .bookingDate(requestDTO.getBookingDate())
                .startTime(requestDTO.getStartTime())
                .endTime(requestDTO.getEndTime())
                .status(initialStatus)
                .activityDescription(requestDTO.getActivityDescription())
                .partnerIds(null)
                .build();

        this.save(booking);

        if (hasPartners) {
            String facilityName = facility.getName();
            for (Long partnerId : sortedUniquePartners) {
                BookingInvitation inv = BookingInvitation.builder()
                        .bookingId(booking.getId())
                        .inviteeId(partnerId)
                        .status(InvitationStatusEnum.PENDING.getValue())
                        .build();
                bookingInvitationMapper.insert(inv);

                String msg = user.getName() + " has invited you to a shared training session at "
                        + facilityName + " on " + booking.getBookingDate()
                        + " (" + booking.getStartTime() + " - " + booking.getEndTime()
                        + "). Please accept or decline in 'My Bookings'. Booking ID: " + booking.getId();
                notificationService.sendNotification(partnerId, booking.getId(), msg);
            }
        }

        return convertToVO(booking, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void respondToInvitation(Long inviteeId, Long bookingId, boolean accept) {
        Booking booking = baseMapper.lockBookingById(bookingId);
        if (booking == null) {
            throw new BusinessException(404, "Booking record not found");
        }
        if (!BookingStatusEnum.AWAITING_PARTNER.getValue().equals(booking.getStatus())) {
            throw new BusinessException(400, "Booking is no longer awaiting partner confirmation");
        }

        BookingInvitation inv = bookingInvitationMapper.lockByBookingAndInvitee(bookingId, inviteeId);
        if (inv == null) {
            throw new BusinessException(404, "Invitation not found");
        }
        if (!InvitationStatusEnum.PENDING.getValue().equals(inv.getStatus())) {
            throw new BusinessException(400, "Invitation has already been responded to");
        }

        User invitee = userService.getById(inviteeId);
        User booker = userService.getById(booking.getUserId());
        String inviteeName = invitee != null ? invitee.getName() : ("User " + inviteeId);
        String bookerName = booker != null ? booker.getName() : "Your partner";
        List<BookingInvitation> otherActiveInvitees = accept
                ? Collections.emptyList()
                : bookingInvitationMapper.selectList(new LambdaQueryWrapper<BookingInvitation>()
                .eq(BookingInvitation::getBookingId, bookingId)
                .ne(BookingInvitation::getId, inv.getId())
                .in(BookingInvitation::getStatus,
                        InvitationStatusEnum.PENDING.getValue(),
                        InvitationStatusEnum.ACCEPTED.getValue()));

        if (accept) {
            lockAndCheckUserConflict(inviteeId, booking.getBookingDate(),
                    booking.getStartTime(), booking.getEndTime(), "You");
        }

        String targetStatus = accept
                ? InvitationStatusEnum.ACCEPTED.getValue()
                : InvitationStatusEnum.DECLINED.getValue();
        int affected = bookingInvitationMapper.update(null,
                new LambdaUpdateWrapper<BookingInvitation>()
                        .eq(BookingInvitation::getId, inv.getId())
                        .eq(BookingInvitation::getStatus, InvitationStatusEnum.PENDING.getValue())
                        .set(BookingInvitation::getStatus, targetStatus)
                        .set(BookingInvitation::getRespondedAt, LocalDateTime.now()));
        if (affected == 0) {
            throw new BusinessException(409, "Invitation status changed concurrently, please retry");
        }

        if (accept) {
            List<Long> currentPartners = new ArrayList<>(parsePartnerIds(booking.getPartnerIds()));
            if (!currentPartners.contains(inviteeId)) {
                currentPartners.add(inviteeId);
            }
            String newPartnerIds = joinPartnerIds(currentPartners);
            boolean partnerWrite = this.update(null, new LambdaUpdateWrapper<Booking>()
                    .eq(Booking::getId, bookingId)
                    .eq(Booking::getStatus, BookingStatusEnum.AWAITING_PARTNER.getValue())
                    .set(Booking::getPartnerIds, newPartnerIds));
            if (!partnerWrite) {
                throw new BusinessException(409, "Booking state changed concurrently (likely cancelled), please refresh");
            }
            booking.setPartnerIds(newPartnerIds);
        }

        long stillPending = bookingInvitationMapper.countPending(bookingId);
        boolean advanced = false;
        if (accept && stillPending == 0) {
            boolean adv = this.update(null, new LambdaUpdateWrapper<Booking>()
                    .eq(Booking::getId, bookingId)
                    .eq(Booking::getStatus, BookingStatusEnum.AWAITING_PARTNER.getValue())
                    .set(Booking::getStatus, BookingStatusEnum.PENDING.getValue()));
            advanced = adv;
        }
        boolean downgraded = false;
        if (!accept) {
            bookingInvitationMapper.update(null, new LambdaUpdateWrapper<BookingInvitation>()
                    .eq(BookingInvitation::getBookingId, bookingId)
                    .ne(BookingInvitation::getId, inv.getId())
                    .in(BookingInvitation::getStatus,
                            InvitationStatusEnum.PENDING.getValue(),
                            InvitationStatusEnum.ACCEPTED.getValue())
                    .set(BookingInvitation::getStatus, InvitationStatusEnum.DECLINED.getValue())
                    .set(BookingInvitation::getRespondedAt, LocalDateTime.now()));

            downgraded = this.update(null, new LambdaUpdateWrapper<Booking>()
                    .eq(Booking::getId, bookingId)
                    .eq(Booking::getStatus, BookingStatusEnum.AWAITING_PARTNER.getValue())
                    .set(Booking::getStatus, BookingStatusEnum.PENDING.getValue())
                    .set(Booking::getPartnerIds, null));
            if (!downgraded) {
                throw new BusinessException(409, "Booking state changed concurrently (likely cancelled), please refresh");
            }
            booking.setStatus(BookingStatusEnum.PENDING.getValue());
            booking.setPartnerIds(null);
        }

        String aMsg = accept
                ? inviteeName + " has accepted your invitation for booking ID: " + bookingId + "."
                : inviteeName + " has declined your invitation for booking ID: " + bookingId
                + ". The booking has been downgraded to your personal booking and is now pending staff approval.";
        notificationService.sendNotification(booking.getUserId(), bookingId, aMsg);
        if (advanced) {
            String advanceMsg = "All partners have responded. Booking ID: " + bookingId
                    + " is now pending staff approval.";
            notificationService.sendNotification(booking.getUserId(), bookingId, advanceMsg);
        }

        String bMsg = accept
                ? "You have accepted the invitation from " + bookerName + " (Booking ID: " + bookingId + ")."
                : "You have declined the invitation from " + bookerName + " (Booking ID: " + bookingId + ").";
        notificationService.sendNotification(inviteeId, bookingId, bMsg);
        if (downgraded) {
            String declineDowngradeMsg = "This shared booking (ID: " + bookingId
                    + ") has been downgraded to a personal booking for " + bookerName + ".";
            notificationService.sendNotification(inviteeId, bookingId, declineDowngradeMsg);

            for (BookingInvitation otherInv : otherActiveInvitees) {
                String otherMsg = "The shared training session (ID: " + bookingId + ") with " + bookerName
                        + " has been downgraded to an individual booking because another invited partner declined. "
                        + "You are no longer attached to this booking.";
                notificationService.sendNotification(otherInv.getInviteeId(), bookingId, otherMsg);
            }
        }
    }

    @Override
    public List<BookingVO> getUserBookings(Long userId) {
        List<Booking> owned = this.list(new LambdaQueryWrapper<Booking>()
                .eq(Booking::getUserId, userId)
                .orderByDesc(Booking::getBookingDate, Booking::getStartTime));

        List<Long> invitedIds = bookingInvitationMapper.findVisibleBookingIdsForInvitee(userId);
        List<Booking> invited = invitedIds.isEmpty()
                ? Collections.emptyList()
                : this.list(new LambdaQueryWrapper<Booking>()
                        .in(Booking::getId, invitedIds));

        List<Booking> merged = mergeAndSortDesc(owned, invited);
        return convertToVOList(merged, userId);
    }

    @Override
    public List<BookingVO> getUpcomingBookings(Long userId) {
        LocalDate today = LocalDate.now();
        java.time.LocalTime now = java.time.LocalTime.now();

        List<Booking> owned = this.list(new LambdaQueryWrapper<Booking>()
                .eq(Booking::getUserId, userId)
                .and(w -> w.gt(Booking::getBookingDate, today)
                        .or(w2 -> w2.eq(Booking::getBookingDate, today).gt(Booking::getStartTime, now)))
                .orderByAsc(Booking::getBookingDate, Booking::getStartTime));

        List<Long> invitedIds = bookingInvitationMapper.findVisibleBookingIdsForInvitee(userId);
        List<Booking> invited = invitedIds.isEmpty()
                ? Collections.emptyList()
                : this.list(new LambdaQueryWrapper<Booking>()
                        .in(Booking::getId, invitedIds)
                        .and(w -> w.gt(Booking::getBookingDate, today)
                                .or(w2 -> w2.eq(Booking::getBookingDate, today).gt(Booking::getStartTime, now))));

        List<Booking> merged = mergeAndSortAsc(owned, invited);
        return convertToVOList(merged, userId);
    }

    private List<Booking> mergeAndSortDesc(List<Booking> a, List<Booking> b) {
        Map<Long, Booking> map = new LinkedHashMap<>();
        a.forEach(x -> map.put(x.getId(), x));
        b.forEach(x -> map.putIfAbsent(x.getId(), x));
        List<Booking> result = new ArrayList<>(map.values());
        result.sort((x, y) -> {
            int c = y.getBookingDate().compareTo(x.getBookingDate());
            if (c != 0) return c;
            return y.getStartTime().compareTo(x.getStartTime());
        });
        return result;
    }

    private List<Booking> mergeAndSortAsc(List<Booking> a, List<Booking> b) {
        Map<Long, Booking> map = new LinkedHashMap<>();
        a.forEach(x -> map.put(x.getId(), x));
        b.forEach(x -> map.putIfAbsent(x.getId(), x));
        List<Booking> result = new ArrayList<>(map.values());
        result.sort((x, y) -> {
            int c = x.getBookingDate().compareTo(y.getBookingDate());
            if (c != 0) return c;
            return x.getStartTime().compareTo(y.getStartTime());
        });
        return result;
    }

    private List<BookingVO> convertToVOList(List<Booking> bookings, Long viewerUserId) {
        if (bookings.isEmpty()) return Collections.emptyList();

        Set<Long> bookingIds = bookings.stream()
                .map(Booking::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<BookingInvitation> invitations = bookingIds.isEmpty()
                ? Collections.emptyList()
                : bookingInvitationMapper.selectList(new LambdaQueryWrapper<BookingInvitation>()
                .in(BookingInvitation::getBookingId, bookingIds));

        Set<Long> allPartnerIds = bookings.stream()
                .flatMap(b -> parsePartnerIds(b.getPartnerIds()).stream())
                .collect(Collectors.toSet());

        Set<Long> allInviteeIds = invitations.stream()
                .map(BookingInvitation::getInviteeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> allRelevantUserIds = new HashSet<>(allPartnerIds);
        allRelevantUserIds.addAll(allInviteeIds);

        Map<Long, User> userMap = allRelevantUserIds.isEmpty()
                ? Collections.emptyMap()
                : userService.listByIds(allRelevantUserIds).stream()
                        .collect(Collectors.toMap(User::getId, Function.identity()));

        Map<Long, List<BookingInvitation>> invitationsByBookingId = invitations.stream()
                .collect(Collectors.groupingBy(BookingInvitation::getBookingId));

        return bookings.stream()
                .map(b -> convertToVO(
                        b,
                        userMap,
                        invitationsByBookingId.getOrDefault(b.getId(), Collections.emptyList()),
                        viewerUserId))
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingVO> getBookingsForFacilityAndDate(Long facilityId, LocalDate date) {
        LambdaQueryWrapper<Booking> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Booking::getFacilityId, facilityId)
                .eq(Booking::getBookingDate, date)
                .in(Booking::getStatus,
                        BookingStatusEnum.PENDING.getValue(),
                        BookingStatusEnum.APPROVED.getValue(),
                        BookingStatusEnum.AWAITING_PARTNER.getValue())
                .orderByAsc(Booking::getStartTime);

        return convertToVOList(this.list(queryWrapper), null);
    }

    @Override
    public IPage<BookingVO> getPendingBookings(Long staffId, int page, int size, String status) {
        User user = userService.getById(staffId);
        if (user == null || (!UserRoleEnum.STAFF.getValue().equals(user.getRole()) && !UserRoleEnum.ADMIN.getValue().equals(user.getRole()))) {
            throw new BusinessException(403, "No permission to view pending bookings");
        }

        LambdaQueryWrapper<Booking> queryWrapper = new LambdaQueryWrapper<>();

        if (status != null && !status.isBlank()) {
            queryWrapper.eq(Booking::getStatus, status);
        } else {
            queryWrapper.in(Booking::getStatus,
                    BookingStatusEnum.PENDING.getValue(),
                    BookingStatusEnum.APPROVED.getValue());
        }
        queryWrapper.orderByAsc(Booking::getBookingDate, Booking::getStartTime);

        if (UserRoleEnum.STAFF.getValue().equals(user.getRole())) {
            List<Long> assignedFacilityIds = facilityService.lambdaQuery()
                    .eq(Facility::getAssignedStaffId, staffId)
                    .list()
                    .stream()
                    .map(Facility::getId)
                    .collect(Collectors.toList());

            if (assignedFacilityIds.isEmpty()) {
                Page<BookingVO> empty = new Page<>(page, size);
                empty.setTotal(0);
                empty.setRecords(List.of());
                return empty;
            }
            queryWrapper.in(Booking::getFacilityId, assignedFacilityIds);
        }

        IPage<Booking> bookingPage = this.page(new Page<>(page, size), queryWrapper);
        Page<BookingVO> voPage = new Page<>(page, size);
        voPage.setTotal(bookingPage.getTotal());
        voPage.setRecords(convertToVOList(bookingPage.getRecords(), null));
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBookingStatus(Long staffId, Long bookingId, BookingStatusUpdateDTO reviewDTO) {
        // Validate staff role
        User user = userService.getById(staffId);
        if (user == null || (!UserRoleEnum.STAFF.getValue().equals(user.getRole()) && !UserRoleEnum.ADMIN.getValue().equals(user.getRole()))) {
            throw new BusinessException(403, "No permission to approve bookings");
        }

        String normalizedStatus = reviewDTO.getStatus().toLowerCase();
        if (!BookingStatusEnum.APPROVED.getValue().equals(normalizedStatus) && !BookingStatusEnum.REJECTED.getValue().equals(normalizedStatus)) {
            throw new BusinessException(400, "Invalid approval status, must be approved or rejected");
        }

        Booking booking = this.getById(bookingId);
        if (booking == null) {
            throw new BusinessException(404, "Booking record not found");
        }

        if (UserRoleEnum.STAFF.getValue().equals(user.getRole())) {
            Facility facility = facilityService.getById(booking.getFacilityId());
            if (facility == null || !staffId.equals(facility.getAssignedStaffId())) {
                throw new BusinessException(403, "No permission to approve bookings for this facility");
            }
        }

        if (reviewDTO.getSuggestedFacilityId() != null) {
            Facility suggested = facilityService.getById(reviewDTO.getSuggestedFacilityId());
            if (suggested == null) {
                throw new BusinessException(404, "Suggested alternative facility not found");
            }
        }

        boolean affected = this.update(null, new LambdaUpdateWrapper<Booking>()
                .eq(Booking::getId, bookingId)
                .eq(Booking::getStatus, BookingStatusEnum.PENDING.getValue())
                .set(Booking::getStatus, normalizedStatus)
                .set(Booking::getStaffNote, reviewDTO.getStaffNote())
                .set(Booking::getSuggestedFacilityId, reviewDTO.getSuggestedFacilityId()));
        if (!affected) {
            throw new BusinessException(400, "Can only approve pending bookings");
        }

        String message;
        if (BookingStatusEnum.APPROVED.getValue().equals(normalizedStatus)) {
            message = "Your booking request (ID: " + bookingId + ") has been approved! Looking forward to your visit.";
        } else {
            String note = (reviewDTO.getStaffNote() != null && !reviewDTO.getStaffNote().isBlank())
                    ? "Staff note: " + reviewDTO.getStaffNote()
                    : "Please contact the sports centre for further details.";
            message = "Your booking request (ID: " + bookingId + ") has been rejected. " + note;
        }
        notificationService.sendNotification(booking.getUserId(), bookingId, message);

        if (booking.getPartnerIds() != null) {
            User booker = userService.getById(booking.getUserId());
            String bookerName = booker != null ? booker.getName() : "Your partner";
            String partnerMsg;
            if (BookingStatusEnum.APPROVED.getValue().equals(normalizedStatus)) {
                partnerMsg = "The shared training session (ID: " + bookingId + ") with " + bookerName + " has been approved!";
            } else {
                partnerMsg = "The shared training session (ID: " + bookingId + ") with " + bookerName + " has been rejected.";
            }
            notifyAcceptedPartners(booking, partnerMsg);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBooking(Long userId, Long bookingId) {
        Booking booking = this.getById(bookingId);
        if (booking == null) {
            throw new BusinessException(404, "Booking record not found");
        }

        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException(403, "No permission to cancel others' bookings");
        }

        String prevStatus = booking.getStatus();
        boolean affected = this.update(null, new LambdaUpdateWrapper<Booking>()
                .eq(Booking::getId, bookingId)
                .eq(Booking::getUserId, userId)
                .in(Booking::getStatus,
                        BookingStatusEnum.PENDING.getValue(),
                        BookingStatusEnum.AWAITING_PARTNER.getValue())
                .set(Booking::getStatus, BookingStatusEnum.CANCELLED.getValue()));
        if (!affected) {
            throw new BusinessException(400, "Can only cancel pending or awaiting-partner bookings");
        }

        User booker = userService.getById(userId);
        String bookerName = booker != null ? booker.getName() : "Your partner";
        String msg = bookerName + " has cancelled the shared training session (ID: " + bookingId + ").";

        if (BookingStatusEnum.AWAITING_PARTNER.getValue().equals(prevStatus)) {
            notifyActiveInvitees(bookingId, msg);
        } else if (booking.getPartnerIds() != null) {
            notifyAcceptedPartners(booking, msg);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markBookingCompleted(Long staffId, Long bookingId) {
        User staff = userService.getById(staffId);
        if (staff == null || (!UserRoleEnum.STAFF.getValue().equals(staff.getRole())
                && !UserRoleEnum.ADMIN.getValue().equals(staff.getRole()))) {
            throw new BusinessException(403, "No permission to perform this action");
        }

        Booking booking = this.getById(bookingId);
        if (booking == null) {
            throw new BusinessException(404, "Booking record not found");
        }

        if (UserRoleEnum.STAFF.getValue().equals(staff.getRole())) {
            Facility facility = facilityService.getById(booking.getFacilityId());
            if (facility == null || !staffId.equals(facility.getAssignedStaffId())) {
                throw new BusinessException(403, "No permission to mark bookings for this facility as completed");
            }
        }

        boolean affected = this.update(null, new LambdaUpdateWrapper<Booking>()
                .eq(Booking::getId, bookingId)
                .eq(Booking::getStatus, BookingStatusEnum.APPROVED.getValue())
                .set(Booking::getStatus, BookingStatusEnum.COMPLETED.getValue()));
        if (!affected) {
            throw new BusinessException(400, "Only approved bookings can be marked as completed");
        }

        String message = "Your facility booking (ID: " + bookingId + ") has been confirmed completed by staff. Thank you for using the sports center facilities!";
        notificationService.sendNotification(booking.getUserId(), bookingId, message);

        if (booking.getPartnerIds() != null) {
            User booker = userService.getById(booking.getUserId());
            String bookerName = booker != null ? booker.getName() : "Your partner";
            String partnerMsg = "The shared training session (ID: " + bookingId + ") with " + bookerName + " has been marked as completed. Thank you!";
            notifyAcceptedPartners(booking, partnerMsg);
        }
    }

    private BookingVO convertToVO(Booking booking, Long viewerUserId) {
        List<Long> pIds = parsePartnerIds(booking.getPartnerIds());
        List<BookingInvitation> invitations = booking.getId() == null
                ? Collections.emptyList()
                : bookingInvitationMapper.selectList(new LambdaQueryWrapper<BookingInvitation>()
                .eq(BookingInvitation::getBookingId, booking.getId()));

        Set<Long> relevantUserIds = new HashSet<>(pIds);
        invitations.stream()
                .map(BookingInvitation::getInviteeId)
                .filter(Objects::nonNull)
                .forEach(relevantUserIds::add);

        Map<Long, User> userMap = relevantUserIds.isEmpty()
                ? Collections.emptyMap()
                : userService.listByIds(relevantUserIds).stream()
                        .collect(Collectors.toMap(User::getId, Function.identity()));
        return convertToVO(booking, userMap, invitations, viewerUserId);
    }

    private BookingVO convertToVO(Booking booking, Map<Long, User> userMap,
                                  List<BookingInvitation> invitations, Long viewerUserId) {
        List<Long> pIds = parsePartnerIds(booking.getPartnerIds());
        List<String> pNames = pIds.stream()
                .map(id -> {
                    User partner = userMap.get(id);
                    return partner != null ? partner.getName() : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        String myInvitationStatus = null;
        if (viewerUserId != null && !viewerUserId.equals(booking.getUserId())) {
            myInvitationStatus = invitations.stream()
                    .filter(invitation -> viewerUserId.equals(invitation.getInviteeId()))
                    .map(BookingInvitation::getStatus)
                    .findFirst()
                    .orElse(null);
        }

        List<InvitationMemberStatusVO> invitationStatuses = invitations.stream()
                .sorted(Comparator
                        .comparingInt((BookingInvitation invitation) -> getInvitationStatusOrder(invitation.getStatus()))
                        .thenComparing(invitation -> {
                            User invitee = userMap.get(invitation.getInviteeId());
                            if (invitee != null && invitee.getName() != null && !invitee.getName().isBlank()) {
                                return invitee.getName();
                            }
                            return "User " + invitation.getInviteeId();
                        }, String.CASE_INSENSITIVE_ORDER))
                .map(invitation -> {
                    User invitee = userMap.get(invitation.getInviteeId());
                    String inviteeName = invitee != null && invitee.getName() != null && !invitee.getName().isBlank()
                            ? invitee.getName()
                            : "User " + invitation.getInviteeId();
                    return InvitationMemberStatusVO.builder()
                            .inviteeId(invitation.getInviteeId())
                            .inviteeName(inviteeName)
                            .status(invitation.getStatus())
                            .build();
                })
                .collect(Collectors.toList());

        if (viewerUserId != null && !viewerUserId.equals(booking.getUserId()) && myInvitationStatus == null) {
            myInvitationStatus = bookingInvitationMapper.findStatus(booking.getId(), viewerUserId);
        }

        return BookingVO.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .facilityId(booking.getFacilityId())
                .bookingDate(booking.getBookingDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .status(booking.getStatus())
                .activityDescription(booking.getActivityDescription())
                .staffNote(booking.getStaffNote())
                .suggestedFacilityId(booking.getSuggestedFacilityId())
                .partnerIds(pIds.isEmpty() ? null : pIds)
                .partnerNames(pNames.isEmpty() ? null : pNames)
                .invitationStatuses(invitationStatuses.isEmpty() ? null : invitationStatuses)
                .myInvitationStatus(myInvitationStatus)
                .createdAt(booking.getCreatedAt())
                .build();
    }

    private int getInvitationStatusOrder(String status) {
        if (InvitationStatusEnum.PENDING.getValue().equals(status)) {
            return 0;
        }
        if (InvitationStatusEnum.ACCEPTED.getValue().equals(status)) {
            return 1;
        }
        if (InvitationStatusEnum.DECLINED.getValue().equals(status)) {
            return 2;
        }
        return 3;
    }
}
