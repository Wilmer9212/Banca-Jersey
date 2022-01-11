package com.fenoreste.rest.WsTDD;

import com.fenoreste.rest.Util.AbstractFacade;
import com.fenoreste.rest.Util.UtilidadesGenerales;
import com.fenoreste.rest.entidades.Tablas;
import com.fenoreste.rest.entidades.TablasPK;
import com.fenoreste.rest.entidades.WsSiscoopFoliosTarjetas1;
import com.fenoreste.rest.entidades.WsSiscoopFoliosTarjetasPK1;
import javax.persistence.EntityManager;
import com.syc.ws.endpoint.siscoop.BalanceQueryResponseDto;
import com.syc.ws.endpoint.siscoop.DoWithdrawalAccountResponse;
import com.syc.ws.endpoint.siscoop.LoadBalanceResponse;
import consumo_tdd.Siscoop_TDD;
import java.sql.Timestamp;
import javax.persistence.Query;

/**
 *
 * @author Elliot
 */
public class TarjetaDeDebito {

    // CONSULTA Y ACTUALIZA EL SALDO DE LA TarjetaDeDebito
    public Tablas productoTddWS(EntityManager em) {
        try {
            TablasPK pkt = new TablasPK("identificador_uso_tdd", "activa");
            Tablas tb = em.find(Tablas.class, pkt);
            if (tb != null) {
                return tb;
            } else {
                return null;
            }
        } catch (Exception e) {
            System.out.println("No existe producto activo para tdd:" + e.getMessage());
        }
        return null;
    }

    // PRODUCTO VALIDO PARA LA TDD
    public Tablas productoTddwebservice(EntityManager em) {
        Tablas tabla = null;
        System.out.println("Llegando a buscar el producto para Tarjeta de debito....");
        try {
            EntityManager d = AbstractFacade.conexion();
            // Producto de la tdd

            TablasPK tablasPK = new TablasPK("bankingly_banca_movil", "producto_tdd");
            tabla = d.find(Tablas.class, tablasPK);

        } catch (NumberFormatException e) {
            System.out.println("Error en consultar producto en producto_para_webservice de TarjetaDeDebito." + e.getMessage());
            return tabla;
        }
        return tabla;
    }

    public WsSiscoopFoliosTarjetas1 buscaTarjetaTDD(int idorigenp, int idproducto, int idauxiliar, EntityManager em) {
        WsSiscoopFoliosTarjetasPK1 foliosPK1 = new WsSiscoopFoliosTarjetasPK1(idorigenp, idproducto, idauxiliar);
        WsSiscoopFoliosTarjetas1 wsSiscoopFoliosTarjetas = new WsSiscoopFoliosTarjetas1();
        try {
            String consulta = " SELECT w.* "
                    + "         FROM ws_siscoop_folios_tarjetas w "
                    + "         INNER JOIN ws_siscoop_tarjetas td using(idtarjeta)"
                    + "         WHERE w.idorigenp = ? "
                    + "         AND w.idproducto = ?"
                    + "         AND w.idauxiliar = ?"
                    + "          AND td.fecha_vencimiento > (select distinct fechatrabajo from origenes limit 1) ";
            System.out.println("Consulta tarjeta:"+consulta);
            Query query = em.createNativeQuery(consulta, WsSiscoopFoliosTarjetas1.class);
            query.setParameter(1, idorigenp);
            query.setParameter(2, idproducto);
            query.setParameter(3, idauxiliar);
            wsSiscoopFoliosTarjetas = (WsSiscoopFoliosTarjetas1) query.getSingleResult();
            if (wsSiscoopFoliosTarjetas != null) {
                wsSiscoopFoliosTarjetas.setActiva(wsSiscoopFoliosTarjetas.getActiva());
                wsSiscoopFoliosTarjetas.setAsignada(wsSiscoopFoliosTarjetas.getAsignada());
                wsSiscoopFoliosTarjetas.setBloqueada(wsSiscoopFoliosTarjetas.getBloqueada());
                wsSiscoopFoliosTarjetas.setWsSiscoopFoliosTarjetasPK(foliosPK1);
            }
        } catch (Exception e) {
            System.out.println("Error en buscaTarjetaTDD de WsSiscoopFoliosTarjetas: " + e.getMessage());
            return wsSiscoopFoliosTarjetas;
        }
        System.out.println("Se encontro el foilio tdd:"+wsSiscoopFoliosTarjetas.getIdtarjeta());
        return wsSiscoopFoliosTarjetas;
    }

    public BalanceQueryResponseDto saldoTDD(WsSiscoopFoliosTarjetasPK1 foliosPK) {
        BalanceQueryResponseDto response = new BalanceQueryResponseDto();
        EntityManager em=AbstractFacade.conexion();
        WsSiscoopFoliosTarjetas1 tarjeta = em.find(WsSiscoopFoliosTarjetas1.class, foliosPK);
        try {
            System.out.println("Estatus de la tarjeta de debito:" + tarjeta.getActiva());
            if (tarjeta.getActiva()) {                
                /*
                response.setAvailableAmount(200000);                     
                response.setCode(1);
                response.setDescription("activa");
                */  
              response = conexionSiscoop().getSiscoop().getBalanceQuery(tarjeta.getIdtarjeta());

            } else {
                response.setDescription("La tarjeta esta inactiva: " + tarjeta.getIdtarjeta());
            }
        } catch (Exception e) {
            System.out.println("Error al buscar Saldo tdd:" + e.getMessage());
            response.setDescription("Tarjeta Inactiva");
            em.close();
            return response;
        }
        em.close();
        return response;
    }

