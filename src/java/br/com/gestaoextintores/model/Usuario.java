/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.gestaoextintores.model;

import java.io.Serializable;
import br.com.gestaoextintores.model.Filial;

/**
 *
 * @author Dev Fabio Santos
 */
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idUsuario;
    private String nome;
    private String login;
    private String senha;
    private String perfil;
    private Integer idFilial;

    private Filial filial;

    public Usuario() {
    }

    public int getIdUsuario() {
        return idUsuario; 
    }
    
    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario; 
    }
    
    public String getNome() {
        return nome; 
    }
    
    public void setNome(String nome) {
        this.nome = nome; 
    }
    
    public String getLogin() {
        return login; 
    }
    
    public void setLogin(String login) {
        this.login = login; 
    }
    public String getSenha() {
        return senha; 
    }
    
    public void setSenha(String senha) {
        this.senha = senha; 
    }
    
    public String getPerfil() {
        return perfil; 
    }
    
    public void setPerfil(String perfil) {
        this.perfil = perfil; 
    }
    
    public Integer getIdFilial() {
        return idFilial; 
    }
    
    public void setIdFilial(Integer idFilial) {
        this.idFilial = idFilial; 
    }

    public Filial getFilial() {
        return filial;
    }

    public void setFilial(Filial filial) {
        this.filial = filial;
    }
}