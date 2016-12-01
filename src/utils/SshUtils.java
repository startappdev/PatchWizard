package utils;

import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.diagnostic.Logger;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import config.PatchWizardConfig;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

import javax.naming.NoPermissionException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

public class SshUtils {

    private static final Logger LOG = Logger.getInstance("#SshUtils");

    private static final int SSH_PORT = 22;
    private static final int SSH_CONNECTION_TIMEOUT = 5 * 1000; // in ms
    private static final String SSH_UPLOAD_CHANNEL_TYPE = "sftp";
    private static final String SSH_EXECUTE_CHANNEL_TYPE = "exec";
    private static final ImmutableMap<String, String> SSH_SESSION_CONFIG = ImmutableMap.of(
            "StrictHostKeyChecking", "no",
            "PreferredAuthentications", "publickey"
    );

    /**
     * @param config           contains server details
     * @param filePath         the file to upload to config.host
     * @param compiledFilesDir output directory
     * @throws JSchException
     * @throws FileNotFoundException
     * @throws NoPermissionException if can't upload to host.destinationFolder
     *                               need to "chmod -R 777" to folder
     */
    public static void scp(PatchWizardConfig config, String filePath, String compiledFilesDir)
            throws JSchException, FileNotFoundException, NoPermissionException {
        Session session = getSshSession(config);

        Channel channel = session.openChannel(SSH_UPLOAD_CHANNEL_TYPE);
        channel.connect();

        ChannelSftp sftpChannel = (ChannelSftp) channel;
        try {
            sftpChannel.cd(config.getDestinationPath());
        } catch (SftpException e) {
            LOG.error(e.getMessage());
            return;
        }

        FileInputStream fis = new FileInputStream(new File(filePath));
        final String PATH_DELIMITER = "/";
        String replace = filePath.replace(compiledFilesDir, PATH_DELIMITER);
        try {
            sftpChannel.put(fis, replace);
        } catch (SftpException e) {
            // P flag in mkdir command, making parent directories as needed
            LOG.debug("Keeping structure, creating folders (mkdir -p)");
            String paths_until_file = replace.substring(0, replace.lastIndexOf(PATH_DELIMITER));
            String[] requiredFoldersArr = paths_until_file.split(PATH_DELIMITER);
            String curDir = ".";
            for (String folder : requiredFoldersArr) {
                if (folder.length() > 0) {
                    try {
                        LOG.debug("Current dir : ", curDir);
                        sftpChannel.cd(folder);
                        curDir = sftpChannel.pwd();
                    } catch (SftpException e2) {
                        try {
                            sftpChannel.mkdir(folder);
                            sftpChannel.cd(folder);
                            curDir = sftpChannel.pwd();
                        } catch (SftpException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
            String dst = curDir + PATH_DELIMITER + FilenameUtils.getName(filePath);
            LOG.debug("Uploading file to: ", dst);
            try {
                sftpChannel.put(fis, dst);
            } catch (SftpException e1) {
                if (e1.getMessage().contains("Permission denied")) {
                    throw new NoPermissionException("Permission denied to upload file to "
                            + config.getHost() + "@" + config.getDestinationPath()
                            + "\n consider run: chmod -R 777 dir/");
                } else {
                    e1.printStackTrace();
                }
            }
        }

        sftpChannel.exit();
        session.disconnect();

        LOG.debug("File ", filePath, " uploaded successfully");
    }

    public static void exec(PatchWizardConfig config, String cmd) throws JSchException, IOException {
        Session session = getSshSession(config);
        Channel channel = session.openChannel(SSH_EXECUTE_CHANNEL_TYPE);

        ChannelExec channelExec = (ChannelExec) channel;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(channelExec.getInputStream()));
            channelExec.setCommand(cmd);
            /** http://stackoverflow.com/questions/32024651/tail-f-process-will-not-exit-after-jsch-connection-closed */
//            channelExec.setPty(true);
            channelExec.connect();

            String msg;
            LOG.debug("server output begin");
            while ((msg = in.readLine()) != null) {
                LOG.debug(msg);
            }
            LOG.debug("server output end");
        } catch (Exception e) {
            LOG.error(e.getMessage());
        } finally {
            channelExec.disconnect();
            session.disconnect();
        }
    }

    @NotNull
    private static Session getSshSession(PatchWizardConfig config) throws JSchException {
        JSch jsch = new JSch();
        jsch.addIdentity(config.getKeyPath());
        Session session = jsch.getSession(config.getUserName(), config.getHost(), SSH_PORT);
        Properties properties = new Properties();
        properties.putAll(SSH_SESSION_CONFIG);
        session.setConfig(properties);
        session.connect(SSH_CONNECTION_TIMEOUT);
        return session;
    }
}