/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.fishy.plugin.idea.ponytail;

import cn.fishy.plugin.idea.ponytail.constant.LogViewerIcons;
import cn.fishy.plugin.idea.ponytail.constant.SeekType;
import cn.fishy.plugin.idea.ponytail.domain.ViewLog;
import cn.fishy.plugin.idea.ponytail.process.LogReader;
import cn.fishy.plugin.idea.ponytail.process.TrackTimer;
import cn.fishy.plugin.idea.ponytail.util.Matcher;
import com.intellij.execution.impl.ConsoleViewUtil;
import com.intellij.execution.impl.EditorHyperlinkSupport;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.editor.ex.EditorMarkupModel;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.util.EditorPopupHandler;
import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;


/**
 * User: duxing
 * Date: 2015.8.28
 */
public class Console implements Disposable {

    private Project project;
    private ViewLog viewLog;
    private final NotNullLazyValue<Editor> myLogEditor = new NotNullLazyValue<Editor>() {
        @NotNull
        @Override
        protected Editor compute() {
            return createLogEditor();
        }
    };
    private long position;
    private boolean skipNow = false;
    private boolean autoScrollDown = true;
    private TrackTimer trackTimer;
    private boolean reload = false;


    private final NotNullLazyValue<EditorHyperlinkSupport> myHyperlinkSupport;


    public Console(final Project project, ViewLog viewLog) {
        this.project = project;
        this.viewLog = viewLog;
        final Editor editor = getConsoleEditor();
        editor.getSettings().setAdditionalLinesCount(3);
        editor.getSettings().setLineNumbersShown(SettingManager.isShowLineNumber());
        editor.getSettings().setUseSoftWraps(SettingManager.isSoftWrap());
        editor.getDocument().setCyclicBufferSize(viewLog.getCycleBufferSize());
        Disposer.register(project, this);
        myHyperlinkSupport = new NotNullLazyValue<EditorHyperlinkSupport>() {
            @NotNull
            @Override
            protected EditorHyperlinkSupport compute() {
            return new EditorHyperlinkSupport(myLogEditor.getValue(), project);
            }
        };
    }

    public ViewLog getViewLog() {
        return viewLog;
    }

    private Editor createLogEditor() {
        final Editor editor = ConsoleViewUtil.setupConsoleEditor(project, false, false);
        ((EditorMarkupModel) editor.getMarkupModel()).setErrorStripeVisible(true);

        final ClearLogAction clearLog = new ClearLogAction(this);
        clearLog.registerCustomShortcutSet(ActionManager.getInstance().getAction(IdeActions.CONSOLE_CLEAR_ALL).getShortcutSet(), editor.getContentComponent());

        final EditorMouseListener listener = new EditorPopupHandler() {
            public void invokePopup(final EditorMouseEvent event) {
                final ActionManager actionManager = ActionManager.getInstance();
                final ActionPopupMenu menu = actionManager.createActionPopupMenu(ActionPlaces.EDITOR_POPUP, createPopupActions(actionManager, clearLog));
                final MouseEvent mouseEvent = event.getMouseEvent();
                menu.getComponent().show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
            }
        };
        editor.addEditorMouseListener(listener);
        /*Disposer.register(this, new Disposable() {
            @Override
            public void dispose() {
                EditorFactory.getInstance().releaseEditor(editor);
            }
        });*/
        return editor;
    }

    private DefaultActionGroup createPopupActions(ActionManager actionManager, ClearLogAction action) {
        AnAction[] children = ((ActionGroup) actionManager.getAction(IdeActions.GROUP_CONSOLE_EDITOR_POPUP)).getChildren(null);
        DefaultActionGroup group = new DefaultActionGroup(children);
        group.addSeparator();
        group.add(action);
        return group;
    }

    public Editor getConsoleEditor() {
        return myLogEditor.getValue();

    }

    public void append(String s) {
        /*Charset newCharset = SettingManager.getProjectCharset(project);
        if(!viewLog.getCharset().isEqualWith(newCharset)){
            try {
                s = CharsetChanger.changeCharset(s, viewLog.getCharset().getName(), newCharset.name());
            }catch (Exception e){}
        }*/
        Document document = getConsoleEditor().getDocument();
        document.insertString(document.getTextLength(), s);
        EditorUtil.scrollToTheEnd(getConsoleEditor());
    }

