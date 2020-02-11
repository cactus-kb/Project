package Util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Pinyin4jUtil {

    //中文字符格式
    private static final String CHINESE_PATTERN = "[\\u4E00-\\u9FA5]";

    //拼音输出格式
    private static final HanyuPinyinOutputFormat FORMAT = new HanyuPinyinOutputFormat();

    static {
        //拼音小写
        FORMAT.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        //不带音调
        FORMAT.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        //包含字符v
        FORMAT.setVCharType(HanyuPinyinVCharType.WITH_V);
    }

    //判断字符串中是否包含中文-->匹配中文字符格式
    public static boolean containsChinese(String str) {
        //.代表任意匹配；*代表0到多个匹配
        return str.matches(".*" + CHINESE_PATTERN + ".*");//正则表达式
    }


    //取每个汉字拼音的第一个字符串的首字母，拼接返回
    public static String[] get(String hanyu) {
        String[] array = new String[2];
        //全拼
        StringBuilder pinyin= new StringBuilder();
        //拼音首字母
        StringBuilder pinyin_first = new StringBuilder();
        for (int i = 0; i < hanyu.length(); i++) {
            try {
                String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(hanyu.charAt(i));
                //中文字符返回的是字符串数组，可能为null或长度为0
                //返回原始的字符
                if (pinyins == null || pinyins.length == 0) {
                    pinyin.append(hanyu.charAt(i));
                    pinyin_first.append(hanyu.charAt(i));
                } else {//可以转换为拼音，只取第一个第一个字符串作为拼音
                    pinyin.append(pinyins[0]);
                    pinyin_first.append(pinyins[0].charAt(0));
                }
            } catch (Exception e) {//出现异常，返回原始的字符
                pinyin.append(hanyu.charAt(i));
                pinyin_first.append(hanyu.charAt(i));
            }
        }
        array[0] = pinyin.toString();
        array[1] = pinyin_first.toString();
        return array;
    }


    //返回字符串所有的拼音
    //hanyu 字符串
    //fullSpell  是否为全拼
    public static String[][] get(String hanyu, boolean fullSpell) {
        String[][] array = new String[hanyu.length()][];
        for (int i = 0; i < hanyu.length(); i++) {
            try {
                String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(hanyu.charAt(i), FORMAT);
                if (pinyins == null || pinyins.length == 0) { //a -> {"a"}
                    //返回二维数组中的某一行
                    array[i] = new String[]{String.valueOf(hanyu.charAt(i))};
                } else {//去重
                    array[i] = unique(pinyins, fullSpell);
                }
            } catch (Exception e) {
                array[i] = new String[] {String.valueOf(hanyu.charAt(i))};
            }
        }
        return array;
    }

    //拼音字符串数组去重操作：返回去重后的数组
    //fullSpell true为全拼，false取首字母
    private static String[] unique(String[] pinyins, boolean fullSpell) {
        Set<String> set = new HashSet<>();
       for (int i = 0; i < pinyins.length; i++) {
           if (fullSpell) {
               set.add(pinyins[i]);
           } else {
               set.add(String.valueOf(pinyins[i].charAt(0)));
           }
       }
       return set.toArray(new String[set.size()]);//set转为数组
    }

    public static void main(String[] args) {
//        String[] pinyins = get("你好");
//        System.out.println(Arrays.toString(pinyins));
    }
}
