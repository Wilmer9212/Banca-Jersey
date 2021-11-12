/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.Util;

import com.fenoreste.rest.entidades.Tablas;
import com.fenoreste.rest.entidades.TablasPK;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author wilmer
 */
public class UtilidadesGenerales {

    public boolean actividad(EntityManager em) {
        boolean bandera = false;
        
        try {
            String actividad = "SELECT sai_bankingly_servicio_activo_inactivo()";
            Query query = em.createNativeQuery(actividad);
            bandera = (boolean) query.getSingleResult();
        } catch (Exception e) {           
            System.out.println("Error al recuperar el tiempo de actividad:" + e.getMessage());
        }
        return bandera;
    }

    public String obtenerOrigen(EntityManager em) {
        String origen = "";
        try {
            Query query = em.createNativeQuery("SELECT nombre FROM origenes WHERE matriz=0");
            origen = String.valueOf(query.getSingleResult());            
        } catch (Exception e) {
            System.out.println("Error al buscar origen:" + e.getMessage());            
            return origen;
        }
        return origen.toUpperCase().replace(" ","").toUpperCase();
    }
    
    
        public String obtenerOrigen(int idorigenp,EntityManager em) {
        String origen = "";
        try {
            Query query = em.createNativeQuery("SELECT nombre FROM origenes WHERE idorigen="+idorigenp);
            origen = String.valueOf(query.getSingleResult());            
        } catch (Exception e) {
            System.out.println("Error al buscar origen:" + e.getMessage());            
            return origen;
        }
        return origen;
    }

        

    public Tablas busquedaTabla(EntityManager em,String idtabla, String idelemento) {
       Tablas tb = null;
        System.out.println("Buscando la tabla idtabla:"+idtabla+" idelemento:"+idelemento);
        try {
            TablasPK tbPK = new TablasPK(idtabla, idelemento);
            tb = em.find(Tablas.class, tbPK);
        } catch (Exception e) {
            System.out.println("Error al buscar tabla:" + e.getMessage());            
            return tb;
        } 
        System.out.println("la Tabla encontrada es="+tb);
        return tb;
    }
    
   

}
