package cn.fishy.plugin.idea.ponytail.persistence.domain;

import cn.fishy.plugin.idea.ponytail.constant.CharsetType;
import cn.fishy.plugin.idea.ponytail.constant.SeekType;
import cn.fishy.plugin.idea.ponytail.constant.SizeType;

import java.io.Serializable;

/**
 * User: duxing
 * Date: 2015-08-30 20:09
 */
public class SettingConfig implements Serializable {
    private static final long serialVersionUID = -9077797013434832931L;

    public String dir;
    public int logBuffer = SizeType.SMALLER.getSize();
    public int delay = 1000;
    public boolean seek = false;
    public SeekType seekType = SeekType.TAIL_LINE;
    public long seekPos = 0;
    public CharsetType charset = CharsetType.UTF_8;
    public int overflowNum = 5;
    public boolean showLineNumber = true;
    public boolean softWrap = false;

    public SettingConfig() {
    }

    public static SettingConfig getDefaultSettingConfig() {
        return new SettingConfig();
    }

    @Override
    public SettingConfig clone(){
        SettingConfig sc = new SettingConfig();
        sc.dir = this.dir;
        sc.logBuffer = this.logBuffer;
        sc.delay = this.delay;
        sc.seek = this.seek;
        sc.seekType = this.seekType;
        sc.seekPos = this.seekPos;
        sc.charset = this.charset;
        sc.overflowNum = this.overflowNum;
        sc.showLineNumber = this.showLineNumber;
        sc.softWrap = this.softWrap;
        return sc;
    }
}
