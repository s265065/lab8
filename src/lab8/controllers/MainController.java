package lab8.controllers;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lab8.client.Client;
import lab8.client.Message;
import lab8.previous.Hat;
import lab8.previous.Item;
import lab8.previous.Thing;
import lab8.ui.HatCanvas;
import lab8.ui.HatTable;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class MainController implements Initializable {

    private static ResourceBundle bundle = ResourceBundle.getBundle("lab8.i18n.Text", new Locale("en", "EN"));

    Client client = new Client();

    @FXML
    private Slider sizeSlider;
    @FXML
    private Slider shelfSlider;

    final FileChooser fileChooser = new FileChooser();

    @FXML
    private Button loadButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button submitButton;

    @FXML
    private ColorPicker colorPicker;

    @FXML
    private CheckBox socksCheck;
    @FXML
    private CheckBox nailCheck;
    @FXML
    private CheckBox towelCheck;
    @FXML
    private CheckBox copperwireCheck;
    @FXML
    private CheckBox dentifrieceCheck;
    @FXML
    private CheckBox chiefCheck;
    @FXML
    private CheckBox toothbrushCheck;
    @FXML
    private CheckBox soapCheck;

    @FXML
    private ComboBox comboBox;

    @FXML
    HatCanvas canvas;

    @FXML
    HatTable hatTable;

    @FXML
    private Label usernameLabel;

    public MainController() {
        Executors.newSingleThreadScheduledExecutor((runnable) -> {
            Thread ret = Executors.defaultThreadFactory().newThread(runnable);

            ret.setDaemon(true);
            return ret;
        }).scheduleAtFixedRate(() -> updateHats(client.loadHats()), 0, 500, TimeUnit.MILLISECONDS);
    }

    @FXML
    private void submitClick(ActionEvent event) {
        String command = Objects.toString(comboBox.getValue());

        if (command == null) {
            return;
        }

        Hat selectedHat = (Hat) hatTable.getSelectionModel().getSelectedItem();
        Hat hatFromParams = makeHatFromParam();

        if (command.equals(MainController.bundle.getString("command.info"))) {
                        createAlert("INFO",(client.sendCommand("info", null, bundle.getLocale())).getMessage());
        } else if (command.equals(MainController.bundle.getString("command.update"))) {

            change();
        } else if (command.equals(MainController.bundle.getString("command.clear"))) {

            updateHats(client.sendCommand("clear", null, null)
                    .getArgumentAs(Hat[].class));
        } else if (command.equals(MainController.bundle.getString("command.add"))) {
            Message<?> message = client.sendCommand("add", hatFromParams, null);

            if ((message.getMessage()).equals("success")) {
                updateHats(message.getArgumentAs(Hat[].class));
            } else {
                //createAlert("ERRRRRROOOOORRRR!", bundle.getString(message.getMessage()));
            }
        } else if (command.equals(MainController.bundle.getString("command.addifmin"))) {
            Message<?> message = client.sendCommand("add_min", hatFromParams, null);

            if ((message.getMessage()).equals("success")) {
                updateHats(message.getArgumentAs(Hat[].class));
            } else {
                //createAlert("ERRRRRROOOOORRRR!", bundle.getString(message.getMessage()));
            }
        } else if (command.equals(MainController.bundle.getString("command.remove"))) {
            Message<?> message = client.sendCommand("remove", selectedHat == null ? hatFromParams : selectedHat,
                    null);

            if ((message.getMessage()).equals("success")) {
                Hat removed = message.getArgumentAs(Hat.class);

                hatTable.getItems().removeIf(hat -> ((Hat) hat).getId() == removed.getId());
            } else {
                //createAlert("ERRRRRROOOOORRRR!", bundle.getString(message.getMessage()));
            }
        } else if (command.equals(MainController.bundle.getString("command.edit"))) {
            if (selectedHat != null) {
                hatFromParams.setId(selectedHat.getId());
            }

            Message<?> message = client.sendCommand("edit", hatFromParams, null);
            if ((message.getMessage()).equals("success")) {
                updateHats(message.getArgumentAs(Hat[].class));
            } else {
                //createAlert("ERRRRRROOOOORRRR!", bundle.getString(message.getMessage()));
            }
        }
    }

    void createAlert(String head, String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(head);

        alert.setHeaderText(head);
        alert.setContentText(text);

        alert.showAndWait();
    }

    @FXML
    private Hat makeHatFromParam() {
        Hat hat = new Hat(
                (int) (sizeSlider.getValue()),
                colorPicker.getValue().toString(),
                (int) (shelfSlider.getValue())
        );

        if (chiefCheck.isSelected()) {
            hat.addthing(new Thing(Item.CHIEF));
        }
        if (copperwireCheck.isSelected()) {
            hat.addthing(new Thing(Item.COPPERWIRE));
        }
        if (dentifrieceCheck.isSelected()) {
            hat.addthing(new Thing(Item.DENTIFRIECE));
        }
        if (nailCheck.isSelected()) {
            hat.addthing(new Thing(Item.NAIL));
        }
        if (soapCheck.isSelected()) {
            hat.addthing(new Thing(Item.SOAP));
        }
        if (socksCheck.isSelected()) {
            hat.addthing(new Thing(Item.SOCKS));
        }
        if (toothbrushCheck.isSelected()) {
            hat.addthing(new Thing(Item.TOOTHBRUSH));
        }
        if (towelCheck.isSelected()) {
            hat.addthing(new Thing(Item.TOWEL));
        }
        hat.setUsername(client.getUsername());
        hat.setUserColor(client.getUserColor());
        return hat;
    }

    @FXML
    private void loadSelectedClick() {
        Hat hat = (Hat) hatTable.getSelectionModel().getSelectedItem();

        Predicate<Item> contains = (item) -> {
            Thing thing = new Thing(item);

            return Arrays.stream(hat.getContent()).parallel().anyMatch(current -> Objects.equals(current, thing));
        };

        sizeSlider.setValue(hat == null ? 0 : hat.getSize());
        colorPicker.setValue(hat == null ? Color.WHITE : Color.web(hat.getColor()));
        shelfSlider.setValue(hat == null ? 1 : hat.getShelf());
        chiefCheck.setSelected(hat != null && contains.test(Item.CHIEF));
        toothbrushCheck.setSelected(hat != null && contains.test(Item.TOOTHBRUSH));
        copperwireCheck.setSelected(hat != null && contains.test(Item.COPPERWIRE));
        towelCheck.setSelected(hat != null && contains.test(Item.TOWEL));
        soapCheck.setSelected(hat != null && contains.test(Item.SOAP));
        socksCheck.setSelected(hat != null && contains.test(Item.SOCKS));
        dentifrieceCheck.setSelected(hat != null && contains.test(Item.DENTIFRIECE));
        nailCheck.setSelected(hat != null && contains.test(Item.NAIL));
    }

    @FXML
    private void randomClick() {
        Random random = new Random();

        sizeSlider.setValue(random.nextInt(8));
        colorPicker.setValue(Color.color(random.nextDouble(), random.nextDouble(), random.nextDouble()));
        shelfSlider.setValue(random.nextInt(5) + 1);
        chiefCheck.setSelected(false);
        toothbrushCheck.setSelected(false);
        copperwireCheck.setSelected(false);
        towelCheck.setSelected(false);
        soapCheck.setSelected(false);
        socksCheck.setSelected(false);
        dentifrieceCheck.setSelected(false);
        nailCheck.setSelected(false);

        int i = random.nextInt(1000);
        if (i % 10 == 0) {
            chiefCheck.setSelected(true);
        }
        if (i >= 890) {
            copperwireCheck.setSelected(true);
        }
        if (i % 3 == 0) {
            towelCheck.setSelected(true);
        }
        if (i % 5 == 0) {
            soapCheck.setSelected(true);
        }
        if (i <= 110) {
            socksCheck.setSelected(true);
        }
        if (i % 11 == 0) {
            dentifrieceCheck.setSelected(true);
        }
        if (i % 13 == 0) {
            nailCheck.setSelected(true);
        }
        if (i % 7 == 0) {
            toothbrushCheck.setSelected(true);
        }
    }

    @FXML
    private void DAlanguage() {
        bundle = ResourceBundle.getBundle("lab8.i18n.Text", new Locale("da", "DK"));
        change();
    }

    @FXML
    private void ENlanguage() {
        bundle = ResourceBundle.getBundle("lab8.i18n.Text", new Locale("en", "EN"));
        change();
    }

    @FXML
    private void ESlanguage() {
        bundle = ResourceBundle.getBundle("lab8.i18n.Text", new Locale("es", "MX"));
        change();
    }

    @FXML
    private void RUlanguage() {
        bundle = ResourceBundle.getBundle("lab8.i18n.Text", new Locale("ru", "RU"));
        change();
    }

    @FXML
    private void SKlanguage() {
        bundle = ResourceBundle.getBundle("lab8.i18n.Text", new Locale("sk", "SK"));
        change();
    }

    @FXML
    void setUsernameLabel(String name) {
        usernameLabel.setText(name);
    }

    private void onEdited() {
        submitButton.setDisable(false);
    }

    public void loadClick(){
        File file = fileChooser.showOpenDialog(usernameLabel.getScene().getWindow());
        if (file != null) {
            if ((getFileExtension(file).equals("txt"))||(getFileExtension(file).equals("csv"))){
            BufferedReader bufferedReader = null;
            StringBuilder fileContent = new StringBuilder();
            try {
                bufferedReader = new BufferedReader(new FileReader(file));
                String text = null;

                String line;
                while ((line = bufferedReader.readLine()) != null)
                    fileContent.append(line + "\n");

                String content = fileContent.toString();
                client.sendCommand("import", content, null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
            else createAlert("ERRRRRROOORRRR!","incorrect-file-format");
        }
    }

    public void saveClick(){
        File file = fileChooser.showOpenDialog(usernameLabel.getScene().getWindow());
        if (file != null) {
            if ((getFileExtension(file).equals("txt"))||(getFileExtension(file).equals("csv"))){
            client.sendCommand("save", file.getAbsolutePath(), null);
        }
            else createAlert("ERRRRRROOORRRR!","incorrect-file-format");
        }

    }

    public void change() {
        try {
            Stage stageP = (Stage) usernameLabel.getScene().getWindow();
            stageP.close();
        } catch (Throwable ignored) {}

        ObservableList<Hat> hatsObservableList = null;

        if (hatTable != null) {
            hatsObservableList = hatTable.getItems();
        }

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(bundle);
            loader.setController(this);
            loader.setLocation(MainController.class.getResource("/view/Main.fxml"));
            Parent helpUI = loader.load();
            Scene scene = new Scene(helpUI, Color.web("#c085db"));
            Stage stage = new Stage();
            stage.setScene(scene);
            usernameLabel.setText(client.getUsername());
            stage.setResizable(false);
            stage.show();
            stage.setWidth(850);
            stage.setHeight(600);

            colorPicker.valueProperty().addListener((observable, oldValue, newValue) -> onEdited());
            sizeSlider.valueProperty().addListener((observable, oldValue, newValue) -> onEdited());
            shelfSlider.valueProperty().addListener((observable, oldValue, newValue) -> onEdited());
            soapCheck.selectedProperty().addListener((observable, oldValue, newValue) -> onEdited());
            towelCheck.selectedProperty().addListener((observable, oldValue, newValue) -> onEdited());
            toothbrushCheck.selectedProperty().addListener((observable, oldValue, newValue) -> onEdited());
            nailCheck.selectedProperty().addListener((observable, oldValue, newValue) -> onEdited());
            copperwireCheck.selectedProperty().addListener((observable, oldValue, newValue) -> onEdited());
            dentifrieceCheck.selectedProperty().addListener((observable, oldValue, newValue) -> onEdited());
            chiefCheck.selectedProperty().addListener((observable, oldValue, newValue) -> onEdited());
            socksCheck.selectedProperty().addListener((observable, oldValue, newValue) -> onEdited());

            if (hatsObservableList != null) {
                hatTable.setItems(hatsObservableList);
            }

            hatTable.setVisible(true);
            hatTable.heightProperty().addListener((observable, oldValue, newValue) -> hatTable.setPrefHeight(hatTable.getHeight()));

            canvas.clear();
            canvas.setTarget(hatTable.getItems());

            hatTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                    canvas.selectHat((Hat) newValue));

            canvas.setSelectingListener((hat) -> {
                if (hat != null) {
                    hatTable.getSelectionModel().select(hat);
                } else {
                    hatTable.getSelectionModel().clearSelection();
                }
            });

              } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void showCanvas() {
        hatTable.setVisible(false);
        canvas.setVisible(true);
    }

    @FXML
    private void showTable() {
        hatTable.setVisible(true);
        canvas.setVisible(false);
    }

    @FXML
    private void exit() {
        System.exit(0);
    }

    @FXML
    private void menuLogout() throws IOException {
        Stage stageP = (Stage) usernameLabel.getScene().getWindow();
        stageP.close();

        createWin(bundle, "/view/Login.fxml", 250, 300, "Login", new LoginController());
    }

    @FXML
    private void addHelp() {
        HelpController controller = new HelpController(bundle);
        controller.addHelp();
    }

    @FXML
    private void addifminHelp() {
        HelpController controller = new HelpController(bundle);
        controller.addifminHelp();
    }

    @FXML
    private void removeHelp() {
        HelpController controller = new HelpController(bundle);
        controller.removeHelp();
    }

    @FXML
    private void updateHelp() {
        HelpController controller = new HelpController(bundle);
        controller.updateHelp();
    }

    @FXML
    private void infoHelp() {
        HelpController controller = new HelpController(bundle);
        controller.infoHelp();
    }

    @FXML
    private void clearHelp() {
        HelpController controller = new HelpController(bundle);
        controller.clearHelp();
    }

    @FXML
    private void hatparamHelp() {
        HelpController controller = new HelpController(bundle);
        controller.hatparamHelp();
    }

    @FXML
    private void aboutapp() {
        HelpController controller = new HelpController(bundle);
        controller.aboutappHelp();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboBox.getItems().addAll(
                MainController.bundle.getString("command.add"),
                MainController.bundle.getString("command.addifmin"),
                MainController.bundle.getString("command.remove"),
                MainController.bundle.getString("command.edit"),
                MainController.bundle.getString("command.update"),
                MainController.bundle.getString("command.info"),
                MainController.bundle.getString("command.clear"));
    }

    private void updateHats(Hat[] hats) {
        Platform.runLater(() -> {
            if (hats == null || hatTable == null) {
                return;
            }

            ObservableList<Hat> tableHats = hatTable.getItems();
            if (tableHats == null) {
                return;
            }

            tableHats.removeIf(hat -> Arrays.stream(hats).parallel().noneMatch(h -> hat.getId() == h.getId()));
            Map<Long, Integer> tableHatsIds = new HashMap<>();
            for (int i = 0; i < tableHats.size(); ++i) {
                tableHatsIds.put(tableHats.get(i).getId(), i);
            }

            for (Hat hat : hats) {
                Integer index = tableHatsIds.get(hat.getId());

                if (index == null) {
                    tableHats.add(hat);
                } else {
                    int i = index;
                    boolean selected = hatTable.getSelectionModel().getSelectedItem() == tableHats.get(i);

                    tableHats.set(i, hat);
                    if (selected) {
                        hatTable.getSelectionModel().select(i);
                    }

                }
            }
        });
    }

    @FXML
    void createWin(ResourceBundle bundle, String file, int width, int height, String title, Object controller) throws IOException {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        FXMLLoader loader = new FXMLLoader();
        loader.setResources(bundle);
        loader.setController(controller);
        loader.setLocation(MainController.class.getResource(file));
        Parent helpUI = loader.load();
        Scene scene = new Scene(helpUI,Color.web("#c085db"));
        stage.setScene(scene);
        stage.setTitle(title);
        stage.setResizable(false);
        stage.show();
        stage.setWidth(width);
        stage.setHeight(height);
    }

    //метод определения расширения файла
    private static String getFileExtension(File file) {
        String fileName = file.getName();
        // если в имени файла есть точка и она не является первым символом в названии файла
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            // то вырезаем все знаки после последней точки в названии файла, то есть ХХХХХ.txt -> txt
            return fileName.substring(fileName.lastIndexOf(".")+1);
            // в противном случае возвращаем заглушку, то есть расширение не найдено
        else return "";}

    public static ResourceBundle getBundle() {
        return bundle;
    }


}
