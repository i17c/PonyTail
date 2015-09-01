package cn.fishy.plugin.idea.ponytail.constant;

import java.nio.charset.Charset;

/**
 * User: duxing
 * Date: 2015.08.12 00:50:000
 */
public enum CharsetType {
    UTF_8("UTF-8"),
    GBK("GBK"),
    US_ASCII("US-ASCII"),
    ISO_8859_1("ISO-8859-1"),
    UTF_16("UTF-16"),
    ;
    private String name;

    CharsetType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static CharsetType get(String name){
        if(name==null)return UTF_8;
        for(CharsetType e:CharsetType.values()){
            if(e.getName().equals(name)){
                return e;
            }
        }
        return UTF_8;
    }

    public boolean isEqualWith(Charset charset){
        return this.name.equals(charset.name());
    }
}
