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

public class RemessaItem implements Serializable {
    private int idRemessaItem;
    private int idRemessa;
    private int idExtintor;
    private String observacaoTecnico;
    
    // Objetos (para futuro JOIN)
    private Remessa remessa;
    private Extintor extintor;

    public RemessaItem() { 
    }

    public int getIdRemessaItem() {
        return idRemessaItem;
    }

    public void setIdRemessaItem(int idRemessaItem) {
        this.idRemessaItem = idRemessaItem;
    }

    public int getIdRemessa() {
        return idRemessa;
    }

    public void setIdRemessa(int idRemessa) {
        this.idRemessa = idRemessa;
    }

    public int getIdExtintor() {
        return idExtintor;
    }

    public void setIdExtintor(int idExtintor) {
        this.idExtintor = idExtintor;
    }

    public String getObservacaoTecnico() {
        return observacaoTecnico;
    }

    public void setObservacaoTecnico(String observacaoTecnico) {
        this.observacaoTecnico = observacaoTecnico;
    }

    public Remessa getRemessa() {
        return remessa;
    }

    public void setRemessa(Remessa remessa) {
        this.remessa = remessa;
    }

    public Extintor getExtintor() {
        return extintor;
    }

    public void setExtintor(Extintor extintor) {
        this.extintor = extintor;
    }
    
}