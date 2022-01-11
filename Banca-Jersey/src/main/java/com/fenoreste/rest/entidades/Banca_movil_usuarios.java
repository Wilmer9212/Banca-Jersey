/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.entidades;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 *
 * @author Elliot
 */
@Entity
@Table(name = "banca_movil_usuarios_bankingly")
public class Banca_movil_usuarios implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @EmbeddedId
    protected PersonasPK personasPK;    
    @Column(name="alias_usuario")
    private String alias_usuario;
    @Column(name="idorigenp")
    private int idorigenp;
    @Column(name = "idproducto")
    private int idproducto;
    @Column(name="idauxiliar")
    private int idauxiliar;
    @Column(name="estatus")
    private boolean estatus;

    public Banca_movil_usuarios() {
    
    }

    public Banca_movil_usuarios(PersonasPK personasPK, String alias_usuario, int idorigenp, int idproducto, int idauxiliar, boolean estatus) {
        this.personasPK = personasPK;
        this.alias_usuario = alias_usuario;
        this.idorigenp = idorigenp;
        this.idproducto = idproducto;
        this.idauxiliar = idauxiliar;
        this.estatus = estatus;
    }

    public PersonasPK getPersonasPK() {
        return personasPK;
    }

    public void setPersonasPK(PersonasPK personasPK) {
        this.personasPK = personasPK;
    }

    public String getAlias_usuario() {
        return alias_usuario;
    }

    public void setAlias_usuario(String alias_usuario) {
        this.alias_usuario = alias_usuario;
    }

    public int getIdorigenp() {
        return idorigenp;
    }

    public void setIdorigenp(int idorigenp) {
        this.idorigenp = idorigenp;
    }

    public int getIdproducto() {
        return idproducto;
    }

    public void setIdproducto(int idproducto) {
        this.idproducto = idproducto;
    }

    public int getIdauxiliar() {
        return idauxiliar;
    }

    public void setIdauxiliar(int idauxiliar) {
        this.idauxiliar = idauxiliar;
    }

    public boolean isEstatus() {
        return estatus;
    }

    public void setEstatus(boolean estatus) {
        this.estatus = estatus;
    }

    @Override
    public String toString() {
        return "usuarios_banca_bankingly{" + "personasPK=" + personasPK + ", alias_usuario=" + alias_usuario + ", idorigenp=" + idorigenp + ", idproducto=" + idproducto + ", idauxiliar=" + idauxiliar + ", estatus=" + estatus + '}';
    }

   

}
