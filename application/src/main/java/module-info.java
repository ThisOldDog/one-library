module pers.dog {
    requires java.sql;
    requires javafx.controls;
    requires javafx.fxml;

    requires cglib;
    requires java.persistence;
    requires org.hibernate.orm.core;
    requires org.hibernate.commons.annotations;
    requires spring.core;
    requires spring.beans;
    requires spring.context;
    requires spring.data.jpa;
    requires spring.data.commons;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.boot.starter.data.jpa;

    requires javafx.spring.boot.starter;

    requires org.slf4j;
    requires org.apache.commons.lang3;
    requires org.jfxtras.styles.jmetro;
    requires org.controlsfx.controls;
    requires org.fxmisc.richtext;

    // resource
    opens pers.dog;
    // package
    opens pers.dog.api.controller;
    opens pers.dog.api.callback;
    opens pers.dog.app.service;
    opens pers.dog.app.service.impl;
    opens pers.dog.domain.entity;
    opens pers.dog.domain.repository;
    opens pers.dog.infra.action;
    opens pers.dog.infra.action.application;
    opens pers.dog.infra.action.project;
    opens pers.dog.infra.constant;
    opens pers.dog.infra.control;
    opens pers.dog.infra.listener;
    opens pers.dog.infra.resource;
    opens pers.dog.infra.status;
    opens messages;

}