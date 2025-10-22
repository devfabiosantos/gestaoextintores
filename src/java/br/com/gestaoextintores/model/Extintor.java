package br.com.gestaoextintores.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Classe que representa a entidade Extintor no sistema
 * @author Dev Fabio Santos
 */
public class Extintor implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private int idExtintor;
    private String tipoEquipamento;
    private String numeroControle;
    private String classeExtintora;
    private String cargaNominal;
    private String referenciaLocalizacao;
    private Date dataRecarga;
    private Date dataValidade;
    private String observacao;
    
    private int idSetor;
    private int idStatus;
    
    private Setor setor;
    private StatusExtintor status;

    public Extintor() {
    }

    public int getIdExtintor() {
        return idExtintor;
    }

    public void setIdExtintor(int idExtintor) {
        this.idExtintor = idExtintor;
    }

    public String getTipoEquipamento() {
        return tipoEquipamento;
    }

    public void setTipoEquipamento(String tipoEquipamento) {
        this.tipoEquipamento = tipoEquipamento;
    }

    public String getNumeroControle() {
        return numeroControle;
    }

    public void setNumeroControle(String numeroControle) {
        this.numeroControle = numeroControle;
    }

    public String getClasseExtintora() {
        return classeExtintora;
    }

    public void setClasseExtintora(String classeExtintora) {
        this.classeExtintora = classeExtintora;
    }

    public String getCargaNominal() {
        return cargaNominal;
    }

    public void setCargaNominal(String cargaNominal) {
        this.cargaNominal = cargaNominal;
    }

    public String getReferenciaLocalizacao() {
        return referenciaLocalizacao;
    }

    public void setReferenciaLocalizacao(String referenciaLocalizacao) {
        this.referenciaLocalizacao = referenciaLocalizacao;
    }

    public Date getDataRecarga() {
        return dataRecarga;
    }

    public void setDataRecarga(Date dataRecarga) {
        this.dataRecarga = dataRecarga;
    }

    public Date getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(Date dataValidade) {
        this.dataValidade = dataValidade;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public int getIdSetor() {
        return idSetor;
    }

    public void setIdSetor(int idSetor) {
        this.idSetor = idSetor;
    }

    public int getIdStatus() {
        return idStatus;
    }

    public void setIdStatus(int idStatus) {
        this.idStatus = idStatus;
    }

    public Setor getSetor() {
        return setor;
    }

    public void setSetor(Setor setor) {
        this.setor = setor;
    }

    public StatusExtintor getStatus() {
        return status;
    }

    public void setStatus(StatusExtintor status) {
        this.status = status;
    }
}