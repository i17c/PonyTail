package cn.fishy.plugin.idea.ponytail.ui;

import cn.fishy.plugin.idea.ponytail.Console;
import cn.fishy.plugin.idea.ponytail.EnvManager;
import cn.fishy.plugin.idea.ponytail.SettingManager;
import cn.fishy.plugin.idea.ponytail.constant.CharsetType;
import cn.fishy.plugin.idea.ponytail.constant.LogViewerIcons;
import cn.fishy.plugin.idea.ponytail.constant.SizeType;
import cn.fishy.plugin.idea.ponytail.process.LogReader;
import cn.fishy.plugin.idea.ponytail.util.InfoManager;
import cn.fishy.plugin.idea.ponytail.ToolWindowFactory;
import cn.fishy.plugin.idea.ponytail.constant.SeekType;
import cn.fishy.plugin.idea.ponytail.domain.LogCtrlResult;
import cn.fishy.plugin.idea.ponytail.domain.ViewLog;
import cn.fishy.plugin.idea.ponytail.persistence.ConfigHolder;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vcs.VcsShowConfirmationOptionImpl;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.refactoring.ui.InfoDialog;
import com.intellij.util.ui.ConfirmationDialog;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class LogViewerSetting extends JDialog {
    private static final long serialVersionUID = -6350095993951244600L;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField TEXT_path;
    private JButton BTN_choose;
    private JCheckBox CHK_seek;
    private JLabel LABEL_seekType;
    private JComboBox SELECT_seekType;
    private JLabel LABEL_seekPos;
    private JTextField TEXT_seekPos;
    private JLabel LABEL_seek;
    private JTextField TEXT_filter;
    private JComboBox SELECT_charset;
    private JComboBox SELECT_logBuffer;
    private Project project;
    private final FileChooserDescriptor chooseFileOnlyDescriptor = FileChooserDescriptorFactory.createSingleLocalFileDescriptor();
    private Timer t;
    private boolean editMode = false;
    private ViewLog viewLog;
    private Console console;
    private LogViewerLayout logViewerLayout;

    public LogViewerSetting() {
        setContentPane(contentPane);
        setModal(true);
        setTitle("PonyTail Creator");
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        BTN_choose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chooseFileOnlyDescriptor.setTitle("Select File");
                chooseFileOnlyDescriptor.setDescription("Select Log File To Track");
                VirtualFile vf = null;
                try{
                    vf = LocalFileSystem.getInstance().findFileByIoFile(new File(TEXT_path.getText()));
                }catch (Exception e1){
                    //ignore errors
                }
                VirtualFile file = FileChooser.chooseFile(chooseFileOnlyDescriptor, project, vf);
                if (file != null) {
                    TEXT_path.setText(file.getPath());
                    checkSet();
                }
            }
        });
        SELECT_logBuffer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkSet();
            }
        });
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
                if(t==null){
                    t = new Timer(500, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent actionEvent) {
                            generateSeekInfo();
                        }
                    });
                    t.start();
                }else{
                    t.restart();
                }
            }

            @Override
            public void focusLost(FocusEvent focusEvent) {
                if(t.isRunning()){
                    t.stop();
                }
                generateSeekInfo();
            }
        });
        checkSeek();

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void checkSet() {
        //换一个逻辑, 用兼容性去解决, 而不是设置解决
        /*String path = TEXT_path.getText();
        if(StringUtils.isNotBlank(path)) {
            File file = new File(path);
            if (file.exists() && !file.isDirectory()) {
                if (file.length() > SettingManager.getDefaultOverflowFileLength()
                        || file.length()>SizeType.get((String)SELECT_logBuffer.getSelectedItem()).getSize()
                        ) {
                    if (SELECT_seekType.getItemCount() == 4) {
                        SELECT_seekType.removeItem(SeekType.HEAD_LINE.name());
                        SELECT_seekType.removeItem(SeekType.TAIL_LINE.name());
                    }
                    SELECT_seekType.setSelectedItem(SeekType.TAIL_POS.name());
                } else {
                    if (SELECT_seekType.getItemCount() == 2) {
                        SELECT_seekType.addItem(SeekType.HEAD_LINE.name());
                        SELECT_seekType.addItem(SeekType.TAIL_LINE.name());
                    }
                }
            }
        }*/
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

    private void checkSeek() {
        boolean seek = CHK_seek.isSelected();
        SELECT_seekType.setVisible(seek);
        TEXT_seekPos.setVisible(seek);
        LABEL_seekPos.setVisible(seek);
        LABEL_seekType.setVisible(seek);
        LABEL_seek.setVisible(seek);
    }

    private void onOK() {
        if(ConfigHolder.logViewerNumOverflow(project)){
            VcsShowConfirmationOptionImpl option = new VcsShowConfirmationOptionImpl("", "", "", "", "");
            boolean r = ConfirmationDialog.requestForConfirmation(option, project, "You Just Have Some Tracker Logs In This Project, \nWould You Add More Trackers Even If It Will Slow Down The System?", "confirm", LogViewerIcons.confirm);
            if(!r){
                dispose();
                return;
            }
        }
        String path = TEXT_path.getText();
        File file = new File(path);
        if(file.exists() && !file.isDirectory()){
            boolean overflow = false;
            //OVERFLOW_FILE_LENGTH
            if(LogReader.getFileCharacterLength(file)>SettingManager.getDefaultOverflowFileLength()){
                overflow = true;
            }
            String text = TEXT_seekPos.getText();
            long l = 0;
            boolean seek = CHK_seek.isSelected();
            SeekType seekType = SeekType.get((String)SELECT_seekType.getSelectedItem());
            if(StringUtils.isNotBlank(text)) {
                try {
                    l = Long.parseLong(text);
                } catch (Exception e) {
                }
            }
            if(overflow && (seekType==null || (seekType.isSeekLine() && l>0))){
                seekType = SeekType.TAIL_POS;
                l = 0;
            }
            int buffer = SizeType.get((String)SELECT_logBuffer.getSelectedItem()).getSize();
            ViewLog vl = new ViewLog(path, seek, seekType, l);
            if(buffer>=0){
                vl.setCycleBufferSize(buffer);
            }
            String filter = TEXT_filter.getText();
            if(!filter.equals("")){
                vl.setFilter(filter);
            }else{
                vl.setFilter(null);
            }
            vl.setCharset(CharsetType.get((String)SELECT_charset.getSelectedItem()));
            //新增模式
            if(!editMode) {
                final LogCtrlResult r = ConfigHolder.addStatic(project, vl);
                if (r == null) {
                    InfoManager.error(project, "can't get persistence");
                } else {
                    if (r.isSuccess()) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                ToolWindowFactory.getInstance(project).CreateTab(project, r.getLog(), true);
                            }
                        });
                    } else {
                        EnvManager.setSelectedContent(project, r.getLog());
                        InfoManager.info(project, "log is added before");
                    }
                }
            }else{ //修改模式
                if(viewLog!=null){
                    viewLog.setCharset(vl.getCharset());
                    viewLog.setCycleBufferSize(vl.getCycleBufferSize());
                    viewLog.setFilter(vl.getFilter());
                    viewLog.setSeek(vl.isSeek());
                    viewLog.setSeekType(vl.getSeekType());
                    viewLog.setSeekPos(vl.getSeekPos());
                    viewLog.setFilter(vl.getFilter());
                }
                console.refresh(viewLog);
            }
            if(logViewerLayout!=null){
                logViewerLayout.getPanelFilter().setVisible(!filter.equals(""));
                logViewerLayout.resetFilter();
            }
            dispose();
        }else{
            InfoDialog infoDialog = new InfoDialog("please set an exist log"+(file.isDirectory()?",\""+file.getPath()+"\" is a directory":""),project);
            infoDialog.show();
        }
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }

    public static LogViewerSetting getInstance(){
        try {
            return ServiceManager.getService(LogViewerSetting.class);
        }catch (Exception e){
            return new LogViewerSetting();
        }
    }

    public static void main(String[] args) {
        LogViewerSetting dialog = new LogViewerSetting();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    public static void pop(Project project) {
        pop(project, SettingManager.getDefaultViewLog(), null, null);
    }

    public static void pop(Console console) {
        if(console!=null) {
            pop(console.getProject(), console.getViewLog(), console,null);
        }else{
            InfoDialog infoDialog = new InfoDialog("Open Setting Dialog Error",ProjectManager.getInstance().getDefaultProject());
            infoDialog.show();
        }
    }


    public static void pop(Project project, ViewLog viewLog, Console console,LogViewerLayout logViewerLayout) {
        LogViewerSetting logViewerSetting = LogViewerSetting.getInstance();
        if(logViewerSetting==null) {
            logViewerSetting = new LogViewerSetting();
        }
        logViewerSetting.init(project, viewLog, console, console != null); //console不为null是编辑模式
        if(console!=null){
            logViewerSetting.setTitle("PonyTail Modifier");
        }else{
            logViewerSetting.setTitle("PonyTail Creator");
        }
        logViewerSetting.pack();
        Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
        logViewerSetting.setLocation(screenBounds.x + (screenBounds.width - logViewerSetting.getWidth()) / 2, screenBounds.y + (screenBounds.height - logViewerSetting.getHeight()) / 2);
        if(viewLog!=null){
            if(logViewerLayout!=null){
                logViewerSetting.logViewerLayout = logViewerLayout;
            }
            logViewerSetting.TEXT_path.setText(viewLog.getPath());
            logViewerSetting.CHK_seek.setSelected(viewLog.isSeek());
            logViewerSetting.SELECT_seekType.setSelectedItem(viewLog.getSeekType().name());
            logViewerSetting.TEXT_seekPos.setText(viewLog.getSeekPos() + "");
            logViewerSetting.SELECT_charset.setSelectedItem(viewLog.getCharset().getName());
            logViewerSetting.SELECT_logBuffer.setSelectedItem(SizeType.get(viewLog.getCycleBufferSize()).getName());
            logViewerSetting.TEXT_filter.setText(viewLog.getFilter());
            logViewerSetting.checkSeek();
            logViewerSetting.generateSeekInfo();
        }
        logViewerSetting.setVisible(true);
    }

    private void init(Project project, ViewLog viewLog, Console console, boolean editMode) {
        this.project = project;
        this.console = console;
        this.viewLog = viewLog;
        this.editMode = editMode;
        this.TEXT_path.setEditable(!editMode);
        this.BTN_choose.setVisible(!editMode);
        SELECT_logBuffer.removeAllItems();
        for(SizeType st:SizeType.values()){
            SELECT_logBuffer.addItem(st.getName());
        }
        SELECT_charset.removeAllItems();
        for(CharsetType ct:CharsetType.values()){
            SELECT_charset.addItem(ct.getName());
        }

    }

    public static void pop(LogViewerLayout logViewerLayout) {
        pop(logViewerLayout.getProject(), logViewerLayout.getViewLog(), logViewerLayout.getConsole(), logViewerLayout);
    }
}
