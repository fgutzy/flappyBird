module org.example.flappybird {
    requires javafx.controls;
    requires javafx.fxml;
    requires okhttp;


    opens org.example.flappybird to javafx.fxml;
    exports org.example.flappybird;
}