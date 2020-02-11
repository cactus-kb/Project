package Util;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class DBUtil {

    //单例模式---->数据库连接池
    private static volatile DataSource DATA_SOURCE;

    //私有的构造方法
    private DBUtil() {

    }

    //获取SQLite数据本地文件路径（target目录下）
    private static String getUrl() throws URISyntaxException {
        String dbName = "Searching.db";
        //getClassLoader()获取类加载器
        URL url = DBUtil.class.getClassLoader().getResource(".");
        //separator 不同系统分隔符写法不同；为了不将分隔符写死
        return "jdbc:sqlite://" + new File(url.toURI()).getParent() + File.separator + dbName;
    }


    //获取数据库连接池
    private static DataSource getDataSource() throws URISyntaxException {
        if (DATA_SOURCE == null) {
            synchronized (DBUtil.class) {
                if (DATA_SOURCE == null) {
                    //mysql日期格式：yyyy-MM-dd HH:mm:ss
                    //sqlite日期格式：yyyy-MM-dd HH:mm:ss:SSS
                    //下面两行为设置时间的类型
                    SQLiteConfig config = new SQLiteConfig();
                    config.setDateStringFormat(Util.DATA_SOURCE);
                    DATA_SOURCE = new SQLiteDataSource(config);
                    ((SQLiteDataSource)DATA_SOURCE).setUrl(getUrl());
                }
            }
        }
        return DATA_SOURCE;
    }

    //获取数据库连接：
    // (1)Class.forName("驱动类全名") 加载驱动，DriverManager.getConnection()
    //(2)DataSource
    public static Connection getConnection() {
        try {
            return getDataSource().getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("数据库连接获取失败");
        }
    }

    public static void close(Connection connection, Statement statement, ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("释放数据库资源错误");
        }
    }

    public static void close (Connection connection, Statement statement) {
        close(connection, statement, null);
    }

    public static void main(String[] args) throws URISyntaxException {
//        URL url = DBUtil.class.getClassLoader().getResource(".");
//        System.out.println(new File(url.toURI()).getParent());
        Connection connection = getConnection();
    }
}
