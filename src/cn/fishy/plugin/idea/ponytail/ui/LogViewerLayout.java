package cn.fishy.plugin.idea.ponytail.ui;

import cn.fishy.plugin.idea.ponytail.Console;
import cn.fishy.plugin.idea.ponytail.domain.ViewLog;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.ui.content.Content;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * User: duxing
 * Date: 2015-08-24 16:51
 */
public class LogViewerLayout {
    private JPanel panelContainer;
    private JButton BTN_OK;
    private JToolBar TOOLBAR;
    private JPanel panelFilterContainer;
    private JTextField TEXT_filter;
    private JPanel panelConsoleContainer;
    private Content content;
    private ViewLog viewLog;
    private Editor editor;
    private Project project;
    private Console console;

    public LogViewerLayout() {
    }

    public LogViewerLayout(Project p,ViewLog viewLog) {
        this.project = p;
        this.viewLog = viewLog;
        BTN_OK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sureForFilter();
            }
        });
        TEXT_filter.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sureForFilter();
                }
            }
        });
        panelFilterContainer.setVisible(viewLog.getFilter()!=null && !viewLog.getFilter().equals(""));
    }

    private void sureForFilter() {
        String filter = TEXT_filter.getText();
        if(!filter.equals("")){
            console.getViewLog().setFilter(filter);
        }else{
            console.getViewLog().setFilter(null);
            panelFilterContainer.setVisible(false);
        }
        console.refresh(null);
    }

    public void resetFilter(){
        TEXT_filter.setText(viewLog.getFilter());
    }

    public Editor getEditor() {
        return editor;
    }

    public void setEditor(Editor editor) {
        this.editor = editor;
    }

    public JToolBar getToolbar() {
        return TOOLBAR;
    }

    public JButton getFilterBtn() {
        return BTN_OK;
    }

    public JPanel getPanel() {
        return panelContainer;
    }

    public JPanel getPanelConsole() {
        return panelConsoleContainer;
    }
    public JPanel getPanelFilter() {
        return panelFilterContainer;
    }

    public static LogViewerLayout getInstance(){
        return ServiceManager.getService(LogViewerLayout.class);
    }

    public void setContent(Content contentDefault) {
        content = contentDefault;
    }

    public Content getContent() {
        return content;
    }

    public ViewLog getViewLog() {
        return viewLog;
    }

    public void setViewLog(ViewLog viewLog) {
        this.viewLog = viewLog;
    }

    public Console getConsole() {
        return console;
    }

    public void setConsole(Console console) {
        if(console !=null) {
            this.console = console;
            this.editor = console.getConsoleEditor();
            this.panelConsoleContainer.add(this.editor.getComponent(), BorderLayout.CENTER);

        }
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
