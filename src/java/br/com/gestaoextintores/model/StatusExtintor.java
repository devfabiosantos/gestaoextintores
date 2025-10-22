/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.com.gestaoextintores.model;

/**
 *
 * @author Dev Fabio Santos
 */

import java.io.Serializable;

public class StatusExtintor implements Serializable {
    private int idStatus;
    private String nome;

    public StatusExtintor() { 
    }
    
    public int getIdStatus() { 
        return idStatus; 
    }
    
    public void setIdStatus(int idStatus) { 
        this.idStatus = idStatus; 
    }
    
    public String getNome() { 
        return nome; 
    }
    
    public void setNome(String nome) { 
        this.nome = nome; 
    }
}