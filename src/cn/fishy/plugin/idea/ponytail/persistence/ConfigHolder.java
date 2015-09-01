package cn.fishy.plugin.idea.ponytail.persistence;

import cn.fishy.plugin.idea.ponytail.DefaultContentFactory;
import cn.fishy.plugin.idea.ponytail.SettingManager;
import cn.fishy.plugin.idea.ponytail.domain.LogCtrlResult;
import cn.fishy.plugin.idea.ponytail.domain.ViewLog;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * User: duxing
 * Date: 2015.8.25
 */

@com.intellij.openapi.components.State(
        name = "LogViewerConfig",
        storages = {
                @Storage(id = "default", file = StoragePathMacros.PROJECT_FILE),
                @Storage(id = "dir", file = StoragePathMacros.PROJECT_CONFIG_DIR + "/logViewer.xml", scheme = StorageScheme.DIRECTORY_BASED)
        }
)
public class ConfigHolder implements PersistentStateComponent<ConfigHolder>, Serializable {

    private static final long serialVersionUID = -6306596618421883024L;
    public LinkedHashMap<String,ViewLog> logMap = new LinkedHashMap<String,ViewLog>();
    public String lastContent = DefaultContentFactory.defaultTabKey;

    @Nullable
    @Override
    public ConfigHolder getState() {
        return this;
    }

    @Override
    public void loadState(ConfigHolder state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public static LinkedHashMap<String,ViewLog> getLogMapStatic(Project project) {
        ConfigHolder holder = getConfigHolder(project);
        if(holder!=null) {
            return holder.logMap;
        }else{
            return null;
        }
    }

    public static LogCtrlResult addStatic(Project project, ViewLog v) {
        ConfigHolder holder = getConfigHolder(project);
        LogCtrlResult r = new LogCtrlResult();
        if(holder!=null) {
            ViewLog e = holder.logMap.get(v.getKey());
            if (e == null) {
                holder.logMap.put(v.getKey(), v);
                r.setLog(v);
                r.setSuccess(true);
                return r;
            } else {
                if (e.equals(v)) {
                    r.setLog(e);
//                    Env.contentManager.getContents();
                    return r;
                } else {
                    v.plusKey();
                    return addStatic(project, v);
                }
            }
        }
        return r;
    }

    public static void delStatic(Project project, String key) {
        ConfigHolder holder = getConfigHolder(project);
        if(holder!=null) {
            holder.logMap.remove(key);
        }
    }

    public static ViewLog getLastLogByKeyStatic(Project project) {
        try {
            ConfigHolder holder = getConfigHolder(project);
            if(holder!=null){
                return holder.logMap.get(holder.lastContent);
            }
        }catch (Exception e){
            //ignore
        }
        return null;
    }


    public static void setLastContentStatic(Project project, String lastContent) {
        ConfigHolder holder = getConfigHolder(project);
        if(holder!=null) {
            holder.lastContent = lastContent;
        }
    }

    public static ViewLog getViewLogByKeyStatic(Project project, String key) {
        ConfigHolder holder = getConfigHolder(project);
        if(holder!=null) {
            return holder.logMap.get(key);
        }
        return null;
    }

    public LinkedHashMap<String,ViewLog> getLogMap() {
        return this.logMap;
    }

    public LogCtrlResult add(ViewLog v) {
        LogCtrlResult r = new LogCtrlResult();
        ViewLog e = this.logMap.get(v.getKey());
        if (e == null) {
            this.logMap.put(v.getKey(), v);
            r.setLog(v);
            r.setSuccess(true);
            return r;
        } else {
            if (e.equals(v)) {
                r.setLog(e);
                return r;
            } else {
                v.plusKey();
                return add(v);
            }
        }
    }

    public void del(String key) {
        this.logMap.remove(key);
    }


    @Nullable
    public static ConfigHolder getConfigHolder(Project project) {
        if(project==null)return null;
        try {
            return ServiceManager.getService(project, ConfigHolder.class).getState();
        }catch (Exception e){
            return null;
        }
    }

    public static void removeStatic(Project project, String tabKey) {
        LinkedHashMap<String, ViewLog> map = getLogMapStatic(project);
        if(map!=null){
            map.remove(tabKey);
        }
    }

    public static boolean logViewerNumOverflow(Project project) {
        ConfigHolder holder = getConfigHolder(project);
        return holder != null && holder.logMap.size() > SettingManager.getDefaultOverflowNum();
    }
}
