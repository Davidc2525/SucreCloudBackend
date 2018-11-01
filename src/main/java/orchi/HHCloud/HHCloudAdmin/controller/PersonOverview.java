package orchi.HHCloud.HHCloudAdmin.controller;

import com.jfoenix.controls.JFXSpinner;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import orchi.HHCloud.HHCloudAdmin.Main;
import orchi.HHCloud.HHCloudAdmin.Util;
import orchi.HHCloud.HHCloudAdmin.model.Person;
import orchi.HHCloud.Start;
import orchi.HHCloud.quota.Exceptions.QuotaException;
import orchi.HHCloud.user.DataUser;
import orchi.HHCloud.user.Exceptions.UserException;
import orchi.HHCloud.user.User;
import orchi.HHCloud.user.Users;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class PersonOverview implements Initializable {

    public static ObservableList<Person> personData = FXCollections.observableArrayList();
    public static ObservableList<String> itemsComboBoxFilterBy = FXCollections.observableArrayList("Nombre", "Usuario", "Id", "Correo");

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
    @FXML
    private Button openQuotaDialog;
    @FXML
    private Button userAvailable;
    @FXML
    private TextField filterInput;
    @FXML
    private JFXSpinner spinnerWait;
    @FXML
    private ComboBox filterBy;

    private Main mainApp;


    private void loadUsers() {
        Task<Users> task = new Task<Users>() {
            @Override
            protected Users call() throws Exception {
                return Main.client.getService().getAllUsers();
            }
        };
        task.setOnSucceeded(ws -> {
            Users users = (Users) ws.getSource().getValue();
            users.getUsers().forEach((User u) -> {
                DataUser user = (DataUser) u;
                personData.add(new Person(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getPassword(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.isEmailVerified(),
                        user.getGender()
                ));
            });
        });

        new Thread(task).start();
    }

    private void getUser(Person p){
        spinnerWait.setVisible(true);
        if(p.getPassword().equalsIgnoreCase("")){
            Task<User> task = new Task<User>() {
                @Override
                protected User call() throws Exception {
                    return Main.client.getService().getUser(p.getEmail());
                }
            };
            task.setOnSucceeded(ws -> {
                spinnerWait.setVisible(false);
                Person np = Util.userToPerson((User) ws.getSource().getValue());
                //personData.get(personData.indexOf(p)).setPassword(np.getPassword());
                int index =personData.indexOf(p);
                Person pind = personData.get(index);
                pind.setPassword(np.getPassword());
                pind.setLastName(np.getLastName());
                pind.setFirstName(np.getFirstName());
                pind.setUsername(np.getUsername());
               // personTable.getSelectionModel().select(index);
                setPersonDetails(np);
            });

            new Thread(task).start();
        }else{

            setPersonDetails(p);
        }

    }

    private void searchUsers(String query) {
        spinnerWait.setVisible(true);
        Task<Users> task = new Task<Users>() {
            @Override
            protected Users call() throws Exception {
                return Main.client.getService().search(query);
            }
        };
        task.setOnSucceeded(ws -> {
            spinnerWait.setVisible(false);
            personData.clear();
            Users users = (Users) ws.getSource().getValue();
            users.getUsers().forEach((User u) -> {
                DataUser user = (DataUser) u;
                personData.add(new Person(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getPassword(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.isEmailVerified(),
                        user.getGender()
                ));
            });
        });

        new Thread(task).start();
    }

    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     *
     * @param location  The location used to resolve relative paths for the root object, or
     *                  <tt>null</tt> if the location is not known.
     * @param resources The resources used to localize the root object, or <tt>null</tt> if
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {


        //iniciar combobox de filtrar resultado de filtrado
        //filterBy.setValue("Nombre");
        //filterBy.setItems(itemsComboBoxFilterBy);


        //iniciarlizar la tabla de personas con dos columnas
        firstNameColumn.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());
        lastNameColumn.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());




        filterInput.textProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("escibiendo");
            //String filterby = (String) filterBy.getValue();
            //System.out.println("filtrando por " + filterby);
            //filterby = filterby.toLowerCase();
            //String finalFilterby = filterby;
            if(newValue.length()==0){
                personData.clear();
                loadUsers();
            }else if(!newValue.startsWith("*")){
                searchUsers(newValue);
            }
        });

        personTable.setItems(personData);
        // limpiar detalle de persona
        setPersonDetails(null);

        // Escuchar por cambios de selecion y mostrar los datos de personas cuando cambian
        personTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if(filterInput.getText().length()>0&&!filterInput.getText().startsWith("*")){
                        getUser(newValue);
                    }else{
                        setPersonDetails(newValue);
                    }

                });


        loadUsers();
    }

    private String toGenderString(String g) {
        String gender = "No espesificado";
        if (g != null) {
            if (!g.equals("")) {
                if (g.equals("m")) {
                    gender = "Hombre";
                }
                if (g.equals("f")) {
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

    private void setPersonDetails(Person person) {
        if (person != null) {
            openQuotaDialog.setDisable(false);
            userAvailable.setDisable(false);
            idLabel.setText(person.getId());
            firstNameLabel.setText(person.getFirstName());
            lastNameLabel.setText(person.getLastName());
            emailLabel.setText(person.getEmail());
            genderLabel.setText(toGenderString(person.getGender()));
            verifiedLabel.setText(toVerifiedString(person.isIsVerified()));
            userNameLabel.setText(person.getUsername());
        } else {
            openQuotaDialog.setDisable(true);
            userAvailable.setDisable(true);
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
        showCreateOrEditUser(tempPerson, true);
        if (true) {
            return;
        }
        boolean okClicked = showPersonEditDialog(tempPerson, true);

        if (okClicked) {
            DataUser user = (DataUser) Util.personToUser(tempPerson);

            try {
                if (Main.client.getService().createUser(user) != null) {
                    tempPerson.setPassword(Start.getCipherManager().getCipherProvider().encrypt(tempPerson.getPassword()));
                    personData.add(tempPerson);
                } else {
                    tempPerson = null;
                }
            } catch (UserException e) {
                e.printStackTrace();
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
            showCreateOrEditUser(selectedPerson, false);
            setPersonDetails(selectedPerson);
            if (true) {
                return;
            }
            boolean okClicked = showPersonEditDialog(selectedPerson, false);
            DataUser user = (DataUser) Util.personToUser(selectedPerson);
            if (okClicked) {
                try {
                    if (Main.client.getService().editUser(user) != null) {
                        setPersonDetails(selectedPerson);
                    }
                } catch (UserException e) {
                    e.printStackTrace();
                } catch (QuotaException e) {
                    e.printStackTrace();
                }
            }

        }

    }


    /**
     * Llamada cuando se clickea en boton eliminar
     */
    @FXML
    private void handleDeletePerson() {

        Optional<ButtonType> confir = Util.dialog(Alert.AlertType.CONFIRMATION,
                "Dialogo de confirmacion",
                "Eliminar datos de usuario",
                "Al eliminar este usuario no podras recuperar sus datos luego.");

        if (confir.get() == ButtonType.OK) {
            int selectedIndex = personTable.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                Person person = personTable.getItems().get(selectedIndex);
                DataUser user = (DataUser) Util.personToUser(person);
                try {
                    if (Main.client.getService().deleteUser(user)) {
                        //personTable.getItems().remove(selectedIndex);
                        personData.remove(selectedIndex);
                    }
                } catch (UserException e) {
                    e.printStackTrace();
                } catch (QuotaException e) {
                    e.printStackTrace();
                }

            }
        }

    }

    /**
     * Llamada cuando se clickea en boton eliminar
     */
    @FXML
    private void handleQuotaPerson() {

        Person selectedPerson = personTable.getSelectionModel().getSelectedItem();
        if (selectedPerson != null) {

            boolean okClicked = showQuotaDialog(selectedPerson);
            DataUser user = (DataUser) Util.personToUser(selectedPerson);
            if (okClicked) {
                /*if (mainApp.getClient().getService().editUser(user) != null) {
                    setPersonDetails(selectedPerson);
                }*/
            }

        }

    }
    /**
     * Llamada cuando se clickea en boton edicion de disponibilidad de usuario
     */
    @FXML
    private void handleAvailableUser() {

        Person selectedPerson = personTable.getSelectionModel().getSelectedItem();
        if (selectedPerson != null) {

            boolean okClicked = showAvailableDialog(selectedPerson);
            DataUser user = (DataUser) Util.personToUser(selectedPerson);
            if (okClicked) {
                /*if (mainApp.getClient().getService().editUser(user) != null) {
                    setPersonDetails(selectedPerson);
                }*/
            }

        }

    }

    public void updateTalbePerson() {
        setPersonDetails(null);
        new Thread(() -> {
            spinnerWait.setVisible(true);

            personData.clear();


            loadUsers();
            ;
            spinnerWait.setVisible(false);
        }).start();

    }

    public void showCreateOrEditUser(Person person, Boolean isCreate) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getClassLoader().getResource("PersonEditDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edicion de usuario");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(Main.primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the person into the controller.
            PersonEditDialog controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setPerson(person, isCreate);

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();


        } catch (IOException e) {
            e.printStackTrace();

        }
    }
    /**dialogos de edicion*/
    /**
     * mostrar dialogo de edicion de usuario
     */
    public boolean showPersonEditDialog(Person person, boolean isCreate) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getClassLoader().getResource("PersonEditDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edicion de usuario");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(Main.primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the person into the controller.
            PersonEditDialog controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setPerson(person, isCreate);

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * mostrar dialogo de quota de usuario
     */
    public boolean showQuotaDialog(Person person) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getClassLoader().getResource("QuotaEdit.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Cuota de usuario");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(Main.primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the person into the controller.
            QuotaEdit controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setPerson(person);

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * mostrar dialogo de quota de usuario
     */
    public boolean showAvailableDialog(Person person) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getClassLoader().getResource("AvailableUser.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edicion de disponibilidad de usuario");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(Main.primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the person into the controller.
            Available controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setPerson(person);

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
