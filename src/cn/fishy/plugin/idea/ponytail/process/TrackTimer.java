package cn.fishy.plugin.idea.ponytail.process;

import cn.fishy.plugin.idea.ponytail.Console;
import cn.fishy.plugin.idea.ponytail.EnvManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.Timer;

import javax.swing.*;
import java.io.IOException;

/**
 * User: duxing
 * Date: 2015-08-29 14:01
 */
public class TrackTimer extends Timer{
    private final Console console;
    private int i = 1;
    private String name;
    private boolean tabChecked = false;
    public TrackTimer(Console console, String name, int milliSec) {
        super(name, milliSec);
        this.name = name;
        this.console = console;
    }

    @Override
    protected void onTimer() throws InterruptedException {
//        LogUtil.log(name + "" + i);
        Project project = console.getProject();
        if(i%10!=9 || (i%10==9 && EnvManager.canRun(project,name))) {
            if(!tabChecked){
                EnvManager.checkTabs(console, name);
                tabChecked = true;
            }

            final long willReadNext = console.willReadNext();
            if (willReadNext>0) {
                this.suspend();
                final TrackTimer timer = this;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            console.readNext(willReadNext);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            timer.resume();
                        }
                    }
                });
            }

        }else{
            EnvManager.removeTimer(project,name);
        }
        i++;
    }

    public Console getConsole() {
        return console;
    }
}
