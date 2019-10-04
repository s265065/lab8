package lab8.client;

import java.io.Serializable;
import java.util.Locale;

/**
 * Класс, использующийся для передачи сообщений между клиентом и сервером,
 * используется для соответствия требованию о сериализации передаваемых объектов
 * и для решения проблемы с разделением команды и её объектов-аргументов
 */
public class Message<T extends Serializable> implements Serializable {
    private String message;
    private Serializable argument;
    private String userName;
    private String password;
    private Locale locale;

    /**
     * Создаёт сообщение с указанным текстовым запросом, объектом-аргументов и флагом окончания
     * @param message текстовый запрос
     * @param argument объект-аргумент, прикреплённый к сообщению
     */
    public Message(String message, T argument) {
        this.message = message;
        this.argument = argument;
    }

    public Message(String message) {
        this.message = message;
    }

    /**
     * @return текстовый запрос сообщения
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return объект-аргумент, прикреплённый к сообщению
     */
    public Serializable getArgument() {
        return argument;
    }

    public boolean isArgument(Class<?> clazz) {
        return clazz.isInstance(argument);
    }

    public <R extends Serializable> R getArgumentAs(Class<? extends R> clazz) {
        return clazz.cast(argument);
    }

    public <R extends Serializable> R getArgumentAsOrNull(Class<? extends R> clazz) {
        if (!clazz.isInstance(argument)) {
            return null;
        }

        return clazz.cast(argument);
    }

    /**
     * @return true, если к сообщению прикреплён объект-аругмент
     */
    public boolean hasArgument() {
        return argument != null;
    }

    void setUserName(String userName) {
        this.userName = userName;
    }

    void setPassword(String password){this.password=password;}

    void setLocale(Locale locale){this.locale=locale;}

    public String getPassword() {return password;}

    public String getUserName() {
        return userName;
    }
    public Locale getLocale(){return locale;}
}
