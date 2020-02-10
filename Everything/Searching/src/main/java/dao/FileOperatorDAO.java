package dao;

import Util.DBUtil;
import Util.Pinyin4jUtil;
import app.FileMeta;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FileOperatorDAO {

    public static List<FileMeta> query(String dirPath) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<FileMeta> metas = new ArrayList<>();
        try {
            //获取连接
            connection = DBUtil.getConnection();
            String sql = "select name,path,size,last_modified,is_directory from file_meta where path = ?";
            //获取操作命令对象
            statement = connection.prepareStatement(sql);
            statement.setString(1,dirPath);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String path = resultSet.getString("path");
                long size = resultSet.getLong("size");
                long last_modified = resultSet.getLong("last_modified");
                boolean is_directory = resultSet.getBoolean("is_directory");
                FileMeta meta = new FileMeta(name,path,size,last_modified,is_directory);
                metas.add(meta);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(connection, statement, resultSet);
        }
        return metas;
    }

    public static void insert(FileMeta localMeta) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            try {
                //1.获取数据库连接
                connection = DBUtil.getConnection();
                String sql = "insert into file_meta(name,path,is_directory,pinyyin,oinyin_first,size,last_modified) values(?,?,?,?,?,?,?)";
                //2.获取操作命令对象
                statement = connection.prepareStatement(sql);
                //填充占位符
                statement.setString(1,localMeta.getName());
                statement.setString(2,localMeta.getPath());
                statement.setBoolean(3,localMeta.getDirectory());
                String pinyin = null;
                String oinyin_first = null;
                if (Pinyin4jUtil.containsChinese(localMeta.getName())) {
                    String[] pinyins = Pinyin4jUtil.get(localMeta.getName());
                    pinyin = pinyins[0];
                    oinyin_first = pinyins[1];
                }
                statement.setString(4,pinyin);
                statement.setString(5,oinyin_first);
                statement.setLong(6,localMeta.getSize());
                statement.setTimestamp(7,new Timestamp(localMeta.getLastModified()));
                //3.执行sql语句
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(connection,statement);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void delete(FileMeta meta) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            //1.获取数据库连接
            connection = DBUtil.getConnection();
            connection.setAutoCommit(false);
            String sql = "DELETE FROM file_meta WHERE name = ? AND path = ? AND is_directory = ?";
            //2.获取操作命令对象
            statement = connection.prepareStatement(sql);
            //填充占位符
            statement.setString(1, meta.getName());
            statement.setString(2, meta.getPath());
            statement.setBoolean(3, meta.getDirectory());
            //3.执行sql语句
            statement.executeUpdate();
            //删除子文件/子文件夹
            if (meta.getDirectory()) {
                sql = "DELETE FROM file_meta WHERE path = ? or path like ?";
                statement = connection.prepareStatement(sql);
                String path = meta.getPath() + File.separator + meta.getName();
                statement.setString(1, path);
                statement.setString(2, path + File.separator + "%");
                statement.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            DBUtil.close(connection, statement);
        }
    }

    public static void main(String[] args) {
        //      System.out.println(query("F:\\360Downloads\\Software"));
        delete(new FileMeta("sxs","F:\\360Downloads\\Software\\漏洞补丁目录\\sxs",0L, 0L, true));
    }

    public static List<FileMeta> search(String dir, String text) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<FileMeta> metas = new ArrayList<>();
        try {
            connection = DBUtil.getConnection();
            boolean empty = dir == null || dir.trim().length() == 0;
            String sql = "select name,path,size,last_modified,is_directory from file_meta where name like ? or pinyyin like ? or oinyin_first LIKE ? "
             + (empty ? "":"and(path = ? or path like ?)");
            statement = connection.prepareStatement(sql);
            statement.setString(1, "%" + text + "%");
            statement.setString(2, "%" + text + "%");
            statement.setString(3, "%" + text + "%");
            if (!empty) {
                statement.setString(4, dir);
                statement.setString(5, dir + File.separator + "%");//分隔符
            }
            System.out.println("search path = " + dir + ", " + "text = " + text);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String path = resultSet.getString("path");
                long size = resultSet.getLong("size");
                long last_modified = resultSet.getLong("last_modified");
                boolean is_directory = resultSet.getBoolean("is_directory");
                FileMeta meta = new FileMeta(name,path,size,last_modified,is_directory);
                System.out.println("search: " + name + " , " + path);
                metas.add(meta);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(connection, statement, resultSet);
        }
        return metas;
    }
}
