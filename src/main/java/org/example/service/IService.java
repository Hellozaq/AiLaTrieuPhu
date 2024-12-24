package org.example.service;

import org.example.model.QuestionModel;

import java.sql.SQLException;
import java.util.ArrayList;

public interface IService<E> {
    ArrayList<E> findAll(long id) throws SQLException;
    E findById(long id) throws SQLException;

    void save(E E) throws SQLException;

    void update(E e) throws SQLException;
    void delete(E e) throws SQLException;
}
