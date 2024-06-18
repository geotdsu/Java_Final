module com.example.java_saba_urushadze {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;
    requires java.sql;
    requires mysql.connector.j;


    opens com.example.java_saba_urushadze to javafx.fxml;
    exports com.example.java_saba_urushadze;
}