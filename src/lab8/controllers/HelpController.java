package lab8.controllers;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class HelpController {
    private static ResourceBundle bundle = ResourceBundle.getBundle("lab8.i18n.Text", new Locale("en", "EN"));

    @FXML
    private VBox addHelpWin;
    @FXML
    private Label addifminHelpWin;
    @FXML
    private Label removeHelpWin;
    @FXML
    private Label infoHelpWin;
    @FXML
    private Label updateHelpWin;
    @FXML
    private Label clearHelpWin;
    @FXML
    private Group hatparamHelpWin;
    @FXML
    private Group aboutappHelpWin;
    @FXML
    private Label sizeHelpLabel;
    @FXML
    private Label shelfHelpLabel;
    @FXML
    private Label colorHelpLabel;
    @FXML
    private Label contentHelpLabel;
    @FXML
    private Label optionsHelpLabel;
    @FXML
    private Label howworksHelpLabel;

    private MainController controller = new MainController();

    @FXML
    private void createWinHelp() {
        try {
            controller.createWin(bundle, "/view/Help.fxml",450, 250, "Help",this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void addHelp() {
        createWinHelp();
        addHelpWin.setVisible(true);
    }

    @FXML
    void addifminHelp() {
        createWinHelp();
        addifminHelpWin.setVisible(true);
    }

    @FXML
    void removeHelp() {
        createWinHelp();
        removeHelpWin.setVisible(true);
    }

    @FXML
    void infoHelp() {
        createWinHelp();
        infoHelpWin.setVisible(true);
    }

    @FXML
    void updateHelp() {
        createWinHelp();
        updateHelpWin.setVisible(true);
    }

    @FXML
    void clearHelp() {
        createWinHelp();
        clearHelpWin.setVisible(true);
    }

    @FXML
    void hatparamHelp() {
        createWinHelp();
        hatparamHelpWin.setVisible(true);
    }

    @FXML
    void aboutappHelp() {
        createWinHelp();
        aboutappHelpWin.setVisible(true);
    }

    @FXML
    void sizeHelp() {
        sizeHelpLabel.setVisible(true);
        shelfHelpLabel.setVisible(false);
        colorHelpLabel.setVisible(false);
        contentHelpLabel.setVisible(false);
    }

    @FXML
    void shelfHelp() {
        shelfHelpLabel.setVisible(true);
        sizeHelpLabel.setVisible(false);
        colorHelpLabel.setVisible(false);
        contentHelpLabel.setVisible(false);
    }

    @FXML
    void colorHelp() {
        colorHelpLabel.setVisible(true);
        shelfHelpLabel.setVisible(false);
        sizeHelpLabel.setVisible(false);
        contentHelpLabel.setVisible(false);

    }

    @FXML
    void contentHelp() {
        contentHelpLabel.setVisible(true);
        shelfHelpLabel.setVisible(false);
        sizeHelpLabel.setVisible(false);
        colorHelpLabel.setVisible(false);
    }

    @FXML
    void optionsHelp() {
        howworksHelpLabel.setVisible(false);
        optionsHelpLabel.setVisible(true);
    }

    @FXML
    void howworksHelp() {
        optionsHelpLabel.setVisible(false);
        howworksHelpLabel.setVisible(true);
    }

    HelpController(ResourceBundle bundle){
        this.bundle=bundle;
    }


}