    /**
     * 返回文件大小,  当需要读取的时候, 文件大小为正
     * @return
     */
    public long willReadNext() {
        long read = 0;
        if (!myLogEditor.getValue().isDisposed()) {
            long nowLength = LogReader.getFileCharacterLength(viewLog.file());
            if (position < nowLength) {
                read = nowLength;
            }
        }
        return read;
    }
    public void readNext(long nowLength) throws IOException {
        if(position>nowLength){
            refresh(viewLog);
            return;
        }
        boolean forceSeek = viewLog.isSeek();
        SeekType forceSeekType = viewLog.getSeekType();
        //当起始行没有初始化的时候
        if ((forceSeek && !viewLog.seekInited()) || reload) {
            position = 0;
            viewLog.processReaderLocate();
            position = viewLog.seekLocate();
            myLogEditor.getValue().getDocument().setText("");
            reload = false;
        }
        if(nowLength-position>viewLog.getCycleBufferSize()){
            position =  LogReader.getFileCharacterLength(viewLog.file())-viewLog.getCycleBufferSize();
        }

//        LogUtil.log("position - " + position + "\nnowLength - " + nowLength + "\ncharacterLength - " + LogReader.getFileCharacterLength(viewLog.file()));
        if (position < nowLength) {
//            LogUtil.log("next");
            LineNumberReader lineNr;
//            if(viewLog.getCharset().isEqualWith(SettingManager.getProjectCharset(project))){
//                lineNr = new LineNumberReader(new FileReader(viewLog.file()));
//            }else{
                lineNr = new LineNumberReader(new InputStreamReader(new FileInputStream(viewLog.file()), viewLog.getCharset().getName()));
//            }

            if(position>0) {
                lineNr.skip(position);
            }else{
                if (forceSeek && !viewLog.seekInited()) {
                    if (forceSeekType.isSeekPos()) {
                        long ar = lineNr.skip(viewLog.seekLocate());
//                        LogUtil.log(viewLog.seekLocate() + " - "+ ar );
                        viewLog.seekInit(true);
                    }
                }
            }
            try {
                for (String line = lineNr.readLine(); line != null; line = lineNr.readLine()) {
                    if (!skipNow) {
                        if(forceSeek && !viewLog.seekInited() && forceSeekType.isSeekLine() && lineNr.getLineNumber()<viewLog.seekLocate()){
                            continue;
                        }
                        processLine(line, lineNr.getLineNumber());

                    }
                }
                if(forceSeek && !viewLog.seekInited() && forceSeekType.isSeekLine()){
                    viewLog.seekInit(true);
                }
                position = LogReader.getFileCharacterLength(viewLog.file());
            } finally {
                lineNr.close();
            }
        }
    }

    private boolean processLine(String line, int lineNo) {
//        LogUtil.log(lineNo);
        if(viewLog.getFilter()!=null && !viewLog.getFilter().equals("")){
            if(Matcher.match(viewLog.getFilter(),line)){
                append(line + "\n");
            }
        }else{
            append(line + "\n");
        }
        return true;
    }

    public boolean isAutoScrollDown() {
        return autoScrollDown;
    }

    public void setAutoScrollDown(boolean autoScrollDown) {
        this.autoScrollDown = autoScrollDown;
    }

    public TrackTimer getTrackTimer() {
        return trackTimer;
    }

    public void setTrackTimer(TrackTimer trackTimer) {
        this.trackTimer = trackTimer;
    }

    @Override
    public void dispose() {
        EditorFactory.getInstance().releaseEditor(getConsoleEditor());
    }

    public void refresh(ViewLog viewLog) {
        boolean timerRestarted = false;
        if(viewLog!=null){
            this.viewLog = viewLog;
            myLogEditor.getValue().getDocument().setCyclicBufferSize(viewLog.getCycleBufferSize());
            if(trackTimer.getSpan()!=viewLog.getDelay()){
                trackTimer.dispose();
                TrackTimer trackTimer = new TrackTimer(this,viewLog.getKey(),viewLog.getDelay());
                trackTimer.start();
                setTrackTimer(trackTimer);
                timerRestarted = true;
            }
        }
        getViewLog().reset();
        if(!timerRestarted) {
            getTrackTimer().restart();
        }
        position = 0;
        reload = true;
    }

    public static class ClearLogAction extends DumbAwareAction {
        private Console myConsole;

        public ClearLogAction(Console console) {
            super("Clear All", "Clear the contents of the Event Log", LogViewerIcons.clearAll);
            myConsole = console;
        }

        @Override
        public void update(AnActionEvent e) {
            Editor editor = e.getData(CommonDataKeys.EDITOR);
            e.getPresentation().setEnabled(editor != null && editor.getDocument().getTextLength() > 0);
        }

        public void actionPerformed(final AnActionEvent e) {
            final Editor editor = e.getData(CommonDataKeys.EDITOR);
            if (editor != null) {
                editor.getDocument().deleteString(0, editor.getDocument().getTextLength());
            }
        }
    }

    public boolean isSkipNow() {
        return skipNow;
    }

    public void setSkipNow(boolean skipNow) {
        this.skipNow = skipNow;
    }


    public Project getProject() {
        return project;
    }

}
