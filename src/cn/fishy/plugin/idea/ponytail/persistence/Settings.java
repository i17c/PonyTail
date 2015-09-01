package cn.fishy.plugin.idea.ponytail.persistence;


import cn.fishy.plugin.idea.ponytail.persistence.domain.SettingConfig;
import cn.fishy.plugin.idea.ponytail.ui.LogViewerConfigPanel;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * app 持久化
 *
 * User: duxing
 * Date: 2014.9.17
 *
 */

@State(
        name="SettingConfig",
        storages= {
                @Storage(
                        file = StoragePathMacros.APP_CONFIG + "/logViewerSetting.xml"
                )}
)
public class Settings implements ApplicationComponent, Configurable, PersistentStateComponent<SettingConfig> {
    private LogViewerConfigPanel form;
    SettingConfig settingConfig = SettingConfig.getDefaultSettingConfig();


    public void initComponent() {
    }

    public void disposeComponent() {
    }

    @NotNull
    public String getComponentName() {
        return "PonyTail";
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "PonyTail";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "PonyTail";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        if (this.form == null) {
            this.form = new LogViewerConfigPanel(settingConfig);
            this.form.init();

        }
        return this.form.getBasePanel();
    }

    @Override
    public boolean isModified() {
        return (this.form != null) && (this.form.isModified(this));
    }

    @Override
    public void apply() throws ConfigurationException {
        if (this.form != null){
            settingConfig = this.form.getSettingConfig().clone();
        }
    }

    @Override
    public void reset() {
        if (this.form != null) this.form.loadData(this.settingConfig);
    }

    @Override
    public void disposeUIResources() {
        this.form = null;
    }

    public SettingConfig getSettingConfig() {
        return settingConfig;
    }

    public static Settings getInstance(){
        return ApplicationManager.getApplication().getComponent(Settings.class);
    }

    @Nullable
    @Override
    public SettingConfig getState() {
        if (settingConfig == null) {
            settingConfig = SettingConfig.getDefaultSettingConfig();
        }
        return settingConfig;
    }

    @Override
    public void loadState(SettingConfig state) {
        this.settingConfig = state;
    }

}
