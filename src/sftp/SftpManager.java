package sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.InputStream;
import java.util.Properties;

public class SftpManager {

    private Session session;
    private ChannelSftp channelSftp;

    public boolean connect(String host, int port, String username, String password) {
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(username, host, port);
            session.setPassword(password);

            // Настройки, чтобы не проверять known_hosts
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect(); // Подключение по SSH
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect(); // Подключение по SFTP

            System.out.println("Успешное подключение к SFTP-серверу!");
            return true;

        } catch (Exception e) {
            System.out.println("Ошибка подключения: " + e.getMessage());
            return false;
        }
    }

    public void uploadFile(InputStream localData, String remotePath) throws SftpException { channelSftp.put(localData, remotePath); }

    public ChannelSftp getChannel() {
        return channelSftp;
    }

    public void disconnect() {
        if (channelSftp != null && channelSftp.isConnected()) {
            channelSftp.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        System.out.println("Соединение закрыто.");
    }
}
