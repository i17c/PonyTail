package cn.fishy.plugin.idea.ponytail.domain;


import cn.fishy.plugin.idea.ponytail.constant.CharsetType;
import cn.fishy.plugin.idea.ponytail.constant.SeekType;
import cn.fishy.plugin.idea.ponytail.constant.SizeType;
import cn.fishy.plugin.idea.ponytail.process.LogReader;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.Serializable;

/**
 * User: duxing
 * Date: 2015-08-22 20:53
 */
public class ViewLog implements Serializable{
    private static final long serialVersionUID = 8395209544959713371L;
    private String path = "G:\\home\\admin\\kavass\\logs\\kavass.log";
    private String key;
    private int nameStarter = 0;
    private CharsetType charset = CharsetType.UTF_8;
    private String filter;
    private String extract;
    private int cycleBufferSize = SizeType.SMALLER.getSize();
    private int delay = 1000;

    private boolean seek = false;
    private SeekType seekType = SeekType.TAIL_LINE;
    private long seekPos = 0;
    private transient long seekLocate;
    private transient boolean seekInit = false;
    private transient File file;

    public ViewLog() {
    }

    public ViewLog(String path, boolean seek, SeekType seekType, long seekPos) {
        this.path = path;
        this.seek = seek;
        if(seek){
            this.seekType = seekType;
            this.seekPos = seekPos;
        }else{
            this.seekType = SeekType.HEAD_LINE;
            this.seekPos = 0;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ViewLog) {
            ViewLog o = (ViewLog) obj;
            if (o.getPath() != null && o.getPath().equals(getPath())) {
                return true;
            }
        }
        return false;
    }

    public String getKey() {
        return key!=null?key:fileName();
    }

    public File file() {
        if(path!=null && file==null) {
            file = new File(path);
        }
        return file;
    }

    public long fileLength(){
        if(file.exists() && !file.isDirectory()){
            return LogReader.getFileCharacterLength(file);
        }
        return 0;
    }

    public boolean valid() {
        return file != null && file.exists();
    }
    private String fileName() {
        if(StringUtils.isNotBlank(path)){
            try {
                return new File(path).getName();
            }catch (Exception e){}
        }
        return "";
    }

    public void plusKey() {
        nameStarter++;
        key = fileName()+"."+nameStarter;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setNameStarter(int nameStarter) {
        this.nameStarter = nameStarter;
    }

    public CharsetType getCharset() {
        return charset;
    }

    public void setCharset(CharsetType charset) {
        this.charset = charset;
    }

    public int getCycleBufferSize() {
        return cycleBufferSize;
    }

    public void setCycleBufferSize(int cycleBufferSize) {
        this.cycleBufferSize = cycleBufferSize;
    }

    public boolean seekInited() {
        return seekInit || seekPos ==0;
    }

    public void seekInit(boolean seekInit) {
        this.seekInit = seekInit;
    }

    public void processReaderLocate() {
        long fileLength = fileLength();
        if(valid()) {
            if (seekType.isSeekHead()) {
                seekLocate = seekPos;
                if(seekType.isSeekPos() && fileLength-seekLocate>cycleBufferSize){
                    seekLocate = fileLength - cycleBufferSize;
                }
            } else {
                if (seekType.isSeekLine()) {
                    int allLines = LogReader.getFileLineNumber(file);
                    if (allLines <= seekPos) {
                        seekLocate = 0;
                    } else {
                        seekLocate = allLines - seekPos;
                    }
                } else {
                    long allPos = LogReader.getFileCharacterLength(file);
                    if (allPos <= seekPos) {
                        seekLocate = 0;
                    } else {
                        seekLocate = allPos - seekPos;
                        if(seekPos>cycleBufferSize){
                            seekLocate = fileLength - cycleBufferSize;
                        }
                    }
                }
            }
        }
    }

    public long seekLocate() {
        return seekLocate;
    }

    public SeekType getSeekType() {
        return seekType;
    }

    public void setSeekType(SeekType seekType) {
        this.seekType = seekType;
    }

    public long getSeekPos() {
        return seekPos;
    }

    public void setSeekPos(long seekPos) {
        this.seekPos = seekPos;
    }

    public void reset() {
        seekLocate = 0;
        seekInit(false);
    }

    public boolean isSeek() {
        return seek;
    }

    public void setSeek(boolean seek) {
        this.seek = seek;
    }

    public String getFilter() {
        return filter;
    }

    public String getExtract()
    {
        return extract;
    }

    public void setExtract(String aInExtract)
    {
        extract = aInExtract;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public int getDelay() {
        if(delay<100)return 100;
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }
}