import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import com.jcraft.jsch.JSchException;
import config.PatchWizardConfig;
import org.apache.log4j.Level;
import org.jetbrains.annotations.NotNull;
import utils.JavaCompilerWrapper;
import utils.Notification;
import utils.SshUtils;
import javax.naming.NoPermissionException;
import javax.tools.JavaFileObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Iddo Eldor
 * @version 1.1
 * @since 29 November 2016
 */
public class PatchWizardAction extends AnAction {

    /**
     * Log are written to file idea.log, to view logs while debugging you need to run
     * $: less +F /root/.IntelliJIdea2016.1/system/plugins-sandbox/system/log/idea.log
     */
    private static final Logger LOG = Logger.getInstance("#PatchWizardAction");

    @Override
    public void actionPerformed(AnActionEvent act) {
        LOG.setLevel(Level.DEBUG);
        LOG.debug("Start: actionPerformed");

        Project curProject = getCurrentProject();
        PatchWizardConfig config = PatchWizardConfig.getInstance(curProject);
        LOG.debug("Fetched config: ", config);

        // getting changelist java files to compile
        final String compiledFilesDir = curProject.getBasePath() + "/patchOut/";
        // empty folder each iteration
        emptyFolder(compiledFilesDir, curProject);
        Collection<File> changelistFiles = getChangelistFiles(curProject, config.getChangeList());
        LOG.debug("Changelist files:", changelistFiles);
        if (changelistFiles.isEmpty()) {
            Notification.err(curProject, "No '.java' files in changelist " + config.getChangeList());
        } else {
            if (config.isCompileFlag()) {
                try {
                    JavaCompilerWrapper.compile(changelistFiles, compiledFilesDir);
                    LOG.debug("Files compiled successfully.");
                    // iterate compiled files directory files and upload each .class to host via scp
                    // todo support multiple servers via comma separated list and scp to all
                    List<String> uploadCompleteList = Collections.synchronizedList(new ArrayList<>());
                    Files.walk(Paths.get(compiledFilesDir))
                            .map(String::valueOf)
                            .filter(p -> p.endsWith(JavaFileObject.Kind.CLASS.extension))
                            .forEach(fileToUpload -> {
                                try {
                                    SshUtils.scp(config, fileToUpload, compiledFilesDir);
                                    uploadCompleteList.add(fileToUpload);
                                } catch (JSchException e) {
                                    Notification.err(curProject, "SCP error: " + e.getMessage());
                                } catch (FileNotFoundException e2) {
                                    Notification.err(curProject, "File not found " + e2.getMessage());
                                } catch (NoPermissionException e) {
                                    Notification.err(curProject, e.getMessage());
                                }
                            });

                    boolean scpSuccessfullyCompleted = uploadCompleteList.size() == changelistFiles.size();
                    if (scpSuccessfullyCompleted) {
                        Notification.out(curProject, "Woohoo notification\nscp completed");
                        /**
                         * todo restart server, get stdout, optional: open logs by level && less +F server.log
                         * its better to concatenate commands
                         */
                        String cmd = "cd " + config.getDestinationPath() + "\nfind . -type f\npwd";
                        SshUtils.exec(config, cmd);
                    }

                } catch (IOException | JSchException e) {
                    Notification.err(curProject, e.getMessage());
                }
            } else {
                Notification.err(curProject, "Please go to File:Setting:Tools:Deploy and check 'Compile' checkbox");
            }
        }
    }

    /**
     *
     * @param directoryName path for recursive delete files & folders
     *                      without deleting main directory
     * @param curProject used to notify in case of IOException
     */
    private void emptyFolder(String directoryName, Project curProject) {
        File dir = new File(directoryName);
        if (dir.exists() && dir.isDirectory()) {
            Path directory = Paths.get(directoryName);
            try {
                Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        if (!dir.equals(Paths.get(directoryName))) { // so it won't delete parent directory
                            Files.delete(dir);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                Notification.err(curProject, e.getMessage());
            }
        } else {
            if (dir.mkdir()) {
                LOG.debug(dir.toString(), " successfully created");
            }
        }
    }

    /**
     * @param curProject     current project context
     * @param changeListName changelist identifier to get java files from
     * @return list of java files inside a specific changelist from tab "Version Control"
     *      or empty list if changelist not found or has no java files inside
     */
    @NotNull
    private static Collection<File> getChangelistFiles(Project curProject, String changeListName) {
        Collection<File> result = Collections.synchronizedList(new ArrayList<>());
        ChangeListManager changeListManager = ChangeListManager.getInstance(curProject);
        List<LocalChangeList> changeLists = changeListManager.getChangeLists();
        changeLists.stream().filter(changeList -> changeList.getName().equals(changeListName)).findFirst()
                .ifPresent(localChangeList -> localChangeList.getChanges().stream()
                        .filter(PatchWizardAction::isJavaFile)
                        .forEach(change -> {
                            VirtualFile virtualFile = change.getVirtualFile();
                            // redundant check
                            // virtualFile != null already checked inside .filter(PatchWizardAction::isJavaFile)
                            // but intellij keep on warning
                            if (virtualFile != null) {
                                result.add(new File(virtualFile.getPath()));
                            }
                        })
                );
        return result;
    }

    /**
     * @param change contains file full path
     * @return if file extension equal to ".java"
     */
    private static boolean isJavaFile(Change change) {
        return change.getVirtualFile() != null
                && change.getVirtualFile().getPath().trim().endsWith(JavaFileObject.Kind.SOURCE.extension);
    }

    /**
     * @return returns the current project which user is focused on
     * until we will need to select from list of project
     */
    private Project getCurrentProject() {
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        Project curProject = openProjects[0];
        LOG.debug("User is in focus at project ", curProject);
        return curProject;
    }


}