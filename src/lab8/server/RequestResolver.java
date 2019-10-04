package lab8.server;

import lab8.client.Message;
import lab8.previous.Hat;

import java.io.*;
import java.net.Socket;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.ResourceBundle;

class RequestResolver implements Runnable {

    private static final String AUTOSAVE = "autosave.csv";
    private static DataBaseConnection db;
    private ObjectOutputStream out;
    private ObjectInputStream ois;
    private Wardrobe wardrobe;
    private Socket socket;
    private Logger logger;
    private static ResourceBundle bundle;
    private static ZonedDateTime dateTime;

    RequestResolver(Socket socket, Wardrobe wardrobe, Logger logger, DataBaseConnection db) {
        try {
            wardrobe.clear();
            ois = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
            this.socket = socket;

            this.logger = logger;
            this.db = db;
            wardrobe.addAll(Arrays.asList(db.loadHats()));
            this.wardrobe = wardrobe;
            this.dateTime = ZonedDateTime.now();
//            this.wardrobe.addAll(Arrays.asList(db.loadHats()));
        } catch (IOException e) {
            logger.err("Ошибка создания решателя запроса: " + e.toString());
        }
    }

    @Override
    public void run() {
        try {
            Message<?> message = (Message<?>) ois.readObject();
            processMessage(message);

            logger.log("Запрос от " + socket.getInetAddress() + ": " + message.getMessage());
        } catch (EOFException e) {
            logger.err("Сервер наткнулся на неожиданный конец");
            //sendEndMessage("Не удалось обработать ваш запрос: в ходе чтения запроса сервер наткнулся на неожиданный конец данных");
        } catch (IOException e) {
            logger.err("Ошибка исполнения запроса: " + e.toString());
            // sendEndMessage("На сервере произошла ошибка: " + e.toString());
        } catch (ClassNotFoundException e) {
            sendMessage("");
        }
    }

    /**
     * Отправляет сообщение с указанным флагом окончания
     *
     * @param message текст сообщения
     */
    private void sendMessage(String message) {
        try {
            out.writeObject(new Message(message));
        } catch (IOException e) {
            logger.log("Ошибка отправки данных клиенту: " + e.getLocalizedMessage());
        }
    }

    private <T extends Serializable> void sendMessage(String message, T argument) {
        try {
            out.writeObject(new Message<>(message, argument));
        } catch (IOException e) {
            logger.log("Ошибка отправки данных клиенту: " + e.getLocalizedMessage());
        }
    }

    /**
     * Обрабатывает сообщение, отправляемый клиенту результат будет отмечен как последний
     *
     * @param message сообщение
     */
    private <T extends Serializable> void processMessage(Message<T> message) {
        processMessage(message, true);
    }

