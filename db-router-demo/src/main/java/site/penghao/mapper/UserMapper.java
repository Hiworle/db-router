package site.penghao.mapper;

import org.apache.ibatis.annotations.Mapper;
import site.penghao.dbrouter.annotation.EnableRouter;
import site.penghao.dbrouter.annotation.TableRouting;
import site.penghao.dbrouter.strategy.impl.HashRoutingStrategy;
import site.penghao.pojo.User;

@Mapper
@EnableRouter
public interface UserMapper {

    @TableRouting(key = "id", strategy = HashRoutingStrategy.class)
    User selectById(Integer id);

    @TableRouting(key = "id")
    void insert(User user);

    @TableRouting(key = "id")
    void update(User user);

    @TableRouting(key = "id")
    void delete(Integer id);
}
