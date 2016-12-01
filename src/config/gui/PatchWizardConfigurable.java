package config.gui;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import config.PatchWizardConfig;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

public class PatchWizardConfigurable implements SearchableConfigurable {

    private PatchWizardConfigurationGUI gui;
    private final PatchWizardConfig config;

    @SuppressWarnings("FieldCanBeLocal")
    private final Project project;

    public PatchWizardConfigurable(@NotNull Project project) {
        this.project = project;
        this.config = PatchWizardConfig.getInstance(project);
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Deploy patch plugin display name";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "config.DeployPatchHelp";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        gui = new PatchWizardConfigurationGUI();
        gui.createUI(project);
        return gui.getRootPanel();
    }

    @Override
    public boolean isModified() {
        return gui.isModified();
    }

    @Override
    public void apply() throws ConfigurationException {
        gui.apply();
    }

    @Override
    public void reset() {
        gui.reset();
    }

    @Override
    public void disposeUIResources() {
        gui = null;
    }

    @NotNull
    @Override
    public String getId() {
        return "preferences.DeployPatch";
    }

    @Nullable
    @Override
    public Runnable enableSearch(String s) {
        return null;
    }
}
