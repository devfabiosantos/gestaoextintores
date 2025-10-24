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
import java.util.Date;
import java.util.List;
import br.com.gestaoextintores.model.Usuario;
import br.com.gestaoextintores.model.Filial;

public class Remessa implements Serializable {
    private int idRemessa;
    private int idUsuarioTecnico;
    private int idFilial;
    private Date dataCriacao;
    private String statusRemessa;
    private Integer idUsuarioAdmin;
    private Date dataAprovacao;
    
    private Usuario tecnico;
    private Usuario admin;
    private Filial filial;
    private List<RemessaItem> itens;

    public Remessa() { 
    }

    public int getIdRemessa() {
        return idRemessa;
    }

    public void setIdRemessa(int idRemessa) {
        this.idRemessa = idRemessa;
    }

    public int getIdUsuarioTecnico() {
        return idUsuarioTecnico;
    }

    public void setIdUsuarioTecnico(int idUsuarioTecnico) {
        this.idUsuarioTecnico = idUsuarioTecnico;
    }

    public int getIdFilial() {
        return idFilial;
    }

    public void setIdFilial(int idFilial) {
        this.idFilial = idFilial;
    }

    public Date getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(Date dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public String getStatusRemessa() {
        return statusRemessa;
    }

    public void setStatusRemessa(String statusRemessa) {
        this.statusRemessa = statusRemessa;
    }

    public Integer getIdUsuarioAdmin() {
        return idUsuarioAdmin;
    }

    public void setIdUsuarioAdmin(Integer idUsuarioAdmin) {
        this.idUsuarioAdmin = idUsuarioAdmin;
    }

    public Date getDataAprovacao() {
        return dataAprovacao;
    }

    public void setDataAprovacao(Date dataAprovacao) {
        this.dataAprovacao = dataAprovacao;
    }

    public Usuario getTecnico() {
        return tecnico;
    }

    public void setTecnico(Usuario tecnico) {
        this.tecnico = tecnico;
    }

    public Usuario getAdmin() {
        return admin;
    }

    public void setAdmin(Usuario admin) {
        this.admin = admin;
    }

    public Filial getFilial() {
        return filial;
    }

    public void setFilial(Filial filial) {
        this.filial = filial;
    }

    public List<RemessaItem> getItens() {
        return itens;
    }

    public void setItens(List<RemessaItem> itens) {
        this.itens = itens;
    }
    
}