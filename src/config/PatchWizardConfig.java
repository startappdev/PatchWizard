package config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

@State(name = "PatchWizardConfig", storages = {@Storage("patch_wizard_config.xml")})
public class PatchWizardConfig implements PersistentStateComponent<PatchWizardConfig> {
    public String host;
    public String userName;
    public String changeList;
    public String keyPath;
    public String destinationPath;
    public boolean compileFlag;

    public PatchWizardConfig() {
        // setting defaults
        this.host = "web48";
        this.userName = "iddo";
        this.changeList = "Default";
        this.keyPath = "/home/.ssh/startapp_ec2";
        this.destinationPath = "/usr/local/tomcat/webapps/AdPlatform/WEB-INF/classes";
        this.compileFlag = true;
    }

    public static PatchWizardConfig getInstance(Project project) {
        PatchWizardConfig cfg = ServiceManager.getService(project, PatchWizardConfig.class);
        if (cfg == null) {
            cfg = new PatchWizardConfig();
        }
        return cfg;
    }

    @Nullable
    @Override
    public PatchWizardConfig getState() {
        return this;
    }

    @Override
    public void loadState(PatchWizardConfig patchWizardConfig) {
        XmlSerializerUtil.copyBean(patchWizardConfig, this);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getChangeList() {
        return changeList;
    }

    public void setChangeList(String changeList) {
        this.changeList = changeList;
    }

    public String getKeyPath() {
        return keyPath;
    }

    public void setKeyPath(String keyPath) {
        this.keyPath = keyPath;
    }

    public String getDestinationPath() {
        return destinationPath;
    }

    public void setDestinationPath(String destinationPath) {
        this.destinationPath = destinationPath;
    }

    public boolean isCompileFlag() {
        return compileFlag;
    }

    public void setCompileFlag(boolean compileFlag) {
        this.compileFlag = compileFlag;
    }

    @Override
    public String toString() {
        return "PatchWizardConfig{" +
                "host='" + host + '\'' +
                ", userName='" + userName + '\'' +
                ", changeList='" + changeList + '\'' +
                ", keyPath='" + keyPath + '\'' +
                ", destinationPath='" + destinationPath + '\'' +
                ", compileFlag=" + compileFlag +
                '}';
    }
}
