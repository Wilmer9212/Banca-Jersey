/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.Util;

import com.fenoreste.rest.entidades.Auxiliares;
import com.fenoreste.rest.entidades.AuxiliaresPK;
import com.fenoreste.rest.entidades.Origenes;
import com.fenoreste.rest.entidades.Persona;
import com.fenoreste.rest.entidades.PersonasPK;
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

    public boolean actividad_spei(EntityManager em) {
        boolean bandera = false;

        try {
            String actividad = "SELECT sai_bankingly_servicio_activo_inactivo_spei()";
            Query query = em.createNativeQuery(actividad);
            bandera = (boolean) query.getSingleResult();
        } catch (Exception e) {
            System.out.println("Error al recuperar el tiempo de actividad:" + e.getMessage());
        }
        return bandera;
    }

    public int obtenerOrigen(EntityManager em) {
        int origen = 0;
        try {
            Query query = em.createNativeQuery("SELECT idorigen FROM origenes WHERE matriz=0");
            origen = Integer.parseInt(String.valueOf(query.getSingleResult()));
        } catch (Exception e) {
            System.out.println("Error al buscar origen:" + e.getMessage());
            return 0;
        }
        return origen;
    }

    public String obtenerOrigen(int idorigenp, EntityManager em) {
        String origen = "";
        try {
            Query query = em.createNativeQuery("SELECT nombre FROM origenes WHERE idorigen=" + idorigenp);
            origen = String.valueOf(query.getSingleResult());
        } catch (Exception e) {
            System.out.println("Error al buscar origen:" + e.getMessage());
            return origen;
        }
        return origen;
    }

    public Tablas busquedaTabla(EntityManager em, String idtabla, String idelemento) {
        Tablas tb = null;
        System.out.println("Buscando la tabla idtabla:" + idtabla + " idelemento:" + idelemento);
        try {
            TablasPK tbPK = new TablasPK(idtabla, idelemento);
            tb = em.find(Tablas.class, tbPK);
        } catch (Exception e) {
            System.out.println("Error al buscar tabla:" + e.getMessage());
            return tb;
        }
        System.out.println("la Tabla encontrada es=" + tb);
        return tb;
    }

    public boolean validacionSopar(int idorigen, int idgrupo, int idsocio,int identificador) {
        boolean bandera_sopar = false;        
        EntityManager em = AbstractFacade.conexion();
        try {
            Persona p = null;
            Auxiliares a = null;
            if(identificador==1){
            PersonasPK personasPk= new PersonasPK(idorigen, idgrupo,idsocio);
            p=em.find(Persona.class, personasPk);      
            idorigen=p.getPersonasPK().getIdorigen();
            idgrupo=p.getPersonasPK().getIdgrupo();
            idsocio=p.getPersonasPK().getIdsocio();
            }else{
                AuxiliaresPK auxPk=new AuxiliaresPK(idorigen, idgrupo,idsocio);
                a=em.find(Auxiliares.class,auxPk);
                idorigen=a.getIdorigen();
                idgrupo=a.getIdgrupo();
                idsocio=a.getIdsocio();
            }
             
            Tablas tb_sopar = busquedaTabla(em, "bankingly_banca_movil", "sopar");
            String consulta_sopar = "SELECT count(*) FROM sopar WHERE idorigen=" + idorigen + " AND idgrupo=" + idgrupo + " AND idsocio=" + idsocio + " AND tipo='" + tb_sopar.getDato2() + "'";
            Query query_sopar = em.createNativeQuery(consulta_sopar);
            int count_sopar = Integer.parseInt(String.valueOf(query_sopar.getSingleResult()));
            if(count_sopar>0){
                bandera_sopar=true;
            }
        } catch (Exception e) {
            System.out.println("Error al generar validacione en tabla sopar:"+e.getMessage());
        }
        em.close();
        return bandera_sopar;

    }
    
    public Origenes busquedaMatriz(){
        EntityManager em = AbstractFacade.conexion();
        Origenes origen_matriz = null;
        try {
            String sql = "SELECT * FROM origenes WHERE matriz=0";
            Query query_sql = em.createNativeQuery(sql,Origenes.class);
            origen_matriz = (Origenes) query_sql.getSingleResult();
            
        } catch (Exception e) {
            System.out.println("Error al buscar la matriz:"+e.getMessage());
        }
        return origen_matriz;
    }
    
    

}
