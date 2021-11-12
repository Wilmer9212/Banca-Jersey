/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.entidades;

import java.io.Serializable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 *
 * @author wilmer
 */
@Entity
@Table(name = "referenciasp")
public class ReferenciasP implements Serializable {

    @EmbeddedId
    protected AuxiliaresPK auxiliaresPK;
    private Double tiporeferencia;
    private String referencia;
    private Integer idorigenpr;
    private Integer idproductor;
    private Integer idauxiliarr;

    public ReferenciasP() {
    }

    public AuxiliaresPK getAuxiliaresPK() {
        return auxiliaresPK;
    }

    public void setAuxiliaresPK(AuxiliaresPK auxiliaresPK) {
        this.auxiliaresPK = auxiliaresPK;
    }

    public Double getTiporeferencia() {
        return tiporeferencia;
    }

    public void setTiporeferencia(Double tiporeferencia) {
        this.tiporeferencia = tiporeferencia;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public Integer getIdorigenpr() {
        return idorigenpr;
    }

    public void setIdorigenpr(Integer idorigenpr) {
        this.idorigenpr = idorigenpr;
    }

    public Integer getIdproductor() {
        return idproductor;
    }

    public void setIdproductor(Integer idproductor) {
        this.idproductor = idproductor;
    }

    public Integer getIdauxiliarr() {
        return idauxiliarr;
    }

    public void setIdauxiliarr(Integer idauxiliarr) {
        this.idauxiliarr = idauxiliarr;
    }

    @Override
    public String toString() {
        return "ReferenciasP{" + "auxiliaresPK=" + auxiliaresPK + ", tiporeferencia=" + tiporeferencia + ", referencia=" + referencia + ", idorigenpr=" + idorigenpr + ", idproductor=" + idproductor + ", idauxiliarr=" + idauxiliarr + '}';
    }
    
    private static final long serialVersionUID = 1L;
}
