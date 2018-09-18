package orchi.HHCloud.HHCloudAdmin.controller;

import com.jfoenix.controls.JFXSpinner;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import orchi.HHCloud.HHCloudAdmin.DefaultUserValidator;
import orchi.HHCloud.HHCloudAdmin.Main;
import orchi.HHCloud.HHCloudAdmin.Util;
import orchi.HHCloud.HHCloudAdmin.model.Person;
import orchi.HHCloud.Start;
import orchi.HHCloud.cipher.CipherProvider;
import orchi.HHCloud.user.DataUser;
import orchi.HHCloud.user.Exceptions.ValidationException;


/**
 * Dialogo para editar detalles de un usuario
 *
 * @author David
 */
public class PersonEditDialog {
    public static final String STYLE_FIELD_INVALID = "-fx-background-color:pink;";
    public static ObservableList<String> itemsComboBoxGender = FXCollections.observableArrayList("n", "f", "m");
    public static ObservableMap<String, String> invalidFields = FXCollections.observableHashMap();
    //public static MapProperty<String,String> invalidFields = new SimpleMapProperty<>();
    CipherProvider cp = Start.getCipherManager().getCipherProvider();
    DefaultUserValidator uv = new DefaultUserValidator();
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
    private ComboBox genderSelect;
    @FXML
    private GridPane gridPane;
    @FXML
    private JFXSpinner spinnerLoad;
    @FXML
    private HBox actionButtos;
    @FXML
    private Button buttonOk;
    @FXML
    private Button buttonCancel;

    private Stage dialogStage;
    private Person person;
    private boolean okClicked = false;
    private boolean isCreate = false;
    private Person tmpPerson;
    private Model model;

    @FXML
    private void initialize() {
        genderSelect.setValue("n");
        genderSelect.setItems(itemsComboBoxGender);
        genderSelect.setConverter(new StringConverter<String>() {
            @Override
            public String toString(String object) {
                String d = "No espesificar";

                if (object.equals("f")) {
                    d = "Mujer";
                }

                if (object.equals("m")) {
                    d = "Hombre";
                }

                return d;
            }

            @Override
            public String fromString(String string) {
                return null;
            }
        });

    }

