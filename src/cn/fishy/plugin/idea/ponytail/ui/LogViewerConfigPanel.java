package cn.fishy.plugin.idea.ponytail.ui;

import cn.fishy.plugin.idea.ponytail.constant.CharsetType;
import cn.fishy.plugin.idea.ponytail.constant.SeekType;
import cn.fishy.plugin.idea.ponytail.constant.SizeType;
import cn.fishy.plugin.idea.ponytail.persistence.Settings;
import cn.fishy.plugin.idea.ponytail.persistence.domain.SettingConfig;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;

/**
 * User: duxing
 * Date: 2015-08-30 19:14
 */
public class LogViewerConfigPanel {
    private JTabbedPane basePanel;
    private JTextField TEXT_dir;
    private JButton BTN_chooseDir;
    private JComboBox SELECT_logBuffer;
    private JComboBox SELECT_delay;
    private JCheckBox CHK_seek;
    private JComboBox SELECT_seekType;
    private JTextField TEXT_seekPos;

    private JLabel LABEL_seekType;
    private JLabel LABEL_seekPos;
    private JLabel LABEL_seek;
    private JComboBox SELECT_charset;
    private JTextField TEXT_limit;
    private JCheckBox CHK_showLineNumber;
    private JCheckBox CHK_softWrap;

    private SettingConfig settingConfig;
    private Timer t;


    public LogViewerConfigPanel(SettingConfig settingConfig) {
        this.settingConfig = settingConfig;
    }

    private void bindListeners() {
        ActionListener actionSeek = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkSeek();
            }
        };
        CHK_seek.addActionListener(actionSeek);
        ActionListener generateSeek = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateSeekInfo();
            }
        };
        SELECT_seekType.addActionListener(generateSeek);
        TEXT_seekPos.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent focusEvent) {
                generateSeekInfo();
                if (t == null) {
                    t = new Timer(500, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent actionEvent) {
                            generateSeekInfo();
                        }
                    });
                    t.start();
                } else {
                    t.restart();
                }
            }

            @Override
            public void focusLost(FocusEvent focusEvent) {
                if (t.isRunning()) {
                    t.stop();
                }
                generateSeekInfo();
            }
        });
        BTN_chooseDir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileChooserDescriptor chooseFolderOnlyDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
                chooseFolderOnlyDescriptor.setTitle("Select Path");
                chooseFolderOnlyDescriptor.setDescription("Select Path To Focus With");
                VirtualFile old = null;
                String pathStr = TEXT_dir.getText();
                if (StringUtils.isNotBlank(pathStr)) {
                    old = LocalFileSystem.getInstance().findFileByIoFile(new File(TEXT_dir.getText()));
                }
                VirtualFile file = FileChooser.chooseFile(chooseFolderOnlyDescriptor, null, old);
                if (file != null) {
                    TEXT_dir.setText(file.getPath());
//                    System.out.println(file.getPath());
                }
            }
        });

    }

    private void generateSeekInfo() {
        String seekTypeStr = (String)SELECT_seekType.getSelectedItem();
        SeekType seekType = SeekType.get(seekTypeStr);
        long pos = 0;
        try{
            pos = Long.parseLong(TEXT_seekPos.getText());
        }catch (Exception e){}
        String info = "Print From " + (seekType.isSeekHead() ? "HEAD" : "TAIL") + " by " + (seekType.isSeekLine() ? "Lines" : "Pos") + " at "+ (seekType.isSeekLine() ? "Line" : "Pos") + " " + pos;
        LABEL_seek.setText(info);
    }

    public JTabbedPane getBasePanel(){
        return basePanel;
    }

    public void loadData(SettingConfig settingConfig) {
        TEXT_dir.setText(settingConfig.dir);
        try {
            SELECT_logBuffer.setSelectedItem(SizeType.get(settingConfig.logBuffer).getName());
        }catch (Exception e){}
        try {
            SELECT_delay.setSelectedItem(settingConfig.delay + "");
        }catch (Exception e){}
        CHK_seek.setSelected(settingConfig.seek);
        CHK_showLineNumber.setSelected(settingConfig.showLineNumber);
        CHK_softWrap.setSelected(settingConfig.softWrap);
        TEXT_seekPos.setText(settingConfig.seekPos+"");
        try {
            SELECT_seekType.setSelectedItem(settingConfig.seekType.name());
        }catch (Exception e){}
        try {
            SELECT_charset.setSelectedItem(settingConfig.charset.getName());
        }catch (Exception e){}
        TEXT_limit.setText(settingConfig.overflowNum+"");
        checkSeek();
    }

    private void checkSeek() {
        boolean seek = CHK_seek.isSelected();
        SELECT_seekType.setVisible(seek);
        TEXT_seekPos.setVisible(seek);
        LABEL_seekPos.setVisible(seek);
        LABEL_seekType.setVisible(seek);
        LABEL_seek.setVisible(seek);
    }

    public SettingConfig getSettingConfig() {
        return generateSetting();
    }

    private SettingConfig generateSetting() {
        SettingConfig s = new SettingConfig();
        s.dir = TEXT_dir.getText();
        s.logBuffer = SizeType.get((String) SELECT_logBuffer.getSelectedItem()).getSize();
        try {
            s.delay = Integer.parseInt((String) SELECT_delay.getSelectedItem());
            if(s.delay<100)s.delay=100;
        }catch (Exception e){}
        s.seek = CHK_seek.isSelected();
        if(s.seek){
            try {
                s.seekType = SeekType.get((String) SELECT_seekType.getSelectedItem());
            }catch (Exception e){}
            try {
                s.seekPos = Long.parseLong(TEXT_seekPos.getText());
            }catch (Exception e){}
        }
        s.charset = CharsetType.get((String) SELECT_charset.getSelectedItem());
        try{
            s.overflowNum = Integer.parseInt(TEXT_limit.getText());
        }catch (Exception e){}
        s.showLineNumber = CHK_showLineNumber.isSelected();
        s.softWrap = CHK_softWrap.isSelected();
        return s;
    }

    public boolean isModified(Settings settings) {
        SettingConfig configSaved = settings.getSettingConfig();
        return (!TEXT_dir.getText().equals(configSaved.dir) && configSaved.dir!=null) || (configSaved.dir==null && !TEXT_dir.getText().equals(""))
                || !(SizeType.get((String) SELECT_logBuffer.getSelectedItem()).getSize()==configSaved.logBuffer)
                || !(SELECT_delay.getSelectedItem()).equals(configSaved.delay+"")
                || !(SELECT_charset.getSelectedItem()).equals(configSaved.charset.getName()+"")
                || !(CHK_seek.isSelected() == configSaved.seek)
                || !(CHK_showLineNumber.isSelected() == configSaved.showLineNumber)
                || !(CHK_softWrap.isSelected() == configSaved.softWrap)
                || !(SELECT_seekType.getSelectedItem().equals(configSaved.seekType.name()))
                || !(TEXT_seekPos.getText().equals(configSaved.seekPos + ""))
                || !(TEXT_limit.getText().equals(configSaved.overflowNum + ""))
                ;
    }

    public void init() {
        SELECT_logBuffer.removeAllItems();
        for(SizeType st:SizeType.values()){
            SELECT_logBuffer.addItem(st.getName());
        }
        SELECT_charset.removeAllItems();
        for(CharsetType ct:CharsetType.values()){
            SELECT_charset.addItem(ct.getName());
        }
        loadData(this.settingConfig);
        bindListeners();
        checkSeek();
        generateSeekInfo();
    }
}
