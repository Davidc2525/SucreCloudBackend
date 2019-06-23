/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package orchi.HHCloud.HHCloudAdmin.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class PersonCloudViewCell {
    @FXML
    public Label cellLabelPathName;

    @FXML
    public ImageView cellImg;

    @FXML
    public AnchorPane anchorPane;

    @FXML
    public HBox actions;

    @FXML
    public Button actionDelete;

    @FXML
    public Button actionDownload;
    @FXML
    public Text detail;



    @FXML
    private void initialize() {

    }

}
