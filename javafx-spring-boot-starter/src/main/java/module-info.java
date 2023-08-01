module javafx.spring.boot.starter {
    requires java.desktop;

    requires javafx.controls;
    requires javafx.fxml;

    requires spring.core;
    requires spring.beans;
    requires spring.context;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires static spring.boot.starter.data.jpa;

    requires org.slf4j;
    requires org.apache.commons.lang3;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires static com.h2database;
    requires static org.controlsfx.controls;

    exports pers.dog.boot;
    exports pers.dog.boot.autoconfiguration;
    exports pers.dog.boot.component.cache;
    exports pers.dog.boot.component.cache.status;
    exports pers.dog.boot.component.control;
    exports pers.dog.boot.component.event;
    exports pers.dog.boot.component.file;
    exports pers.dog.boot.context;
    exports pers.dog.boot.context.property;
    exports pers.dog.boot.infra.dialog;
    exports pers.dog.boot.infra.constant;
    exports pers.dog.boot.infra.i18n;
    exports pers.dog.boot.infra.util;

    opens pers.dog.boot;
    opens pers.dog.boot.autoconfiguration;
    opens pers.dog.boot.component.cache;
    opens pers.dog.boot.component.cache.status;
    opens pers.dog.boot.component.control;
    opens pers.dog.boot.component.event;
    opens pers.dog.boot.component.file;
    opens pers.dog.boot.context;
    opens pers.dog.boot.context.property;
    opens pers.dog.boot.infra.constant;
    opens pers.dog.boot.infra.i18n;
    opens pers.dog.boot.infra.util;
    opens pers.dog.boot.infra.dialog;
}