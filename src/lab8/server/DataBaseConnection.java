package lab8.server;

import lab8.previous.Hat;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static lab8.previous.Hat.contentFromString;

public class DataBaseConnection implements Serializable {
    private final static byte[] SALT = "HF2Ddf3s436".getBytes();

    private Connection connection = null;
    private AtomicInteger hatindex = new AtomicInteger(Math.round((ZonedDateTime.now()).getNano()));
    private AtomicInteger userindex = new AtomicInteger(Math.round((ZonedDateTime.now()).getNano()));

    public DataBaseConnection() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        //System.out.println("Installed Driver");
        String url = "jdbc:postgresql://localhost:5432/studs";
        String name = "lesti";
        String pass = "rbh.ifkfgf";
        try {
            connection = DriverManager.getConnection(url, name, pass);

        } catch (SQLException e) {
            e.printStackTrace();
            // System.out.println("Не удалось подключиться к базе данных");
        }

        System.out.println("Успешное подключение\n");
    }

    Connection getConnection() {
        return this.connection;
    }

    int getSize() {
        int result = -1;
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT count(id) from hats");
            ResultSet count = statement.executeQuery();
            if (count.next()) {
                result = count.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    Hat[] loadHats() {
        try {
            PreparedStatement preStatement = connection.prepareStatement("SELECT * FROM hats ORDER BY id");
            ResultSet result = preStatement.executeQuery();

            List<Hat> ret = new LinkedList<>();
            while (result.next()) {
                String username = result.getString("username");
                int size = result.getInt("size");
                String color = result.getString("color");
                int shelf = result.getInt("shelf");
                Hat h = new Hat(size, color, shelf);
                h.setCreatedDate(ZonedDateTime.ofInstant(result.getTimestamp("createdDate").toInstant(), ZoneId.systemDefault()));
                h.setUsername(username);
                h.setId(result.getLong("id"));
                h.setUserColor(result.getString("userColor"));
                h.setContent(contentFromString(result.getString("contents")));
                ret.add(h);
            }

            return ret.toArray(new Hat[0]);
        } catch (SQLException e) {
            return null;
        }
    }

    void addToDB(Hat h, String username, String password) {
        try {
            addHat(h, username, password);
        } catch (Exception e) {
            e.printStackTrace();
            //System.out.println("Ошибка при добавлении шляпы в базу данных");
        }
    }

    private void addHat(Hat h, String username, String password) throws SQLException {
        hatindex.incrementAndGet();

        if ((this.executeLogin(username, password)) > 2) {
            PreparedStatement preStatement = connection.prepareStatement("" +
                    "INSERT INTO hats VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

            int pointer = 0;
            preStatement.setInt(++pointer, new Integer(String.valueOf(hatindex)));
            preStatement.setInt(++pointer, h.getSize());
            preStatement.setString(++pointer, h.getColor());
            preStatement.setInt(++pointer, h.getShelf());
            preStatement.setString(++pointer, h.contentlist());
            preStatement.setTimestamp(++pointer, Timestamp.from((h.getCreatedDate()).toInstant()));
            preStatement.setString(++pointer, username);
            preStatement.setString(++pointer, getUserColor(username, password));
            preStatement.executeUpdate();
        }
    }

    public String getUserColor(String username, String password) {
        return Integer.toString(this.executeLogin(username, password)).substring(0, 6);
    }

    void clear() throws SQLException {
        PreparedStatement preStatement = connection.prepareStatement("DELETE FROM hats");
        preStatement.executeUpdate();
    }

    boolean removeAllHats(String username, String password) {
        if (this.executeLogin(username, password) > 2) {
            try {
                PreparedStatement preStatement = connection.prepareStatement("DELETE FROM hats WHERE username = ?");
                preStatement.setString(1, username);

                return preStatement.executeUpdate() > 0;
            } catch (Exception e) {
                //System.out.println("database-error.delete");
            }
        }

        return false;
    }

    Hat removeHat(Hat hat, String username, String password) {
        Hat rhat = null;

        if (this.executeLogin(username, password) > 2) {
            try {
                long id = hat.getId();

                if (!hat.hasId()) {
                    PreparedStatement preStatement = connection.prepareStatement("" +
                            "SELECT id FROM hats " +
                            "WHERE size = ? AND color = ? AND shelf = ? AND contents = ? AND username = ?");

                    int pointer = 0;
                    preStatement.setInt(++pointer, hat.getSize());
                    preStatement.setString(++pointer, hat.getColor());
                    preStatement.setInt(++pointer, hat.getShelf());
                    preStatement.setString(++pointer, hat.contentlist());
                    preStatement.setString(++pointer, username);

                    ResultSet result = preStatement.executeQuery();
                    if (!result.next()) {
                        return null;
                    }

                    id = result.getLong("id");
                }

                PreparedStatement preStatement1 = connection
                        .prepareStatement("DELETE FROM hats WHERE username = ? AND id = ? RETURNING *");

                int pointer = 0;
                preStatement1.setString(++pointer, username);
                preStatement1.setLong(++pointer, id);

                rhat = Hat.fromResultSet(preStatement1.executeQuery());
            } catch (Exception e) {
                //System.out.println("database-error.delete");
                e.printStackTrace();
            }
        }

        return rhat;
    }

    boolean editHat(Hat hat, String username, String password) {
        if (this.executeLogin(username, password) > 2 && hat.hasId()) {
            try {
                PreparedStatement preStatement = connection.prepareStatement("" +
                        "SELECT shelf FROM hats WHERE username = ? AND id = ?");

                int pointer = 0;
                preStatement.setString(++pointer, hat.getUsername());
                preStatement.setLong(++pointer, hat.getId());

                ResultSet result = preStatement.executeQuery();
                if (!result.next()) {
                    return false;
                }

                PreparedStatement preStatement1 = connection.prepareStatement("" +
                        "UPDATE hats SET size = ?, color = ?, shelf = ?, contents = ? WHERE username = ? AND id = ?");

                pointer = 0;
                preStatement1.setInt(++pointer, hat.getSize());
                preStatement1.setString(++pointer, hat.getColor());
                preStatement1.setInt(++pointer, hat.getShelf());
                preStatement1.setString(++pointer, hat.contentlist());
                preStatement1.setString(++pointer, hat.getUsername());
                preStatement1.setLong(++pointer, hat.getId());

                return preStatement1.executeUpdate() != 0;
            } catch (Exception e) {
                //System.out.println("database-error.delete");
                e.printStackTrace();
            }
        }

        return false;
    }

    int executeLogin(String login, String pass) {
        try {
            PreparedStatement preStatement = connection.prepareStatement("" +
                    "SELECT * FROM users WHERE username = ? and password_hash = ?");
            String hash = computeSaltedBase64Hash(pass);
            preStatement.setString(1, login);
            preStatement.setString(2, hash);
            ResultSet result = preStatement.executeQuery();
            if (result.next()) return result.getInt("id");
            else {
                PreparedStatement preStatement2 = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
                preStatement2.setString(1, login);
                ResultSet result2 = preStatement2.executeQuery();
                if (result2.next()) return 2;
                else return 1;
            }
        } catch (Exception e) {
            //System.out.println("login-error");
            return -1;
        }
    }


    int executeRegister(String login, String mail, String pass, Locale locale) {
        int userindex = this.userindex.addAndGet(050505);

        try {
            PreparedStatement ifLog = connection.prepareStatement("SELECT * FROM users WHERE username = ?");

            ifLog.setString(1, login);
            ResultSet result = ifLog.executeQuery();
            if (result.next()) {
                return 0;
            }
            String hash = computeSaltedBase64Hash(pass);
            PreparedStatement statement = connection.prepareStatement("INSERT INTO users VALUES (?, ?, ?, ?)");
            statement.setInt(1, userindex);
            statement.setString(2, login);
            statement.setString(3, mail);
            statement.setString(4, hash);
            statement.executeUpdate();
            new Thread(() -> JavaMail.registration(mail, pass, locale)).start();
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            //
            // System.out.println("signup-error");
            return -1;
        }
    }

    private static String computeSaltedBase64Hash(String password) throws NoSuchAlgorithmException {
        // transform the password string into a byte[]. we have to do this to work with it later.
        byte[] passwordBytes = password.getBytes();
        byte[] saltBytes;

        saltBytes = DataBaseConnection.SALT;

        // MessageDigest converts our password and salt into a hash.
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
        // concatenate the salt byte[] and the password byte[].
        byte[] saltAndPassword = concatArrays(saltBytes, passwordBytes);
        // create the hash from our concatenated byte[].
        byte[] saltedHash = messageDigest.digest(saltAndPassword);
        // get java's base64 encoder for encoding.
        Base64.Encoder base64Encoder = Base64.getEncoder();
        // create a StringBuilder to build the result.

        // return a salt and salted hash combo.
        return base64Encoder.encodeToString(saltBytes) + // base64-encode the salt and append it.
                base64Encoder.encodeToString(saltedHash);
    }

    private static byte[] concatArrays(byte[]... arrays) {
        int concatLength = 0;
        // get the actual length of all arrays and add it so we know how long our concatenated array has to be.
        for (byte[] array : arrays) {
            concatLength += array.length;
        }
        // prepare our concatenated array which we're going to return later.
        byte[] concatArray = new byte[concatLength];
        // this index tells us where we write into our array.
        int index = 0;
        // concatenate the arrays.
        for (byte[] array : arrays) {
            for (byte b : array) {
                concatArray[index] = b;
                index++;
            }
        }
        // return the concatenated arrays.
        return concatArray;
    }

    void saveHats(Wardrobe hats, String filename) {
        try {
            if (hats != null) {
                hats.clear();
                hats.addAll(Arrays.asList(this.loadHats()));
                Iterator<Hat> iterator = hats.iterator();
                while (iterator.hasNext()) {
                    Hat h = iterator.next();
                    WardrobeLoaderSaver.save(hats, filename);
                }
                System.out.println("База данных была обновлена");
            } else {
                System.out.println("Гардероб пуст.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Ошибка при сохранении в базу данных");
        }
    }
}
