module com.hospital.hospital {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.hospital.hospital to javafx.fxml;
    exports com.hospital.hospital;
}