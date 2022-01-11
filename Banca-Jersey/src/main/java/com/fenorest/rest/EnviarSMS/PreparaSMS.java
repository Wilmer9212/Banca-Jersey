/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenorest.rest.EnviarSMS;

import com.fenoreste.rest.DTO.OgsDTO;
import com.fenoreste.rest.DTO.OpaDTO;
import com.fenoreste.rest.Util.Utilidades;
import com.fenoreste.rest.Util.UtilidadesGenerales;
import com.fenoreste.rest.entidades.AuxiliaresD;
import com.fenoreste.rest.entidades.Persona;
import com.fenoreste.rest.entidades.PersonasPK;
import com.fenoreste.rest.entidades.Productos;
import com.fenoreste.rest.entidades.Tablas;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author wilmer
 */
public class PreparaSMS {

    UtilidadesGenerales util = new UtilidadesGenerales();
    Utilidades util2 = new Utilidades();
    EnviarSMS sendSMS = new EnviarSMS();

    // ENVIA EL SMS METODO PRA CSN
    public String enviaSMS_CSN(EntityManager em, String montoAbono, int identificadorOperacion, String debitAccount, String creditAccount, String numeroSocio) {
        System.out.println("preparando el sms");
        String respuesta_sms = "";
        //consulto en tablas si existe la url del script de san nicolas para envio de mensajes
        try {

            Tablas tablasUrlSMS = util.busquedaTabla(em, "bankingly_banca_movil", "liga_envio_mensajes");
            OpaDTO opa_origen = util2.opa(debitAccount);

            OpaDTO opa_destino = null;

            //Buscamos el movimiento que se hizo
            String busqueda_aux_d_origen = "SELECT * FROM auxiliares_d WHERE idorigenp=" + opa_origen.getIdorigenp() + " AND idproducto=" + opa_origen.getIdproducto() + " AND idauxiliar=" + opa_origen.getIdauxiliar() + " ORDER BY fecha DESC LIMIT 1";
            System.out.println("Busqueda del auxiliar:" + busqueda_aux_d_origen);
            Query query_aux_d_origen = em.createNativeQuery(busqueda_aux_d_origen, AuxiliaresD.class);
            AuxiliaresD ad_origen = (AuxiliaresD) query_aux_d_origen.getSingleResult();
            AuxiliaresD ad_destino = null;

            //Buscamos el nombre del producto 
            Productos pr_origen = em.find(Productos.class, opa_origen.getIdproducto());
            Productos pr_destino = null;
            String auth_destino = "";
            if (identificadorOperacion != 5) {
                opa_destino = util2.opa(creditAccount);
                String busqueda_aux_d_destino = "SELECT * FROM auxiliares_d WHERE idorigenp=" + opa_destino.getIdorigenp() + " AND idproducto=" + opa_destino.getIdproducto() + " AND idauxiliar=" + opa_destino.getIdauxiliar() + " ORDER BY fecha DESC LIMIT 1";
                System.out.println("Busqueda del auxiliar:" + busqueda_aux_d_destino);
                Query query_aux_d_destino = em.createNativeQuery(busqueda_aux_d_destino, AuxiliaresD.class);
                ad_destino = (AuxiliaresD) query_aux_d_destino.getSingleResult();
                pr_destino = em.find(Productos.class, opa_destino.getIdproducto());
                auth_destino = String.valueOf(ad_destino.getTransaccion().intValue());//ad_destino.getIdorigenc() + "" + ad_destino.getPeriodo() + "" + ad_destino.getIdtipo() + "" + ad_destino.getIdpoliza();

            }

            if (tablasUrlSMS.getDato2().length() > 0) {
                System.out.println("se encontro la url para envio de sms");
                //Obtengo el celular del socio 
                OgsDTO ogs = util2.ogs(numeroSocio);
                PersonasPK personaPK = new PersonasPK(ogs.getIdorigen(), ogs.getIdgrupo(), ogs.getIdsocio());
                Persona p = em.find(Persona.class, personaPK);
                Tablas tablaContenidoSMS = null;
                String contenidoSMS = "";
                String auth_origen = String.valueOf(ad_origen.getTransaccion());//ad_origen.getIdorigenc() + "" + ad_origen.getPeriodo() + "" + ad_origen.getIdtipo() + "" + ad_origen.getIdpoliza();
                //Se identifica para transferencias a cuentas propias
                if (identificadorOperacion == 1) {
                    tablaContenidoSMS = util.busquedaTabla(em, "bankingly_banca_movil", "sms_retiro_cuenta_propia");
                    System.out.println("tabla contenido sms:" + tablaContenidoSMS);
                    contenidoSMS = contenidoSMS(tablaContenidoSMS.getDato2(), montoAbono, pr_origen.getNombre(), pr_destino.getNombre(), auth_origen, auth_destino);
                    System.out.println("El contenido de tu mensaje es:" + contenidoSMS);
                    //sendSMS.enviar(tablasUrlSMS.getDato2(), p.getCelular(), contenidoSMS);
                    respuesta_sms = sendSMS.enviarSMS(tablasUrlSMS.getDato2(), p.getCelular(), contenidoSMS);
                    //Transferencia a terceros dentro de la entidad
                } else if (identificadorOperacion == 2) {
                    System.out.println("Tercero");
                    tablaContenidoSMS = util.busquedaTabla(em, "bankingly_banca_movil", "sms_retiro_cuenta_tercero");
                    System.out.println("tabla contenido sms:" + tablaContenidoSMS);
                    contenidoSMS = contenidoSMS(tablaContenidoSMS.getDato2(), montoAbono, pr_origen.getNombre(), pr_destino.getNombre(), auth_origen, auth_destino);
                    System.out.println("El contenido de tu mensaje es:" + contenidoSMS);
                    respuesta_sms = sendSMS.enviarSMS(tablasUrlSMS.getDato2(), p.getCelular(), contenidoSMS);
                    //Pago de prestamos
                } else if (identificadorOperacion == 3) {
                    System.out.println("Pago prestamo propio");
                    tablaContenidoSMS = util.busquedaTabla(em, "bankingly_banca_movil", "sms_retiro_cuenta_propia");
                    System.out.println("tabla contenido sms:" + tablaContenidoSMS);
                    contenidoSMS = contenidoSMS(tablaContenidoSMS.getDato2(), montoAbono, pr_origen.getNombre(), pr_destino.getNombre(), auth_origen, auth_destino);
                    System.out.println("El contenido de tu mensaje es:" + contenidoSMS);
                    respuesta_sms = sendSMS.enviarSMS(tablasUrlSMS.getDato2(), p.getCelular(), contenidoSMS);
                } else if (identificadorOperacion == 4) {
                    System.out.println("Pago prestamo tercero");
                    tablaContenidoSMS = util.busquedaTabla(em, "bankingly_banca_movil", "sms_retiro_cuenta_tercero");
                    System.out.println("tabla contenido sms:" + tablaContenidoSMS);
                    contenidoSMS = contenidoSMS(tablaContenidoSMS.getDato2(), montoAbono, pr_origen.getNombre(), pr_destino.getNombre(), auth_origen, auth_destino);
                    System.out.println("El contenido de tu mensaje es:" + contenidoSMS);
                    respuesta_sms = sendSMS.enviarSMS(tablasUrlSMS.getDato2(), p.getCelular(), contenidoSMS);
                }
            }
        } catch (Exception e) {
            System.out.println("Error en sms:" + e.getMessage());
        }
        return respuesta_sms;
    }

