package cn.fishy.plugin.idea.ponytail.ui;

import cn.fishy.plugin.idea.ponytail.EnvManager;
import cn.fishy.plugin.idea.ponytail.util.InfoManager;
import cn.fishy.plugin.idea.ponytail.ToolbarFactory;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * User: duxing
 * Date: 2015-08-25 22:38
 */
public class LogViewerDefaultLayout {
    private JPanel panel;
    private JButton btn;

    public LogViewerDefaultLayout(final Project project) {
        panel.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                InfoManager.info(null, "focus default focus granted");
            }

            @Override
            public void focusLost(FocusEvent e) {
                InfoManager.info(null,"focus default focus lost");
            }
        });

        panel.add(new ToolbarFactory(project).createToolbarForDefault().getComponent(), BorderLayout.WEST);
        final EditorColorsScheme colorScheme = EditorColorsManager.getInstance().getGlobalScheme();
        final Color bgColor = colorScheme.getDefaultBackground();
        final Color fgColor = colorScheme.getDefaultForeground();

        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EnvManager.checkPersistence(project);
                LogViewerSetting.pop(project);
            }
        });
//        final Color bgColor = btn.getBackground();
//        final Color fgColor = btn.getForeground();

        btn.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                btn.setForeground(bgColor);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                btn.setForeground(JBColor.RED);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                btn.setForeground(bgColor);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(JBColor.GRAY);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
                btn.setForeground(fgColor);
            }
        });
    }

    public JPanel getPanel() {
        return panel;
    }

    public JButton getBtn() {
        return btn;
    }

}
