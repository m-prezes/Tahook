module com.tahook {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires json.simple;

    opens com.tahook to javafx.fxml;

    exports com.tahook;
    exports com.tahook.player;

    opens com.tahook.player to javafx.fxml;

    exports com.tahook.host;

    opens com.tahook.host to javafx.fxml;
}