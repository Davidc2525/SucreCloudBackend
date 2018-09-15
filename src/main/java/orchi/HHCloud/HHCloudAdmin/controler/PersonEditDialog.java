package orchi.HHCloud.HHCloudAdmin.controler;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import orchi.HHCloud.HHCloudAdmin.model.Person;
import orchi.HHCloud.Start;


/**
 * Dialogo para editar detalles de un usuario
 *
 * @author David
 */
public class PersonEditDialog {

    @FXML
    private TextField idField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField passwordField;
    @FXML
    private ToggleButton verifiedButton;
    @FXML
    private ChoiceBox genderSelect;

    @FXML
    private GridPane gridPane;



    private Stage dialogStage;
    private Person person;
    private boolean okClicked = false;

    @FXML
    private void initialize() {

    }

    /**
     * Configurar stage para este dialogo
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Configurar la persona a ser editada en el dialogo.
     * @param person
     */
    public void setPerson(Person person) {
        this.person = person;
        idField.setText(person.getId());
        emailField.setText(person.getEmail());
        usernameField.setText(person.getUsername());
        firstNameField.setText(person.getFirstName());
        lastNameField.setText(person.getLastName());
        String pass = "";
        if(person.getPassword()!=""){
           pass = Start.getCipherManager().getCipherProvider().decrypt(person.getPassword());
        }
        passwordField.setText(pass);
        verifiedButton.setSelected(person.isIsVerified());
        //genderSelect.

    }

    /**
     * Devuelve true si el usuario clickeo OK, false de lo contrario
     *
     */
    public boolean isOkClicked() {
        return okClicked;
    }

    /**
     * LLamada cuando el usuario clickea Ok
     */
    @FXML
    private void handleOk() {
        if (isInputValid()) {
            person.setFirstName(firstNameField.getText());
            person.setLastName(lastNameField.getText());
            person.setId(idField.getText());
            person.setEmail(emailField.getText());
            person.setIsVerified(verifiedButton.isSelected());
            person.setUsername(usernameField.getText());
            String pass = "";
            if(passwordField.getText()!=""){
                pass = Start.getCipherManager().getCipherProvider().encrypt(passwordField.getText());
            }
            person.setPassword(pass);

            okClicked = true;
            dialogStage.close();
        }
    }

    /**
     * LLamada cuando el usuario clickea el boton cancelar
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * Validacion de datos
     * TODO falta por terminar
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (firstNameField.getText() == null || firstNameField.getText().length() == 0) {
            errorMessage += "No valid first name!\n";
        }
        if (lastNameField.getText() == null || lastNameField.getText().length() == 0) {
            errorMessage += "No valid last name!\n";
        }

        return true;
    }



}