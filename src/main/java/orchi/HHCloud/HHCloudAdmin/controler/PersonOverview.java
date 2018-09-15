package orchi.HHCloud.HHCloudAdmin.controler;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import orchi.HHCloud.HHCloudAdmin.Main;
import orchi.HHCloud.HHCloudAdmin.Util;
import orchi.HHCloud.HHCloudAdmin.model.Person;
import orchi.HHCloud.user.DataUser;

import java.util.Optional;

public class PersonOverview {
    @FXML
    private AnchorPane personOverview;

    @FXML
    private TableView<Person> personTable;
    @FXML
    private TableColumn<Person, String> firstNameColumn;
    @FXML
    private TableColumn<Person, String> lastNameColumn;

    @FXML
    private Label idLabel;
    @FXML
    private Label firstNameLabel;
    @FXML
    private Label lastNameLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label genderLabel;
    @FXML
    private Label verifiedLabel;
    @FXML
    private Label userNameLabel;

    private Main mainApp;

    {

    }

    @FXML
    private void initialize() {

        //iniciarlizar la tabla de personas con dos columnas
        firstNameColumn.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());
        lastNameColumn.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());

        // limpiar detalle de persona
        showPersonDetails(null);

        // Escuchar por cambios de selecion y mostrar los datos de personas cuando cambian
        personTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showPersonDetails(newValue));
    }

    public void setPersons(ObservableList<Person> personData) {
        personTable.setItems(personData);
    }

    public void setAppMain(Main appMain) {
        this.mainApp = appMain;
        setPersons(mainApp.getPersonData());
    }

    private String toGenderString(String g) {
        String gender = "No espesificado";
        if (g != null) {
            if (!g.equals("")) {
                if (g.equals("m")) {
                    gender = "Hombre";
                } else {
                    gender = "Mujer";
                }
            }
        }
        return gender;
    }

    private String toVerifiedString(boolean iv) {
        String isVerified = "Cuenta no verificada";
        if (iv) {
            isVerified = "Cuenta verificada";
        }
        return isVerified;
    }

    private void showPersonDetails(Person person) {
        if (person != null) {
            idLabel.setText(person.getId());
            firstNameLabel.setText(person.getFirstName());
            lastNameLabel.setText(person.getLastName());
            emailLabel.setText(person.getEmail());
            genderLabel.setText(toGenderString(person.getGender()));
            verifiedLabel.setText(toVerifiedString(person.isIsVerified()));
            userNameLabel.setText(person.getUsername());
        } else {
            idLabel.setText("");
            firstNameLabel.setText("");
            lastNameLabel.setText("");
            emailLabel.setText("");
            genderLabel.setText("");
            verifiedLabel.setText("");
            userNameLabel.setText("");
        }
    }


    /**
     * LLamada cuando el usuario clikcea el boton nuevo,
     * para registrar un nuevo usuario en la plataforma
     */
    @FXML
    private void handleNewPerson() {
        Person tempPerson = new Person();
        boolean okClicked = mainApp.showPersonEditDialog(tempPerson);


        if (okClicked) {
            DataUser user = (DataUser) Util.personToUser(tempPerson);
            if( mainApp.getClient().getService().createUser(user)!=null){
                mainApp.getPersonData().add(tempPerson);
            }else{
                tempPerson=null;
            }

        }
    }

    /**
     * LLamada cuando el usuario clikcea el boton editar, abiendo el dialogo para editar
     * detalles para el usuario seleccionado
     */
    @FXML
    private void handleEditPerson() {
        Person selectedPerson = personTable.getSelectionModel().getSelectedItem();
        if (selectedPerson != null) {

            boolean okClicked = mainApp.showPersonEditDialog(selectedPerson);
            DataUser user = (DataUser) Util.personToUser(selectedPerson);
            if (okClicked) {
                if (mainApp.getClient().getService().editUser(user) != null) {
                    showPersonDetails(selectedPerson);
                }
            }

        }

    }


    /**
     * Llamada cuando se clickea en boton eliminar
     */
    @FXML
    private void handleDeletePerson() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Dialogo de confirmacion");
        alert.setHeaderText("Eliminar datos de usuario");
        alert.setContentText("Al eliminar este usuario no podras recuperar sus datos luego.");

        Optional<ButtonType> confir = alert.showAndWait();
        if (confir.get() == ButtonType.OK) {
            int selectedIndex = personTable.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                Person person = personTable.getItems().get(selectedIndex);
                DataUser user = (DataUser) Util.personToUser(person);
                if (mainApp.getClient().getService().deleteUser(user)) {
                    personTable.getItems().remove(selectedIndex);
                }

            }
        }

    }

}
