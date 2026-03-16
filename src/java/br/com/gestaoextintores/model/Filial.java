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
public class Filial {

    private Integer idFilial;
    private String nome;
    private String cnpj;
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String endereco;
    private String cidade;
    private String estado;

    public Filial() {
    }

    public Integer getIdFilial() {
        return idFilial;
    }

    public void setIdFilial(Integer idFilial) {
        this.idFilial = idFilial;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getEnderecoCompleto() {
        if (isBlank(logradouro) || isBlank(numero) || isBlank(bairro) || isBlank(cidade) || isBlank(estado)) {
            return endereco;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(logradouro).append(", ").append(numero);
        if (!isBlank(complemento)) {
            sb.append(" - ").append(complemento);
        }
        sb.append(" - ").append(bairro);
        sb.append(", ").append(cidade).append(" - ").append(estado);
        if (!isBlank(cep)) {
            sb.append(", ").append(formatarCep(cep));
        }
        return sb.toString();
    }

    public String getCnpjFormatado() {
        if (isBlank(cnpj)) {
            return "";
        }
        String digits = cnpj.replaceAll("\\D", "");
        if (digits.length() != 14) {
            return cnpj;
        }
        return digits.substring(0, 2) + "." + digits.substring(2, 5) + "." + digits.substring(5, 8)
                + "/" + digits.substring(8, 12) + "-" + digits.substring(12);
    }

    public String getCepFormatado() {
        return formatarCep(cep);
    }

    private String formatarCep(String valor) {
        if (isBlank(valor)) {
            return "";
        }
        String digits = valor.replaceAll("\\D", "");
        if (digits.length() != 8) {
            return valor;
        }
        return digits.substring(0, 5) + "-" + digits.substring(5);
    }

    private boolean isBlank(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
