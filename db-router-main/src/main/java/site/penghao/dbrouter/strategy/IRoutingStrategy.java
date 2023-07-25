package site.penghao.dbrouter.strategy;

public interface IRoutingStrategy {

    /**
     * 根据策略进行路由，更新上下文
     */
    public void route(Object value, int dbCount, int tbCount);

    /**
     * 清空上下文
     */
    public void clear();
}
