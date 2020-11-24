package com.wxy.web.favorites.util;

import com.github.promeg.pinyinhelper.Pinyin;

/**
 * @Author wangxiaoyuan
 * @Date 2020/4/21 13:51
 * @Description
 **/
public class PinYinUtils {

    /**
     * 汉字转换为拼音
     *
     * @param chinese
     * @return
     */
    public static String toPinyin(String chinese) {
        StringBuilder sb = new StringBuilder();
        char[] chars = chinese.toCharArray();
        for (char c : chars) {
            String s = Pinyin.toPinyin(c);
            sb.append(s.toLowerCase());
        }
        return sb.toString();
    }

    /**
     * 汉子转拼音首字母
     *
     * @param chinese
     * @return
     */
    public static String toPinyinS(String chinese) {
        StringBuilder sb = new StringBuilder();
        char[] chars = chinese.toCharArray();
        for (char c : chars) {
            if (Pinyin.isChinese(c)) {
                String s = Pinyin.toPinyin(c);
                sb.append(s.substring(0, 1).toLowerCase());
            }
        }
        return sb.toString();
    }
}
