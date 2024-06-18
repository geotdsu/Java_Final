package com.example.java_saba__urushadze;


import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ProductController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}