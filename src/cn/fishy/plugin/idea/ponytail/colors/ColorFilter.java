package cn.fishy.plugin.idea.ponytail.colors;

import com.intellij.util.xmlb.annotations.Transient;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorFilter {

    private String name = "New Filter";
    private boolean enabled = true;
    private String regularExpression;

    private ColorWrapper bgColor;
    private ColorWrapper fgColor;

    @Transient
    private transient Pattern pattern;

    public ColorFilter(String name, boolean enabled, String regularExpression) {
        this.name = name;
        this.enabled = enabled;
        setRegularExpression(regularExpression);
        setBg(null);
        setFg(null);
    }

    public ColorFilter() {
        this("New Filter", true, "");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRegularExpression() {
        return regularExpression;
    }

    public void setRegularExpression(String regularExpression) {
        this.regularExpression = regularExpression;
        pattern = Pattern.compile(regularExpression);
    }

    public ColorWrapper getBgColor() {
        return bgColor;
    }

    public void setBgColor(ColorWrapper color) {
        if (color == null)
        {
            color = new ColorWrapper(new Color(255, 255, 255));
        }
        this.bgColor = color;
    }

    @Transient
    public void setBg(Color aInColor)
    {
        if (aInColor == null)
        {
            setBgColor(null);
        }
        else
        {
            setBgColor(new ColorWrapper(aInColor));
        }
    }

    @Transient
    public Color getBg()
    {
        return bgColor.getColor();
    }


    public ColorWrapper getFgColor() {
        return fgColor;
    }

    @Transient
    public Color getFg()
    {
        return fgColor.getColor();
    }

    @Transient
    public void setFg(Color aInColor)
    {
        if (aInColor == null)
        {
            setFgColor(null);
        }
        else
        {
            setFgColor(new ColorWrapper(aInColor));
        }
    }

    public void setFgColor(ColorWrapper color) {
        if (color == null)
        {
            color = new ColorWrapper(new Color(0, 0, 0));
        }
        this.fgColor = color;
    }

    @Transient
    public ColorFilter clone() {
        ColorFilter clone = new ColorFilter(getName(), isEnabled(), getRegularExpression());
        clone.setBgColor(getBgColor());
        clone.setFgColor(getFgColor());
        return clone;
    }

    @Transient
    public boolean isMatch(String aInString) {
        if (!enabled) {
            return false;
        }
        Matcher lMatch = pattern.matcher(aInString);
        return lMatch.find();
    }

    @Override
    public String toString() {
        return "ColorFilter{" +
               "name='" + name + '\'' +
               ", enabled=" + enabled +
               ", regularExpression='" + regularExpression + '\'' +
               ", bgColor=" + bgColor +
               ", fgColor=" + fgColor +
               ", pattern=" + pattern +
               '}';
    }
}