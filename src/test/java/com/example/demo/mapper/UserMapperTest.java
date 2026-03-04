package com.example.demo.mapper;

import com.example.demo.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testSelectAll() {
        System.out.println("----- Select All Users -----");
        // Using MyBatis-Plus BaseMapper's selectList method
        // Passing null means no query conditions (select all)
        List<User> userList = userMapper.selectList(null);
        
        for (User user : userList) {
            System.out.println(user.getId() + " | " + user.getName() + " | " + user.getEmail() + " | Role: " + user.getRole());
        }
        System.out.println("Total users found: " + userList.size());
    }
}