    // RELLENA EL CONTENIDO DEL SMS
    private String contenidoSMS(String contenidoSMS, String monto, String productoOrigen, String productoDestino, String authOrigen, String authDestino) {

        Date hoy = new Date();
        System.out.println("Formando contenido fecha:" + hoy);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyy HH:MM:ss");
        String fecha = sdf.format(hoy);
        System.out.println("Fecha String:" + fecha);
        return contenidoSMS.replace("@monto@", monto)
                .replace("@fechayHora@", fecha)
                .replace("@productoOrigen@", productoOrigen)
                .replace("@productoDestino@", productoDestino)
                .replace("@autorizacionOrigen@", authOrigen)
                .replace("@autorizacionDestino@", authDestino);
    }

    public String enviaSMSOrdenSpei(EntityManager em, String ogss, int idorden, String estado, String folio, String causaDevolucion) {
        Tablas tablasUrlSMS = util.busquedaTabla(em, "bankingly_banca_movil", "liga_envio_mensajes");
        OgsDTO ogs = util2.ogs(ogss);
        PersonasPK personaPK = new PersonasPK(ogs.getIdorigen(), ogs.getIdgrupo(), ogs.getIdsocio());
        Persona p = em.find(Persona.class, personaPK);
        System.out.println("Pago orden SPEI");
        Tablas tablaContenidoSMS = util.busquedaTabla(em, "bankingly_banca_movil", "sms_actualizacion_estado_spei");
        System.out.println("tabla contenido sms:" + tablaContenidoSMS);
        String contenidoSMS = contenidoOrdenSPEI(tablaContenidoSMS.getDato2(), idorden, estado, folio, causaDevolucion);
        System.out.println("El contenido de tu mensaje es:" + contenidoSMS);

        String respuesta_sms = sendSMS.enviarSMS(tablasUrlSMS.getDato2(), p.getCelular(), contenidoSMS);
        return "";

    }
    
