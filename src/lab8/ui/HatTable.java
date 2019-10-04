package lab8.ui;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import lab8.controllers.MainController;
import lab8.previous.Hat;

import java.util.Arrays;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class HatTable extends TableView {

    private TableColumn<Hat, String> colorColumn = new TableColumn<>();
    private TableColumn<Hat, String> usercolorColumn = new TableColumn<>();
    private TableColumn<Hat, String> sizeColumn = new TableColumn<>();
    private TableColumn<Hat, String> shelfColumn = new TableColumn<>();
    private TableColumn<Hat, String> contentsColumn = new TableColumn<>();
    private TableColumn<Hat, String> ownerColumn = new TableColumn<>();
    private TableColumn<Hat, String> createdColumn = new TableColumn<>();
    private TableColumn<Hat, String> idColumn = new TableColumn<>("ID");

    public HatTable() {

        colorColumn.setPrefWidth(80);
        usercolorColumn.setPrefWidth(80);
        sizeColumn.setPrefWidth(40);
        shelfColumn.setPrefWidth(40);
        contentsColumn.setPrefWidth(100);
        ownerColumn.setPrefWidth(60);
        createdColumn.setPrefWidth(80);
        idColumn.setPrefWidth(60);
        setMinWidth(290);
        setMinHeight(290);

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        colorColumn.setCellValueFactory(new PropertyValueFactory<>("color"));
        usercolorColumn.setCellValueFactory(new PropertyValueFactory<>("userColor"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
        shelfColumn.setCellValueFactory(new PropertyValueFactory<>("shelf"));
        ownerColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        createdColumn.setCellValueFactory(new PropertyValueFactory<>("createdDate"));
        contentsColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().contentlist()));

        getColumns().addAll(colorColumn, usercolorColumn, sizeColumn, shelfColumn,
                contentsColumn, ownerColumn, createdColumn, idColumn);
        initLocalizedData(MainController.getBundle());
    }

    public void initLocalizedData(ResourceBundle bundle) {
        colorColumn.setText(bundle.getString("table.color"));
        usercolorColumn.setText(bundle.getString("table.user-color"));
        sizeColumn.setText(bundle.getString("table.size"));
        shelfColumn.setText(bundle.getString("table.shelf"));
        contentsColumn.setText(bundle.getString("table.content"));
        ownerColumn.setText(bundle.getString("table.username"));
        createdColumn.setText(bundle.getString("table.date"));

        setPlaceholder(new Label(bundle.getString("table.empty")));

        contentsColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(Arrays
                .stream(param.getValue().getContent()).parallel()
                .filter(Objects::nonNull).map(item -> bundle.getString("item." + item.getItemType()))
                .collect(Collectors.joining(", "))));

    }
}












