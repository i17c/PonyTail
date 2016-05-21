
package cn.fishy.plugin.idea.ponytail.persistence;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.fishy.plugin.idea.ponytail.colors.ColorFilter;

@State(
        name="PonyTailColorFilters",
        storages= {
                @Storage(
                        id = "PonyTailColorFilters",
                        file = StoragePathMacros.APP_CONFIG + "/PonyTailColorFilters.xml"
                )}
)
public class ColorFilterHolder implements PersistentStateComponent<ColorFilterHolder> {

    @Transient
    public static ColorFilterHolder getInstance(){
        return ServiceManager.getService(ColorFilterHolder.class);
    }

    public ColorFilterHolder() {
    }

    private List<ColorFilter> filters = new ArrayList<ColorFilter>();

    public void add(ColorFilter colorFilter)
    {
        filters.add(colorFilter);
    }

    public List<ColorFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<ColorFilter> filters)
    {
        this.filters = filters;
    }

    public ColorFilter getMatch(String str)
    {
        Iterator<ColorFilter> lItr = filters.iterator();
        while (lItr.hasNext())
        {
            ColorFilter lNext = lItr.next();
            if (lNext.isMatch(str))
            {
                return lNext;
            }
        }
        return null;
    }

    public List<ColorFilter> clone()
    {
        List<ColorFilter> lResult = new ArrayList<ColorFilter>();
        Iterator<ColorFilter> lItr = filters.iterator();
        while (lItr.hasNext())
        {
            lResult.add(lItr.next().clone());
        }
        return lResult;
    }

    @Nullable
    @Override
    public ColorFilterHolder getState() {
        return this;
    }

    @Override
    public void loadState(ColorFilterHolder state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}