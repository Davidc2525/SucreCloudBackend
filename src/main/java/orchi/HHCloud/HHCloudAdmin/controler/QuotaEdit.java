package orchi.HHCloud.HHCloudAdmin.controler;

import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import orchi.HHCloud.HHCloudAdmin.Client;
import orchi.HHCloud.HHCloudAdmin.Main;
import orchi.HHCloud.HHCloudAdmin.Util;
import orchi.HHCloud.HHCloudAdmin.model.Person;
import orchi.HHCloud.store.ContentSummary;
import orchi.HHCloud.user.DataUser;
import orchi.HHCloud.user.User;
import org.apache.commons.io.FileUtils;

import java.net.URL;
import java.util.ResourceBundle;

public class QuotaEdit {
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

    @FXML
    public void initialize() {
        client = Main.client;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setPerson(Person person) {
        this.person = person;
        userId.setText(person.getId());
        userName.setText(person.getLastName()+" "+person.getFirstName());

        DataUser u = (DataUser) Util.personToUser(person);
        ContentSummary cs = client.getService().getContentSummary(u);

        spaceConsumed.setText(FileUtils.byteCountToDisplaySize(cs.getLength()));
        quotaSize.setText(FileUtils.byteCountToDisplaySize(cs.getSpaceQuota()));
        inputQuota.setText(Long.toString(cs.getSpaceQuota()));
    }

    public boolean isOkClicked() {
        return okClicked;
    }
}
