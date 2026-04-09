package com.example.demo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.dto.PartnerProfileDTO;
import com.example.demo.dto.PartnerRequestDTO;
import com.example.demo.entity.PartnerRequest;
import com.example.demo.vo.PartnerMatchVO;
import com.example.demo.vo.PartnerRequestVO;
import com.example.demo.vo.UserVO;

public interface PartnerMatchingService extends IService<PartnerRequest> {

    /**
     * 更新伙伴匹配活动简介
     */
    UserVO updatePartnerProfile(Long userId, PartnerProfileDTO dto);

    /**
     * 获取匹配的伙伴列表（分页，支持筛选和按运动搜索）
     */
    IPage<PartnerMatchVO> getMatches(Long userId, int page, int size, String skillLevel, String availability, String sport);

    /**
     * 发送配对请求
     */
    PartnerRequestVO sendRequest(Long userId, PartnerRequestDTO dto);

    /**
     * 获取收到的配对请求（分页）
     */
    IPage<PartnerRequestVO> getReceivedRequests(Long userId, int page, int size);

    /**
     * 获取已发出的配对请求（分页）
     */
    IPage<PartnerRequestVO> getSentRequests(Long userId, int page, int size);

    /**
     * 处理配对请求（接受/拒绝）
     */
    PartnerRequestVO handleRequest(Long userId, Long requestId, String status);

    /**
     * 取消已发送的配对请求
     */
    void cancelRequest(Long userId, Long requestId);

    /**
     * 获取我的伙伴列表（已接受的伙伴关系）
     */
    IPage<PartnerMatchVO> getMyPartners(Long userId, int page, int size);
}
