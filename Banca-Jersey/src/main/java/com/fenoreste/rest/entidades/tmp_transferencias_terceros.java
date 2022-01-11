/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.entidades;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author wilmer
 */
@Entity
@Table(name = "temporales_depositos_terceros")
public class tmp_transferencias_terceros implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    protected PersonasPK personas;
    @Column(name = "monto")
    private Double monto;
    @Column(name = "fecha")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;
    @Column(name = "cargoabono")
    private Integer cargoabono;
    @Column(name = "totalenudis")
    private Double totalenudis;
    @Column(name = "tipotransferencia")
    private String tipotransferencia;

    public tmp_transferencias_terceros() {
    }

    public PersonasPK getPersonas() {
        return personas;
    }

    public void setPersonas(PersonasPK personas) {
        this.personas = personas;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Integer getCargoabono() {
        return cargoabono;
    }

    public void setCargoabono(Integer cargoabono) {
        this.cargoabono = cargoabono;
    }

    public Double getTotalenudis() {
        return totalenudis;
    }

    public void setTotalenudis(Double totalenudis) {
        this.totalenudis = totalenudis;
    }

    public String getTipotransferencia() {
        return tipotransferencia;
    }

    public void setTipotransferencia(String tipotransferencia) {
        this.tipotransferencia = tipotransferencia;
    }

    @Override
    public String toString() {
        return "tmp_transferencias_terceros{" + "personas=" + personas + ", monto=" + monto + ", fecha=" + fecha + ", cargoabono=" + cargoabono + ", totalenudis=" + totalenudis + ", tipotransferencia=" + tipotransferencia + '}';
    }
    
    

}
