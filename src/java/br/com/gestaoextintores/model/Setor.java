/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.gestaoextintores.model;

import java.io.Serializable;

/**
 *
 * @author Dev Fabio Santos
 */
public class Setor implements Serializable {
    private int idSetor;
    private String nome;
    private int idFilial;
    
    private Filial filial;

    public Setor() { 
    }

    public int getIdSetor() { 
        return idSetor; 
    }
    
    public void setIdSetor(int idSetor) { 
        this.idSetor = idSetor; 
    }
    
    public String getNome() { 
        return nome; 
    }
    
    public void setNome(String nome) { 
        this.nome = nome; 
    }
    
    public int getIdFilial() { 
        return idFilial; 
    }
    
    public void setIdFilial(int idFilial) { 
        this.idFilial = idFilial; 
    }
    
    public Filial getFilial() { 
        return filial; 
    }
    
    public void setFilial(Filial filial) { 
        this.filial = filial; 
    }
}