    /**
     * Обрабатывает сообщение
     *
     * @param message сообщение
     * @param endFlag если он true, результат обработки отправится клиенту как последний
     */
    private <T extends Serializable> void processMessage(Message<T> message, boolean endFlag) {
        if (message == null) {
            sendMessage("message.error.empty-request", endFlag);
            return;
        }

        switch (message.getMessage()) {
            case "getdb":
                sendMessage("connection", this.db);
                return;

            case "checksize":
                sendMessage("success",db.getSize());
                return;

            case "info":
                bundle = ResourceBundle.getBundle("lab8.i18n.Text", message.getLocale());
                String info = bundle.getString("info")+db.getSize()+bundle.getString("info-part");
                sendMessage(info, endFlag);
                return;


            case "save":
                if (wardrobe != null) {
                    db.saveHats(wardrobe, message.getArgumentAs(String.class));
                    sendMessage("Гардероб сохранён в файл");
                }

                sendMessage("Гардероб пуст, сохранять нечего");
                return;

            case "clear":
                wardrobe.removeAll(message.getUserName());
                db.removeAllHats(message.getUserName(), message.getPassword());
                sendMessage("success", db.loadHats());
                return;

            case "import":
                if (!message.hasArgument()) {
                    sendMessage("Имя не указано.\n" +
                            "Введите \"help import\", чтобы узнать, как пользоваться командой");
                    return;
                }
                try {
                    if (!(message.getArgument() instanceof String)) {
                        sendMessage("Клиент отправил запрос в неверном формате (аргумент сообщения должен быть строкой)", endFlag);
                        return;
                    }
                    WardrobeLoaderSaver.imload(wardrobe, (String) message.getArgument(), this.db, message.getUserName(), message.getPassword());
                    sendMessage("Загрузка успешна! В гардеробе " + wardrobe.size() + " шляп", endFlag);
                   // db.saveHats(wardrobe, AUTOSAVE);
                } catch (WardrobeOverflowException e) {
                    sendMessage("В гардеробе не остмалось места, некоторые шляпы не загрузились", endFlag);
                }
                return;

            case "load":
                try {
                    sendMessage("success", Objects.requireNonNull(db.loadHats()));
                } catch(Throwable e) {
                    sendMessage("error");
                }
                return;

            case "add":
                try {
                    if (!message.hasArgument()) {
                        sendMessage("message.error.use-help");
                        return;
                    }

                    Hat hat = message.getArgumentAsOrNull(Hat.class);
                    if (hat == null) {
                        sendMessage("message.error.illegal-format");
                        return;
                    }

                    if (wardrobe.addH(hat, message.getUserName())!=null) {
                        db.addToDB(hat, message.getUserName(), message.getPassword());
                       // db.saveHats(wardrobe, AUTOSAVE);
                        sendMessage("success", db.loadHats());
                    }
                    return;
                } catch (WardrobeOverflowException e) {
                    sendMessage("message.error.wardrobe-full" + Wardrobe.getMaxCollectionElements() + " шляп.\n" +
                            "message.error.please-delete", endFlag);
                } catch (Exception e) {
                    sendMessage( "message.error.create-hat" + e.getMessage(), endFlag);
                }
                return;

            case "add_min":
                try {
                    if (!message.hasArgument()) {
                        sendMessage("message.error.use-help", endFlag);
                        return;
                    }

                    if (!message.isArgument(Hat.class)) {
                        sendMessage("message.error.illegal-format");
                        return;
                    }

                    if (wardrobe.addIfMin(message.getArgumentAs(Hat.class), message.getUserName())) {
                        db.addToDB(message.getArgumentAs(Hat.class), message.getUserName(), message.getPassword());
                       // db.saveHats(wardrobe, AUTOSAVE);
                        sendMessage("success", db.loadHats());
                    } else sendMessage("message.error.create-hat");
                    return;
                } catch (Exception e) {
                    sendMessage(e.getMessage(), endFlag);
                }

            case "remove":
                try {
                    if (!message.hasArgument()) {
                        sendMessage("message.error.use-help");
                        return;
                    }

                    if (!(message.isArgument(Hat.class))) {
                        sendMessage("message.error.illegal-format");
                        return;
                    }

                    Hat hat = db.removeHat(message.getArgumentAs(Hat.class), message.getUserName(), message.getPassword());
                    if (hat != null) {
                        sendMessage("success",  hat);
                        //db.saveHats(wardrobe, AUTOSAVE);
                    } else sendMessage("message.error.delete-no-hat");
                } catch (Exception e) {
                    sendMessage(e.getMessage(), endFlag);
                }
                return;

            case "edit":
                try {
                    if (!message.hasArgument()) {
                        sendMessage("message.error.use-help");
                        return;
                    }

                    if (!(message.isArgument(Hat.class))) {
                        sendMessage("message.error.illegal-format");
                        return;
                    }

                    if (db.editHat(message.getArgumentAs(Hat.class), message.getUserName(), message.getPassword())) {
                        sendMessage("success",  db.loadHats());
                    } else sendMessage("message.error.edit-no-hat");
                } catch (Exception e) {
                    sendMessage(e.getMessage(), endFlag);
                }
                return;

            case "register":
                bundle = ResourceBundle.getBundle("lab8.i18n.Text", message.getLocale());
                if (message.hasArgument()) {
                    try {
                        String[] args = message.getArgument().toString().split(" ");
                        String usernameS = args[0];
                        String mailS = args[1];
                        if (args.length > 2) {
                            String passwordS = args[2];
                            int resultR = db.executeRegister(usernameS, mailS, passwordS, message.getLocale());
                            if (resultR == 1) {
                                sendMessage("success");
                            } else if (resultR == 0) {
                                sendMessage(bundle.getString("already-registered"));
                            } else {
                                sendMessage(bundle.getString("register-error"));
                            }
                        } else {
                            int resultR = db.executeRegister(usernameS, mailS, (new Integer(Math.round((ZonedDateTime.now()).getNano()))).toString(),message.getLocale());
                            if (resultR == 1) {
                                sendMessage("success");
                            } else if (resultR == 0) {
                                sendMessage(bundle.getString("already-registered"));
                            } else {
                                sendMessage(bundle.getString("register-error"));
                            }
                        }
                    } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
                        sendMessage("Невернный ввод. Вы должны ввести имя пользователя почту и (при желании) пароль разделенный одним пробелом");
                    }
                } else {
                    sendMessage("Укажите имя пользователя и почту при желании пароль");
                }
                break;
            case "login":
                try {
                    String[] args = message.getArgument().toString().split(" ");
                    String usernameS = args[0];
                    String passwordS = args[1];
                    int result = db.executeLogin(usernameS, passwordS);
                    if (result>2) {
                        String color = (((new Integer(result)).toString()).substring(0,6));
                        sendMessage(usernameS +" #"+color);
                    } else if (result == 1) {
                        sendMessage((bundle.getString("message.error.register-first")));
                    } else if (result == 2) {
                        sendMessage(bundle.getString(("message.error.wrong-pass")));
                    } else {
                        sendMessage((bundle.getString("message.error.login")));
                    }
                } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
                    sendMessage(bundle.getString(("message.error.need-login-pass")));
                }
                break;

            default:
                if (message.getMessage().length() < 64)
                    sendMessage(("message.error.unknown-command") + message.getMessage() + ("message.error.use-help"), endFlag);
                else
                    sendMessage(("message.error.unknown-command")+("message.error.use-help"), endFlag);
        }
    }
}
