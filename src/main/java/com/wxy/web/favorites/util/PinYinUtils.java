package com.wxy.web.favorites.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

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
        StringBuilder pinyinStr = new StringBuilder();
        char[] newChar = chinese.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (char c : newChar) {
            try {
                String[] pinyinStringArray = PinyinHelper.toHanyuPinyinStringArray(c, defaultFormat);
                if (pinyinStringArray != null && pinyinStringArray.length >= 1) {
                    pinyinStr.append(pinyinStringArray[0]);
                } else {
                    pinyinStr.append(Character.toLowerCase(c));
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                e.printStackTrace();
            }
        }
        return pinyinStr.toString();
    }
}
