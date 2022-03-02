/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.service;

import com.fenoreste.rest.Util.AbstractFacade;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author wilmer
 */
public class IFuncionesServiceImpl implements IFuncionesService{

    @Override
    public Double montoLiquidarPrestamo(Integer idorigenp, Integer idproducto, Integer idauxiliar, String fecha) {
        EntityManager em = AbstractFacade.conexion();
        System.out.println("entro aquiiiiiiiiiiiiiiiiiiiiiiiiiii");
        Double monto_liquidacion = 0.0;
        try {
            String sql_funcion = "SELECT monto_para_liquidar_prestamo("+idorigenp+","+idproducto+","+idauxiliar+",'"+fecha+"')";
            Query query_funcion = em.createNativeQuery(sql_funcion);
            monto_liquidacion = Double.parseDouble(String.valueOf(query_funcion.getSingleResult()));            
        } catch (Exception e) {
            System.out.println("Error al obtener monto de liquidacion_prestamo :"+e.getMessage());
        }
        return monto_liquidacion;        
    }
    
    
    
}
