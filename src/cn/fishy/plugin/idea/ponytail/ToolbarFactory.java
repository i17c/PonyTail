package cn.fishy.plugin.idea.ponytail;

import cn.fishy.plugin.idea.ponytail.constant.LogViewerIcons;
import cn.fishy.plugin.idea.ponytail.domain.ViewLog;
import cn.fishy.plugin.idea.ponytail.ui.LogViewerLayout;
import cn.fishy.plugin.idea.ponytail.ui.LogViewerSetting;
import com.intellij.ide.actions.ShowFilePathAction;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsShowConfirmationOptionImpl;
import com.intellij.util.ui.ConfirmationDialog;

/**
 * User: duxing
 * Date: 2015-08-24 17:21
 */
public class ToolbarFactory {

    private LogViewerLayout logViewerLayout;
    private Project project;

    public ToolbarFactory(LogViewerLayout logViewerLayout) {
        this.logViewerLayout = logViewerLayout;
    }

    public ToolbarFactory(Project project) {
        this.project = project;
    }


    public ActionToolbar createToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new SettingsForLog());
        group.add(new DirOpen());
        group.add(new LogDel());
        group.add(new LogAdd());
        group.add(new FilterAction());
        group.add(new SkipTracker());
        group.add(new PauseTracker());
        group.add(new ShowLineNumbers());
        group.add(new ToggleSoftWraps());
        group.add(new ScrollToTheEnd());
        group.add(new Refresh());
        group.add(new ClearAll());
        return ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, group, false);
    }

    public ActionToolbar createToolbarForDefault() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new Settings());
        group.add(new LogAdd());
        return ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, group, false);
    }



    public class ToggleSoftWraps extends ToggleAction implements DumbAware {
        public ToggleSoftWraps() {
            super("Toggle Soft Wraps", "Toggle Soft Wraps", LogViewerIcons.toggleSoftWraps);
        }
        @Override
        public boolean isSelected(AnActionEvent anActionEvent) {
            try {
                return logViewerLayout.getEditor().getSettings().isUseSoftWraps();
            }catch (Exception e){
                return false;
            }
        }

        @Override
        public void setSelected(AnActionEvent anActionEvent, boolean isSelected) {
            try {
                logViewerLayout.getEditor().getSettings().setUseSoftWraps(isSelected);
            }catch (Exception e){
                //ignore errors
            }
        }
    }

    public class PauseTracker extends ToggleAction implements DumbAware {
        public PauseTracker() {
            super("Pause Tracker", "Pause To Print,Cancel Will Continue", LogViewerIcons.pause);
        }
        @Override
        public boolean isSelected(AnActionEvent anActionEvent) {
            try {
                return logViewerLayout.getConsole().getTrackTimer().isDisposed();
            }catch (Exception e){
                return false;
            }
        }

        @Override
        public void setSelected(AnActionEvent anActionEvent, boolean isSelected) {
            try {
                if(isSelected) {
                    logViewerLayout.getConsole().getTrackTimer().dispose();
                }else{
                    logViewerLayout.getConsole().getTrackTimer().restart();
                }
            }catch (Exception e){

            }
        }
    }

    public class SkipTracker extends ToggleAction implements DumbAware {
        public SkipTracker() {
            super("Skip Tracker", "Skip The Current Change To Print, Cancel Will Print From Cancel Moment", LogViewerIcons.skip);
        }
        @Override
        public boolean isSelected(AnActionEvent anActionEvent) {
            try {
                return logViewerLayout.getConsole().isSkipNow();
            }catch (Exception e){
                return false;
            }
        }

        @Override
        public void setSelected(AnActionEvent anActionEvent, boolean isSelected) {
            try {
                logViewerLayout.getConsole().setSkipNow(isSelected);
            }catch (Exception e){

            }
        }
    }


    private class ScrollToTheEnd extends ToggleAction implements DumbAware {
        public ScrollToTheEnd() {
            super("Scroll To The End", "Scroll To The End If Log Changes", LogViewerIcons.scrollToEnd);
        }

        @Override
        public boolean isSelected(AnActionEvent anActionEvent) {
            try {
                return logViewerLayout.getConsole().isAutoScrollDown();
            }catch (Exception e){
                return false;
            }
        }

        @Override
        public void setSelected(AnActionEvent anActionEvent, boolean b) {
            try {
                logViewerLayout.getConsole().setAutoScrollDown(b);
                if(b){
                    EditorUtil.scrollToTheEnd(logViewerLayout.getEditor());
                }
            }catch (Exception e){
                //ignore
            }
        }



    }

    public class LogAdd extends AnAction implements DumbAware {
        public LogAdd() {
            super("Add New Log", "Add New Log To Track", LogViewerIcons.add);
        }

        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {
            if(logViewerLayout!=null){
                project = logViewerLayout.getProject();
            }
            LogViewerSetting.pop(project);
        }
    }

    public class Settings extends AnAction implements DumbAware {
        public Settings() {
            super("Settings", "Default Settings For Plugin", LogViewerIcons.setting);
        }

        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {
            ShowSettingsUtil.getInstance().showSettingsDialog(project, cn.fishy.plugin.idea.ponytail.persistence.Settings.getInstance());
        }
    }

    public class SettingsForLog extends AnAction implements DumbAware {
        public SettingsForLog() {
            super("Settings", "Settings For Current Active Log", LogViewerIcons.setting2);
        }

        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {
            LogViewerSetting.pop(logViewerLayout);
        }
    }

    public class LogDel extends AnAction implements DumbAware {
        public LogDel() {
            super("Close Tab", "Close This Log Track Tab, And Stop Track", LogViewerIcons.del);
        }

        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {
            EnvManager envManager = EnvManager.getInstance(logViewerLayout.getProject());
            VcsShowConfirmationOptionImpl option = new VcsShowConfirmationOptionImpl("", "", "", "", "");
            boolean r = ConfirmationDialog.requestForConfirmation(option, logViewerLayout.getProject(), "Close Current Log Tracker?", "confirm", LogViewerIcons.confirm);
            if(r){
                envManager.contentManager.removeContent(logViewerLayout.getContent(),true);
            }
        }
    }


    public class Refresh extends AnAction implements DumbAware {
        public Refresh() {
            super("Refresh", "Refresh Current Log Print", LogViewerIcons.refresh);
        }

        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {
            logViewerLayout.getConsole().refresh(null);
        }
    }

    public class ClearAll extends AnAction implements DumbAware {
        public ClearAll() {
            super("Clear All", "Clear All Print", LogViewerIcons.clearAll);
        }

        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {
            clearAll();
        }
    }

    public void clearAll(){
        logViewerLayout.getEditor().getDocument().setText("");
    }

    public class ShowLineNumbers extends ToggleAction implements DumbAware {
        public ShowLineNumbers() {
            super("Show Line Numbers", "Toggle Show Line Numbers For Current Console", LogViewerIcons.showLineNumbers);
        }
        @Override
        public boolean isSelected(AnActionEvent anActionEvent) {
            try {
                return logViewerLayout.getEditor().getSettings().isLineNumbersShown();
            }catch (Exception e){
                return false;
            }
        }

        @Override
        public void setSelected(AnActionEvent anActionEvent, boolean isSelected) {
            try {
                logViewerLayout.getEditor().getSettings().setLineNumbersShown(isSelected);
            }catch (Exception e){

            }
        }
    }
    public class FilterAction extends ToggleAction implements DumbAware {

        public FilterAction() {
            super("Filter", "Toggle Filter Logs", LogViewerIcons.filter);
        }
        @Override
        public boolean isSelected(AnActionEvent anActionEvent) {
            return logViewerLayout.getPanelFilter().isVisible();
        }

        @Override
        public void setSelected(AnActionEvent anActionEvent, boolean isSelected) {
            if(!isSelected) {
                logViewerLayout.getViewLog().setFilter(null);
            }
            logViewerLayout.resetFilter();
            logViewerLayout.getPanelFilter().setVisible(isSelected);
        }

    }

    private class DirOpen  extends AnAction implements DumbAware {
        public DirOpen() {
            super("Show in Directory", "Open Directory See The Log", LogViewerIcons.open);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            ViewLog vl = logViewerLayout.getViewLog();
            if (vl.valid()) {
                ShowFilePathAction.openFile(vl.file());
            }
        }
    }
}
