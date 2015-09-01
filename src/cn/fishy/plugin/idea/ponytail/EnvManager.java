package cn.fishy.plugin.idea.ponytail;

import cn.fishy.plugin.idea.ponytail.domain.ViewLog;
import cn.fishy.plugin.idea.ponytail.persistence.ConfigHolder;
import cn.fishy.plugin.idea.ponytail.process.TrackTimer;
import cn.fishy.plugin.idea.ponytail.util.InfoManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.content.ContentManagerEvent;
import com.intellij.ui.content.ContentManagerListener;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * User: duxing
 * Date: 2015-08-22 17:59
 */
public class EnvManager {
    public ToolWindow toolWindow;
    public ContentManager contentManager;
    public Map<String,Content> contentMap = new HashMap<String,Content>();
    public Map<String,Boolean> monitorRunnableMap = new HashMap<String, Boolean>();
    public Map<String, TrackTimer> timerMap = new HashMap<String, TrackTimer>();

    public static EnvManager getInstance(Project project){
        return ServiceManager.getService(project,EnvManager.class);
    }

    public static String getSelectedContentTabName(Project project){
        try {
            return getInstance(project).contentManager.getSelectedContent().getTabName();
        }catch (Exception e){
            try {
                return ConfigHolder.getConfigHolder(project).lastContent;
            }catch (Exception e1){
                return "default";
            }
        }
    }

    public void init(Project p, ToolWindow t) {
        this.toolWindow = t;
        this.contentManager = t.getContentManager();
    }

    public static void addContent(Project p, Content content) {
        addContent(p,content,true);
    }
    public static void addContent(Project p, Content content, boolean needWithFocus) {
        try {
            prepareRunTab(p, content.getTabName());
        }catch (Exception e){
            InfoManager.error(p, "prepare run error, please contact duxing@taobao.com"+e.getCause());
        }
        try {
            getContentMap(p).put(content.getTabName(), content);
        }catch (Exception e){
            InfoManager.error(p, "add Content Map error, please contact duxing@taobao.com"+e.getCause());
        }
        try {
            getContentManager(p).addContent(content);
        }catch (Exception e){
            InfoManager.error(p, "addContent error, please contact duxing@taobao.com"+e.getCause());
        }
        try {
            if(needWithFocus) {
                EnvManager.setSelectedContent(p, content);
            }
        }catch (Exception e){
            InfoManager.error(p, "env persistence manager lost, please contact duxing@taobao.com"+e.getCause());
        }
    }

    //补偿机制
    public static void checkTabs(Console console, String name) {
        Project p = console.getProject();
        EnvManager env = getInstance(p);
        ViewLog vl = console.getViewLog();
        try{
            Boolean canRun = env.monitorRunnableMap.get(name);
            TrackTimer tt = env.timerMap.get(name);
            Content c = env.contentMap.get(name);
            if(canRun && tt!=null && (c==null || !env.tabShown(name))){
                ToolWindowFactory.getInstance(p).CreateTab(p, vl, true);
            }
        }catch(Exception e){

        }
    }

