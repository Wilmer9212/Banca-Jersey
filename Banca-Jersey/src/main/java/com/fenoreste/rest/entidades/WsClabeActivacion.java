/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.entidades;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
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
@Table(name="ws_bankingly_clabe_activacion")
public class WsClabeActivacion implements Serializable{
    
   @Id
   @Column(name="clabe")   
   private String clabe;
   @Column(name="empresa")
   private String empresa;
   @Column(name="estado")
   private String estado;
   @Column(name="observacion")
   private String observacion;
   @Column(name="activa")
   private boolean activa;
   @Column(name="fecha_hora")
   @Temporal(TemporalType.TIMESTAMP)
   private Date fecha_hora;

    public WsClabeActivacion() {
    }

    public String getClabe() {
        return clabe;
    }

    public void setClabe(String clabe) {
        this.clabe = clabe;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    public Date getFecha_hora() {
        return fecha_hora;
    }

    public void setFecha_hora(Date fecha_hora) {
        this.fecha_hora = fecha_hora;
    }

    
    
}
