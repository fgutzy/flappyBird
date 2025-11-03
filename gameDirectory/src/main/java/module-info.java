module org.example.gamedirectory {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.net.http;

    opens org.example.gamedirectory to javafx.fxml;
    exports org.example.gamedirectory;
}