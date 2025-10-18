package br.com.gestaoextintores.model;

import java.util.Date;

/**
 * Classe que representa a entidade Extintor no sistema
 * @author Dev Fabio Santos
 */
public class Extintor {

    private int idExtintor;
    private String numeroControle;
    private String tipo; // CO2, PQS, Água, etc.
    private Date dataRecarga; // Data da última recarga
    private Date dataValidade; // Data de validade da carga
    private String localizacao; // Onde o extintor está instalado
    private int idFilial; // Referência à filial onde o extintor está localizado

    public Extintor() {}

    public Extintor(int idExtintor, String numeroControle, String tipo, Date dataRecarga,
                    Date dataValidade, String localizacao, int idFilial) {
        this.idExtintor = idExtintor;
        this.numeroControle = numeroControle;
        this.tipo = tipo;
        this.dataRecarga = dataRecarga;
        this.dataValidade = dataValidade;
        this.localizacao = localizacao;
        this.idFilial = idFilial;
    }

    public int getIdExtintor() {
        return idExtintor;
    }

    public void setIdExtintor(int idExtintor) {
        this.idExtintor = idExtintor;
    }

    public String getNumeroControle() {
        return numeroControle;
    }

    public void setNumeroControle(String numeroControle) {
        this.numeroControle = numeroControle;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
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

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public int getIdFilial() {
        return idFilial;
    }

    public void setIdFilial(int idFilial) {
        this.idFilial = idFilial;
    }

    @Override
    public String toString() {
        return "Extintor{" +
                "idExtintor=" + idExtintor +
                ", numeroControle='" + numeroControle + '\'' +
                ", tipo='" + tipo + '\'' +
                ", dataRecarga=" + dataRecarga +
                ", dataValidade=" + dataValidade +
                ", localizacao='" + localizacao + '\'' +
                ", idFilial=" + idFilial +
                '}';
    }
}
