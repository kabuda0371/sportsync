package com.example.demo.converter;

import com.example.demo.entity.User;
import com.example.demo.vo.UserVO;
import org.mapstruct.Mapper;

/**
 * 用户实体转换器 (MapStruct)
 */
@Mapper(componentModel = "spring")
public interface UserConverter {

    UserVO toVO(User user);
}
