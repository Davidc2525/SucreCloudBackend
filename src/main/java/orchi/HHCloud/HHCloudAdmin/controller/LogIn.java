package orchi.HHCloud.HHCloudAdmin.controller;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import orchi.HHCloud.HHCloudAdmin.Main;
import orchi.HHCloud.HHCloudAdmin.Util;
import orchi.HHCloud.user.DataUser;

public class LogIn {
    @FXML
    private JFXSpinner spinnerLoading;

    @FXML
    private Button loginButton;

    @FXML
    private GridPane contentForm;

    @FXML
    private JFXTextField emailField;

    @FXML
    private JFXPasswordField passwordField;
    private Model model;
    private Stage stage;

    @FXML
    public void initialize() {

    }

    public boolean cantEnter() {
        return model.isCantEnter();
    }

    public void setStage(Stage stage) {
        this.stage = stage;

        model = new Model(this);
    }

    public static class Model {
        private LogIn controller = null;
        private BooleanProperty loadin = new SimpleBooleanProperty(false);
        private BooleanProperty cantEnter = new SimpleBooleanProperty(false);

        private StringProperty email = new SimpleStringProperty("");
        private StringProperty pass = new SimpleStringProperty("");

        public Model(LogIn controller) {
            this.controller = controller;
            this.controller.contentForm.disableProperty().bind(loadin);
            this.controller.spinnerLoading.visibleProperty().bind(loadin);
            this.controller.emailField.textProperty().bindBidirectional(email);
            this.controller.passwordField.textProperty().bindBidirectional(pass);

            this.controller.loginButton.setOnAction(event -> {
                singIn();
            });
        }

        public void singIn() {
            Task<Void> t = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    DataUser user = new DataUser();
                    user.setEmail(email.getValue());
                    user.setPassword(pass.getValue());
                    Main.client.getService().singIn(user);
                    return null;
                }
            };

            t.setOnScheduled(event -> {
                loadin.setValue(true);
                cantEnter.setValue(false);
            });
            t.setOnSucceeded(event -> {
                loadin.setValue(false);
                cantEnter.setValue(true);
                this.controller.stage.close();
            });
            t.setOnFailed(event -> {
                loadin.setValue(false);
                cantEnter.setValue(false);

                Util.exceptionDialog(event.getSource().getException());
            });
            t.setOnCancelled(event -> {
                loadin.setValue(false);
                cantEnter.setValue(false);
            });
            new Thread(t).start();
        }

        public boolean isCantEnter() {
            return cantEnter.get();
        }

        public BooleanProperty cantEnterProperty() {
            return cantEnter;
        }
    }
}
