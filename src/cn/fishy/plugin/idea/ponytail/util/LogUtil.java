package cn.fishy.plugin.idea.ponytail.util;

/**
 * User: duxing
 * Date: 2015-08-30 11:48
 */
public class LogUtil {
    private static boolean debug = false;

    public static void log(Object s){
        System.out.println(s.toString());
    }

    public static void setDebug(boolean debug) {
        LogUtil.debug = debug;
    }
}
