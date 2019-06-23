package orchi.HHCloud.HHCloudAdmin;


import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import orchi.HHCloud.HHCloudAdmin.controller.LogIn;
import orchi.HHCloud.HHCloudAdmin.controller.PersonEditDialog;
import orchi.HHCloud.HHCloudAdmin.model.Person;
import orchi.HHCloud.provider.Providers;
import orchi.HHCloud.user.DataUser;
import orchi.HHCloud.user.User;
import orchi.HHCloud.user.Users;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Main extends Application {

    public static ObservableList<Person> personData = FXCollections.observableArrayList();
    public static Client client;
    public static BorderPane root;
    public static Window primaryStage;

    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException {
        launch(args);
    }

    public ObservableList<Person> getPersonData() {
        return personData;
    }

    public void setPersonData(ObservableList<Person> p) {
        this.personData = p;
    }

    public Client getClient() {
        return client;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            client = new Client();
        } catch (Exception e) {
            e.printStackTrace();
            Util.exceptionDialog(e);
            System.exit(1);
        }
        //login
        if(!showLogIn()){
            System.exit(1);
        }

        //loadUsers();

        this.primaryStage = primaryStage;

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getClassLoader().getResource("RootLayout.fxml"));
        root = (BorderPane) loader.load();
        //root.getStylesheets().add(Main.class.getClassLoader().getResource("modena_dark.css").toExternalForm());
        primaryStage.setTitle("HHCloud admin");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();

        showPersonPreview();
    }

    public boolean showLogIn(){
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getClassLoader().getResource("LogIn.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Entrar");
            dialogStage.setResizable(false);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            LogIn controller = loader.getController();
            controller.setStage(dialogStage);

            dialogStage.showAndWait();


            return controller.cantEnter();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void showPersonPreview() throws IOException {
        // Load person overview.
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("PersonOverview.fxml"));
        AnchorPane personOverview = (AnchorPane) loader.load();

        // Set person overview into the center of root layout.
        root.setCenter(personOverview);

        // Give the controller access to the main app.
        //PersonOverview controller = loader.getController();
        //controller.setPersons(personData);
        //controller.setAppMain(this);

    }

    public boolean showPersonEditDialog(Person person) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getClassLoader().getResource("PersonEditDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Person");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the person into the controller.
            PersonEditDialog controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setPerson(person, false);

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ObservableList<Person> getUsers() {
        ObservableList<Person> persons = FXCollections.observableArrayList();
        ;
        Users users = client.getService().getAllUsers();
        users.getUsers().forEach((User u) -> {
            DataUser user = (DataUser) u;
            persons.add(new Person(
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

        return persons;
    }

    private void loadUsers() {
        Users users = client.getService().getAllUsers();
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

    }
}
