package orchi.HHCloud.HHCloudAdmin.controller;

import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXToggleButton;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import orchi.HHCloud.HHCloudAdmin.Main;
import orchi.HHCloud.HHCloudAdmin.Util;
import orchi.HHCloud.HHCloudAdmin.model.Person;
import orchi.HHCloud.user.DataUser;
import orchi.HHCloud.user.userAvailable.AvailableDescriptor;

public class Available {
    @FXML
    private AnchorPane window;
    @FXML
    private Label idUser;

    @FXML
    private Label nameUser;

    @FXML
    private TextArea reasonArea;

    @FXML
    private Button close;

    @FXML
    private JFXToggleButton availableUser;

    @FXML
    private Button saveAvailable;

    @FXML
    private HBox actionButtons;

    @FXML
    private JFXSpinner spinnerLoadin;

    private Stage dialogStage;
    private Person person;


    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setPerson(Person person) {
        this.person = person;
        new Model(this);
    }

    /**
     * model
     */
    public static class Model {

        private Available available;
        private BooleanProperty loading = new SimpleBooleanProperty(false);
        private AvailableDescriptorProperty avd = null;

        public Model(Available available) {
            this.available = available;
            avd = new AvailableDescriptorProperty(this.available.person);
            this.available.window.disableProperty().bind(loading);
            this.available.saveAvailable.disableProperty().bind(loading);
            this.available.spinnerLoadin.visibleProperty().bind(loading);
            this.available.idUser.textProperty().bind(avd.getUser().idProperty());
            this.available.nameUser.textProperty().bind(avd.getUserCompletName());
            this.available.reasonArea.disableProperty().bind(this.available.availableUser.selectedProperty());
            this.available.reasonArea.textProperty().bindBidirectional(avd.reasonProperty());
            this.available.availableUser.selectedProperty().bindBidirectional(avd.availableProperty());
            this.available.saveAvailable.setOnAction(click -> {
                saveAvailableData();
            });

            this.available.close.setOnAction(click -> {
                this.available.dialogStage.close();
            });

            getAvailableData();
        }

        public void saveAvailableData() {
            Task<Void> t = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    Thread.sleep(100);

                    if (avd.isAvailable()) {
                        Main.client.getService().enableUser((DataUser) Util.personToUser(avd.getUser()));
                    } else {
                        Main.client.getService().disableUser((DataUser) Util.personToUser(avd.getUser()), avd.getReason());
                    }
                    return null;
                }
            };
            t.setOnScheduled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    loading.setValue(true);
                }
            });
            t.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    loading.setValue(false);
                }
            });
            t.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    loading.setValue(false);
                }
            });
            t.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    loading.setValue(false);
                }
            });
            t.exceptionProperty().addListener(new ChangeListener<Throwable>() {
                @Override
                public void changed(ObservableValue<? extends Throwable> observable, Throwable oldValue, Throwable newValue) {
                    if (newValue != null) {
                        Util.exceptionDialog(newValue);
                    }
                }
            });
            new Thread(t).start();
        }

        public void getAvailableData() {

            Task<AvailableDescriptor> t = new Task<AvailableDescriptor>() {
                @Override
                protected AvailableDescriptor call() throws Exception {
                    Thread.sleep(100);
                    if (true) {
                        new Exception("test");
                    }
                    return Main.client.getService().getAvialableDescriptor((DataUser) Util.personToUser(available.person));
                }
            };
            t.setOnScheduled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    loading.setValue(true);
                }
            });
            t.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    avd.setValue(((AvailableDescriptor) event.getSource().getValue()));
                    loading.setValue(false);
                }
            });
            t.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    loading.setValue(false);
                }
            });
            t.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    loading.setValue(false);
                }
            });
            t.exceptionProperty().addListener(new ChangeListener<Throwable>() {
                @Override
                public void changed(ObservableValue<? extends Throwable> observable, Throwable oldValue, Throwable newValue) {
                    if (newValue != null) {
                        Util.exceptionDialog(newValue);
                    }
                }
            });
            new Thread(t).start();
        }
    }

    public static class AvailableDescriptorProperty {

        private BooleanProperty available = new SimpleBooleanProperty(false);
        private Person user = new Person();
        private StringProperty reason = new SimpleStringProperty("");
        private LongProperty createdAt = new SimpleLongProperty(0L);

        public AvailableDescriptorProperty() {
        }

        public AvailableDescriptorProperty(AvailableDescriptor avd) {
            available.setValue(avd.isAvailable());
            user = Util.userToPerson(avd.getUser());
            reason.setValue(avd.getReason());
            createdAt.setValue(avd.getCreatedAt());
        }

        public AvailableDescriptorProperty(Person person) {
            user = person;
        }

        public AvailableDescriptorProperty(BooleanProperty available, Person user, StringProperty reason, LongProperty createdAt) {
            this.available = available;
            this.user = user;
            this.reason = reason;
            this.createdAt = createdAt;
        }

        public void setValue(AvailableDescriptor avd) {
            available.setValue(avd.isAvailable());
            //user = Util.userToPerson(avd.getUser());
            reason.setValue(avd.getReason());
            createdAt.setValue(avd.getCreatedAt());
        }

        public boolean isAvailable() {
            return available.get();
        }

        public void setAvailable(boolean available) {
            this.available.set(available);
        }

        public BooleanProperty availableProperty() {
            return available;
        }

        public Person getUser() {
            return user;
        }

        public void setUser(Person user) {
            this.user = user;
        }

        public StringExpression getUserCompletName() {
            return user.firstNameProperty().concat(" ").concat(user.lastNameProperty());
        }

        public String getReason() {
            return reason.get();
        }

        public void setReason(String reason) {
            this.reason.set(reason);
        }

        public StringProperty reasonProperty() {
            return reason;
        }

        public long getCreatedAt() {
            return createdAt.get();
        }

        public void setCreatedAt(long createdAt) {
            this.createdAt.set(createdAt);
        }

        public LongProperty createdAtProperty() {
            return createdAt;
        }
    }
}

