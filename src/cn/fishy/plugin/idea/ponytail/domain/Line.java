package cn.fishy.plugin.idea.ponytail.domain;

import java.io.Serializable;

/**
 * User: duxing
 * Date: 2015-08-30 12:40
 */
public class Line implements Serializable {
    private static final long serialVersionUID = -4164880242835480219L;

    private int allNum;
    private int pos;

    public int getAllNum() {
        return allNum;
    }

    public void setAllNum(int allNum) {
        this.allNum = allNum;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }
}