    public boolean retiroTDD(WsSiscoopFoliosTarjetas1 tarjeta, Double monto) {
        LoadBalanceResponse.Return loadBalanceResponse = new LoadBalanceResponse.Return();
        DoWithdrawalAccountResponse.Return doWithdrawalAccountResponse = new DoWithdrawalAccountResponse.Return();
        System.out.println("WS...Restiro");
        boolean retiro = false;
        try {
            if (tarjeta.getActiva()) {
                /*doWithdrawalAccountResponse.setBalance(200);
                doWithdrawalAccountResponse.setCode(1);
                */
                doWithdrawalAccountResponse = conexionSiscoop().getSiscoop().doWithdrawalAccount(tarjeta.getIdtarjeta(), monto);
                if (doWithdrawalAccountResponse.getCode() == 0) {
                    // 0 = Existe error
                    //retiro = false;
                    retiro=false;
                } else {
                    retiro = true;
                }
            }
        } catch (Exception e) {
            retiro =  errorRetiroDespositoSYC(loadBalanceResponse, e);
        }
        return retiro;
    }

    // REALIZA EL DEPOSITO DE LA TARJETA TDD
    public boolean depositoTDD(WsSiscoopFoliosTarjetas1 tarjeta, Double monto) {
        LoadBalanceResponse.Return loadBalanceResponse = new LoadBalanceResponse.Return();
        boolean deposito = false;
        if (tarjeta.getActiva()) {
            try {                
                loadBalanceResponse = conexionSiscoop().getSiscoop().loadBalance(tarjeta.getIdtarjeta(), monto);
                if (loadBalanceResponse.getCode() == 0) {
                    deposito = false;
                } else {
                    deposito = true;
                }
            } catch (Exception e) {
                deposito = errorRetiroDespositoSYC(loadBalanceResponse, e);
            }
        }
        return deposito;
    }

    public Siscoop_TDD conexionSiscoop() {
//        EntityManagerFactory emf = AbstractFacade.conexion();
        EntityManager em = AbstractFacade.conexion();//      EntityManager em = emf.createEntityManager();
        Siscoop_TDD conexionWSTDD = null;
        UtilidadesGenerales util = new UtilidadesGenerales();
        try {
            //Tabla para obtener usuario y contraseña
            Tablas crendenciales = util.busquedaTabla(em, "bankingly_banca_movil", "wsdl_credenciales");
            Tablas parametros = util.busquedaTabla(em, "bankingly_banca_movil", "wsdl_parametros");
            if (parametros != null) {
                System.out.println("Conectando ws ALestra....");
                //1.-Usuario,2.-contraseña,3.-host,4.-puerto,5.-wsdl
                conexionWSTDD = new consumo_tdd.Siscoop_TDD(crendenciales.getDato1(), crendenciales.getDato2(), parametros.getDato1(), parametros.getDato3(), parametros.getDato2());
                //conexionWSTDD = new (parametros.getDato1(), parametros.getDato2());

            }
        } catch (Exception e) {
            System.out.println("No existen parametros para conexion:" + e.getMessage());
        }
        return conexionWSTDD;
    }

    public void actualizarSaldoTDD(SaldoTddPK saldoTddPK, Double saldo, EntityManager em) {

        try {
            if (saldo > 0) {
                long time = System.currentTimeMillis();
                Timestamp timestamp = new Timestamp(time);
                //DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                //System.out.println("yyyy/MM/dd HH:mm:ss-> " + dtf.format(LocalDateTime.now()));
                em.getTransaction().begin();
                Query q = em.createNativeQuery("UPDATE saldo_tdd "
                        + " SET "
                        + " saldo = ?,"
                        + " fecha = ?"
                        //+ "fecha = ?, "
                        + " WHERE idorigenp= ?  AND idproducto = ? and idauxiliar = ?");
                q.setParameter(1, saldo);
                q.setParameter(2, timestamp);
                q.setParameter(3, saldoTddPK.getIdorigenp());
                q.setParameter(4, saldoTddPK.getIdproducto());
                q.setParameter(5, saldoTddPK.getIdauxiliar());
                int ac = q.executeUpdate();

                /*int queryUpdateSaldo = em.createNativeQuery("UPDATE saldo_tdd SET saldo=" + saldo + "WHERE "
                        + " idorigenp=" + saldoTddPK.getIdorigenp()
                        + " AND idproducto=" + saldoTddPK.getIdproducto()
                        + " AND idauxiliar=" + saldoTddPK.getIdauxiliar()).executeUpdate();
                 */
                em.getTransaction().commit();
            }

            /*em.getTransaction().begin();
            
              if(queryUpdateSaldo>0){
                return true;
            }*/
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al actualizar saldo de la TDD:" + e.getMessage());
        }
    }

    // ERROR AL CONSULTAR SYC TIEMPO AGOTADO
    public boolean errorRetiroDespositoSYC(LoadBalanceResponse.Return loadBalanceResponse, Exception e) {
        System.out.println("Error al consultar SYC, tiempo agotado. " + e.getMessage());
        loadBalanceResponse.setDescription("Connect timed out");
        return false;
    }

}
