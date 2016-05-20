/**
 ****************************************************************************
 *
 * $RCSfile$
 *
 * Andrew Stone
 *
 ****************************************************************************
 *
 * $Revision$
 *
 * $Id$
 *
 ****************************************************************************
 *
 * Copyright (c) 2012 Alcatel-Lucent Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 *
 ****************************************************************************/
package cn.fishy.plugin.idea.ponytail.colors;

import com.intellij.util.xmlb.annotations.Transient;

import java.awt.*;

public class ColorWrapper {

    /**
     * Used to wrap around Color object, since xml serialization blows up with 'Color' type and we only need to persist one code
     */
    @Transient
    private transient Color color;

    private String colorCode;

    public ColorWrapper(Color color) {
        setColor(color);
    }

    public ColorWrapper() {
    }

    @Transient
    public Color getColor() {
        return color;
    }

    @Transient
    public void setColor(Color color) {
        this.color = color;
        colorCode = Integer.toString(color.getRGB());
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        setColor(new Color(Integer.parseInt(colorCode)));
    }
}