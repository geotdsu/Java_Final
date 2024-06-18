package com.example.java_saba_urushadze;


import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.util.stream.Collectors;
import java.util.Map;
import java.sql.*;

public class ProductApplication extends Application {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/shop";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "";

    private ObservableList<Product> products = FXCollections.observableArrayList();
    private BorderPane root;
    private PieChart pieChart;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Product Management");

        root = new BorderPane();
        root.setPadding(new Insets(10));

        GridPane inputGrid = new GridPane();
        inputGrid.setVgap(5);
        inputGrid.setHgap(5);

        Label nameLabel = new Label("Name:");
        inputGrid.add(nameLabel, 0, 0);

        TextField nameField = new TextField();
        inputGrid.add(nameField, 1, 0);

        Label quantityLabel = new Label("Quantity:");
        inputGrid.add(quantityLabel, 0, 1);

        TextField quantityField = new TextField();
        inputGrid.add(quantityField, 1, 1);

        Label priceLabel = new Label("Price:");
        inputGrid.add(priceLabel, 0, 2);

        TextField priceField = new TextField();
        inputGrid.add(priceField, 1, 2);

        Button addButton = new Button("Add");
        inputGrid.add(addButton, 0, 3);

        addButton.setOnAction(e -> {
            String name = nameField.getText();
            int quantity = Integer.parseInt(quantityField.getText());
            double price = Double.parseDouble(priceField.getText());

            Product product = new Product(name, quantity, price);
            saveProduct(product);

            nameField.clear();
            quantityField.clear();
            priceField.clear();

            retrieveProducts();
            updatePieChart();
        });

        root.setLeft(inputGrid);

        pieChart = new PieChart();
        pieChart.setTitle("Product Quantity");
        root.setCenter(pieChart);

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();

        retrieveProducts();
        updatePieChart();
    }

    private void saveProduct(Product product) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String query = "SELECT * FROM product WHERE name = ?";
            PreparedStatement selectStatement = conn.prepareStatement(query);
            selectStatement.setString(1, product.getName());
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                int existingQuantity = resultSet.getInt("quantity");
                double existingPrice = resultSet.getDouble("price");

                int newQuantity = existingQuantity + product.getQuantity();
                double newPrice = product.getPrice();

                String updateQuery = "UPDATE product SET quantity = ?, price = ? WHERE name = ?";
                PreparedStatement updateStatement = conn.prepareStatement(updateQuery);
                updateStatement.setInt(1, newQuantity);
                updateStatement.setDouble(2, newPrice);
                updateStatement.setString(3, product.getName());
                updateStatement.executeUpdate();
            } else {
                String insertQuery = "INSERT INTO product (name, quantity, price) VALUES (?, ?, ?)";
                PreparedStatement insertStatement = conn.prepareStatement(insertQuery);
                insertStatement.setString(1, product.getName());
                insertStatement.setInt(2, product.getQuantity());
                insertStatement.setDouble(3, product.getPrice());
                insertStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void retrieveProducts() {
        products.clear();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String query = "SELECT name, quantity, price FROM product";
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                int quantity = resultSet.getInt("quantity");
                double price = resultSet.getDouble("price");

                Product product = new Product(name, quantity, price);
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updatePieChart() {
        pieChart.getData().clear();

        Map<String, Integer> productQuantities = products.stream()
                .collect(Collectors.groupingBy(Product::getName, Collectors.summingInt(Product::getQuantity)));

        int totalCount = productQuantities.values().stream().mapToInt(Integer::intValue).sum();

        productQuantities.forEach((name, quantity) -> {
            PieChart.Data data = new PieChart.Data(name + " (" + quantity + ")", quantity);
            pieChart.getData().add(data);
        });

        pieChart.setTitle("Product Quantity (Total: " + totalCount + ")");
    }
}

