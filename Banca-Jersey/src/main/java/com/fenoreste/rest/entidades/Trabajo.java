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
@Table(name = "trabajo")
public class Trabajo implements Serializable {

    @EmbeddedId
    PersonasPK llaves;
    private String nombre;
    private String calle;
    private String numero;
    private Integer idcolonia;
    private String telefono;
    private String telefono2;
    private String ocupacion;
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaingreso;
    private String puesto;
    private String entrecalles;
    private double tipo_empleo;
    private double sector_laboral;
    private double giro_empresa;
    private double forma_comprobar_ing;
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechasalida;
    private Integer consecutivo;
    private double ing_mensual_bruto;
    private double ing_mensual_neto;
    private int frecuencia_ingresos;
    private int tipo_ocupacion;
    private Integer num_empleados;
    private Integer num_taxis;
    private double deducciones_deudas;
    private double deducciones_otros;
    private Integer actividad_economica;
    private Integer ocupacion_numero;
    private String actividad_economica_pld;
    private String ext_tel_1;
    private String ext_tel_2;

    public Trabajo() {
    }

    public Trabajo(PersonasPK llaves, String nombre, String calle, String numero, Integer idcolonia, String telefono, String telefono2, String ocupacion, Date fechaingreso, String puesto, String entrecalles, double tipo_empleo, double sector_laboral, double giro_empresa, double forma_comprobar_ing, Date fechasalida, Integer consecutivo, double ing_mensual_bruto, double ing_mensual_neto, int frecuencia_ingresos, int tipo_ocupacion, Integer num_empleados, Integer num_taxis, double deducciones_deudas, double deducciones_otros, Integer actividad_economica, Integer ocupacion_numero, String actividad_economica_pld, String ext_tel_1, String ext_tel_2) {
        this.llaves = llaves;
        this.nombre = nombre;
        this.calle = calle;
        this.numero = numero;
        this.idcolonia = idcolonia;
        this.telefono = telefono;
        this.telefono2 = telefono2;
        this.ocupacion = ocupacion;
        this.fechaingreso = fechaingreso;
        this.puesto = puesto;
        this.entrecalles = entrecalles;
        this.tipo_empleo = tipo_empleo;
        this.sector_laboral = sector_laboral;
        this.giro_empresa = giro_empresa;
        this.forma_comprobar_ing = forma_comprobar_ing;
        this.fechasalida = fechasalida;
        this.consecutivo = consecutivo;
        this.ing_mensual_bruto = ing_mensual_bruto;
        this.ing_mensual_neto = ing_mensual_neto;
        this.frecuencia_ingresos = frecuencia_ingresos;
        this.tipo_ocupacion = tipo_ocupacion;
        this.num_empleados = num_empleados;
        this.num_taxis = num_taxis;
        this.deducciones_deudas = deducciones_deudas;
        this.deducciones_otros = deducciones_otros;
        this.actividad_economica = actividad_economica;
        this.ocupacion_numero = ocupacion_numero;
        this.actividad_economica_pld = actividad_economica_pld;
        this.ext_tel_1 = ext_tel_1;
        this.ext_tel_2 = ext_tel_2;
    }

    public PersonasPK getLlaves() {
        return llaves;
    }

