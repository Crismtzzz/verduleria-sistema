package com.verduleria.repository;

import com.verduleria.model.Categoria;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaRepository {
    public List<Categoria> listar(Connection c) throws SQLException {
        List<Categoria> lista = new ArrayList<>();
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT id_categoria, nombre FROM categorias ORDER BY nombre")) {
            while (rs.next()) lista.add(new Categoria(rs.getInt(1), rs.getString(2)));
        }
        return lista;
    }
}
