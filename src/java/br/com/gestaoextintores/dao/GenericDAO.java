/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.gestaoextintores.dao;

import java.util.List;

/**
 *
 * @author Dev Fabio Santos
 */
public interface GenericDAO {
    
    public boolean cadastrar(Object object);
    
    public List<Object> listar();
    
    public List<Object> listar(int idObject);
    
    public Boolean excluir(int idObject);
    
    public Object carregar(int idObject);
    
    public Boolean alterar(Object object);
    
}
