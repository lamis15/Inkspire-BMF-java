package service;

import java.sql.SQLException;
import java.util.List;

public interface IService <T>{
    boolean ajouter(T t) throws SQLException;
    boolean modifier(T t) throws SQLException;
    boolean supprimer(int id) throws SQLException;
    List<T> recuperer() throws SQLException;

}
