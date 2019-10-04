package lab8.previous;

import javafx.scene.paint.Color;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Hat implements Serializable, Comparable<Hat> {

    private String color;
    private int size;
    private int shelf;
    private String username;
    private ZonedDateTime createdDate = ZonedDateTime.now();
    private Thing[] content;
    private String userColor;
    private long id = -1;
    private boolean hasId = false;

    public String getHatColor(){
        return ("шляпа с цветом " + this.color);
    }

    @Override
    public int compareTo(Hat o){return getShelf() - o.getShelf();}

    public void setId(Long id) {
        this.id = id;
        hasId = true;
    }

    public Hat() {}

    /**
     * Добавляет предмет в шляпу
     * @param obj предмет, который нужно добавить
     */
    public void addthing(Thing obj) {
        if (checkspace() != -1) {
            if (checkitem(obj) == -1) {
                System.out.println("Объект " + obj.rus() + " был успешно добавлен в шляпу.");
                this.content[checkspace()] = obj;
            } else {
                System.out.println("Объект " + obj.rus() + " уже есть в этой шляпе");
            }
        } else {
            System.out.println("В шляпе не осталось места. Пожалуйста удалите какой-нибудь предмет прежде чем добавлять новый.\n" +
                    "Объект" + obj.rus() + "не был добавлен в шляпу.");
        }
    }

    /**
     * Проверяет есть ли в шляпе свободное место
     * @return индекс ближайшей свободной ячейки; -1, если свободного места не осталось
     */
    public int checkspace(){
        for (int i=0; i < this.size; i++){
            if (this.content[i]==null){return i;}
        }

        return -1;
    }

    public void setContent(Thing[] content) {
        this.content = content;
    }

    public static Thing[] contentFromString(String stringcontent) {
        Thing[] result = new Thing[8];

        if (!(stringcontent.equals(""))) {
            String[] contentarr = stringcontent.split(", ");
            //Thing result[] = new Thing[contentarr.length];
            for (int i = 0; i < contentarr.length; ++i) {
                result[i] = new Thing(Item.valueOf(contentarr[i]));
            }
        }
        return result;
    }

    /**
     * Метод для того чтобы узнать только содержимое шляпы
     * @return строку в которой перечисленно все содержимое шляпы
     */
    public String contentlist() {
        return Arrays.stream(content).parallel()
                .filter(Objects::nonNull)
                .map(Thing::getItemType)
                .map(Object::toString)
                .collect(Collectors.joining(", "));
    }



    public String ruscontentlist() {
        return Arrays.stream(content).parallel()
                .filter(Objects::nonNull).map(Thing::rus)
                .collect(Collectors.joining(", "));
    }


    /**
     * Проверяет есть ли заданный предмет в шляпе
     * @param item предмет, наличие которого нужно проверить
     * @return индекс найденного предмета; -1, если предмета в шляпе нет
     */
    private int checkitem(Thing item) {
        if (item != null) {
            for (int i = 0; i < size; i++) {
                if (item.equals(content[i])) {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * Выводит информацию о шляпе: размер, цвет, местоположение, дату создания и содержимое
     */
    public String showHat() {
        StringBuilder result = new StringBuilder();
        result.append("Размер шляпы ").append(this.size)
                .append("; Цвет шляпы ").append(this.color)
                .append("; Расположение шляпы: полка №").append(this.shelf)
                .append("; Дата создания: ").append(this.createdDate)
                .append("Владелец: ").append(this.username);

        for (int i = 0; i < this.size; i++) {
            if (this.content[i] != null)
                result.append("В шляпе лежит ").append(this.content[i].rus()).append("\n ");
        }

        return result.toString();
    }

    public boolean hasId() {
        return hasId;
    }

    public Hat(int a, String c, int shelf) {
        this.size = a;
        this.shelf = shelf;
        this.color = c;
        this.content = new Thing[a];
    }

    public Hat(int a, String c, int shelf, int position){
        this.size = a;
        this.shelf = shelf;
        this.color = c;
        this.content = new Thing[a];
    }

    public Hat(int a, String c, int shelf, int position, Thing[] arr){
        this.size=a;
        this.shelf = shelf;
        this.color=c;
        this.content=arr;
    }

    public int getSize() { return size; }

    public String getUsername() {
        return username;
    }

    public void setUsername(String name){ this.username =name;}

    public void setCreatedDate(ZonedDateTime date){this.createdDate=date;}

    public String getColor() { return color; }

    public int getShelf() { return shelf; }

    public Thing[] getContent() { return content; }

    public ZonedDateTime getCreatedDate(){return createdDate;}

    public Hat(long id, int size, int shelf, Color color, String usercolor, Thing[] content) {
        this.size = size;
        this.shelf = shelf;
        this.color = color.toString();
        this.id = id;
        hasId = true;
        this.content = content;
        this.userColor = usercolor;
        this.createdDate = ZonedDateTime.now();
    }

    public Hat(String color, int x, int shelf,  String usercolor, long id, Thing[] content) {
        this.size = x;
        this.shelf = shelf;
        this.id = id;
        hasId = true;
        this.color = color;
        this.userColor = usercolor;
        this.createdDate = ZonedDateTime.now();
        this.content=content;
    }

    /**
     * Создаёт существо на основе объекта {@link ResultSet}
     * @param set объект с данными
     * @return экземпляр существа
     * @throws SQLException если что-то пойдёт не так
     */
    public static Hat fromResultSet(ResultSet set) throws SQLException {
        Hat result = new Hat();
        if (set.next()) {
            result.color = set.getString("color");
            result.size = set.getInt("size");
            result.shelf = set.getInt("shelf");
            result.id = set.getLong("id");
            result.hasId = true;
            result.userColor = set.getString("userColor");
            result.content = contentFromString(set.getString("contents"));
            result.createdDate = ZonedDateTime.ofInstant(set.getTimestamp("createddate").toInstant(), ZoneId.systemDefault());
        }

        return result;
    }

    /**
     * Уникальный идентифкатор существа. Можно использовать для сравнения существ.
     * У двух одинаковых по параметрам существ может быть разный идентификатор.
     * У двух разных существ не может быть один идентифкатор.
     *
     * @return уникальный идентификатор существа
     */
    public long getId(){
        return id;
    }

    public String getUserColor() {
        return userColor;
    }

    public void setUserColor(String color) { this.userColor = color; }

    /**
     * @return время создания существа
     */
    public ZonedDateTime getCreated() {
        return createdDate;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || o.getClass() != getClass()) return false;
        if (this == o) return true;
        Hat c = (Hat) o;
        return  c.getUserColor().equals(getUserColor()) &&
                c.getSize() == getSize() &&
                c.getShelf() == getShelf() &&
                c.getContent() == getContent() &&
                c.getColor().equals(getColor()) &&
                c.getId()==getId() &&
                c.getCreated().equals(getCreated());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSize(), getShelf(), getColor(), getCreated(), getId());
    }
}
