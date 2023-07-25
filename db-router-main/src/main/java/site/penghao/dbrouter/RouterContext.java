package site.penghao.dbrouter;

/**
 * 记录 Router 上下文信息，供动态使用
 */
public class RouterContext {

    private final static ThreadLocal<String> dbName = new ThreadLocal<>();
    private final static ThreadLocal<String> tbName = new ThreadLocal<>();

    public static String getDbName() {
        return dbName.get();
    }

    public static void setDbName(String dbName) {
        RouterContext.dbName.set(dbName);
    }

    public static String getTbName() {
        return tbName.get();
    }

    public static void setTbName(String tbName) {
        RouterContext.tbName.set(tbName);
    }

    public static void clearDbName() {
        dbName.remove();
    }
    public static void clearTbName() {
        tbName.remove();
    }
}
