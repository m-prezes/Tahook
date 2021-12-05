module com.kahoot {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.tahook to javafx.fxml;
    exports com.tahook;
    exports com.tahook.player;
    opens com.tahook.player to javafx.fxml;
    exports com.tahook.host;
    opens com.tahook.host to javafx.fxml;
}