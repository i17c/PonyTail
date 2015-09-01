package cn.fishy.plugin.idea.ponytail;

import cn.fishy.plugin.idea.ponytail.domain.ViewLog;
import cn.fishy.plugin.idea.ponytail.persistence.ConfigHolder;
import cn.fishy.plugin.idea.ponytail.process.TrackTimer;
import cn.fishy.plugin.idea.ponytail.ui.LogViewerLayout;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ShutDownTracker;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;

/**
 * User: duxing
 * Date: 2015-08-24 17:14
 */
public class ToolWindowFactory implements com.intellij.openapi.wm.ToolWindowFactory, Disposable {
    private Project project;
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        this.project = project;
        EnvManager.getInstance(project).init(project, toolWindow);
        /**
         * 此处可以监控tab的关闭显示和隐藏, 隐藏后可以优化一些逻辑处理 //TODO
         */
        /*toolWindow.getComponent().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                System.out.println(evt.getPropertyName() + " - " + evt.getOldValue() +" -> " + evt.getNewValue());
            }
        });*/
        EnvManager.addContent(project, (new DefaultContentFactory(project)).getContentDefault());
        EnvManager.contentManagerInitListener(project);
        CreateSavedTabs(project);
//        registerShutdownTask();
    }

    private void registerShutdownTask() {
        ShutDownTracker.getInstance().registerShutdownTask(new Runnable() {
            @Override
            public void run() {
                EnvManager.shutdown(project);
            }
        });
    }

    private void CreateSavedTabs(Project project) {
        ConfigHolder holder = ConfigHolder.getConfigHolder(project);
        if(holder!=null) {
            LinkedHashMap<String, ViewLog> logMap = holder.getLogMap();
            if (logMap != null && logMap.size() > 0) {
                for (String title : logMap.keySet()) {
                    ViewLog vl = logMap.get(title);
                    CreateTab(project,vl, false);
                }
            }
            EnvManager.setSelectedContent(project, holder.lastContent);
        }
    }

    public void CreateTab(Project project, ViewLog vl, boolean needWithFocus) {
        LogViewerLayout logViewerLayout = new LogViewerLayout(project,vl);
        JPanel panel = logViewerLayout.getPanel();
        panel.add(new ToolbarFactory(logViewerLayout).createToolbar().getComponent(), BorderLayout.WEST);
        final Console newConsole = new Console(project,vl);

        logViewerLayout.setConsole(newConsole);
        Content content = ContentFactory.SERVICE.getInstance().createContent(panel, vl.getKey(), true);
        logViewerLayout.setContent(content);
        if(vl.getFilter()!=null && !vl.getFilter().equals("")){
            logViewerLayout.resetFilter();
            logViewerLayout.getPanelFilter().setVisible(true);
        }
        EnvManager.addContent(project, content, needWithFocus);
        TrackTimer trackTimer = new TrackTimer(newConsole,vl.getKey(),vl.getDelay());
        trackTimer.start();
        newConsole.setTrackTimer(trackTimer);
        EnvManager.addTimer(project, vl.getKey(), trackTimer);
    }

    @Override
    public void dispose() {
    }

    public static ToolWindowFactory getInstance(Project project){
        return ServiceManager.getService(project, ToolWindowFactory.class);
    }

    @Nullable
    public static ToolWindow getLogViewer(Project project) {
        return project == null ? null : ToolWindowManager.getInstance(project).getToolWindow("PonyTail");
    }


}
