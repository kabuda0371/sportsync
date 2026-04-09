package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    IPage<User> selectMatchedPartners(
            Page<User> page,
            @Param("excludeIds") List<Long> excludeIds,
            @Param("searchSports") List<String> searchSports,
            @Param("skillLevel") String skillLevel,
            @Param("availability") String availability,
            @Param("currentSkillLevel") String currentSkillLevel,
            @Param("currentAvailabilities") List<String> currentAvailabilities
    );
}
