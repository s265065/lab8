package lab8.client;
//TODO: server & address settings?

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lab8.controllers.LoginController;
import lab8.previous.Hat;
import lab8.server.DataBaseConnection;

import java.io.*;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.ResourceBundle;

public class Client extends Application {
    private static String serverAddress = "localhost";
    private static int serverPort = 8080;
    private String username;
    private String password;
    private String userColor;
    private static ResourceBundle bundle = ResourceBundle.getBundle("lab8.i18n.Text", new Locale("en", "EN"));
private DataBaseConnection dataBaseConnection;

    public DataBaseConnection getDataBaseConnection() {
        return sendCommand("getdb", null, null).getArgumentAs(DataBaseConnection.class);
    }

    public String getUsername() {
        return this.username;
    }

    public String getUserColor(){return this.userColor;}

    private static Command previousCommand = null;


    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setResources(bundle);
        loader.setLocation(getClass().getResource("/view/Login.fxml"));
        loader.setController(new LoginController());
        Parent root = loader.load();
        Scene scene = new Scene(root, Color.web("#c085db"));
        stage.setScene(scene);
        stage.setTitle("Login");
        stage.setResizable(false);
        stage.show();
        stage.setWidth(250);
        stage.setHeight(300);
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            serverAddress = args[0];
            if (args.length > 1) {
                try {
                    serverPort = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    e.getLocalizedMessage();
                }
            }
        }

        Application.launch(Client.class, args);
    }

    public void setUsername(String name) {
        username = name;
    }

    public void setPassword(String pass) {
        password = pass;
    }

    static private void sendMessage(ByteArrayOutputStream baos, SocketChannel channel) {
        // Sending message using channel
        ByteBuffer sendingBuffer = ByteBuffer.allocate(baos.size());
        sendingBuffer.put(baos.toByteArray());
        sendingBuffer.flip();
        new Thread(() -> {
            try {
                channel.write(sendingBuffer);
            } catch (IOException ignored) {
            }
        }).start();
    }

    public String doLogin(String name, String argument) {
        try (SocketChannel channel = SocketChannel.open()) {
            String result;
            channel.connect(new InetSocketAddress(serverAddress, serverPort));
            // Making a Message instance and writing it to ByteArrayOutputStream
            Message<String> message = new Message<>(name, argument);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(message);

            //sendMessage(baos, channel);
            ByteBuffer sendingBuffer = ByteBuffer.allocate(baos.size());
            sendingBuffer.put(baos.toByteArray());
            sendingBuffer.flip();
            new Thread(() -> {
                try {
                    channel.write(sendingBuffer);
                } catch (IOException ignored) {
                }
            }).start();
            // Getting Message instance from response
            ObjectInputStream ois = new ObjectInputStream(channel.socket().getInputStream());
            Message<?> incoming = (Message<?>) ois.readObject();
            System.out.println(incoming.getMessage());
            result = incoming.getMessage();
            String[] mess = incoming.getMessage().split(" ");
            try {
                this.username = mess[0];
                this.userColor = mess[1];
                if (result.equals(username+" "+userColor)){result="success";}
                String[] argumentpass = argument.split(" ");
                this.password = argumentpass[1];
            } catch (ArrayIndexOutOfBoundsException e) {}
            return result;
        } catch (UnresolvedAddressException e) {
            return "Не удалось определить адрес сервера. Воспользуйтесь командой address, чтобы изменить адрес.";
        } catch (UnknownHostException e) {
            return "Ошибка подключения к серверу: неизвестный хост. Воспользуйтесь командой address, чтобы изменить адрес";
        } catch (SecurityException e) {
            return "Нет разрешения на подключение, проверьте свои настройки безопасности";
        } catch (ConnectException e) {
            return "Нет соединения с сервером. Введите repeat, чтобы попытаться ещё раз, или измените адрес (команда address)";
        } catch (IOException e) {
            return "Ошибка ввода-вывода, обработка запроса прервана";
        } catch (ClassNotFoundException e) {
            return "Ошибка: клиент отправил данные в недоступном для клиента формате (" + e.getLocalizedMessage() + ")";
        }
    }

    public Hat[] loadHats() {
        Message<?> result = sendCommand("load", null, null);

        if (!result.getMessage().equals("success")) {
            return null;
        }

        Hat[] hats = result.getArgumentAs(Hat[].class);
        Arrays.sort(hats, Comparator.comparing(Hat::getId));

        return hats;
    }

    /**
     * Отправляет команду на сервер, результат отправляет в System.out,
     * использует каналы согласно условию задания
     *
     * @param name     команда, которую нужно отправить
     * @param argument аргумент команды
     * @return пустую строку или сообщение об ошибке, если есть
     */
    public synchronized <T extends Serializable> Message<?> sendCommand(String name, T argument, Locale locale) {
        try (SocketChannel channel = SocketChannel.open()) {
            channel.connect(new InetSocketAddress(serverAddress, serverPort));

            // Making a Message instance and writing it to ByteArrayOutputStream
            Message<T> message = new Message<>(name, argument);
            message.setUserName(this.username);
            message.setPassword(this.password);
            message.setLocale(locale);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(message);

            sendMessage(baos, channel);

            // Getting Message instance from response
            ObjectInputStream ois = new ObjectInputStream(channel.socket().getInputStream());
            Message<?> incoming = (Message<?>) ois.readObject();
            System.out.println(incoming.getMessage());

            return incoming;
        } catch (UnresolvedAddressException e) {
            return new Message("Не удалось определить адрес сервера. Воспользуйтесь командой address, чтобы изменить адрес.");
        } catch (UnknownHostException e) {
            return new Message("Ошибка подключения к серверу: неизвестный хост. Воспользуйтесь командой address, чтобы изменить адрес");
        } catch (SecurityException e) {
            return new Message("Нет разрешения на подключение, проверьте свои настройки безопасности");
        } catch (ConnectException e) {
            return new Message("Нет соединения с сервером. Введите repeat, чтобы попытаться ещё раз, или измените адрес (команда address)");
        } catch (IOException e) {
            return new Message("Ошибка ввода-вывода, обработка запроса прервана");
        } catch (ClassNotFoundException e) {
            return new Message("Ошибка: клиент отправил данные в недоступном для клиента формате (" + e.getLocalizedMessage() + ")");
        }
    }


//    /**
//     * Вощвращает слелующую команду пользователя. Предназначен для многострочного ввода.
//     *
//     * @param reader поток, из которого будет читаться команда
//     * @return введённая пользователем команда
//     * @throws IOException если что-то пойдёт не так
//     */
//    static String getMultilineCommand(BufferedReader reader) throws IOException {
//        StringBuilder builder = new StringBuilder();
//        char current;
//        boolean inString = false;
//        do {
//            current = (char) reader.read();
//            if (current != ';' || inString)
//                builder.append(current);
//            if (current == '"')
//                inString = !inString;
//        } while (current != ';' || inString);
//        return builder.toString();
//    }

}