    /**
     * Configurar stage para este dialogo
     *
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Configurar la persona a ser editada en el dialogo.
     *
     * @param person
     */
    public void setPerson(Person person, boolean iscreate) {
        this.person = person;
        this.tmpPerson = !iscreate ? Util.clonePerson(this.person) : new Person();//Si es editar clona Person si no, crea uno temporal
        this.isCreate = iscreate;
        model = new Model(person);

        idField.setDisable(!iscreate);

        idField.textProperty().bindBidirectional(this.tmpPerson.idProperty());
        emailField.textProperty().bindBidirectional(this.tmpPerson.emailProperty());
        usernameField.textProperty().bindBidirectional(this.tmpPerson.usernameProperty());
        firstNameField.textProperty().bindBidirectional(this.tmpPerson.firstNameProperty());
        lastNameField.textProperty().bindBidirectional(this.tmpPerson.lastNameProperty());
        genderSelect.valueProperty().bindBidirectional(this.tmpPerson.genderProperty());
        verifiedButton.selectedProperty().bindBidirectional(this.tmpPerson.isVerifiedProperty());

        /*
        * Person -> Field
        *    Si es edicion de usuario, tiene q desifrar la contraseña para pasarla a el Field y sifrar de Field a Person
        *
        *    Si es crear un nuevo usuario, no sifra nada
        * */
        passwordField.textProperty().bindBidirectional(this.tmpPerson.passwordProperty(), new StringConverter<String>() {
            @Override
            public String toString(String object) {
                return !iscreate ? cp.decrypt(object) : (object);
            }

            @Override
            public String fromString(String string) {
                return !iscreate ? cp.encrypt(string) : string;
            }
        });

        this.tmpPerson.passwordProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                System.out.println(oldValue + " " + newValue);
            }
        });

        idField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

            }
        });

        usernameField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    DataUser u = new DataUser();
                    u.setUsername(newValue);
                    uv.validateUsername(u);
                    usernameField.setStyle("");
                    invalidFields.remove("username");
                } catch (ValidationException e) {
                    invalidFields.put("username", "invalid");
                    usernameField.setStyle(STYLE_FIELD_INVALID);
                    //e.printStackTrace();
                }
            }
        });

        firstNameField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    DataUser u = new DataUser();
                    u.setFirstName(newValue);
                    uv.validateFirstName(u);
                    firstNameField.setStyle("");
                    invalidFields.remove("firstname");
                } catch (ValidationException e) {
                    invalidFields.put("firstname", "invalid");
                    firstNameField.setStyle(STYLE_FIELD_INVALID);
                    //e.printStackTrace();
                }
            }
        });

        lastNameField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    DataUser u = new DataUser();
                    u.setLastName(newValue);
                    uv.validateLastName(u);
                    lastNameField.setStyle("");
                    invalidFields.remove("lastname");
                } catch (ValidationException e) {
                    invalidFields.put("lastname", "invalid");
                    lastNameField.setStyle(STYLE_FIELD_INVALID);
                    // e.printStackTrace();
                }
            }
        });

        emailField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    DataUser u = new DataUser();
                    u.setEmail(newValue);
                    uv.validateEmail(u);
                    emailField.setStyle("");
                    invalidFields.remove("email");
                } catch (ValidationException e) {
                    invalidFields.put("email", "invalid");
                    emailField.setStyle(STYLE_FIELD_INVALID);
                    //e.printStackTrace();
                }
            }
        });


        passwordField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    DataUser u = new DataUser();
                    u.setPassword(newValue);
                    uv.validatePassword(u);
                    passwordField.setStyle("");
                    invalidFields.remove("password");
                } catch (ValidationException e) {
                    invalidFields.put("password", "invalid");
                    passwordField.setStyle(STYLE_FIELD_INVALID);
                    //e.printStackTrace();
                }
            }
        });

        invalidFields.addListener(new MapChangeListener<String, String>() {
            @Override
            public void onChanged(Change<? extends String, ? extends String> change) {
                System.out.println(change.getMap().size());
                buttonOk.setDisable(change.getMap().size() > 0);
            }
        });
        //buttonOk.disableProperty().bind(invalidFields.emptyProperty().not());

        actionButtos.disableProperty().bind(model.loadin);
        gridPane.disableProperty().bind(model.loadin);
        spinnerLoad.visibleProperty().bind(model.loadin);

    }

    /**
     * Genera un nuevo id de usuario
     */
    public void generateId() {
        if (isCreate) idField.setText(Long.toString(System.currentTimeMillis()));
    }

    /**
     * LLamada cuando el usuario clickea Ok
     */
    @FXML
    private void handleOk() {

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                isInputValid();
                Thread.sleep(2000);
                if (isCreate) {
                    Main.client.getService().createUser((DataUser) Util.personToUser(tmpPerson));
                }
                if (!isCreate) {
                    Main.client.getService().editUser((DataUser) Util.personToUser(tmpPerson));
                }
                return null;
            }
        };
        task.setOnScheduled(event -> {
            model.loadin.setValue(true);
        });
        task.setOnCancelled(event -> {
            model.loadin.setValue(false);
        });
        task.setOnSucceeded(event -> {
            Util.setPersonInPerson(person, tmpPerson);
            if (isCreate) {
                person.setPassword(cp.encrypt(tmpPerson.getPassword()));
            }
            model.loadin.setValue(false);
            okClicked = true;
            dialogStage.close();
        });
        task.setOnFailed(event -> {
            model.loadin.setValue(false);
            Util.exceptionDialog(event.getSource().getException());
        });

        new Thread(task).start();
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
     */
    private void isInputValid() throws Exception {
        String errorMessage = "";

        if (idField.getText() == null || idField.getText().length() == 0) {
            errorMessage += "No valid id!\n";
        }
        if (firstNameField.getText() == null || firstNameField.getText().length() == 0) {
            errorMessage += "No valid first name!\n";
        }
        if (lastNameField.getText() == null || lastNameField.getText().length() == 0) {
            errorMessage += "No valid last name!\n";
        }
        if (emailField.getText() == null || lastNameField.getText().length() == 0) {
            errorMessage += "No valid last name!\n";
        }
        if (passwordField.getText() == null || lastNameField.getText().length() == 0) {
            errorMessage += "No valid last name!\n";
        }
        if (usernameField.getText() == null || lastNameField.getText().length() == 0) {
            errorMessage += "No valid last name!\n";
        }
        if (errorMessage.length() > 0) {
            throw new Exception(errorMessage);
        }


    }


    public static class Model {
        public Person person;

        public BooleanProperty loadin = new SimpleBooleanProperty(false);


        public Model(Person person) {
            this.person = person;

        }
    }

}