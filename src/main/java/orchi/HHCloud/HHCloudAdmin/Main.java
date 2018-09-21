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
import orchi.HHCloud.HHCloudAdmin.controller.PersonEditDialog;
import orchi.HHCloud.HHCloudAdmin.model.Person;
import orchi.HHCloud.user.DataUser;
import orchi.HHCloud.user.Users;

import java.io.IOException;

public class Main extends Application {

    public static ObservableList<Person> personData = FXCollections.observableArrayList();
    public static Client client;
    public static BorderPane root;
    public static Window primaryStage;

    public static void main(String[] args) {
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
        client = new Client();

        //loadUsers();

        this.primaryStage = primaryStage;
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getClassLoader().getResource("RootLayout.fxml"));
        root = (BorderPane) loader.load();
        primaryStage.setTitle("HHCloud admin");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();

        showPersonPreview();
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
        users.getUsers().forEach((DataUser u) -> {
            persons.add(new Person(
                    u.getId(),
                    u.getUsername(),
                    u.getEmail(),
                    u.getPassword(),
                    u.getFirstName(),
                    u.getLastName(),
                    u.isEmailVerified(),
                    u.getGender()
            ));
        });

        return persons;
    }

    private void loadUsers() {
        Users users = client.getService().getAllUsers();
        users.getUsers().forEach((DataUser u) -> {
            personData.add(new Person(
                    u.getId(),
                    u.getUsername(),
                    u.getEmail(),
                    u.getPassword(),
                    u.getFirstName(),
                    u.getLastName(),
                    u.isEmailVerified(),
                    u.getGender()
            ));
        });

    }
}
