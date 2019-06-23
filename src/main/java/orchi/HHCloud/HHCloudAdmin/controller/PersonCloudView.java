package orchi.HHCloud.HHCloudAdmin.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import orchi.HHCloud.AdminService.FsResponse;
import orchi.HHCloud.HHCloudAdmin.Main;
import orchi.HHCloud.HHCloudAdmin.Util;
import orchi.HHCloud.HHCloudAdmin.model.Person;
import orchi.HHCloud.store.AbsStatus;
import orchi.HHCloud.user.DataUser;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class PersonCloudView {

    @FXML
    public Label status;
    @FXML // fx:id="personCloudView"
    public AnchorPane personCloudView; // Value injected by FXMLLoader
    LinkedList<String> browserStack = new LinkedList<>();
    ObservableList<AbsStatus> statues;
    @FXML // fx:id="browser"
    private AnchorPane browser; // Value injected by FXMLLoader
    @FXML // fx:id="personInfoImg"
    private ImageView personInfoImg; // Value injected by FXMLLoader
    @FXML // fx:id="browserButton"
    private Button browserButton; // Value injected by FXMLLoader
    @FXML // fx:id="personInfoName"
    private Label personInfoName; // Value injected by FXMLLoader
    @FXML // fx:id="pathViewList"
    private ListView<AbsStatus> pathViewList; // Value injected by FXMLLoader
    @FXML // fx:id="browserSearch"
    private TextField browserSearch; // Value injected by FXMLLoader
    @FXML // fx:id="personInfo"
    private AnchorPane personInfo; // Value injected by FXMLLoader
    @FXML // fx:id="personInfoEmail"
    private Label personInfoEmail; // Value injected by FXMLLoader
    @FXML // fx:id="pathView"
    private AnchorPane pathView; // Value injected by FXMLLoader
    @FXML // fx:id="pathViewDelete"
    private Button pathViewDelete; // Value injected by FXMLLoader
    @FXML
    private Button pathViewDownload; // Value injected by FXMLLoader
    @FXML
    private Text pathViewSelecteds; // Value injected by FXMLLoader
    @FXML
    private AnchorPane pathViewSelectedsActions; // Value injected by FXMLLoader
    private Stage dialogStage;
    private Person person = new Person();
    private SimpleIntegerProperty selecteds = new SimpleIntegerProperty(0);

    @FXML
    private void initialize() {
        assert browser != null : "fx:id=\"browser\" was not injected: check your FXML file 'PersonCloudView.fxml'.";
        assert personInfoImg != null : "fx:id=\"personInfoImg\" was not injected: check your FXML file 'PersonCloudView.fxml'.";
        assert browserButton != null : "fx:id=\"browserButton\" was not injected: check your FXML file 'PersonCloudView.fxml'.";
        assert personInfoName != null : "fx:id=\"personInfoName\" was not injected: check your FXML file 'PersonCloudView.fxml'.";
        assert pathViewList != null : "fx:id=\"pathViewList\" was not injected: check your FXML file 'PersonCloudView.fxml'.";
        assert browserSearch != null : "fx:id=\"browserSearch\" was not injected: check your FXML file 'PersonCloudView.fxml'.";
        assert personInfo != null : "fx:id=\"personInfo\" was not injected: check your FXML file 'PersonCloudView.fxml'.";
        assert personInfoEmail != null : "fx:id=\"personInfoEmail\" was not injected: check your FXML file 'PersonCloudView.fxml'.";
        assert pathView != null : "fx:id=\"pathView\" was not injected: check your FXML file 'PersonCloudView.fxml'.";
        assert pathViewDelete != null : "fx:id=\"pathViewDelete\" was not injected: check your FXML file 'PersonCloudView.fxml'.";

        browserSearch.setText("/");
        status.setText("Esperando por usuario");

        personInfoName.textProperty().bind(Bindings.concat(person.lastNameProperty(), " ", person.firstNameProperty()));
        personInfoEmail.textProperty().bindBidirectional(person.emailProperty());

        statues = FXCollections.observableArrayList();
        pathViewList.setItems(statues);
        pathViewList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        pathViewList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            int size = pathViewList.getSelectionModel().getSelectedItems().size();
            int elementCount = pathViewList.getItems().size();
            if(size==0){
                status.setText(browserSearch.getText()+" | "+elementCount+" elementos.");
            }else if(size>0){
                status.setText("Selección "+size +" | "+elementCount+" elementos.");
            }
            selecteds.set(size);
            pathViewSelectedsActions.setDisable(selecteds.get() == 0);
        });


        pathViewSelectedsActions.setDisable(true);


        pathViewList.setOnKeyPressed(event -> {


            if (event.getCode() == KeyCode.F5) {
                updatePath();
            }

            if (event.getCode() == KeyCode.ESCAPE) {
                pathViewList.getSelectionModel().clearSelection();
            }

            if (event.getCode() == KeyCode.DELETE) {

                int size = pathViewList.getSelectionModel().getSelectedItems().size();
                if (size == 1) {
                    AbsStatus t = pathViewList.getSelectionModel().getSelectedItem();
                    if (t != null) deletePath(t.path);
                } else if (size > 1) {
                    deletePaths();
                }
            }

            if (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.BACK_SPACE) {
                handleFordWardPath();
            }

            if (event.getCode() == KeyCode.ENTER) {
                AbsStatus t = pathViewList.getSelectionModel().getSelectedItem();
                if (t != null) {
                    browserSearch.setText(t.path);
                    browserStack.push(t.path);

                    updatePath();
                }
            }

            pathViewList.requestFocus();

        });

        pathViewList.setCellFactory(new Callback<ListView<AbsStatus>, ListCell<AbsStatus>>() {

            @Override
            public ListCell<AbsStatus> call(ListView<AbsStatus> p) {

                ListCell<AbsStatus> cell = new ListCell<AbsStatus>() {

                    FXMLLoader mLLoader = null;
                    PersonCloudViewCell cellController = null;

                    @Override
                    protected void updateItem(AbsStatus t, boolean bln) {
                        super.updateItem(t, bln);

                        if (mLLoader == null) {
                            mLLoader = new FXMLLoader(Main.class.getClassLoader().getResource("CustonCell.fxml"));


                            try {
                                mLLoader.load();
                                cellController = mLLoader.getController();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }

                        if (bln || t == null) {

                            setText(null);
                            setGraphic(null);

                        }
                        if (t != null) {
                            cellController.cellLabelPathName.setText(t.name);

                            String hr = Util.humanReadableByteCount(t.size, true);
                            if (t.isFile) {
                                cellController.detail.setText("Tamaño: " + hr);
                                cellController.cellImg.setImage(new Image("file.png"));
                            } else {
                                cellController.detail.setText("Tamaño: " + hr + ", Contenido: " + t.count);
                                cellController.cellImg.setImage(new Image("folder.png"));
                            }
                            cellController.actionDelete.setOnMousePressed(event -> deletePath(t.path));

                            cellController.actionDownload.setOnMousePressed(event -> {
                                downloadPath(t.path);
                            });
                            setText(null);
                            setGraphic(cellController.anchorPane);

                            cellController.anchorPane.onMouseClickedProperty().addListener(event -> {
                                //System.err.println(t.path);
                                browserSearch.setText(t.path);
                                updatePath();
                            });

                            setOnMousePressed(event -> {
                                if (event.getClickCount() >= 2) {
                                    //System.err.println(t.path + " " + event.getClickCount() + " " + event.getEventType().getName());
                                    browserSearch.setText(t.path);
                                    browserStack.push(t.path);

                                    updatePath();
                                }
                            });
                            //setText(t.isFile +" "+t.group);
                        }
                    }

                };


                return cell;
            }
        });


    }

    public void setDialogStage(Stage dialogStage) {

        this.dialogStage = dialogStage;
    }

    public void setPerson(Person person) {

        //this.person = Util.clonePerson(person);
        Util.setPersonInPerson(this.person, person);
        //System.err.println(this.person.getUsername());

        status.setText("Nube de "+person.getLastName()+" "+person.getFirstName());

        updatePath();
    }

    public void updatePath(ActionEvent actionEvent) {
        updatePath();
    }

    private void updatePath() {
        Task<FsResponse> t = new Task<FsResponse>() {
            @Override
            protected FsResponse call() throws Exception {

                DataUser user = (DataUser) Util.personToUser(person);

                FsResponse res = Main.client.getService().listPath(user, browserSearch.getText());
                System.err.println(res);
                return res;
            }
        };

        t.setOnScheduled(x->status.setText("Iniciando carga."));
        t.setOnRunning(x->status.setText("Cargando..."));
        t.setOnCancelled(x->status.setText("Carga cancelada."));
        t.setOnFailed(wse -> status.setText("Error al cagar."));

        t.setOnSucceeded(wse -> {
            statues.clear();
            status.setText("Listo.");
            FsResponse res = (FsResponse) wse.getSource().getValue();
            if (res.isFile) {
                statues.add(res.status);

            } else {
                if(res.statues.size()>0){
                    statues.addAll(res.statues);
                    status.setText("Listo. "+res.statues.size()+" elementos.");

                }else{
                    status.setText("Listo. Carpeta Vacia.");
                }
            }
        });



        new Thread(t).start();

    }

    private void deletePath(String path) {

        Optional<ButtonType> dialog = Util.dialog("Eliminar " + path, "Esta seguro que desea eliminar: " + path + "?", "No podra recuperar luego esta informacion.");

        dialog.ifPresent(new Consumer<ButtonType>() {
            @Override
            public void accept(ButtonType buttonType) {
                ObservableList<AbsStatus> items = pathViewList.getItems();
                if (buttonType.getButtonData().isDefaultButton()) {
                    DataUser user = new DataUser();
                    user.setId(person.getId());

                    Main.client.getService().removePaths(user, Arrays.asList(path));

                    updatePath();
                }
                /*
                 */
            }
        });


    }

    private void downloadPath(String path) {
        DataUser user = new DataUser();
        user.setId(person.getId());
        File saveIn = getDirectory();
        if (saveIn == null) return;
        try {
            Main.client.getService().copyToLocal(user, path, saveIn.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        updatePath();
    }

    public void deletePaths() {
        int size = pathViewList.getSelectionModel().getSelectedItems().size();
        ObservableList<AbsStatus> items = pathViewList.getSelectionModel().getSelectedItems();
        String header = "Seguro que desea elminar \n" + browserSearch.getText() + ": \n";
        int x = 0;
        for (AbsStatus absStatus : items) {
            if (x > 10) {
                header += "\t\t (" + (size - x) + ") más.  \n";
                break;
            } else {
                header += "\t" + absStatus.name + "\n";
            }

            x++;

        }

        Optional<ButtonType> dialog = Util.dialog("Eliminar " + size + (size > 1 ? " seleccionados" : " seleccionado"), "No podra recuperar luego esta informacion.", header);

        dialog.ifPresent(new Consumer<ButtonType>() {
            @Override
            public void accept(ButtonType buttonType) {

                if (buttonType.getButtonData().isDefaultButton()) {
                    DataUser user = new DataUser();
                    user.setId(person.getId());
                    List<String> list = new ArrayList<>();
                    for (AbsStatus absStatus : items) {
                        list.add(absStatus.path);
                    }

                    Main.client.getService().removePaths(user, list);

                    updatePath();
                }
                /*
                 */
            }
        });


    }

    public void downloadsPaths(ActionEvent actionEvent) {

        File saveIn = getDirectory();
        if (saveIn != null) {
            pathViewList.getSelectionModel().getSelectedItems().forEach(absStatus -> {
                System.err.println(absStatus.path);
                try {
                    System.err.println(absStatus.path + " -> " + saveIn.getPath());
                    Main.client.getService().copyToLocal((DataUser) Util.personToUser(person), absStatus.path, saveIn.getPath());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
        }
    }


    public File getDirectory() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Seleccione rruta para descarga");
        return dc.showDialog(dialogStage);

    }

    public void handleFordWardPath() {

        try {
            //browserStack.pop();
            String fw = browserStack.pop();
            if (fw.equals(browserSearch.getText())) {
                fw = browserStack.pop();
            }
            browserSearch.setText(fw);
        } catch (Exception e) {

            browserSearch.setText("/");

        }

        updatePath();
    }

    public void handleUpdatePath(ActionEvent actionEvent) {
        browserStack.push(browserSearch.getText());

        updatePath();
    }
}
