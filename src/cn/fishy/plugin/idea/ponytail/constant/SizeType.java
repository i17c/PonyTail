package cn.fishy.plugin.idea.ponytail.constant;

import org.apache.commons.lang.StringUtils;

/**
 * User: duxing
 * Date: 2015-08-31 17:28
 */
public enum SizeType {
    MIN("1K",0x1000),
    SMALLER("5K",0x5000),
    SMALL("10K",0x10000),
    NORMAL("20K",0x20000),
    BIG("50K",0x50000),
    BIGGER("100K",0x100000),
    MAX("200K",0x200000);
    private String name;
    private int size;

    SizeType(String name, int size) {
        this.name = name;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public static SizeType get(String name){
        if(StringUtils.isBlank(name)){
            return SizeType.SMALL;
        }
        for(SizeType st:SizeType.values()){
            if(st.getName().equals(name)){
                return st;
            }
        }
        return SizeType.SMALL;
    }

    public static SizeType get(int size){
        if(size<SizeType.MIN.getSize()){
            return SizeType.MIN;
        }
        for(SizeType st:SizeType.values()){
            if(st.getSize()==size){
                return st;
            }
        }
        return SizeType.SMALL;
    }
}
