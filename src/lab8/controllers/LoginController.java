package lab8.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.ResourceBundle;

public class LoginController {

    @FXML
    private VBox registerWin;
    @FXML
    private VBox loginWin;

    @FXML
    private Button toLoginButton;

    @FXML
    private TextField emailFormRegister;
    @FXML
    private TextField usernameFormRegister;
    @FXML
    private TextField passFormRegister;

    @FXML
    private TextField usernameForm;
    @FXML
    private TextField passForm;


    protected MainController controller = new MainController();

    private static ResourceBundle bundle = ResourceBundle.getBundle("lab8.i18n.Text", new Locale("en", "EN"));

    @FXML
    private void DAlanguage() {
        bundle = ResourceBundle.getBundle("lab8.i18n.Text", new Locale("da", "DK"));
        try {
            change();
        } catch (IOException e) {
        }
    }

    @FXML
    private void ENlanguage() {
        bundle = ResourceBundle.getBundle("lab8.i18n.Text", new Locale("en", "EN"));
        try {
            change();
        } catch (IOException e) {
        }
    }

    @FXML
    private void ESlanguage() {
        bundle = ResourceBundle.getBundle("lab8.i18n.Text", new Locale("es", "MX"));
        try {
            change();
        } catch (IOException e) {
        }
    }

    @FXML
    private void RUlanguage() {
        bundle = ResourceBundle.getBundle("lab8.i18n.Text", new Locale("ru", "RU"));
        try {
            change();
        } catch (IOException e) {
        }
    }

    @FXML
    private void SKlanguage() {
        bundle = ResourceBundle.getBundle("lab8.i18n.Text", new Locale("sk", "SK"));
        try {
            change();
        } catch (IOException e) {
        }
    }

    @FXML
    private void change() throws IOException {
        Stage stageP = (Stage) toLoginButton.getScene().getWindow();
        stageP.close();
        controller.createWin(bundle, "/view/Login.fxml",250, 300, "Login", this);
    }

    @FXML
    private void goLogin() {
        registerWin.setVisible(false);
        loginWin.setVisible(true);
    }

    @FXML
    private void goSignup() {
        loginWin.setVisible(false);
        registerWin.setVisible(true);
    }

    @FXML
    private void login() {
        String arg = usernameForm.getText() + " " + passForm.getText();

        String result = controller.client.doLogin("login", arg);
        if (result.equals("success")) {
            Stage stageP = (Stage) toLoginButton.getScene().getWindow();
            stageP.close();
            // controller.createWin(bundle, "view/Main.fxml", 800, 550, "Wardrobe 1.0", controller);
            controller.client.setUsername(usernameForm.getText());
            controller.client.setPassword(passForm.getText());
            // controller.setUsernameLabel(usernameForm.getText());

            controller.change();
        } else {
            createAlert(null, bundle.getString("message.error.register-first"));
        }
    }

    void createAlert(String head, String text){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("EROOOOOOOR!");

        alert.setHeaderText(head);
        alert.setContentText(text);

        alert.showAndWait();
    }

    @FXML
    private void register() {
        String email = emailFormRegister.getText();
        String username = usernameFormRegister.getText();
        String pass = passFormRegister.getText();
        if (pass.equals("")){
            String result = (controller.client.sendCommand("register",username+" "+email+" "+(new Integer(Math.round((ZonedDateTime.now()).getNano()))).toString(), bundle.getLocale())).getMessage();
            if (
                    !(result.equals("success"))
            )
            {
                createAlert("ERRRRRROOORRR!", bundle.getString(result));
            }
        }
        else {
            String result = (controller.client.sendCommand("register",username+" "+email+" "+pass,bundle.getLocale())).getMessage();
            if (!(result.equals("success"))){
                createAlert("ERRROOOOORRR!", bundle.getString(result));  }
            }
    }

    @FXML
    private void exit(ActionEvent event) {
        System.exit(0);
    }
}
