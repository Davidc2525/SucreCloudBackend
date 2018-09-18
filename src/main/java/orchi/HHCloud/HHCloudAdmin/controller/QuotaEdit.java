package orchi.HHCloud.HHCloudAdmin.controller;

import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.LongStringConverter;
import orchi.HHCloud.HHCloudAdmin.Client;
import orchi.HHCloud.HHCloudAdmin.Main;
import orchi.HHCloud.HHCloudAdmin.Util;
import orchi.HHCloud.HHCloudAdmin.model.Person;
import orchi.HHCloud.quota.Quota;
import orchi.HHCloud.store.ContentSummary;
import orchi.HHCloud.user.DataUser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.WordUtils;

import java.util.regex.Pattern;

public class QuotaEdit {

    @FXML
    private Button buttonCancel;

    @FXML
    private Button buttonEdit;
    @FXML
    private JFXSpinner spinnerLoadin;
    @FXML
    private Label quotaSize;

    @FXML
    private Label spaceConsumed;

    @FXML
    private Label userName;

    @FXML
    private JFXTextField inputQuota;

    @FXML
    private Label userId;

    private Stage dialogStage;
    private Person person;
    private boolean okClicked;
    private Client client;
    private Model model;

    @FXML
    public void initialize() {
        client = Main.client;


        TextFormatter<Long> formatter = new TextFormatter<Long>(
                new LongStringConverter(),
                0L,
                c -> Pattern.matches("\\d*", c.getText()) ? c : null);
        inputQuota.setTextFormatter(formatter);
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setPerson(Person person) {
        this.person = person;

        model = new Model(person, client);
        buttonEdit.disableProperty().bind(model.cantEdit.not());
        inputQuota.disableProperty().bind(model.cantEdit.not());

        spinnerLoadin.visibleProperty().bind(model.loadin);
        userId.textProperty().bind(model.userId);
        userName.textProperty().bind(model.userName);
        spaceConsumed.textProperty().bind(model.spaceConsumed);
        inputQuota.textProperty().bindBidirectional(model.quotaSize);
        quotaSize.textProperty().bindBidirectional(model.quotaSize, new StringConverter<String>() {
            @Override
            public String toString(String object) {
                return FileUtils.byteCountToDisplaySize(Long.valueOf(object));
            }

            @Override
            public String fromString(String string) {
                return null;
            }
        });

    }

    public boolean isOkClicked() {
        return okClicked;
    }

    public void editQuota() {
        Task<Void> t = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                model.loadin.setValue(true);
                client.getService().setQuota((DataUser) Util.personToUser(model.person), new Quota(Long.valueOf(model.quotaSize.getValue())));
                return null;
            }
        };
        t.setOnSucceeded((WorkerStateEvent ws) -> {
            model.loadin.setValue(false);
            model = null;
            dialogStage.close();
        });
        t.setOnFailed((WorkerStateEvent ws) -> {
            model.loadin.setValue(false);
            Util.exceptionDialog(ws.getSource().getException());
            System.out.println("error: " + ws.getSource().getException().getMessage());
        });

        new Thread(t).start();

    }

    public void close() {
        model.cancelAnyTask();
        model = null;
        dialogStage.close();
    }

    public static class Model {

        private final Task<ContentSummary> t;
        public Person person;
        public Client client;

        /*models*/
        public StringProperty userName = new SimpleStringProperty("");
        public StringProperty userId = new SimpleStringProperty("");

        public StringProperty spaceConsumed = new SimpleStringProperty("0");
        public StringProperty quotaSize = new SimpleStringProperty("0");
        public BooleanProperty loadin = new SimpleBooleanProperty(false);
        public BooleanProperty error = new SimpleBooleanProperty(false);
        public BooleanProperty cantEdit = new SimpleBooleanProperty(false);

        public Model(Person person, Client client) {
            this.person = person;
            this.client = client;

            cantEdit.bind(loadin.not());
            userId.setValue(person.getId());
            userName.setValue(WordUtils.capitalize(person.getLastName() + " " + person.getFirstName()));

            DataUser u = (DataUser) Util.personToUser(person);
            ContentSummary cs = null;


            t = new Task<ContentSummary>() {

                @Override
                protected ContentSummary call() throws Exception {
                    Thread.sleep(1000);

                    return client.getService().getContentSummary(u);
                }
            };

            t.setOnScheduled(ws -> {
                loadin.setValue(true);
                error.setValue(false);

            });
            t.setOnSucceeded(h -> {
                ContentSummary css = (ContentSummary) h.getSource().getValue();
                System.out.println("devuelto: " + h.getSource().getValue());
                spaceConsumed.setValue(FileUtils.byteCountToDisplaySize(css.getLength()));
                quotaSize.setValue(Long.toString(css.getSpaceQuota()));
                loadin.setValue(false);

            });
            t.setOnFailed(w -> {
                loadin.setValue(false);
                error.setValue(true);
            });
            t.setOnCancelled(w -> {
                loadin.setValue(false);
                error.setValue(false);
            });
            new Thread(t).start();
        }

        public void cancelAnyTask() {
            t.cancel(true);
        }
    }
}
