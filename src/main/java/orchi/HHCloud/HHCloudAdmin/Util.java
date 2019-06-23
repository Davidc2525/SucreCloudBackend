package orchi.HHCloud.HHCloudAdmin;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import orchi.HHCloud.HHCloudAdmin.model.Person;
import orchi.HHCloud.user.DataUser;
import orchi.HHCloud.user.User;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

public class Util {

    public static Person setPersonInPerson(Person dst, Person src) {
        dst.setId(src.getId());
        dst.setEmail(src.getEmail());
        dst.setCreateAt(src.getCreateAt());
        dst.setUsername(src.getUsername());
        dst.setFirstName(src.getFirstName());
        dst.setLastName(src.getLastName());
        dst.setIsVerified(src.isIsVerified());
        dst.setPassword(src.getPassword());
        dst.setGender(src.getGender());
        return dst;
    }

    public static Person clonePerson(Person p) {
        Person cp = new Person();
        cp.setId(p.getId());
        cp.setEmail(p.getEmail());
        cp.setCreateAt(p.getCreateAt());
        cp.setUsername(p.getUsername());
        cp.setFirstName(p.getFirstName());
        cp.setLastName(p.getLastName());
        cp.setIsVerified(p.isIsVerified());
        cp.setPassword(p.getPassword());
        cp.setGender(p.getGender());
        return cp;
    }

    public static Person userToPerson(User user) {
        Person p = null;
        DataUser u = (DataUser) user;
        p = new Person();
        p.setId(u.getId());
        p.setEmail(u.getEmail());
        p.setCreateAt(u.getCreateAt());
        p.setUsername(u.getUsername());
        p.setFirstName(u.getFirstName());
        p.setLastName(u.getLastName());
        p.setIsVerified(u.isEmailVerified());
        p.setPassword(u.getPassword());
        p.setGender(u.getGender());
        return p;
    }

    public static User personToUser(Person person) {
        DataUser u = null;
        u = new DataUser();
        u.setId(person.getId());
        u.setEmail(person.getEmail());
        u.setCreateAt(person.getCreateAt());
        u.setUsername(person.getUsername());
        u.setFirstName(person.getFirstName());
        u.setLastName(person.getLastName());
        u.setEmailVerified(person.isIsVerified());
        u.setPassword(person.getPassword());
        u.setGender(person.getGender());
        return u;
    }

    /*Dialogs*/
    public static Optional<ButtonType> dialog(Alert.AlertType type, String title, String header, String context) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(context);

        return alert.showAndWait();
    }

    public static Optional<ButtonType> dialog(String title, String header, String context) {
        return dialog(Alert.AlertType.CONFIRMATION, title, header, context);
    }


    public static Optional<ButtonType> exceptionDialog(Throwable ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Exception Dialog");
        alert.setHeaderText("Error");
        alert.setContentText(ex.getMessage());

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);


        return alert.showAndWait();
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
