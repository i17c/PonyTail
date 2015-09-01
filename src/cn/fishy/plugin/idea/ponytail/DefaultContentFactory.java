package cn.fishy.plugin.idea.ponytail;

import cn.fishy.plugin.idea.ponytail.ui.LogViewerDefaultLayout;
import com.intellij.openapi.project.Project;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

/**
 * User: duxing
 * Date: 2015-08-26 14:48
 */
public class DefaultContentFactory {
    private Content contentDefault;
    public static final String defaultTabKey = "default";

    public DefaultContentFactory(Project project) {
        contentDefault = ContentFactory.SERVICE.getInstance().createContent((new LogViewerDefaultLayout(project)).getPanel(), defaultTabKey, true);
        contentDefault.setCloseable(false);
    }

    public Content getContentDefault() {
        return contentDefault;
    }
}
