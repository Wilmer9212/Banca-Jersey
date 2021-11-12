/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.entidades;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author wilmer
 */
@Entity
@Table(name="ws_siscoop_clabe_interbancaria")
public class Clabes_Interbancarias implements Serializable  {
    
    private static final long serialVersionUID = 1L;
    
    @EmbeddedId
    AuxiliaresPK aux_pk;
    private String clabe;
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha_hora;
    private boolean asignada;
    private boolean activa;
    private boolean bloqueada;

    public Clabes_Interbancarias() {
    }

    public AuxiliaresPK getAux_pk() {
        return aux_pk;
    }

    public void setAux_pk(AuxiliaresPK aux_pk) {
        this.aux_pk = aux_pk;
    }

    public String getClabe() {
        return clabe;
    }

    public void setClabe(String clabe) {
        this.clabe = clabe;
    }

    public Date getFecha_hora() {
        return fecha_hora;
    }

    public void setFecha_hora(Date fecha_hora) {
        this.fecha_hora = fecha_hora;
    }

    public boolean isAsignada() {
        return asignada;
    }

    public void setAsignada(boolean asignada) {
        this.asignada = asignada;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    public boolean isBloqueada() {
        return bloqueada;
    }

    public void setBloqueada(boolean bloqueada) {
        this.bloqueada = bloqueada;
    }

    @Override
    public String toString() {
        return "Clabes_Interbancarias{" + "aux_pk=" + aux_pk + ", clabe=" + clabe + ", fecha_hora=" + fecha_hora + ", asignada=" + asignada + ", activa=" + activa + ", bloqueada=" + bloqueada + '}';
    }
    
    
}