    public void setLlaves(PersonasPK llaves) {
        this.llaves = llaves;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Integer getIdcolonia() {
        return idcolonia;
    }

    public void setIdcolonia(Integer idcolonia) {
        this.idcolonia = idcolonia;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getTelefono2() {
        return telefono2;
    }

    public void setTelefono2(String telefono2) {
        this.telefono2 = telefono2;
    }

    public String getOcupacion() {
        return ocupacion;
    }

    public void setOcupacion(String ocupacion) {
        this.ocupacion = ocupacion;
    }

    public Date getFechaingreso() {
        return fechaingreso;
    }

    public void setFechaingreso(Date fechaingreso) {
        this.fechaingreso = fechaingreso;
    }

    public String getPuesto() {
        return puesto;
    }

    public void setPuesto(String puesto) {
        this.puesto = puesto;
    }

    public String getEntrecalles() {
        return entrecalles;
    }

    public void setEntrecalles(String entrecalles) {
        this.entrecalles = entrecalles;
    }

    public double getTipo_empleo() {
        return tipo_empleo;
    }

    public void setTipo_empleo(double tipo_empleo) {
        this.tipo_empleo = tipo_empleo;
    }

    public double getSector_laboral() {
        return sector_laboral;
    }

    public void setSector_laboral(double sector_laboral) {
        this.sector_laboral = sector_laboral;
    }

    public double getGiro_empresa() {
        return giro_empresa;
    }

    public void setGiro_empresa(double giro_empresa) {
        this.giro_empresa = giro_empresa;
    }

    public double getForma_comprobar_ing() {
        return forma_comprobar_ing;
    }

    public void setForma_comprobar_ing(double forma_comprobar_ing) {
        this.forma_comprobar_ing = forma_comprobar_ing;
    }

    public Date getFechasalida() {
        return fechasalida;
    }

    public void setFechasalida(Date fechasalida) {
        this.fechasalida = fechasalida;
    }

    public Integer getConsecutivo() {
        return consecutivo;
    }

    public void setConsecutivo(Integer consecutivo) {
        this.consecutivo = consecutivo;
    }

    public double getIng_mensual_bruto() {
        return ing_mensual_bruto;
    }

    public void setIng_mensual_bruto(double ing_mensual_bruto) {
        this.ing_mensual_bruto = ing_mensual_bruto;
    }

    public double getIng_mensual_neto() {
        return ing_mensual_neto;
    }

    public void setIng_mensual_neto(double ing_mensual_neto) {
        this.ing_mensual_neto = ing_mensual_neto;
    }

    public int getFrecuencia_ingresos() {
        return frecuencia_ingresos;
    }

    public void setFrecuencia_ingresos(int frecuencia_ingresos) {
        this.frecuencia_ingresos = frecuencia_ingresos;
    }

    public int getTipo_ocupacion() {
        return tipo_ocupacion;
    }

    public void setTipo_ocupacion(int tipo_ocupacion) {
        this.tipo_ocupacion = tipo_ocupacion;
    }

    public Integer getNum_empleados() {
        return num_empleados;
    }

    public void setNum_empleados(Integer num_empleados) {
        this.num_empleados = num_empleados;
    }

    public Integer getNum_taxis() {
        return num_taxis;
    }

    public void setNum_taxis(Integer num_taxis) {
        this.num_taxis = num_taxis;
    }

    public double getDeducciones_deudas() {
        return deducciones_deudas;
    }

    public void setDeducciones_deudas(double deducciones_deudas) {
        this.deducciones_deudas = deducciones_deudas;
    }

    public double getDeducciones_otros() {
        return deducciones_otros;
    }

    public void setDeducciones_otros(double deducciones_otros) {
        this.deducciones_otros = deducciones_otros;
    }

    public Integer getActividad_economica() {
        return actividad_economica;
    }

    public void setActividad_economica(Integer actividad_economica) {
        this.actividad_economica = actividad_economica;
    }

    public Integer getOcupacion_numero() {
        return ocupacion_numero;
    }

    public void setOcupacion_numero(Integer ocupacion_numero) {
        this.ocupacion_numero = ocupacion_numero;
    }

    public String getActividad_economica_pld() {
        return actividad_economica_pld;
    }

    public void setActividad_economica_pld(String actividad_economica_pld) {
        this.actividad_economica_pld = actividad_economica_pld;
    }

    public String getExt_tel_1() {
        return ext_tel_1;
    }

    public void setExt_tel_1(String ext_tel_1) {
        this.ext_tel_1 = ext_tel_1;
    }

    public String getExt_tel_2() {
        return ext_tel_2;
    }

    public void setExt_tel_2(String ext_tel_2) {
        this.ext_tel_2 = ext_tel_2;
    }

    @Override
    public String toString() {
        return "Trabajo{" + "llaves=" + llaves + ", nombre=" + nombre + ", calle=" + calle + ", numero=" + numero + ", idcolonia=" + idcolonia + ", telefono=" + telefono + ", telefono2=" + telefono2 + ", ocupacion=" + ocupacion + ", fechaingreso=" + fechaingreso + ", puesto=" + puesto + ", entrecalles=" + entrecalles + ", tipo_empleo=" + tipo_empleo + ", sector_laboral=" + sector_laboral + ", giro_empresa=" + giro_empresa + ", forma_comprobar_ing=" + forma_comprobar_ing + ", fechasalida=" + fechasalida + ", consecutivo=" + consecutivo + ", ing_mensual_bruto=" + ing_mensual_bruto + ", ing_mensual_neto=" + ing_mensual_neto + ", frecuencia_ingresos=" + frecuencia_ingresos + ", tipo_ocupacion=" + tipo_ocupacion + ", num_empleados=" + num_empleados + ", num_taxis=" + num_taxis + ", deducciones_deudas=" + deducciones_deudas + ", deducciones_otros=" + deducciones_otros + ", actividad_economica=" + actividad_economica + ", ocupacion_numero=" + ocupacion_numero + ", actividad_economica_pld=" + actividad_economica_pld + ", ext_tel_1=" + ext_tel_1 + ", ext_tel_2=" + ext_tel_2 + '}';
    }

}