    private boolean tabShown(String name) {
        if(contentManager!=null){
            Content[] contents = contentManager.getContents();
            if(contents.length>0){
                for(Content t : contents){
                    if(t.getTabName().equals(name)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static Map<String, Content> getContentMap(Project p) {
        return getInstance(p).contentMap;
    }

    private static ContentManager getContentManager(Project project) {
        return getInstance(project).contentManager;
    }

    public static void setSelectedContent(Project p, String tabName) {
        if(StringUtils.isNotBlank(tabName)&&getContentManager(p)!=null&&getContentManager(p).getContents().length>0){
            for(Content c:getContentManager(p).getContents()){
                if(tabName.equals(c.getTabName())){
                    EnvManager.setSelectedContent(p, c);
                    break;
                }
            }
        }

    }
    public static void setSelectedContent(Project p, Content content) {
        if(content!=null){
            try {
                getContentManager(p).setSelectedContent(content, true);
            }catch (Exception e){
                //ignore errors
            }
        }
    }

    public static void setSelectedContent(Project p,ViewLog viewLog) {
        if(viewLog!=null){
            Content c = getContentMap(p).get(viewLog.getKey());
            if(c!=null){
                EnvManager.setSelectedContent(p, c);
            }
        }
    }

    public static void contentManagerInitListener(final Project p) {
        getContentManager(p).addContentManagerListener(new ContentManagerListener() {
            @Override
            public void contentAdded(ContentManagerEvent contentManagerEvent) {
//                InfoManager.info(p, "content added " + contentManagerEvent.getContent().getTabName());
            }

            @Override
            public void contentRemoved(final ContentManagerEvent contentManagerEvent) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        String tabKey = contentManagerEvent.getContent().getTabName();
                        remove(p, tabKey);
                    }
                });

//                InfoManager.info(p, "content removed " + contentManagerEvent.getContent().getTabName());
            }

            @Override
            public void contentRemoveQuery(ContentManagerEvent contentManagerEvent) {
//                InfoManager.info(p, "content remove query " + contentManagerEvent.getContent().getTabName());
            }

            @Override
            public void selectionChanged(ContentManagerEvent contentManagerEvent) {
                String k = contentManagerEvent.getContent().getTabName();
                if (contentManagerEvent.getOperation().equals(ContentManagerEvent.ContentOperation.remove)) {
//                    InfoManager.info(p, "focus lost " + k);
                }
                if (contentManagerEvent.getOperation().equals(ContentManagerEvent.ContentOperation.add)) {
                    ConfigHolder.setLastContentStatic(p, k);
//                    InfoManager.info(p, "focus granted " + k);
                }
            }
        });
    }

    private static void remove(Project p,String tabKey) {
        //运行map中去除, 终止运行
        getMonitorRunnableMap(p).remove(tabKey);
        //去除Timer
        removeTimer(p, tabKey);
        //tab中去除, 交互不可见
        getContentMap(p).remove(tabKey);
        //持久化的map中去除
        ConfigHolder.removeStatic(p, tabKey);
    }

    public static void removeTimer(Project p, String tabKey) {
        stopTimer(p, tabKey);
    }

    private static Map<String, Boolean> getMonitorRunnableMap(Project p) {
        return getInstance(p).monitorRunnableMap;
    }

    private static void stopTimer(Project p,String tabKey) {
        Map<String, TrackTimer> map = getTimerMap(p);
        try {
            TrackTimer tt = map.get(tabKey);
            if (tt != null) {
                tt.dispose();
            }
            map.remove(tabKey);
        }catch (Exception e){}
    }

    private static void pauseTimer(Project p,String tabKey) {
        TrackTimer tt = getTimer(p, tabKey);
        if(tt!=null){
            tt.resume();
        }
    }

    public static Content getContent(Project p,String key) {
        return getContentMap(p).get(key);
    }

    /**
     * 异常逻辑的兼容性
     */
    public static void checkPersistence(Project p) {
        LinkedHashMap<String, ViewLog> logMap = ConfigHolder.getLogMapStatic(p);
        if(logMap!=null && logMap.size()>0) {
            List<String> keyList = new ArrayList<String>();
            if (getContentManager(p) != null && getContentManager(p).getContents().length > 0) {
                for (Content c : getContentManager(p).getContents()) {
                    if (c != null && c.getTabName() != null) {
                        keyList.add(c.getTabName());
                    }
                }
            }
            if (keyList.size() > 0) {
                for(String k:logMap.keySet()){
                    if(!keyList.contains(k)){
                        logMap.remove(k);
                    }
                }
            } else {
                logMap.clear();
            }
        }
    }

    public static void prepareRunTab(Project p,String key) {
        getMonitorRunnableMap(p).put(key, true);
    }
    public static boolean canRun(Project p,String key) {
        return getMonitorRunnableMap(p).get(key)!=null && getMonitorRunnableMap(p).get(key);
    }
    public static void addTimer(Project p, String key, TrackTimer trackTimer) {
        getTimerMap(p).put(key, trackTimer);
    }

    private static  Map<String, TrackTimer> getTimerMap(Project p) {
        return getInstance(p).timerMap;
    }

    public static TrackTimer getTimer(Project p, String key) {
        return getTimerMap(p).get(key);
    }

    public static void shutdown(Project p) {
//        LogUtil.log("shutdown processing");
        /*Map<String, TrackTimer> timerMap = getTimerMap(p);
        if(timerMap!=null && timerMap.size()>0){
            for(String key:timerMap.keySet()){
                TrackTimer tt = timerMap.get(key);
                if(tt!=null && !tt.isDisposed()){
                    tt.dispose();
                }
            }
        }*/
    }

}
