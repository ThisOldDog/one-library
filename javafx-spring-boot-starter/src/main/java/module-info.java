module javafx.spring.boot.starter {
    requires javafx.controls;
    requires javafx.fxml;

    requires spring.core;
    requires spring.beans;
    requires spring.context;
    requires spring.boot;
    requires spring.boot.autoconfigure;

    requires org.slf4j;
    requires org.apache.commons.lang3;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

    exports pers.dog.boot;
    exports pers.dog.boot.context;
    exports pers.dog.boot.component.event;
    exports pers.dog.boot.component.file;
    exports pers.dog.boot.component.cache.status;
    exports pers.dog.boot.i18n;
    exports pers.dog.boot.util;

    opens pers.dog.boot.autoconfiguration to spring.core, spring.beans, spring.context;
    opens pers.dog.boot.context.property to spring.core, spring.beans, spring.context;
    opens pers.dog.boot.component.event to spring.core, spring.beans, spring.context;
    opens pers.dog.boot.component.file to spring.core, spring.beans, spring.context, spring.boot;
    exports pers.dog.boot.component.cache;
    opens pers.dog.boot.util to javafx.fxml, spring.beans, spring.boot, spring.context, spring.core;
    exports pers.dog.boot.infra.constant;
    opens pers.dog.boot.infra.constant to javafx.fxml, spring.beans, spring.boot, spring.context, spring.core;
}