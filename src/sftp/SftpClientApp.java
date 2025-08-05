package sftp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Scanner;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

public class SftpClientApp {
    public static void main(String[] args) throws SftpException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Добро пожаловать в SFTP-клиент!");
        System.out.print("Введите хост (например, 127.0.0.1): ");
        String host = scanner.nextLine();

        System.out.print("Введите порт (например, 22): ");
        int port = Integer.parseInt(scanner.nextLine());

        System.out.print("Введите логин: ");
        String username = scanner.nextLine();

        System.out.print("Введите пароль: ");
        String password = scanner.nextLine();

        SftpManager sftpManager = new SftpManager();
        boolean connected = sftpManager.connect(host, port, username, password);

        if (!connected) {
            System.out.println("Не удалось подключиться. Программа завершена.");
            return;
        }

        ChannelSftp channel = sftpManager.getChannel();
        String remoteFile = "domains.json";
        DomainManager domainManager = new DomainManager();
        try {
            InputStream is = channel.get(remoteFile);
            domainManager.loadFromInputStream(is);
        } catch (Exception e) {
            System.out.println("Ошибка чтения файла: " + e.getMessage());
        }

        System.out.println("Логин: " + username);
        while (true) {
            System.out.println("\nМеню:");
            System.out.println("1) Получить список доменов");
            System.out.println("2) Найти IP по домену");
            System.out.println("3) Найти домен по IP");
            System.out.println("4) Добавить новую пару");
            System.out.println("5) Удалить пару");
            System.out.println("6) Завершить работу");
            System.out.print("Ваш выбор: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    domainManager.printAll();
                    break;

                case "2":
                    System.out.print("Введите домен: ");
                    String domain = scanner.nextLine();
                    String ip = domainManager.getIpByDomain(domain);
                    if (ip != null) {
                        System.out.println("IP-адрес: " + ip);
                    } else {
                        System.out.println("Домен не найден.");
                    }
                    break;

                case "3":
                    System.out.print("Введите IP-адрес: ");
                    String inputIp = scanner.nextLine();
                    String foundDomain = domainManager.getDomainByIp(inputIp);
                    if (foundDomain != null) {
                        System.out.println("Домен: " + foundDomain);
                    } else {
                        System.out.println("IP не найден.");
                    }
                    break;

                case "4":
                    System.out.print("Введите новый домен: ");
                    String newDomain = scanner.nextLine();
                    System.out.print("Введите новый IP (IPv4): ");
                    String newIp = scanner.nextLine();

                    if (domainManager.addPair(newDomain, newIp)) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        domainManager.saveToStream(baos);
                        InputStream updatedJson = new ByteArrayInputStream(baos.toByteArray());
                        sftpManager.uploadFile(updatedJson, "domains.json");
                        System.out.println("Пара успешно добавлена.");
                    } else {
                        System.out.println("Ошибка: домен или IP уже существует, или IP некорректен.");
                    }
                    break;

                case "5":
                    System.out.print("Введите домен или IP для удаления: ");
                    String inputToRemove = scanner.nextLine();

                    if (domainManager.removePair(inputToRemove)) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        domainManager.saveToStream(baos);
                        InputStream updatedJson = new ByteArrayInputStream(baos.toByteArray());
                        sftpManager.uploadFile(updatedJson, "domains.json");
                        System.out.println("Пара удалена.");
                    } else {
                        System.out.println("Ничего не найдено для удаления.");
                    }
                    break;

                case "6":
                    sftpManager.disconnect();
                    System.out.println("Выход...");
                    return;

                default:
                    System.out.println("Неверный выбор. Попробуйте снова.");
            }
        }
    }
}