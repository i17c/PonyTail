package cn.fishy.plugin.idea.ponytail;

import cn.fishy.plugin.idea.ponytail.domain.ViewLog;
import cn.fishy.plugin.idea.ponytail.persistence.Settings;
import cn.fishy.plugin.idea.ponytail.persistence.domain.SettingConfig;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.encoding.EncodingProjectManager;

import java.nio.charset.Charset;

/**
 * User: duxing
 * Date: 2015-08-31 10:02
 */
public class SettingManager {
    public static ViewLog getDefaultViewLog(){
        SettingConfig sc = Settings.getInstance().getSettingConfig();
        if(sc==null){
            sc = SettingConfig.getDefaultSettingConfig();
        }
        ViewLog vl = new ViewLog(sc.dir,sc.seek,sc.seekType,sc.seekPos);
        vl.setCharset(sc.charset);
        vl.setCycleBufferSize(sc.logBuffer);
        vl.setDelay(sc.delay);
        return vl;
    }

    public static int getDefaultOverflowNum(){
        SettingConfig sc = Settings.getInstance().getSettingConfig();
        if(sc==null){
            sc = SettingConfig.getDefaultSettingConfig();
        }
        return sc.overflowNum;
    }

    public static boolean isShowLineNumber(){
        SettingConfig sc = Settings.getInstance().getSettingConfig();
        if(sc==null){
            sc = SettingConfig.getDefaultSettingConfig();
        }
        return sc.showLineNumber;
    }

    public static boolean isSoftWrap(){
        SettingConfig sc = Settings.getInstance().getSettingConfig();
        if(sc==null){
            sc = SettingConfig.getDefaultSettingConfig();
        }
        return sc.softWrap;
    }

    public static long getDefaultOverflowFileLength() {
        return 0x20000000;
    }

    public static Charset getProjectCharset(Project project){
        try {
            return EncodingProjectManager.getInstance(project).getDefaultCharset();
        }catch (Exception e){
            try{
                return project.getBaseDir().getCharset();
            }catch (Exception e1){
                return Charset.forName("UTF-8");
            }
        }
    }
}
