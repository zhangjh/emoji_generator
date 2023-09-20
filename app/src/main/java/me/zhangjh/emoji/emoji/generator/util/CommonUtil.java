package me.zhangjh.emoji.emoji.generator.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtil {

    public static Boolean isChinese(String val) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher matcher = p.matcher(val);
        return matcher.matches();
    }
}
