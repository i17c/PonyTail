package cn.fishy.plugin.idea.ponytail.constant;

import org.apache.commons.lang.StringUtils;

/**
 * User: duxing
 * Date: 2015-08-30 12:58
 */
public enum SeekType {
    HEAD_LINE,TAIL_LINE,HEAD_POS,TAIL_POS;

    public boolean isSeekLine(){
        return this.equals(HEAD_LINE) || this.equals(TAIL_LINE);
    }

    public boolean isSeekPos(){
        return this.equals(HEAD_POS) || this.equals(TAIL_POS);
    }

    public boolean isSeekHead(){
        return this.equals(HEAD_LINE) || this.equals(HEAD_POS);
    }

    public boolean isSeekTail(){
        return this.equals(TAIL_LINE) || this.equals(TAIL_POS);
    }

    public static SeekType get(String seekTypeStr) {
        if(StringUtils.isBlank(seekTypeStr))return TAIL_POS;
        for(SeekType st:SeekType.values()){
            if(st.name().equals(seekTypeStr)){
                return st;
            }
        }
        return TAIL_POS;
    }
}