    public String enviaSMSNotificaEstadoCuenta(EntityManager em, String ogss, String cuenta,String estado,String observaciones) {
        Tablas tablasUrlSMS = util.busquedaTabla(em, "bankingly_banca_movil", "liga_envio_mensajes");
        OgsDTO ogs = util2.ogs(ogss);
        PersonasPK personaPK = new PersonasPK(ogs.getIdorigen(), ogs.getIdgrupo(), ogs.getIdsocio());
        Persona p = em.find(Persona.class, personaPK);
        System.out.println("Pago orden SPEI");
        Tablas tablaContenidoSMS = util.busquedaTabla(em, "bankingly_banca_movil", "sms_notifica_creacion_cuenta");
        System.out.println("tabla contenido sms:" + tablaContenidoSMS);
        String contenidoSMS = contenidoNotificaEstadoCuenta(tablaContenidoSMS.getDato2(), cuenta, estado,observaciones);
        System.out.println("El contenido de tu mensaje es:" + contenidoSMS);        
        String respuesta_sms = sendSMS.enviarSMS(tablasUrlSMS.getDato2(), p.getCelular(), contenidoSMS);
        return "";

    }
    
    public String enviarTokenAltaTerceros(EntityManager em,String numero,String token){         
         Tablas tablasUrlSMS = util.busquedaTabla(em, "bankingly_banca_movil", "liga_envio_mensajes");
         Tablas tb_contenido_sms_token = util.busquedaTabla(em,"bankingly_banca_movil","sms_token");
         String contenido = tb_contenido_sms_token.getDato2().replace("@@token@@", token);
         String respuesta_sms = sendSMS.enviarSMS(tablasUrlSMS.getDato2(), numero,contenido);
         System.out.println("Respuesta :"+respuesta_sms);
         return respuesta_sms;
    }
    
    private String contenidoOrdenSPEI(String contenidoSMS, int idorden, String estado, String folio, String causaDevolucion) {

        Date hoy = new Date();
        System.out.println("Formando contenido fecha:" + hoy);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyy HH:MM:ss");
        String fecha = sdf.format(hoy);
        System.out.println("Fecha String:" + fecha);

        return contenidoSMS
                .replace("@fechayHora@", fecha)
                .replace("@idorden@", String.valueOf(idorden))
                .replace("@estado@", estado)
                .replace("@folio@", folio)
                .replace("@causadevolucion@", causaDevolucion);
    }

    private String contenidoNotificaEstadoCuenta(String contenidoSMS, String cuenta,String estado, String observaciones) {
        Date hoy = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyy HH:MM:ss");
        String fecha = sdf.format(hoy);
        System.out.println("Fecha String:" + fecha);

        return contenidoSMS
                .replace("@fechayHora@", fecha)
                .replace("@cuenta@", cuenta)
                .replace("@estado@", estado)
                .replace("@observaciones@",observaciones);
                
    }

}
