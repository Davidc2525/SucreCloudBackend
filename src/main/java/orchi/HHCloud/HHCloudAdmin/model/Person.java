package orchi.HHCloud.HHCloudAdmin.model;

import java.time.LocalDate;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.*;

/**
 * Modelo para usuario
 *
 * @author David
 */
public class Person {

    private final StringProperty id = new SimpleStringProperty("");
    private final StringProperty username = new SimpleStringProperty("");
    private final StringProperty email = new SimpleStringProperty("");
    private final BooleanProperty isVerified = new SimpleBooleanProperty(false);
    private final StringProperty gender = new SimpleStringProperty("");
    private final StringProperty firstName = new SimpleStringProperty("");
    private final StringProperty lastName = new SimpleStringProperty("");
    private final StringProperty password = new SimpleStringProperty("");


    private final LongProperty createAt = new SimpleLongProperty(0L);

    public Person() {
    }

    public Person(String id, String username, String email, String password, String firstName, String lastName, boolean isVerified, String gender) {

        setId(id);
        setUsername(username);
        setEmail(email);
        setFirstName(firstName);
        setLastName(lastName);
        setIsVerified(isVerified);
        setGender(gender);
        setPassword(password);

    }

    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public StringProperty idProperty() {
        return id;
    }

    public String getUsername() {
        return username.get();
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public StringProperty emailProperty() {
        return email;
    }

    public boolean isIsVerified() {
        return isVerified.get();
    }

    public void setIsVerified(boolean isVerified) {
        this.isVerified.set(isVerified);
    }

    public BooleanProperty isVerifiedProperty() {
        return isVerified;
    }

    public String getGender() {
        return gender.get();
    }

    public void setGender(String gender) {
        this.gender.set(gender);
    }

    public StringProperty genderProperty() {
        return gender;
    }

    public String getFirstName() {
        return firstName.get();
    }

    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    public StringProperty firstNameProperty() {
        return firstName;
    }

    public String getLastName() {
        return lastName.get();
    }

    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    public StringProperty lastNameProperty() {
        return lastName;
    }

    public String getPassword() {
        return password.get();
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public long getCreateAt() {
        return createAt.get();
    }

    public void setCreateAt(long createAt) {
        this.createAt.set(createAt);
    }

    public LongProperty createAtProperty() {
        return createAt;
    }

}