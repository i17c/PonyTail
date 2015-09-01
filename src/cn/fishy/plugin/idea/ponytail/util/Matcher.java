package cn.fishy.plugin.idea.ponytail.util;

import java.util.regex.Pattern;

/**
 * 内容匹配类
 * User: duxing
 * Date: 2015.07.26 23:38
 */
public class Matcher {

    public static boolean match(String regString, String content){
        java.util.regex.Matcher mr = Pattern.compile(regString,Pattern.DOTALL+Pattern.CASE_INSENSITIVE).matcher(content);
        return mr.find();
    }

    public static void main(String[] args) throws Exception {
        String s = "te2023321 Desktop EnvManager.get";
        System.out.println(match("Desk",s));
    }

}
