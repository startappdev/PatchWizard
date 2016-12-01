package config.gui;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.ui.JBColor;
import com.intellij.uiDesigner.core.GridConstraints;
import config.PatchWizardConfig;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.util.ArrayList;
import java.util.List;


public class PatchWizardConfigurationGUI {

    private PatchWizardConfig config;
    /* inputs */
    private JTextField serverHostTF;
    private JTextField userNameTF;
    private TextFieldWithBrowseButton keyPathTF;
    private JTextField destinationFolderTF;
        private JList fileChangesList;
    private JCheckBox compileFlag;
    /* UI containers */
    private JPanel rootPanel;
    private JPanel configPanel;
    private JPanel fileBrowserPanel;

    public PatchWizardConfigurationGUI() {
    }

    public boolean isModified() {
        boolean modified = false;
        modified |= !serverHostTF.getText().equals(config.getHost());
        modified |= !destinationFolderTF.getText().equals(config.getDestinationPath());
        modified |= !userNameTF.getText().equals(config.getUserName());
        modified |= fileChangesList.getSelectedValue() != null
                && !fileChangesList.getSelectedValue().equals(config.getChangeList());
        modified |= !keyPathTF.getText().equals(config.getKeyPath());
        modified |= compileFlag.isSelected() != config.isCompileFlag();
        return modified;
    }

    public void createUI(Project project) {
        keyPathTF = new TextFieldWithBrowseButton();
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, false);
        keyPathTF.addBrowseFolderListener("Select Public Key", "", project, fileChooserDescriptor);
        fileBrowserPanel.add(keyPathTF, new GridConstraints());
        fileChangesList.setBorder(new LineBorder(JBColor.BLUE));

        config = PatchWizardConfig.getInstance(project);
        if (config != null) {
            destinationFolderTF.setText(config.getDestinationPath());
            serverHostTF.setText(config.getHost());
            userNameTF.setText(config.getUserName());
            fileChangesList.setListData(getCurrentChangelistNames());
            fileChangesList.setSelectedValue(config.getChangeList(), false);
            keyPathTF.setText(config.getKeyPath());
            compileFlag.setSelected(config.isCompileFlag());
        }
    }

    @NotNull
    private Object[] getCurrentChangelistNames() {
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        // user need to be focused on project or we need to extract to an argument
        Project curProject = openProjects[0];
        List<String> localChangelistNames = new ArrayList<>();
        ChangeListManager.getInstance(curProject).getChangeLists().stream()
                .forEach(localChangeList -> localChangelistNames.add(localChangeList.getName()));
        return localChangelistNames.toArray();
    }


    public void reset() {
        destinationFolderTF.setText(config.getDestinationPath());
        serverHostTF.setText(config.getHost());
        userNameTF.setText(config.getUserName());
        keyPathTF.setText(config.getKeyPath());
        fileChangesList.setSelectedValue(config.getChangeList(), false);
        compileFlag.setSelected(config.isCompileFlag());
    }

    public void apply() {
        config.setDestinationPath(destinationFolderTF.getText());
        config.setHost(serverHostTF.getText());
        config.setUserName(userNameTF.getText());
        config.setKeyPath(keyPathTF.getText());
        config.setChangeList(fileChangesList.getSelectedValue().toString());
        config.setCompileFlag(compileFlag.isSelected());
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }
}
