package com.example.demo.converter;

import com.example.demo.entity.User;
import com.example.demo.vo.UserVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Arrays;
import java.util.List;

/**
 * 用户实体转换器 (MapStruct)
 */
@Mapper(componentModel = "spring")
public interface UserConverter {

    @Mapping(target = "token", ignore = true)
    @Mapping(target = "preferredSport", expression = "java(splitToList(user.getPreferredSport()))")
    @Mapping(target = "availability", expression = "java(splitToList(user.getAvailability()))")
    UserVO toVO(User user);

    default List<String> splitToList(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.asList(csv.split(","));
    }
}
