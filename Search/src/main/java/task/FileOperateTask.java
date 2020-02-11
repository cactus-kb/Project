package task;

import Util.DBUtil;
import Util.Pinyin4jUtil;
import app.FileMeta;
import dao.FileOperatorDAO;
import task.FileScanCallback;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class FileOperateTask implements FileScanCallback {
    @Override
    public void execute(File dir) {
        //文件比对
        if (dir.isDirectory()) {//TODO 是否考虑文件的数据库操作
            //本地文件
            File[] children = dir.listFiles();
            //包装为本地的自定义文件类集合
            List<FileMeta> localMetas = compose(children);
            //数据库文件:jdbc查询实现（根据路径查询路径下的所有文件夹/文件）
            List<FileMeta> metas = FileOperatorDAO.query(dir.getPath());

            //数据库有，本地没有的文件
            for (FileMeta meta : metas) {
                if (!localMetas.contains(meta)) {
                    //如果meta是文件夹，还要删除子文件/子文件夹
                    FileOperatorDAO.delete(meta);
                }
            }

            //本地有，数据库没有的文件
            for (FileMeta localMeta : localMetas) {
                //需要FileMeta实现hashCode()和equals()方法
                if (!metas.contains(localMeta)) {
                    FileOperatorDAO.insert(localMeta);
                }
            }
        }
    }

    //File数组转Filemeta
    private List<FileMeta> compose(File[] children) {
        List<FileMeta> metas = new ArrayList<>();
        if (children != null) {
            for (File child : children) {
//                metas.add(new FileMeta(child.getName(), child.getPath(), child.length(), child.lastModified(), child.isDirectory()));
                metas.add(new FileMeta(child));
            }
        }
        return metas;
    }
}
