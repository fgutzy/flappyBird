module org.example.flappybird {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.google.gson;

    opens org.example.flappybird to javafx.fxml;
    exports org.example.flappybird;
}