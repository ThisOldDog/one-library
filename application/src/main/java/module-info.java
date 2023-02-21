module pers.dog {
    requires java.sql;
    requires javafx.controls;
    requires javafx.fxml;

    requires cglib;
    requires spring.core;
    requires spring.beans;
    requires spring.context;
    requires spring.boot;
    requires spring.boot.autoconfigure;

    requires javafx.spring.boot.starter;

    requires org.slf4j;
    requires org.apache.commons.lang3;
    requires org.jfxtras.styles.jmetro;
    requires org.controlsfx.controls;

    exports pers.dog;
    exports pers.dog.controller;
    exports pers.dog.app.service;
    exports pers.dog.app.domain;
    exports pers.dog.infra.control;
    exports pers.dog.config.listener;
    exports pers.dog.config.status;

    opens pers.dog;
    opens pers.dog.infra.control;
    opens pers.dog.infra.action;
    opens pers.dog.infra.action.application;
    opens pers.dog.controller;
    opens pers.dog.config.listener;
    opens pers.dog.config.status;
    opens messages;

}