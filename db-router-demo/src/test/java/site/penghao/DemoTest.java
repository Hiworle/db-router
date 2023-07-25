package site.penghao;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import site.penghao.mapper.UserMapper;
import site.penghao.pojo.User;

import javax.annotation.Resource;

@SpringBootTest
public class DemoTest {

    @Resource
    UserMapper userMapper;

    @Test
    public void test() {

        User user = new User(20, "hope", "123456");

        userMapper.insert(user);

        System.out.println(userMapper.selectById(20));

        user.setPassword("666666");
        userMapper.update(user);
        System.out.println(userMapper.selectById(20));

        userMapper.delete(20);

        System.out.println(userMapper.selectById(20));
    }

}
