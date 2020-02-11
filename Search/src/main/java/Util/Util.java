package Util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {
    public static final String DATA_SOURCE = "yyy-MM-dd HH:mm:ss";

    private static final String[] SIZENAME  = {"B", "KB", "MB", "GB"};

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat(DATA_SOURCE);

    //文件大小带kb/mb...
    public static String parseSize(Long size) {
        int n = 0;
        while (size >= 1024) {
            size = size / 1024;
            n++;
        }
        return size + SIZENAME[n];
    }

    //日期解析
    public static String parseData(Long lastModified) {
        return DATE_FORMAT.format(new Date(lastModified));
    }
}
