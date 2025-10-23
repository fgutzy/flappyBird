module org.example.flappybird {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.flappybird to javafx.fxml;
    exports org.example.flappybird;
}