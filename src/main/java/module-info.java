module com.hospital.hospital {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.kordamp.bootstrapfx.core;
    requires com.fasterxml.jackson.databind;

    opens com.hospital.hospital to javafx.fxml;
    exports com.hospital.hospital;
}