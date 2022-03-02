/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.service;

import javax.ejb.LocalBean;


/**
 *
 * @author wilmer
 */
@LocalBean
public interface IFuncionesService {
    
    public Double montoLiquidarPrestamo(Integer idorigenp,Integer idproducto,Integer idauxiliar,String fecha);
}
