/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.dao;

import com.fenorest.rest.EnviarSMS.PreparaSMS;
import com.fenoreste.rest.Util.UtilidadesGenerales;
import com.fenoreste.rest.DTO.OgsDTO;
import com.fenoreste.rest.DTO.OpaDTO;
import com.fenoreste.rest.Request.RequestDataOrdenPagoDTO;
import com.fenoreste.rest.ResponseDTO.BackendOperationResultDTO;
import com.fenoreste.rest.ResponseDTO.ResponseSPEIDTO;
import com.fenoreste.rest.ResponseDTO.TransactionToOwnAccountsDTO;
import com.fenoreste.rest.ResponseDTO.VaucherDTO;
import com.fenoreste.rest.Util.AbstractFacade;
import com.fenoreste.rest.Util.Utilidades;
import com.fenoreste.rest.WsTDD.TarjetaDeDebito;
import com.fenoreste.rest.entidades.Auxiliares;
import com.fenoreste.rest.entidades.AuxiliaresD;
import com.fenoreste.rest.entidades.AuxiliaresPK;
import com.fenoreste.rest.entidades.Origenes;
import com.fenoreste.rest.entidades.Productos_bankingly;
import com.fenoreste.rest.entidades.Procesa_pago_movimientos;
import com.fenoreste.rest.entidades.Productos;
import com.fenoreste.rest.entidades.Tablas;
import com.fenoreste.rest.entidades.TablasPK;
import com.fenoreste.rest.entidades.Transferencias;
import com.fenoreste.rest.entidades.WsSiscoopFoliosTarjetas1;
import com.fenoreste.rest.entidades.WsSiscoopFoliosTarjetasPK1;
import com.fenoreste.rest.service.IFuncionesService;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.syc.ws.endpoint.siscoop.BalanceQueryResponseDto;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.json.JSONObject;

import java.util.Base64;

/**
 *
 * @author Elliot
 */
public abstract class FacadeTransaction<T> {

    Utilidades util = new Utilidades();
    UtilidadesGenerales util2 = new UtilidadesGenerales();
    IFuncionesService funcionesService;

    public FacadeTransaction(Class<T> entityClass) {
    }

    //El identificador de trasnferencia me dice 1=entre mis cuentas 2=a terceros 3=Pago a prestamos
    public BackendOperationResultDTO transferencias(TransactionToOwnAccountsDTO transactionOWN, int identificadorTransferencia, RequestDataOrdenPagoDTO SPEIOrden) {
        EntityManager em = AbstractFacade.conexion();
        Date hoy = new Date();
        BackendOperationResultDTO backendResponse = new BackendOperationResultDTO();
        backendResponse.setBackendCode("2");
        backendResponse.setBackendMessage("Incorrecto");
        backendResponse.setBackendReference(null);
        backendResponse.setIsError(true);
        backendResponse.setTransactionIdenty("0");
        OpaDTO opaOrigen = util.opa(transactionOWN.getDebitProductBankIdentifier());
        System.out.println(opaOrigen.getIdorigenp() + "-" + opaOrigen.getIdproducto() + "-" + opaOrigen.getIdauxiliar());

        boolean banderaCSN = false;
        ResponseSPEIDTO response = null;
        String messageBackend = "";
        String mensajeBackendResult = "";

        banderaCSN = false;
        boolean banderaTDD = false;

        Tablas tb_spei_cuenta = null;
        Tablas tb_spei_cuenta_comisiones = null;
        Double comisiones = 0.0;
        Double total_a_enviar = 0.0;
        //Si no es TDD pasa directo hasta aca
        //Si es una transferencia entre mis cuentas
        //if (identificadorTransferencia == 1 && retiro == false && banderaCSN == false) {
        if (identificadorTransferencia == 1 && banderaCSN == false) {
            //Valido la transferencia y devuelvo el mensaje que se produce
            //Valido el origen si es CSN 
            if (util2.obtenerOrigen(em) == 30200) {
                //Valido el producto para retiro
                //Busco el producto configurado para retiros
                messageBackend = validarTransferenciaCSN(transactionOWN, identificadorTransferencia, null);
                backendResponse.setBackendMessage(messageBackend);
                banderaCSN = true;
            } else {
                messageBackend = validarTransferenciaEntreMisCuentas(transactionOWN.getDebitProductBankIdentifier(), transactionOWN.getAmount(), transactionOWN.getCreditProductBankIdentifier(), transactionOWN.getClientBankIdentifier());
                backendResponse.setBackendMessage(messageBackend);
            }
        }
        //Si es una transferencia terceros dentro de la entidad
        if (identificadorTransferencia == 2) {
            //Valido la transferencia y devuelvo el mensaje que se produce
            //Valido el origen si es CSN 
            if (util2.obtenerOrigen(em) == 30200) {
                //Valido el producto para retiro
                //Busco el producto configurado para retiros
                messageBackend = validarTransferenciaCSN(transactionOWN, identificadorTransferencia, null);
                backendResponse.setBackendMessage(messageBackend);
                banderaCSN = true;
                if (messageBackend.contains("TDD")) {
                    banderaTDD = true;
                }
            } else {
                messageBackend = validarTransferenciaATerceros(transactionOWN.getDebitProductBankIdentifier(), transactionOWN.getAmount(), transactionOWN.getCreditProductBankIdentifier(), transactionOWN.getClientBankIdentifier());
                backendResponse.setBackendMessage(messageBackend);
            }
        }
        //Si es pago a un prestamo
        if (identificadorTransferencia == 3 || identificadorTransferencia == 4) {
            //Valido la transferencia y devuelvo el mensaje que se produce
            //Valido el origen si es CSN 
            if (util2.obtenerOrigen(em) == 30200) {
                //Valido el producto para retiro
                //Busco el producto configurado para retiros
                messageBackend = validarTransferenciaCSN(transactionOWN, identificadorTransferencia, null);
                System.out.println("");
                backendResponse.setBackendMessage(messageBackend);
                banderaCSN = true;
                System.out.println("mensageBakc:" + messageBackend);
                if (messageBackend.contains("TDD")) {
                    banderaTDD = true;
                }
            } else {
                messageBackend = validarPagoAPrestamos(identificadorTransferencia, transactionOWN.getDebitProductBankIdentifier(), transactionOWN.getAmount(), transactionOWN.getCreditProductBankIdentifier(), transactionOWN.getClientBankIdentifier());
            }
        } else if (identificadorTransferencia == 5) {
            //Valido la transferencia y devuelvo el mensaje que se produce
            //Valido el origen si es CSN 
            if (util2.obtenerOrigen(em) == 30200) {
                //Busco la cuenta spei en tablas solo capital
                tb_spei_cuenta = util2.busquedaTabla(em, "bankingly_banca_movil", "cuenta_spei");
                //Busco la cuenta spei en tablas solo para comisiones                    
                tb_spei_cuenta_comisiones = util2.busquedaTabla(em, "bankingly_banca_movil", "cuenta_spei_comisiones");
                comisiones = Double.parseDouble(tb_spei_cuenta_comisiones.getDato2());
                total_a_enviar = transactionOWN.getAmount() - (comisiones + (comisiones * 0.16));
                //Valido el producto para retiro
                //Busco el producto configurado para retiros
                messageBackend = validarTransferenciaCSN(transactionOWN, identificadorTransferencia, SPEIOrden);

                /*banderaCSN = true;
                if (messageBackend.toUpperCase().contains("TDD")) {
                    banderaTDD = true;
                }*/
                if (messageBackend.toUpperCase().contains("EXITO")) {
                    SPEIOrden.setMonto(total_a_enviar);
                    response = metodoEnviarSPEI(SPEIOrden);
                    if (response.getId() > 3) {
                        backendResponse.setBackendMessage("ORDEN ENVIADA CON EXITO");
                        banderaCSN = true;
                    } else {
                        backendResponse.setBackendMessage(response.getError());
                    }
                } else {
                    backendResponse.setBackendMessage(messageBackend);
                }
            }
        }
        try {
            if (backendResponse.getBackendMessage().toUpperCase().contains("EXITO")) {

                Transferencias transaction = new Transferencias();
                //Si la valicadion se realizo de manera corracta preparo una tabla ttabla historial

                transaction.setTransactionid(new BigDecimal(transactionOWN.getTransactionId()));
                transaction.setSubtransactiontypeid(transactionOWN.getSubTransactionTypeId());
                transaction.setCurrencyid(transactionOWN.getCurrencyId());
                transaction.setValuedate(transactionOWN.getValueDate());
                transaction.setTransactiontypeid(transactionOWN.getTransactionTypeId());
                transaction.setTransactionstatusid(transactionOWN.getTransactionStatusId());
                transaction.setClientbankidentifier(transactionOWN.getClientBankIdentifier());
                transaction.setDebitproductbankidentifier(transactionOWN.getDebitProductBankIdentifier());
                transaction.setDebitproducttypeid(transactionOWN.getDebitProductTypeId());
                transaction.setDebitcurrencyid(transactionOWN.getDebitCurrencyId());
                transaction.setCreditproductbankidentifier(transactionOWN.getCreditProductBankIdentifier());
                transaction.setCreditproducttypeid(transactionOWN.getCreditProductTypeId());
                transaction.setCreditcurrencyid(transactionOWN.getCreditCurrencyId());
                transaction.setAmount(transactionOWN.getAmount());
                transaction.setNotifyto(transactionOWN.getNotifyTo());
                transaction.setNotificationchannelid(transactionOWN.getNotificationChannelId());
                transaction.setDestinationname(transactionOWN.getDestinationName());
                transaction.setDestinationbank(transactionOWN.getDestinationBank());
                transaction.setDescription(transactionOWN.getDescription());
                transaction.setBankroutingnumber(transactionOWN.getBankRoutingNumber());
                transaction.setSourcename(transactionOWN.getSourceName());
                transaction.setSourcebank(transactionOWN.getSourceBank());
                transaction.setRegulationamountexceeded(transactionOWN.isRegulationAmountExceeded());
                transaction.setSourcefunds(transactionOWN.getSourceFunds());
                transaction.setDestinationfunds(transactionOWN.getDestinationFunds());
                transaction.setTransactioncost(transactionOWN.getTransactionCost());
                transaction.setTransactioncostcurrencyid(transactionOWN.getTransactionCostCurrencyId());
                transaction.setExchangerate(transactionOWN.getExchangeRate());
                transaction.setDestinationdocumentid_documentnumber(transactionOWN.getDestinationDocumentId().getDocumentNumber());
                transaction.setDestinationdocumentid_documenttype(transactionOWN.getDestinationDocumentId().getDocumentType());
                transaction.setSourcedocumentid_documentnumber(transactionOWN.getSourceDocumentId().getDocumentNumber());
                transaction.setSourcedocumentid_documenttype(transactionOWN.getSourceDocumentId().getDocumentType());
                transaction.setUserdocumentid_documentnumber(transactionOWN.getUserDocumentId().getDocumentNumber());
                transaction.setUserdocumentid_documenttype(transactionOWN.getUserDocumentId().getDocumentType());
                transaction.setFechaejecucion(hoy);

                OpaDTO opa = util.opa(transaction.getDebitproductbankidentifier());

                //Obtengo los productos origen y destino
                //Origen
                String origenP = "SELECT * FROM auxiliares WHERE idorigenp=" + opa.getIdorigenp() + " AND idproducto=" + opa.getIdproducto() + " AND idauxiliar=" + opa.getIdauxiliar();
                Query queryOrigen = em.createNativeQuery(origenP, Auxiliares.class);
                Auxiliares aOrigen = (Auxiliares) queryOrigen.getSingleResult();

                //Lo utlizon para los datos a procesar 
                long time = System.currentTimeMillis();
                Timestamp timestamp = new Timestamp(time);

                //Obtengo la sesion para los datos a procesar
                Query sesion = em.createNativeQuery("select text(pg_backend_pid())||'-'||trim(to_char(now(),'ddmmyy'))");
                String sesionc = String.valueOf(sesion.getSingleResult());

                //Obtengo un random que uso en conplemento con referencia
                int rn = (int) (Math.random() * 999999 + 1);

                //Obtener HH:mm:ss.microsegundos
                String fechaArray[] = timestamp.toString().substring(0, 10).split("-");
                String fReal = fechaArray[2] + "/" + fechaArray[1] + "/" + fechaArray[0];
                String referencia = String.valueOf(rn) + "" + String.valueOf(transaction.getSubtransactiontypeid()) + "" + String.valueOf(transaction.getTransactiontypeid() + "" + fReal.replace("/", ""));

                //Leemos fechatrabajo e idusuario
                String fechaTrabajo = "SELECT to_char(fechatrabajo,'yyyy-MM-dd HH:mm:ss') FROM ORIGENES LIMIT 1";
                Query fechaTrabajo_ = em.createNativeQuery(fechaTrabajo);
                String fechaTr_ = String.valueOf(fechaTrabajo_.getSingleResult());

                //Buscamos el usuario para la banca movil para la tabla de datos a procesar y para las polizas
                TablasPK idusuarioPK = new TablasPK("bankingly_banca_movil", "usuario_banca_movil");
                Tablas tbUsuario_ = em.find(Tablas.class, idusuarioPK);

                //Conviento a DateTime la fecha de trabajo
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime localDate = LocalDateTime.parse(fechaTr_, dtf);

                Timestamp fecha_transferencia = Timestamp.valueOf(localDate);
                Productos prDestino = null;

                //Leo la tabla donde se almacenan datos temporales de los datos a procesar
                Procesa_pago_movimientos procesaOrigen = new Procesa_pago_movimientos();

                //Preoaro el registro del abono
                Procesa_pago_movimientos procesaDestino = new Procesa_pago_movimientos();

                OpaDTO opaD = null;
                Auxiliares aDestino = null;
                //Si es un spei lo identifico porque aqui va a una cuenta contable

                if (identificadorTransferencia == 5) {//tipo de orden SPEI

                    Tablas tb_usuario_spei = util2.busquedaTabla(em, "bankingly_banca_movil", "usuario_spei");
                    System.out.println("Tb_usuario_spei:" + tb_usuario_spei);

                    //Tablas pra cuenta del iva de comisiones spei
                    //Busco la cuenta spei en tablas solo para comisiones                    
                    Tablas tb_spei_cuenta_comisiones_iva = util2.busquedaTabla(em, "bankingly_banca_movil", "cuenta_spei_comisiones_iva");

                    Double iva_comisiones = Double.parseDouble(tb_spei_cuenta_comisiones.getDato2()) * 0.16;

                    //Double total_a_enviar = transaction.getAmount() - (Double.parseDouble(tb_spei_cuenta_comisiones.getDato2()) - ( Double.parseDouble(tb_spei_cuenta_comisiones.getDato2()))* 0.16);
                    System.out.println("Total a enviar:" + total_a_enviar);
                    //Preparo en temporales los datos para el cargo
                    procesaOrigen.setAuxiliaresPK(aOrigen.getAuxiliaresPK());
                    procesaOrigen.setFecha(fecha_transferencia);
                    procesaOrigen.setIdusuario(Integer.parseInt(tb_usuario_spei.getDato1()));
                    procesaOrigen.setSesion(sesionc);
                    procesaOrigen.setReferencia(referencia);
                    procesaOrigen.setIdorigen(aOrigen.getIdorigen());
                    procesaOrigen.setIdgrupo(aOrigen.getIdgrupo());
                    procesaOrigen.setIdsocio(aOrigen.getIdsocio());
                    procesaOrigen.setCargoabono(0);

                    procesaOrigen.setIdorden(response.getId());

                    procesaOrigen.setMonto(transaction.getAmount());

                    procesaOrigen.setIva(Double.parseDouble(aOrigen.getIva().toString()));
                    procesaOrigen.setTipo_amort(Integer.parseInt(String.valueOf(aOrigen.getTipoamortizacion())));
                    procesaOrigen.setSai_aux("");
                    procesaOrigen.setIdorden(response.getId());

                    em.getTransaction().begin();
                    em.persist(procesaOrigen);
                    em.getTransaction().commit();

                    em.clear();

                    //El abono a cuenta cntable que recibe solo capital de la transferencia
                    AuxiliaresPK aPKSPEI = null;
                    aPKSPEI = new AuxiliaresPK(0, 1, 0);

                    procesaDestino.setAuxiliaresPK(aPKSPEI);
                    procesaDestino.setFecha(fecha_transferencia);
                    procesaDestino.setIdusuario(Integer.parseInt(tb_usuario_spei.getDato1()));
                    procesaDestino.setSesion(sesionc);
                    procesaDestino.setReferencia(referencia);
                    procesaDestino.setIdorigen(aOrigen.getIdorigen());
                    procesaDestino.setIdgrupo(aOrigen.getIdgrupo());
                    procesaDestino.setIdsocio(aOrigen.getIdsocio());
                    procesaDestino.setCargoabono(1);

                    procesaDestino.setIdcuenta(tb_spei_cuenta.getDato1());//el idcuenta para capital
                    procesaDestino.setMonto(total_a_enviar);//transaction.getAmount() - Double.parseDouble(tb_spei_cuenta_comisiones.getDato2()) - (Double.parseDouble(tb_spei_cuenta_comisiones.getDato2()) * 0.16));//El total de la transferencia
                    procesaDestino.setIva(0.0);
                    procesaDestino.setTipo_amort(0);
                    procesaDestino.setSai_aux("");
                    procesaDestino.setIdorden(response.getId());

                    em.getTransaction().begin();
                    em.persist(procesaDestino);
                    em.getTransaction().commit();

                    em.clear();

                    //guardo el abono a la cuenta contable comisiones
                    aPKSPEI = new AuxiliaresPK(1, 1, 1);
                    procesaDestino.setAuxiliaresPK(aPKSPEI);
                    procesaDestino.setFecha(fecha_transferencia);
                    procesaDestino.setIdusuario(Integer.parseInt(tb_usuario_spei.getDato1()));
                    procesaDestino.setSesion(sesionc);
                    procesaDestino.setReferencia(referencia);
                    procesaDestino.setIdorigen(aOrigen.getIdorigen());
                    procesaDestino.setIdgrupo(aOrigen.getIdgrupo());
                    procesaDestino.setIdsocio(aOrigen.getIdsocio());
                    procesaDestino.setCargoabono(1);
                    procesaDestino.setIdcuenta(tb_spei_cuenta_comisiones.getDato1());
                    procesaDestino.setMonto(comisiones);
                    procesaDestino.setIva(0.0);
                    procesaDestino.setTipo_amort(0);
                    procesaDestino.setSai_aux("");
                    procesaDestino.setIdorden(response.getId());

                    em.getTransaction().begin();
                    em.persist(procesaDestino);
                    em.getTransaction().commit();

                    em.clear();
                    //Guardo el abono a la cuenta contable para comisiones de SPi
                    aPKSPEI = new AuxiliaresPK(2, 1, 2);
                    procesaDestino.setAuxiliaresPK(aPKSPEI);
                    procesaDestino.setFecha(fecha_transferencia);
                    procesaDestino.setIdusuario(Integer.parseInt(tb_usuario_spei.getDato1()));
                    procesaDestino.setSesion(sesionc);
                    procesaDestino.setReferencia(referencia);
                    procesaDestino.setIdorigen(aOrigen.getIdorigen());
                    procesaDestino.setIdgrupo(aOrigen.getIdgrupo());
                    procesaDestino.setIdsocio(aOrigen.getIdsocio());
                    procesaDestino.setCargoabono(1);
                    procesaDestino.setIdcuenta(tb_spei_cuenta_comisiones_iva.getDato1());
                    procesaDestino.setMonto(iva_comisiones);
                    procesaDestino.setIva(0.0);
                    procesaDestino.setTipo_amort(0);
                    procesaDestino.setSai_aux("");
                    procesaDestino.setIdorden(response.getId());

                    em.getTransaction().begin();
                    em.persist(procesaDestino);
                    em.getTransaction().commit();

                } else {
                    //Obtengo el opa destino
                    opaD = util.opa(transaction.getCreditproductbankidentifier());
                    //Destino
                    String destinoP = "SELECT * FROM auxiliares WHERE idorigenp=" + opaD.getIdorigenp() + " AND idproducto=" + opaD.getIdproducto() + " AND idauxiliar=" + opaD.getIdauxiliar();
                    Query queryDestino = em.createNativeQuery(destinoP, Auxiliares.class);
                    aDestino = (Auxiliares) queryDestino.getSingleResult();

                    //Obtengo el producto Destino
                    prDestino = em.find(Productos.class, aDestino.getAuxiliaresPK().getIdproducto());

                    procesaOrigen.setAuxiliaresPK(aOrigen.getAuxiliaresPK());
                    procesaOrigen.setFecha(fecha_transferencia);
                    procesaOrigen.setIdusuario(Integer.parseInt(tbUsuario_.getDato1()));
                    procesaOrigen.setSesion(sesionc);
                    procesaOrigen.setReferencia(referencia);
                    procesaOrigen.setIdorigen(aOrigen.getIdorigen());
                    procesaOrigen.setIdgrupo(aOrigen.getIdgrupo());
                    procesaOrigen.setIdsocio(aOrigen.getIdsocio());
                    procesaOrigen.setCargoabono(0);
                    procesaOrigen.setMonto(transaction.getAmount());
                    procesaOrigen.setIva(Double.parseDouble(aOrigen.getIva().toString()));
                    procesaOrigen.setTipo_amort(Integer.parseInt(String.valueOf(aOrigen.getTipoamortizacion())));

                    procesaOrigen.setSai_aux("");

                    em.getTransaction().begin();
                    em.persist(procesaOrigen);
                    em.getTransaction().commit();

                    em.clear();

                    procesaDestino.setAuxiliaresPK(aDestino.getAuxiliaresPK());
                    procesaDestino.setFecha(fecha_transferencia);
                    procesaDestino.setIdusuario(Integer.parseInt(tbUsuario_.getDato1()));
                    procesaDestino.setSesion(sesionc);
                    procesaDestino.setReferencia(referencia);
                    procesaDestino.setIdorigen(aDestino.getIdorigen());
                    procesaDestino.setIdgrupo(aDestino.getIdgrupo());
                    procesaDestino.setIdsocio(aDestino.getIdsocio());
                    procesaDestino.setCargoabono(1);
                    procesaDestino.setMonto(transaction.getAmount());
                    procesaDestino.setIva(Double.parseDouble(aDestino.getIva().toString()));
                    procesaDestino.setTipo_amort(Integer.parseInt(String.valueOf(aDestino.getTipoamortizacion())));

                    procesaDestino.setSai_aux("");

                    //Guardo registros para abono
                    em.getTransaction().begin();
                    em.persist(procesaDestino);
                    em.getTransaction().commit();

                }

                String consulta_datos_procesar = "";
                int total_procesados = 0;
                boolean finish = false;
                boolean clean = false;
                Query procesa_movimiento = null;
                //Si los datos en la tabla temporal el cargo y abono se guardaron correctamente
                //Ejecutamos la funcion para distribuir el capital
                //Solo para CSN
                if (banderaCSN) {
                    //DoWithdrawalAccountResponse.Return doWithdrawalAccountResponse = new DoWithdrawalAccountResponse.Return();
                    boolean bandera_retiro = false;
                    boolean bandera_deposito_origen = false;
                    boolean bandera_deposito_tercero = false;
                    WsSiscoopFoliosTarjetas1 tarjeta_origen = null;
                    WsSiscoopFoliosTarjetas1 tarjeta_destino = null;
                    if (identificadorTransferencia != 5) {//Entran todos los tipos de transferencia excepto SPEI
                        //Si es desde la TDD(Aqui ya se leyo el ws de Alestra y esta levantado por eso trae la etiqueta de TDD)
                        if (backendResponse.getBackendMessage().contains("TDD")) {
                            tarjeta_origen = new TarjetaDeDebito().buscaTarjetaTDD(opaOrigen.getIdorigenp(), opaOrigen.getIdproducto(), opaOrigen.getIdauxiliar(), em);
                            //Realizo un retiro de la TDD
                            bandera_retiro = new TarjetaDeDebito().retiroTDD(tarjeta_origen, procesaOrigen.getMonto());
                            System.out.println("El retiro de la tdd fue:" + bandera_retiro);
                            //bandera_retiro = true;
                            if (bandera_retiro) {//si se retiro de la tdd origen                             
                                if (identificadorTransferencia == 2) {
                                    //Si el destino tambien tdd
                                    if (backendResponse.getBackendMessage().contains("TERCERO")) {
                                        //Busco el folio Destino                                       
                                        tarjeta_destino = new TarjetaDeDebito().buscaTarjetaTDD(opaD.getIdorigenp(), opaD.getIdproducto(), opaD.getIdauxiliar(), em);

                                        if (tarjeta_destino.getActiva()) {
                                            //Proceso un deposito al tercero TDD
                                            bandera_deposito_tercero = new TarjetaDeDebito().depositoTDD(tarjeta_destino, procesaOrigen.getMonto());

                                            if (bandera_deposito_tercero) {
                                                //Datos a procesar
                                                try {
                                                    consulta_datos_procesar = "SELECT sai_bankingly_aplica_transaccion('" + fechaTr_.substring(0, 10) + "'," + procesaOrigen.getIdusuario() + ",'" + procesaOrigen.getSesion() + "','" + procesaOrigen.getReferencia() + "')";
                                                    procesa_movimiento = em.createNativeQuery(consulta_datos_procesar);
                                                    total_procesados = Integer.parseInt(String.valueOf(procesa_movimiento.getSingleResult()));
                                                } catch (Exception e) {
                                                    total_procesados = 0;
                                                    System.out.println("La funcion saicoop no pudo distribuir el capital");
                                                }

                                                if (total_procesados > 0) {
                                                    finish = true;
                                                } else {
                                                    //Si no se aplico el movimiento pero como ya se habia retirado de la TDD oirgen entonce se lo devolvemos
                                                    //Tambien ya se deposito a TDD destino tercero entonces se le retira
                                                    bandera_deposito_origen = new TarjetaDeDebito().depositoTDD(tarjeta_origen, procesaOrigen.getMonto());
                                                    bandera_retiro = new TarjetaDeDebito().retiroTDD(tarjeta_destino, procesaOrigen.getMonto());
                                                    backendResponse.setBackendMessage("NO SE PUDIERON PROCESAR LOS MOVIMIENTOS EN SAICOOP");
                                                }
                                            } else {
                                                bandera_deposito_origen = new TarjetaDeDebito().depositoTDD(tarjeta_origen, procesaOrigen.getMonto());
                                                backendResponse.setBackendMessage("NO SE PUDO DEPOSITAR A LA TARJETA DE DEBITO DESTINO");
                                            }
                                        } else {
                                            //No se pudo depositar a Tercero
                                            backendResponse.setBackendMessage("VERIFIQUE EL ESTATUS DE TARJETA DESTINO");
                                            //Se le regresal el saldo a la tdd origen
                                            bandera_deposito_origen = new TarjetaDeDebito().depositoTDD(tarjeta_origen, procesaOrigen.getMonto());

                                        }
                                    } else {//Si el tercero no es TDD Terceros Tipo 
                                        //Modificado el 27/01/2022
                                        //Datos a procesar
                                        consulta_datos_procesar = "SELECT sai_bankingly_aplica_transaccion('" + fechaTr_.substring(0, 10) + "'," + procesaOrigen.getIdusuario() + ",'" + procesaOrigen.getSesion() + "','" + procesaOrigen.getReferencia() + "')";
                                        //Proceso
                                        procesa_movimiento = em.createNativeQuery(consulta_datos_procesar);
                                        //Obtengo el total de procesados
                                        total_procesados = Integer.parseInt(String.valueOf(procesa_movimiento.getSingleResult()));

                                        //Si la operacion se aplico de manera correcta lo dejamos asi es decir total de procesados mayor a 0
                                        if (total_procesados > 0) {
                                            finish = true;
                                        } else {
                                            //Si no se aplico el movimiento pero como ya se habia retirado de la TDD entonce se lo devolvemos
                                            bandera_deposito_origen = new TarjetaDeDebito().depositoTDD(tarjeta_origen, procesaOrigen.getMonto());
                                            backendResponse.setBackendMessage("NO SE PUDO PROCESAR FUNCION EN SAICOOP");
                                        }
                                    }

                                } else {//Transferencia a ahorros propios,Pagos propios y pagos Terceros

                                    //Verifico que el producto destino sea un prestamo 
                                    if (prDestino.getTipoproducto() == 2) {
                                        //Busco el tipo el tipo de amortizacion

                                        if (aDestino.getTipoamortizacion() == 5) {
                                            //Entra si es hipotecario
                                            String si_se_puede_aplicar = "select sai_bankingly_limite_adelanto (" + aDestino.getAuxiliaresPK().getIdorigenp() + ","
                                                    + "" + aDestino.getAuxiliaresPK().getIdproducto() + ","
                                                    + "" + aDestino.getAuxiliaresPK().getIdauxiliar() + ","
                                                    + "(SELECT date(fechatrabajo) FROM origenes limit 1),"
                                                    + transaction.getAmount() + ",NULL)";
                                            Query query_se_puede_aplicar = em.createNativeQuery(si_se_puede_aplicar);
                                            Double se_puede_pagar = Double.parseDouble(String.valueOf(query_se_puede_aplicar.getSingleResult()));

                                            if (se_puede_pagar > 0) {
                                                //Datos a procesar
                                                try {
                                                    consulta_datos_procesar = "SELECT sai_bankingly_aplica_transaccion('" + fechaTr_.substring(0, 10) + "'," + procesaOrigen.getIdusuario() + ",'" + procesaOrigen.getSesion() + "','" + procesaOrigen.getReferencia() + "')";
                                                    procesa_movimiento = em.createNativeQuery(consulta_datos_procesar);
                                                    total_procesados = Integer.parseInt(String.valueOf(procesa_movimiento.getSingleResult()));
                                                } catch (Exception e) {
                                                    total_procesados = 0;
                                                    System.out.println("La funcion saicoop no pudo distribuir el capital:" + e.getMessage());
                                                }

                                                if (total_procesados > 0) {
                                                    if (procesaOrigen.getMonto() > se_puede_pagar) {
                                                        double totalDevolver = procesaOrigen.getMonto() - se_puede_pagar;
                                                        System.out.println("El total que se cubrio fue de:" + se_puede_pagar + " y se devolvio al producto un total de: " + totalDevolver);
                                                        bandera_deposito_origen = new TarjetaDeDebito().depositoTDD(tarjeta_origen, totalDevolver);
                                                    }
                                                    finish = true;
                                                } else {
                                                    //Si no se aplico el movimiento pero como ya se habia retirado de la TDD entonce se lo devolvemos
                                                    bandera_deposito_origen = new TarjetaDeDebito().depositoTDD(tarjeta_origen, procesaOrigen.getMonto());
                                                    backendResponse.setBackendMessage("NO SE PUDO PROCESAR FUNCION EN SAICOOP");
                                                }

                                            } else {
                                                //Si no se aplico el movimiento pero como ya se habia retirado de la TDD entonce se lo devolvemos                                                
                                                bandera_deposito_origen = new TarjetaDeDebito().depositoTDD(tarjeta_origen, procesaOrigen.getMonto());
                                                backendResponse.setBackendMessage("MONTO ES MENOR A LO QUE SE PERMITE ADELANTAR \n"
                                                        + "ADELANTO PERMITIDO:" + se_puede_pagar + "\n"
                                                        + "PRDUCTO ESQUEMA HIPOTECARIO");
                                            }
                                        } else {//Si es prestamo creciente
                                            //Datos a procesar
                                            consulta_datos_procesar = "SELECT sai_bankingly_aplica_transaccion('" + fechaTr_.substring(0, 10) + "'," + procesaOrigen.getIdusuario() + ",'" + procesaOrigen.getSesion() + "','" + procesaOrigen.getReferencia() + "')";
                                            //Proceso
                                            procesa_movimiento = em.createNativeQuery(consulta_datos_procesar);
                                            //Obtengo el total de procesados
                                            total_procesados = Integer.parseInt(String.valueOf(procesa_movimiento.getSingleResult()));
                                            //Si la operacion se aplico de manera correcta lo dejamos asi es decir total de procesados mayor a 0
                                            if (total_procesados > 0) {
                                                finish = true;
                                            } else {
                                                //Si no se aplico el movimiento pero como ya se habia retirado de la TDD entonce se lo devolvemos
                                                bandera_deposito_origen = new TarjetaDeDebito().depositoTDD(tarjeta_origen, procesaOrigen.getMonto());
                                                backendResponse.setBackendMessage("NO SE PUDO PROCESAR FUNCION EN SAICOOP");
                                            }
                                        }

                                    } else {
                                        //Datos a procesar
                                        consulta_datos_procesar = "SELECT sai_bankingly_aplica_transaccion('" + fechaTr_.substring(0, 10) + "'," + procesaOrigen.getIdusuario() + ",'" + procesaOrigen.getSesion() + "','" + procesaOrigen.getReferencia() + "')";
                                        //Proceso
                                        procesa_movimiento = em.createNativeQuery(consulta_datos_procesar);
                                        //Obtengo el total de procesados
                                        total_procesados = Integer.parseInt(String.valueOf(procesa_movimiento.getSingleResult()));

                                        //Si la operacion se aplico de manera correcta lo dejamos asi es decir total de procesados mayor a 0
                                        if (total_procesados > 0) {
                                            finish = true;
                                        } else {
                                            //Si no se aplico el movimiento pero como ya se habia retirado de la TDD entonce se lo devolvemos
                                            bandera_deposito_origen = new TarjetaDeDebito().depositoTDD(tarjeta_origen, procesaOrigen.getMonto());
                                            backendResponse.setBackendMessage("NO SE PUDO PROCESAR FUNCION EN SAICOOP");
                                        }
                                    }
                                }

                            } else {
                                backendResponse.setBackendMessage("NO SE PUDO PROCESAR EL RETIRO DE TARJETA DE DEBITO ORIGEN");
                            }

                        } else {//Solo para pruebas
                            if (identificadorTransferencia == 2) {//Si el destino es una TDD
                                if (backendResponse.getBackendMessage().contains("TERCERO")) {
                                    System.out.println("ES tercero");
                                    //Busco el folio Destino
                                    tarjeta_destino = new WsSiscoopFoliosTarjetas1(opaD.getIdorigenp(), opaD.getIdproducto(), opaD.getIdauxiliar());
                                    if (tarjeta_destino.getActiva()) {
                                        System.out.println("La tarjeta esta activa");
                                        //Proceso un deposito al tercero TDD
                                        bandera_deposito_tercero = new TarjetaDeDebito().depositoTDD(tarjeta_destino, procesaOrigen.getMonto());
                                        if (bandera_deposito_tercero) {
                                            try {
                                                consulta_datos_procesar = "SELECT sai_bankingly_aplica_transaccion('" + fechaTr_.substring(0, 10) + "'," + procesaOrigen.getIdusuario() + ",'" + procesaOrigen.getSesion() + "','" + procesaOrigen.getReferencia() + "')";
                                                procesa_movimiento = em.createNativeQuery(consulta_datos_procesar);
                                                total_procesados = Integer.parseInt(String.valueOf(procesa_movimiento.getSingleResult()));
                                            } catch (Exception e) {
                                                total_procesados = 0;
                                                System.out.println("La funcion saicoop no pudo distribuir el capital");
                                            }

                                            if (total_procesados > 0) {
                                                finish = true;
                                            } else {
                                                backendResponse.setBackendMessage("NO SE PUDIERON PROCESAR LOS MOVIMENTOS EN SAICOOP");
                                                bandera_retiro = new TarjetaDeDebito().retiroTDD(tarjeta_destino, procesaOrigen.getMonto());
                                            }

                                        } else {
                                            backendResponse.setBackendMessage("LA TARJETA DE DEBITO DESTINO ESTA INACTIVA");
                                        }
                                    }

                                } else {//Si el tercero no es TDD ojo aqui no entran prestamos
                                    consulta_datos_procesar = "SELECT sai_bankingly_aplica_transaccion('" + fechaTr_.substring(0, 10) + "'," + procesaOrigen.getIdusuario() + ",'" + procesaOrigen.getSesion() + "','" + procesaOrigen.getReferencia() + "')";
                                    Query procesa_pago = em.createNativeQuery(consulta_datos_procesar);
                                    total_procesados = Integer.parseInt(String.valueOf(procesa_pago.getSingleResult()));
                                    if (total_procesados > 0) {
                                        finish = true;
                                    }
                                }

                            } else {//Verifico que el producto destino sea un prestamo 
                                if (prDestino.getTipoproducto() == 2) {
                                    //Busco el tipo el tipo de amortizacion

                                    if (aDestino.getTipoamortizacion() == 5) {
                                        //Datos a procesar
                                        try {
                                            consulta_datos_procesar = "SELECT sai_bankingly_aplica_transaccion('" + fechaTr_.substring(0, 10) + "'," + procesaOrigen.getIdusuario() + ",'" + procesaOrigen.getSesion() + "','" + procesaOrigen.getReferencia() + "')";
                                            procesa_movimiento = em.createNativeQuery(consulta_datos_procesar);
                                            total_procesados = Integer.parseInt(String.valueOf(procesa_movimiento.getSingleResult()));
                                        } catch (Exception e) {
                                            total_procesados = 0;
                                        }

                                        String si_se_puede_aplicar = "select sai_bankingly_limite_adelanto (" + aDestino.getAuxiliaresPK().getIdorigenp() + ","
                                                + "" + aDestino.getAuxiliaresPK().getIdproducto() + ","
                                                + "" + aDestino.getAuxiliaresPK().getIdauxiliar() + ","
                                                + "(SELECT date(fechatrabajo) FROM origenes limit 1),"
                                                + transaction.getAmount() + ",NULL)";
                                        Query query_se_puede_aplicar = em.createNativeQuery(si_se_puede_aplicar);
                                        Double se_puede_pagar = Double.parseDouble(String.valueOf(query_se_puede_aplicar.getSingleResult()));

                                        if (total_procesados > 0) {
                                            if (procesaOrigen.getMonto() > se_puede_pagar) {
                                                double totalDevolver = procesaOrigen.getMonto() - se_puede_pagar;
                                                double modificar = aOrigen.getSaldo().doubleValue() + totalDevolver;
                                                System.out.println("Se aplico al prestamo un total de :" + se_puede_pagar + " se devolvio al producto un total de:" + totalDevolver);
                                                em.getTransaction().begin();
                                                int v = em.createNativeQuery("UPDATE auxiliares SET saldo=" + modificar + " WHERE idorigenp=" + opaOrigen.getIdorigenp() + " AND idproducto=" + opaOrigen.getIdproducto() + " AND idauxiliar=" + opaOrigen.getIdauxiliar()).executeUpdate();
                                                em.getTransaction().commit();
                                            }
                                            finish = true;
                                        } else {
                                            //Si no se aplico el movimiento pero como ya se habia retirado de la TDD entonce se lo devolvemos
                                            backendResponse.setBackendMessage("NO SE PUDO PROCESAR FUNCION EN SAICOOP");
                                        }
                                    } else {//Si es prestamo creciente
                                        //Datos a procesar
                                        consulta_datos_procesar = "SELECT sai_bankingly_aplica_transaccion('" + fechaTr_.substring(0, 10) + "'," + procesaOrigen.getIdusuario() + ",'" + procesaOrigen.getSesion() + "','" + procesaOrigen.getReferencia() + "')";
                                        //Proceso
                                        procesa_movimiento = em.createNativeQuery(consulta_datos_procesar);
                                        //Obtengo el total de procesad
                                        total_procesados = Integer.parseInt(String.valueOf(procesa_movimiento.getSingleResult()));
                                        //Si la operacion se aplico de manera correcta lo dejamos asi es decir total de procesados mayor a 0
                                        if (total_procesados > 0) {
                                            System.out.println("Los moviemientos se procesaron de manera correcta");
                                            finish = true;
                                        } else {
                                            //Si no se aplico el movimiento pero como ya se habia retirado de la TDD entonce se lo devolvemos
                                            backendResponse.setBackendMessage("NO SE PUDO PROCESAR FUNCION EN SAICOOP");
                                        }
                                    }

                                } else {
                                    consulta_datos_procesar = "SELECT sai_bankingly_aplica_transaccion('" + fechaTr_.substring(0, 10) + "'," + procesaOrigen.getIdusuario() + ",'" + procesaOrigen.getSesion() + "','" + procesaOrigen.getReferencia() + "')";
                                    System.out.println("consulta app:" + consulta_datos_procesar);
                                    Query procesa_pago = em.createNativeQuery(consulta_datos_procesar);
                                    total_procesados = Integer.parseInt(String.valueOf(procesa_pago.getSingleResult()));
                                    if (total_procesados > 0) {
                                        finish = true;
                                    } else {
                                        backendResponse.setBackendMessage("NO SE TERMINO LA TRANSACCION VERIFIQUE FUNCION");
                                    }
                                }
                            }
                        }
                    } else {//Aplico retiro y genero poliza para SPEI
                        tarjeta_origen = new TarjetaDeDebito().buscaTarjetaTDD(opaOrigen.getIdorigenp(), opaOrigen.getIdproducto(), opaOrigen.getIdauxiliar(), em);
                        bandera_retiro = new TarjetaDeDebito().retiroTDD(tarjeta_origen, procesaOrigen.getMonto());
                        //bandera_retiro = true;
                        System.out.println("El cargo : " + transaction.getAmount() + ", abono a cuenta spei y lo que se envio:" + total_a_enviar + ", comisionn:" + comisiones + ", iva de la comision:" + comisiones * 0.16);
                        Double tota_comision = ((comisiones) + (comisiones * 0.16));
                        if (bandera_retiro) {
                            try {
                                consulta_datos_procesar = "SELECT sai_bankingly_aplica_transaccion('" + fechaTr_.substring(0, 10) + "'," + procesaOrigen.getIdusuario() + ",'" + procesaOrigen.getSesion() + "','" + procesaOrigen.getReferencia() + "')";
                                procesa_movimiento = em.createNativeQuery(consulta_datos_procesar);
                                total_procesados = Integer.parseInt(String.valueOf(procesa_movimiento.getSingleResult()));
                            } catch (Exception e) {
                                System.out.println("Error al procesar datos en SAICOOP por funcion :" + e.getMessage());
                            }
                            if (total_procesados > 0) {
                                mensajeBackendResult = "ORDEN ENVIADA CON EXITO,COSTO DE LA TRANSACCION:" + tota_comision;//+(Double.parseDouble(tb_spei_cuenta_comisiones.getDato2())+(Double.parseDouble(tb_spei_cuenta_comisiones.getDato2())*0.16));
                                clean = true;

                            } else {
                                //Si no se aplico el movimiento pero como ya se habia retirado de la TDD entonce se lo devolvemos
                                bandera_deposito_origen = new TarjetaDeDebito().depositoTDD(tarjeta_origen, transactionOWN.getAmount());
                                backendResponse.setBackendMessage("NO SE PUDO PROCESAR FUNCION EN SAICOOP");
                            }
                        } else {
                            backendResponse.setBackendMessage("NO SE PUDO RETIRAR DE TARJETA DE DEBITO VERIFIQUE ESTATUS CON PROVEEDOR");
                        }

                    }

                } else {
                    //El resto de las cajas
                    try {
                        consulta_datos_procesar = "SELECT sai_bankingly_aplica_transaccion('" + fechaTr_.substring(0, 10) + "'," + procesaOrigen.getIdusuario() + ",'" + procesaOrigen.getSesion() + "','" + procesaOrigen.getReferencia() + "')";
                        procesa_movimiento = em.createNativeQuery(consulta_datos_procesar);
                        total_procesados = Integer.parseInt(String.valueOf(procesa_movimiento.getSingleResult()));
                    } catch (Exception e) {
                        total_procesados = 0;
                        System.out.println("La funcion saicoop no pudo distribuir el capital");
                    }

                    if (total_procesados > 0) {
                        finish = true;
                    } else {
                        backendResponse.setBackendMessage("NO SE APLICO EL MOVIMIENTO FALLA AL PROCESAR MOVIMIENTOS EN SAICOOP");
                    }
                }
                if (finish) {
                    if (identificadorTransferencia != 5) {
                        //Si fue un pago a prestamo propio o de Tercero
                        if (prDestino.getTipoproducto() == 2) {
                            //Obtengo los datos(Seguro hipotecario,comisones cobranza,interes ect.) Me muestra de que manera se distribuyo mi pago
                            String distribucion = "SELECT sai_bankingly_detalle_transaccion_aplicada('" + fechaTr_.substring(0, 10) + "'," + procesaOrigen.getIdusuario() + ",'" + procesaOrigen.getSesion() + "','" + procesaOrigen.getReferencia() + "')";
                            System.out.println("DistribucionConsulta:" + distribucion);
                            Query procesa_distribucion = em.createNativeQuery(distribucion);
                            String distribucionProcesada = String.valueOf(procesa_distribucion.getSingleResult());
                            System.out.println("Distribucion_Procesada:" + distribucionProcesada);
                            //Guardo en una lista los montos que se han procesado
                            String ArrayDistribucion[] = distribucionProcesada.split("\\|");

                            //Mensaje personalizado para CSN
                            if (banderaCSN) {
                                System.out.println("Mostrando detalles de lo aplicado");
                                System.out.println("Seguro hipotecario:" + ArrayDistribucion[0]);
                                System.out.println("COMISIÓN COBRANZA:" + ArrayDistribucion[1]);
                                System.out.println("INTERES MORATORIO:" + ArrayDistribucion[2]);
                                System.out.println("IVA INTERES MORATORIO :" + ArrayDistribucion[3]);
                                System.out.println("INTERES ORDINARIO     :" + ArrayDistribucion[4]);
                                System.out.println("IVA INTERES ORDINARIO :" + ArrayDistribucion[5]);
                                System.out.println("CAPITAL               :" + ArrayDistribucion[6]);
                                System.out.println("ADELANTO DE INTERES :" + ArrayDistribucion[7]);

                                mensajeBackendResult = "<html>"
                                        + "<body>"
                                        + "<h1><center>PAGO EXITOSO</center></h1>"
                                        + "<table border=1 align=center cellspacing=0>"
                                        + "<tr><td>SEGURO HIPOTECARIO</td><td>" + ArrayDistribucion[0] + "</td></tr>"
                                        + "<tr><td>COMISIÓN COBRANZA</td><td>" + ArrayDistribucion[1] + "</td></tr>"
                                        + "<tr><td>INTERES MORATORIO</td><td>" + ArrayDistribucion[2] + "</td></tr>"
                                        + "<tr><td>IVA INTERES MORATORIO </td><td>" + ArrayDistribucion[3] + " </td></tr>"
                                        + "<tr><td>INTERES ORDINARIO     </td><td>" + ArrayDistribucion[4] + " </td></tr>"
                                        + "<tr><td>IVA INTERES ORDINARIO </td><td>" + ArrayDistribucion[5] + " </td></tr>"
                                        + "<tr><td>CAPITAL               </td><td>" + ArrayDistribucion[6] + " </td></tr>"
                                        + "<tr><td>ADELANTO DE INTERES   </td><td>" + ArrayDistribucion[7] + " </td></tr></table>"
                                        + "<center><p>EVITA ATRASOS, EN TU PRESTAMO EL PAGO DE INTERESES DEBE SER MENSUAL</p></center>"
                                        + "</table>"
                                        + "</body>"
                                        + "</html>";

                            } else {
                                /* mensajeBackendResult = "PAGO EXITOSO" + "\n"
                                        + "SEGURO HIPOTECARIO    :" + ArrayDistribucion[0] + "\n "
                                        + "COMISON COBRANZA      :" + ArrayDistribucion[1] + "\n "
                                        + "INTERES MORATORIO     :" + ArrayDistribucion[2] + "\n "
                                        + "IVA INTERES MORATORIO :" + ArrayDistribucion[3] + "\n"
                                        + "INTERES ORDINARIO     :" + ArrayDistribucion[4] + "\n"
                                        + "IVA INTERES ORDINARIO :" + ArrayDistribucion[5] + "\n"
                                        + "CAPITAL               :" + ArrayDistribucion[6] + "\n"
                                       + "ADELANTO DE INTERES   :" + ArrayDistribucion[7] + "\n \n \n \n \n \n";*///Para adelanto de interese solo aplicaria para los productos configurados

                                mensajeBackendResult = "<html>"
                                        + "<body>"
                                        + "<h1><center>PAGO EXITOSO</center></h1>"
                                        + "<table border=1 align=center cellspacing=0>"
                                        + "<tr><td>SEGURO HIPOTECARIO</td><td>" + ArrayDistribucion[0] + "</td></tr>"
                                        + "<tr><td>COMISIÓN COBRANZA</td><td>" + ArrayDistribucion[1] + "</td></tr>"
                                        + "<tr><td>INTERES MORATORIO</td><td>" + ArrayDistribucion[2] + "</td></tr>"
                                        + "<tr><td>IVA INTERES MORATORIO </td><td>" + ArrayDistribucion[3] + " </td></tr>"
                                        + "<tr><td>INTERES ORDINARIO     </td><td>" + ArrayDistribucion[4] + " </td></tr>"
                                        + "<tr><td>IVA INTERES ORDINARIO </td><td>" + ArrayDistribucion[5] + " </td></tr>"
                                        + "<tr><td>CAPITAL               </td><td>" + ArrayDistribucion[6] + " </td></tr>"
                                        + "<tr><td>ADELANTO DE INTERES   </td><td>" + ArrayDistribucion[7] + " </td></tr></table>"
                                        + "</table>"
                                        + "</body>"
                                        + "</html>";
                            }
                            clean = true;
                        } else if (prDestino.getTipoproducto() == 0) {
                            mensajeBackendResult = "TRANSACCION EXITOSA";
                            clean = true;
                        }
                    }
                }

                if (clean) {
                    //Aplico la  funcion para limpiar la tabla donde estaban los pagos cargo y abono
                    String consulta_termina_transaccion = "SELECT sai_bankingly_termina_transaccion('" + fechaTr_.substring(0, 10) + "'," + procesaOrigen.getIdusuario() + ",'" + procesaOrigen.getSesion() + "','" + procesaOrigen.getReferencia() + "')";
                    Query termina_transaccion = em.createNativeQuery(consulta_termina_transaccion);

                    int registros_limpiados = Integer.parseInt(String.valueOf(termina_transaccion.getSingleResult()));
                    System.out.println("Registros Limpiados con exito:" + registros_limpiados);

                    String envio_ok_sms = "";

                    if (util2.obtenerOrigen(em) == 30200) {
                        PreparaSMS envio_sms = new PreparaSMS();
                        //Verfico si esta activo el permitir enviar sms
                        Tablas tb_sms_activo = util2.busquedaTabla(em, "bankingly_banca_movil", "smsactivo");
                        System.out.println("Tablas sms:" + tb_sms_activo.getTablasPK());

                        if (Integer.parseInt(tb_sms_activo.getDato1()) == 1) {
                            //Obtengo el minimo para enviar el SMS
                            Tablas tb_minimo_sms = util2.busquedaTabla(em, "bankingly_banca_movil", "monto_minimo_sms");

                            if (transaction.getAmount() >= Double.parseDouble(tb_minimo_sms.getDato1())) {
                                if (identificadorTransferencia == 1) {
                                    System.out.println("entro a enviar sms a cuenta propia");
                                    //Enviamos datos a preparar el sms indicando que debe obtener datos de mensaje a cuenta propia
                                    //new PreparaSMS().enviaSMS_CSN(em, String.valueOf(transaction.getAmount()), 1, transaction.getDebitproductbankidentifier(), transaction.getCreditproductbankidentifier(), transaction.getClientbankidentifier());
                                    envio_ok_sms = envio_sms.enviaSMS_CSN(em, String.valueOf(transaction.getAmount()), 1, transaction.getDebitproductbankidentifier(), transaction.getCreditproductbankidentifier(), transaction.getClientbankidentifier());
                                } else if (identificadorTransferencia == 2) {
                                    System.out.println("entro a enviar sms a cuenta de tercero");
                                    //Enviamos datos a preparar el sms indicando que debe obtener datos de mensaje a cuenta propia
                                    // new PreparaSMS().enviaSMS_CSN(em, String.valueOf(transaction.getAmount()), 2, transaction.getDebitproductbankidentifier(), transaction.getCreditproductbankidentifier(), transaction.getClientbankidentifier());
                                    envio_ok_sms = envio_sms.enviaSMS_CSN(em, String.valueOf(transaction.getAmount()), 2, transaction.getDebitproductbankidentifier(), transaction.getCreditproductbankidentifier(), transaction.getClientbankidentifier());
                                } else if (identificadorTransferencia == 3) {
                                    System.out.println("entro a enviar sms abono propio");
                                    //Enviamos datos a preparar el sms indicando que debe obtener datos de mensaje a cuenta propia
                                    //new PreparaSMS().enviaSMS_CSN(em, String.valueOf(transaction.getAmount()), 3, transaction.getDebitproductbankidentifier(), transaction.getCreditproductbankidentifier(), transaction.getClientbankidentifier());
                                    envio_ok_sms = envio_sms.enviaSMS_CSN(em, String.valueOf(transaction.getAmount()), 3, transaction.getDebitproductbankidentifier(), transaction.getCreditproductbankidentifier(), transaction.getClientbankidentifier());
                                } else if (identificadorTransferencia == 4) {
                                    System.out.println("entro a enviar sms abono tercero");
                                    //Enviamos datos a preparar el sms indicando que debe obtener datos de mensaje a cuenta propia
                                    //new PreparaSMS().enviaSMS_CSN(em, String.valueOf(transaction.getAmount()), 4, transaction.getDebitproductbankidentifier(), transaction.getCreditproductbankidentifier(), transaction.getClientbankidentifier());

                                    envio_ok_sms = envio_sms.enviaSMS_CSN(em, String.valueOf(transaction.getAmount()), 4, transaction.getDebitproductbankidentifier(), transaction.getCreditproductbankidentifier(), transaction.getClientbankidentifier());
                                }
                            }
                        }
                    }

                    if (envio_ok_sms.toUpperCase().contains("ERROR")) {
                        backendResponse.setBackendMessage(backendResponse.getBackendMessage() + " " + envio_ok_sms);
                    }
                    backendResponse.setBackendCode("1");
                    backendResponse.setBackendReference(transaction.getTransactiontypeid().toString());
                    backendResponse.setIsError(false);
                    //Si fuera transferencia SPEI se devuelve el idTransaccion(Devuelvo el id de la orden SPEI)
                    if (identificadorTransferencia == 5) {
                        //Guardo la orden
                        backendResponse.setTransactionIdenty(String.valueOf(response.getId()));
                    } else {
                        backendResponse.setTransactionIdenty(String.valueOf(transaction.getTransactionid()));
                    }
                    backendResponse.setBackendMessage(mensajeBackendResult);
                    backendResponse.setBackendReference(transaction.getTransactionid().toString());

                    //Para que no se genere error
                    try {
                        //Guardo en una tabla el historial de la operacion realizada
                        transaction.setTransactionid(new BigDecimal(procesaOrigen.getReferencia().trim()));
                        em.getTransaction().begin();
                        em.persist(transaction);
                        em.getTransaction().commit();

                        //Si es una transferencia a tercero ya sea dentro o fuera de la entidad la registro
                        /*if (identificadorTransferencia == 2 || identificadorTransferencia == 5) {
                            tmp_transferencias_terceros transferencia_tercero = new tmp_transferencias_terceros();
                            PersonasPK llave = new PersonasPK(aOrigen.getIdorigen(), aOrigen.getIdgrupo(), aOrigen.getIdsocio());
                            transferencia_tercero.setPersonas(llave);
                            transferencia_tercero.setCargoabono(0);
                            transferencia_tercero.setFecha(hoy);
                            transferencia_tercero.setMonto(transactionOWN.getAmount());
                            if (identificadorTransferencia == 2) {
                                transferencia_tercero.setTipotransferencia("Tercero dentro de la entidad");
                            } else {
                                transferencia_tercero.setTipotransferencia("A otro banco(SPEI");
                            }
                            //tb_precio_udi en el periodo
                            Tablas tb_precio_udi_periodo = util2.busquedaTabla(em, "valor_udi", String.valueOf(fecha_transferencia).substring(0, 7).replace("-", ""));
                            transferencia_tercero.setTotalenudis(Double.parseDouble(tb_precio_udi_periodo.getDato1()) * transactionOWN.getAmount());
                            em.getTransaction().begin();
                            em.persist(transferencia_tercero);
                            em.getTransaction().commit();

                        }*/
                    } catch (Exception e) {
                        System.out.println("Error al persistir:" + e.getMessage());
                    }

                }

            }

        } catch (Exception e) {
            if (backendResponse.getBackendMessage().contains("EXITO")) {
                backendResponse.setBackendMessage("Error:" + e.getMessage());
            } else {
                backendResponse.setBackendMessage(e.getMessage());
            }
            System.out.println("Error Al ejecutar transferencia:" + e.getMessage());
            return backendResponse;
        } finally {
            em.close();
        }

        return backendResponse;
    }

    public VaucherDTO vaucher(String idtransaccion) {
        VaucherDTO voucher = new VaucherDTO();
        try {
            System.out.println("LLEgo");
            String busqueda_Auxiliar = "SELECT * FROM auxiliares_d WHERE transaccion=" + idtransaccion;
            Query busqueda_aux = AbstractFacade.conexion().createNativeQuery(busqueda_Auxiliar, AuxiliaresD.class);
            AuxiliaresD ad = (AuxiliaresD) busqueda_aux.getSingleResult();
            System.out.println("Auxiliar d:" + ad);
            File file_html = construirHtmlVoucher(ad);

            if (crearPDF(ruta(), file_html.getName())) {
                String rutpdf = ruta() + file_html.getName().replace(".html", ".pdf");
                byte[] input_file = Files.readAllBytes(Paths.get(rutpdf));
                byte[] encodedBytesFile = Base64.getEncoder().encode(input_file);
                String bytesFileId = new String(encodedBytesFile);
                voucher.setProductBankStatementFileName(file_html.getName().replace(".html", ".pdf"));
                voucher.setProductBankStatementFile(bytesFileId);
                file_html.delete();
            }
        } catch (Exception e) {
            System.out.println("Error al formar el vaucher:" + e.getMessage());
        }
        return voucher;

    }

    //Metodo para validar transferencia entre cuentas propias
    public String validarTransferenciaEntreMisCuentas(String opaOrigen, Double monto, String opaDestino, String clientBankIdentifier) {
        EntityManager em = AbstractFacade.conexion();
        OpaDTO opaO = util.opa(opaOrigen);
        OpaDTO opaD = util.opa(opaDestino);
        OgsDTO ogs = util.ogs(clientBankIdentifier);

        String cuentaOrigen = "SELECT * FROM auxiliares a WHERE idorigenp=" + opaO.getIdorigenp() + " AND idproducto=" + opaO.getIdproducto() + " AND idauxiliar=" + opaO.getIdauxiliar()
                + " AND  idorigen=" + ogs.getIdorigen() + " AND idgrupo=" + ogs.getIdgrupo() + " AND idsocio=" + ogs.getIdsocio();
        String cuentaDestino = "SELECT * FROM auxiliares a WHERE idorigenp=" + opaD.getIdorigenp() + " AND idproducto=" + opaD.getIdproducto() + " AND idauxiliar=" + opaD.getIdauxiliar();
        String message = "";
        boolean banderaGrupo = false;
        boolean banderaProductosDeposito = false;
        try {
            Auxiliares ctaOrigen = null;
            boolean bOrigen = false;
            System.out.println("ConsultaParaCuentaOrigen:" + cuentaOrigen);
            try {
                Query query = em.createNativeQuery(cuentaOrigen, Auxiliares.class);
                //Obtengo el producto origen
                ctaOrigen = (Auxiliares) query.getSingleResult();
                bOrigen = true;
            } catch (Exception e) {
                System.out.println("No existe Cuenta Origen");
                bOrigen = false;
            }

            //Si existe el auxiliar origen en tabla auxiliares
            if (bOrigen) {
                Double saldo = Double.parseDouble(ctaOrigen.getSaldo().toString());
                if (util2.obtenerOrigen(em) == 30200) {
                    Tablas tablaProductoTDD = new TarjetaDeDebito().productoTddwebservice(em);
                    if (ctaOrigen.getAuxiliaresPK().getIdproducto() == Integer.parseInt(tablaProductoTDD.getDato1())) {
                        //Si es la TDD               
                        WsSiscoopFoliosTarjetasPK1 foliosPK = new WsSiscoopFoliosTarjetasPK1(ctaOrigen.getAuxiliaresPK().getIdorigenp(), ctaOrigen.getAuxiliaresPK().getIdproducto(), ctaOrigen.getAuxiliaresPK().getIdauxiliar());
                        WsSiscoopFoliosTarjetas1 tarjetas = new TarjetaDeDebito().buscaTarjetaTDD(foliosPK.getIdorigenp(), foliosPK.getIdproducto(), foliosPK.getIdauxiliar(), em);
                        try {
                            System.out.println("consultando saldo para idtarjeta:" + tarjetas.getIdtarjeta());
                            BalanceQueryResponseDto saldoTDD = new TarjetaDeDebito().saldoTDD(tarjetas.getWsSiscoopFoliosTarjetasPK());
                            saldo = saldoTDD.getAvailableAmount();
                            message = "TDD";
                            //saldo = 200.0;
                            System.out.println("Saldo TDD:" + saldo);

                        } catch (Exception e) {
                            System.out.println("Error al buscar saldo de TDD:" + ctaOrigen.getAuxiliaresPK().getIdproducto());
                            return message = "ERROR AL CONSUMIR WS TDD Y OBTENER SALDO DEL PRODUCTO";

                        }
                    }
                }

                //Busco descripcion del idproducto origen
                Productos prOrigen = em.find(Productos.class, ctaOrigen.getAuxiliaresPK().getIdproducto());
                //Valido que el producto origen se trabaje en banca movil
                Productos_bankingly cuentasBankingly = em.find(Productos_bankingly.class, ctaOrigen.getAuxiliaresPK().getIdproducto());
                if (cuentasBankingly != null) {
                    //si el producto no es un prestamo            
                    if (prOrigen.getTipoproducto() == 0) {
                        //Verifico el estatus de la cuenta origen
                        if (ctaOrigen.getEstatus() == 2) {
                            //verifico que el saldo del producto origen es mayor o igual a lo que se intenta transferir
                            if (saldo >= monto) {
                                Auxiliares ctaDestino = null;
                                boolean bDestino = false;
                                //Busco la cuenta destino
                                try {
                                    Query queryDestino = em.createNativeQuery(cuentaDestino, Auxiliares.class);
                                    ctaDestino = (Auxiliares) queryDestino.getSingleResult();
                                    bDestino = true;
                                } catch (Exception e) {
                                    System.out.println("Error al encontrar productoDestino:" + e.getMessage());
                                    bDestino = false;
                                }
                                if (bDestino) {
                                    //Busco el producto destino
                                    Productos productoDestino = em.find(Productos.class, ctaDestino.getAuxiliaresPK().getIdproducto());
                                    //Valido que la cuenta destino este activa
                                    if (ctaDestino.getEstatus() == 2) {
                                        //Valido que producto destino opera para banca movil
                                        Productos_bankingly cuentaBankinglyDestino = em.find(Productos_bankingly.class, ctaDestino.getAuxiliaresPK().getIdproducto());
                                        if (cuentaBankinglyDestino != null) {
                                            //Valido que el producto destino no sea un prestamo
                                            if (productoDestino.getTipoproducto() == 0) {
                                                //Valido que realmente el el producto destino pertenezca al mismo socio 
                                                if (ctaOrigen.getIdorigen() == ctaDestino.getIdorigen() && ctaOrigen.getIdgrupo() == ctaDestino.getIdgrupo() && ctaOrigen.getIdsocio() == ctaDestino.getIdsocio()) {
                                                    //Valido que la cuenta origen para CSN esat un grupo de retiro configurado
                                                    if (util2.obtenerOrigen(em) == 30200) {
                                                        //Buscamos que el producto origen pertenezca al grupo de retiro
                                                        Tablas tb = util2.busquedaTabla(em, "bankingly_banca_movil", "grupo_retiro");
                                                        if (ctaOrigen.getIdgrupo() == Integer.parseInt(tb.getDato1())) {
                                                            //Ahora verifico que el destino perteneneza al grupo de depositos
                                                            Tablas tbRetiro = util2.busquedaTabla(em, "bankingly_banca_movil", "grupo_deposito");
                                                            String cadena[] = tbRetiro.getDato1().split("\\|");

                                                            List list = Arrays.asList(cadena);
                                                            for (int i = 0; i < list.size(); i++) {
                                                                if (ctaOrigen.getIdgrupo() == Integer.parseInt(String.valueOf(list.get(i)))) {
                                                                    banderaGrupo = true;
                                                                }
                                                            }
                                                            if (banderaGrupo) {
                                                                //valido que el producto acepte depositos
                                                                Tablas tbDeposito = util2.busquedaTabla(em, "bankingly_banca_movil", "productos_deposito");
                                                                System.out.println("tabla Productos deposito:" + tbDeposito.getDato2());
                                                                String productos_deposito[] = tbDeposito.getDato2().split("\\|");

                                                                List list_deposito = Arrays.asList(productos_deposito);
                                                                for (int i = 0; i < list_deposito.size(); i++) {
                                                                    System.out.println("prod pos " + i + ":" + list_deposito.get(i));
                                                                    if (ctaDestino.getAuxiliaresPK().getIdproducto() == Integer.parseInt(String.valueOf(list_deposito.get(i)))) {
                                                                        banderaProductosDeposito = true;
                                                                    }
                                                                }
                                                                if (banderaProductosDeposito) {
                                                                    message = message + " VALIDADO CON EXITO";
                                                                } else {
                                                                    message = "PRODUCTO NO CONFIGURADO PARA RECIBIR DEPOSITOS";
                                                                }
                                                            } else {
                                                                message = "GRUPO NO CONFIGURADO PARA DEPOSITOS";
                                                            }
                                                        } else {
                                                            message = "SOCIO NO PERTENECE AL GRUPO DE RETIRO";
                                                        }

                                                    } else {
                                                        message = "VALIDADO CON EXITO";
                                                    }
                                                    /* } else {
                                                            message = "MONTO TRASPASA EL PERMITIDO DIARIO";
                                                        }
                                                    } else {
                                                        message = "EL SALDO QUE INTENTA TRANSFERIR ES " + minMax(monto).toUpperCase() + " AL PERMITIDO";
                                                    }*/
                                                } else {
                                                    message = "PRODUCTO DESTINO NO PERTENECE AL MISMO SOCIO";
                                                }
                                            } else {
                                                message = "PRODUCTO DENTINO SOLO ACEPTA PAGOS(PRESTAMO)";
                                            }
                                        } else {
                                            message = "PRODUCTO DESTINO NO OPERA PARA BANCA MOVIL";
                                        }
                                    } else {
                                        message = "PRODUCTO DESTINO ESTA INACTIVA";
                                    }
                                } else {
                                    message = "NO SE ENCONTRO PRODUCTO DESTINO";
                                }
                            } else {
                                message = "FONDOS INSUFICIENTES PARA COMPLETAR LA TRANSACCION";
                            }
                        } else {
                            message = "PRODUCTO ORIGEN INACTIVO";
                        }

                    } else {
                        message = "PRODUCTO ORIGEN NO PERMITE SOBRECARGOS";
                    }
                } else {
                    message = "PRODUCTO ORIGEN NO OPERA PARA BANCA MOVIL";
                }
            } else {
                message = "PRODUCTO ORIGEN NO PERTENECE AL SOCIO:" + clientBankIdentifier;
            }

        } catch (Exception e) {
            message = "ERROR AL PROCESAR CONSULTA VALIDACIONES DE DATOS";
            System.out.println("Error en validacion transferencia entre mis cuentas:" + e.getMessage());
            return message;
        } finally {
            em.close();
        }
        return message.toUpperCase();
    }

    //Metodo para validar transferencia a otras cuentas
    public String validarTransferenciaATerceros(String opaOrigen, Double monto, String opaDestino, String clientBankIdentifier) {
        EntityManager em = AbstractFacade.conexion();
        OpaDTO opaO = util.opa(opaOrigen);
        OpaDTO opaD = util.opa(opaDestino);
        OgsDTO ogs = util.ogs(clientBankIdentifier);

        String cuentaOrigen = "SELECT * FROM auxiliares a WHERE idorigenp=" + opaO.getIdorigenp() + " AND idproducto=" + opaO.getIdproducto() + " AND idauxiliar=" + opaO.getIdauxiliar()
                + " AND  idorigen=" + ogs.getIdorigen() + " AND idgrupo=" + ogs.getIdgrupo() + " AND idsocio=" + ogs.getIdsocio();

        String cuentaDestino = "SELECT * FROM auxiliares a WHERE idorigenp=" + opaD.getIdorigenp() + " AND idproducto=" + opaD.getIdproducto() + " AND idauxiliar=" + opaD.getIdauxiliar();
        String message = "";
        boolean banderaGrupo = false;
        boolean banderaProductosDeposito = false;
        try {
            Auxiliares ctaOrigen = null;
            boolean bOrigen = false;
            try {
                Query query = em.createNativeQuery(cuentaOrigen, Auxiliares.class);
                //Obtengo el producto origen
                ctaOrigen = (Auxiliares) query.getSingleResult();
                bOrigen = true;
            } catch (Exception e) {
                System.out.println("Error al buscar producto origen:" + e.getMessage());
                bOrigen = false;
            }
            Tablas tablaProductoTDD = null;
            if (bOrigen) {
                Double saldo = Double.parseDouble(ctaOrigen.getSaldo().toString());
                if (util2.obtenerOrigen(em) == 30200) {
                    tablaProductoTDD = new TarjetaDeDebito().productoTddwebservice(em);
                    if (ctaOrigen.getAuxiliaresPK().getIdproducto() == Integer.parseInt(tablaProductoTDD.getDato1())) {
                        WsSiscoopFoliosTarjetasPK1 foliosPK = new WsSiscoopFoliosTarjetasPK1(ctaOrigen.getAuxiliaresPK().getIdorigenp(), ctaOrigen.getAuxiliaresPK().getIdproducto(), ctaOrigen.getAuxiliaresPK().getIdauxiliar());
                        WsSiscoopFoliosTarjetas1 tarjetas = new TarjetaDeDebito().buscaTarjetaTDD(foliosPK.getIdorigenp(), foliosPK.getIdproducto(), foliosPK.getIdauxiliar(), em);
                        try {
                            System.out.println("consultando saldo para idtarjeta:" + tarjetas.getIdtarjeta());
                            BalanceQueryResponseDto saldoTDD = new TarjetaDeDebito().saldoTDD(tarjetas.getWsSiscoopFoliosTarjetasPK());
                            message = "DESDE TDD";
                            saldo = saldoTDD.getAvailableAmount();
                            //saldo = 200.0;
                        } catch (Exception e) {
                            System.out.println("Error al buscar saldo de TDD:" + ctaOrigen.getAuxiliaresPK().getIdproducto());
                            return message = "ERROR AL CONSUMIR WS TDD Y OBTENER SALDO DEL PRODUCTO";

                        }
                    }
                }

                //Busco descripcion del idproducto origen
                Productos prOrigen = em.find(Productos.class, ctaOrigen.getAuxiliaresPK().getIdproducto());
                //Valido que el producto origen se trabaje en banca movil
                Productos_bankingly cuentasBankingly = em.find(Productos_bankingly.class, ctaOrigen.getAuxiliaresPK().getIdproducto());
                if (cuentasBankingly != null) {
                    //si el producto no es un prestamo            
                    if (prOrigen.getTipoproducto() != 2) {
                        //Verifico el estatus de la cuenta origen
                        if (ctaOrigen.getEstatus() == 2) {
                            //verifico que el saldo del producto origen es mayor o igual a lo que se intenta transferir
                            if (saldo >= monto) {
                                Auxiliares ctaDestino = null;
                                boolean bDestino = false;
                                try {
                                    //Busco la cuenta destino
                                    Query queryDestino = em.createNativeQuery(cuentaDestino, Auxiliares.class);
                                    ctaDestino = (Auxiliares) queryDestino.getSingleResult();
                                    bDestino = true;
                                } catch (Exception e) {
                                    System.out.println("Error al buscar producto destino:" + e.getMessage());
                                    bDestino = false;
                                }
                                if (bDestino) {
                                    //Busco el producto destino
                                    Productos productoDestino = em.find(Productos.class, ctaDestino.getAuxiliaresPK().getIdproducto());
                                    //Valido que la cuenta destino este activa
                                    if (ctaDestino.getEstatus() == 2) {
                                        //Busco si existe el producto destino en el catalogo de banca movil
                                        Productos_bankingly catalogoDestino = em.find(Productos_bankingly.class, productoDestino.getIdproducto());
                                        if (catalogoDestino != null) {

                                            //Verifico si de verdad cuenta destinono pertence al mismo socio ya que es transferencia a tercero
                                            if (ctaOrigen.getIdsocio() != ctaDestino.getIdsocio()) {
                                                //Valido que el producto destino no sea un prestamo
                                                if (productoDestino.getTipoproducto() == 0) {

                                                    //Valido que la cuenta origen para CSN esat un grupo de retiro configurado
                                                    if (util2.obtenerOrigen(em) == 30200) {
                                                        if (ctaDestino.getAuxiliaresPK().getIdproducto() == Integer.parseInt(tablaProductoTDD.getDato1())) {
                                                            //Por si el destino es TDD 
                                                            message = message + " A TERCERO TDD ";
                                                        }
                                                        //Buscamos que el producto origen pertenezca al grupo de retiro
                                                        Tablas tb = util2.busquedaTabla(em, "bankingly_banca_movil", "grupo_retiro");

                                                        if (ctaOrigen.getIdgrupo() == Integer.parseInt(tb.getDato1())) {
                                                            //Ahora verifico que el destino perteneneza al grupo de depositos
                                                            Tablas tbRetiro = util2.busquedaTabla(em, "bankingly_banca_movil", "grupo_deposito");
                                                            String cadena[] = tbRetiro.getDato1().split("\\|");
                                                            List list = Arrays.asList(cadena);
                                                            for (int i = 0; i < list.size(); i++) {
                                                                if (ctaOrigen.getIdgrupo() == Integer.parseInt(String.valueOf(list.get(i)))) {
                                                                    banderaGrupo = true;
                                                                }
                                                            }
                                                            if (banderaGrupo) {
                                                                Tablas tbDeposito = util2.busquedaTabla(em, "bankingly_banca_movil", "productos_deposito");
                                                                String productos_deposito[] = tbDeposito.getDato2().split("\\|");
                                                                List list_deposito = Arrays.asList(productos_deposito);
                                                                for (int i = 0; i < list_deposito.size(); i++) {
                                                                    if (ctaDestino.getAuxiliaresPK().getIdproducto() == Integer.parseInt(String.valueOf(list_deposito.get(i)))) {
                                                                        banderaProductosDeposito = true;
                                                                    }
                                                                }
                                                                if (banderaProductosDeposito) {
                                                                    if (ctaDestino.getIdgrupo() == 20 || ctaDestino.getIdgrupo() == 25) {
                                                                        //Deposito en pesos mexicanos menores
                                                                        Tablas tb_menores_permitido_diario = util2.busquedaTabla(em, "bankingly_banca_movil", "total_deposito_diario_menores");
                                                                        //Total en udis
                                                                        Tablas tb_menores_udis_mensual = util2.busquedaTabla(em, "bankingly_banca_movil", "total_udis_mensual_menores");
                                                                        //Deposito en pesos mexicanos juveniles
                                                                        Tablas tb_juveniles_permitido_diario = util2.busquedaTabla(em, "bankingly_banca_movil", "total_deposito_diario_juveniles");
                                                                        //Deposito en udis juveniles
                                                                        Tablas tb_juveniles_udis_mensual = util2.busquedaTabla(em, "bankingly_banca_movil", "total_udis_mensual_juveniles");

                                                                        //Obtengo la fecha de trabajo
                                                                        Query query_fecha_trabajo = em.createNativeQuery("SELECT to_char(fechatrabajo,'yyyy/mm/dd') FROM origenes limit 1");

                                                                        String fecha_trabajo = String.valueOf(query_fecha_trabajo.getSingleResult());

                                                                        //tb_precio_udi en el periodo
                                                                        Tablas tb_precio_udi_periodo = util2.busquedaTabla(em, "valor_udi", fecha_trabajo.substring(0, 7).replace("/", ""));

                                                                        //Construyo la consulta para buscar el monto diario de un menor o juvenil
                                                                        String consulta_permitido_diario = "select sum(monto) from auxiliares_d"
                                                                                + " inner join auxiliares a using(idorigenp,idproducto,idauxiliar)"
                                                                                + " inner join productos using(idproducto)"
                                                                                + " where estatus=2"
                                                                                + " and a.idorigen=" + ctaDestino.getIdorigen()
                                                                                + " and idgrupo=" + ctaDestino.getIdgrupo()
                                                                                + " and idsocio=" + ctaDestino.getIdsocio()
                                                                                + " and tipoproducto=0"
                                                                                + " and cargoabono=1"
                                                                                + " and date(fecha)='" + fecha_trabajo + "' and cargoabono=1";//(SELECT to_char(fechatrabajo,'yyyymm') from origenes limit 1)";

                                                                        //Construyo consulta para buscar el total de depositos en el mes 
                                                                        String consulta_udis_permitido = "select sum(monto) from auxiliares_d"
                                                                                + " inner join auxiliares a using(idorigenp,idproducto,idauxiliar)"
                                                                                + " inner join productos using(idproducto)"
                                                                                + " where estatus=2"
                                                                                + " and a.idorigen=" + ctaDestino.getIdorigen()
                                                                                + " and idgrupo=" + ctaDestino.getIdgrupo()
                                                                                + " and idsocio=" + ctaDestino.getIdsocio()
                                                                                + " and tipoproducto=0"
                                                                                + " and cargoabono=1"
                                                                                + " and periodo='" + fecha_trabajo.substring(0, 7).replace("/", "") + "' and cargoabono=1";//(SELECT to_char(fechatrabajo,'yyyymm') from origenes limit 1)";

                                                                        Query monto_permitido_diario = null;
                                                                        double monto_diario = 0;
                                                                        Query udis_mensual = null;
                                                                        double monto_udis_mensual = 0;
                                                                        System.out.println("consulta_deposito_diario:" + consulta_permitido_diario);
                                                                        System.out.println("Consulta_udis_mensual:" + consulta_udis_permitido);

                                                                        //Si el grupo es un menor
                                                                        if (ctaDestino.getIdgrupo() == 20) {
                                                                            //Busco si lo que se le permite a un menor dirario no es mayor a lo que hay en la tabla  
                                                                            try {
                                                                                monto_permitido_diario = em.createNativeQuery(consulta_permitido_diario);
                                                                                monto_diario = Double.parseDouble(String.valueOf(monto_permitido_diario.getSingleResult()));
                                                                            } catch (Exception e) {
                                                                                monto_diario = 0.0;
                                                                            }
                                                                            if ((monto_diario + monto) <= Double.parseDouble(tb_menores_permitido_diario.getDato1())) {
                                                                                //Ahora busco todos los movimientos para saber el total de usdis
                                                                                //Busco la tabla donde esta el total de udis
                                                                                try {
                                                                                    udis_mensual = em.createNativeQuery(consulta_udis_permitido);
                                                                                    monto_udis_mensual = Double.parseDouble(String.valueOf(udis_mensual.getSingleResult())) * Double.parseDouble(tb_precio_udi_periodo.getDato1());
                                                                                } catch (Exception e) {
                                                                                    monto_udis_mensual = 0;
                                                                                }

                                                                                if (monto_udis_mensual <= Double.parseDouble(tb_menores_udis_mensual.getDato1())) {
                                                                                    message = message + " VALIDADO CON EXITO";
                                                                                } else {
                                                                                    message = "MONTO EN UDIS TRASPASA AL PERMITIDO POR MES";
                                                                                }

                                                                            } else {
                                                                                message = "EL MONTO TRASPASA AL PERMITIDO DIARIO";
                                                                            }

                                                                        } else if (ctaDestino.getIdgrupo() == 25) {//Si es un juvenil
                                                                            //Busco si lo que se le permite a un menor dirario no es mayor a lo que hay en la tabla  
                                                                            try {
                                                                                monto_permitido_diario = em.createNativeQuery(consulta_permitido_diario);
                                                                                monto_diario = Double.parseDouble(String.valueOf(monto_permitido_diario.getSingleResult()));
                                                                            } catch (Exception e) {
                                                                                monto_diario = 0.0;
                                                                            }

                                                                            if ((monto_diario + monto) <= Double.parseDouble(tb_juveniles_permitido_diario.getDato1())) {
                                                                                //Ahora busco todos los movimientos para saber el total de usdis
                                                                                //Busco la tabla donde esta el total de udis
                                                                                try {
                                                                                    udis_mensual = em.createNativeQuery(consulta_udis_permitido);
                                                                                    monto_udis_mensual = Double.parseDouble(String.valueOf(udis_mensual.getSingleResult())) * Double.parseDouble(tb_precio_udi_periodo.getDato1());
                                                                                } catch (Exception e) {
                                                                                    monto_udis_mensual = 0;
                                                                                }

                                                                                if (monto_udis_mensual <= Double.parseDouble(tb_juveniles_udis_mensual.getDato1())) {

                                                                                    //Valido que si tiene el 180 no puede recibir depositos en el 125
                                                                                    boolean Bandera_180 = false;
                                                                                    try {
                                                                                        String busqueda_180 = "SELECT * FROM auxiliares WHERE idorigen=" + ctaDestino.getIdorigen() + " AND idgrupo=" + ctaDestino.getIdgrupo() + " AND idsocio=" + ctaDestino.getIdsocio() + " AND idproducto=180";
                                                                                        Query query_180 = em.createNativeQuery(busqueda_180, Auxiliares.class);
                                                                                        Auxiliares a_180 = (Auxiliares) query_180.getSingleResult();
                                                                                        if (a_180 != null) {
                                                                                            Bandera_180 = true;
                                                                                        }
                                                                                    } catch (Exception e) {
                                                                                        Bandera_180 = false;
                                                                                    }

                                                                                    if (!Bandera_180) {
                                                                                        message = message + " VALIDADO CON EXITO";
                                                                                    } else {
                                                                                        message = "VERIFIQUE EL GRUPO DE SOCIO";
                                                                                    }
                                                                                } else {
                                                                                    message = "MONTO EN UDIS TRASPASA AL PERMITIDO POR MES";
                                                                                }

                                                                            } else {
                                                                                message = "EL MONTO DIARIO PERMITIDO TRASPASA AL PERMITIDO";
                                                                            }
                                                                        }
                                                                    } else {//el resto                                                                        
                                                                        //tb_precio_udi en el periodo
                                                                        //Obtengo la fecha de trabajo
                                                                        /*Query query_fecha_trabajo = em.createNativeQuery("SELECT to_char(fechatrabajo,'yyyy/mm/dd') FROM origenes limit 1");

                                                                        String fecha_trabajo = String.valueOf(query_fecha_trabajo.getSingleResult());
                                                                        Tablas tb_precio_udi_periodo = util2.busquedaTabla(em, "valor_udi", fecha_trabajo.substring(0, 7).replace("\\/", ""));
                                                                        Tablas tb_udis_permitido_tercero = util2.busquedaTabla(em, "bankingly_banca_movil","max_udis_por_transferencia");//fecha_trabajo.substring(0, 7).replace("\\/", ""));
                                                                        if((monto * Double.parseDouble(tb_precio_udi_periodo.getDato1())) <= Double.parseDouble(tb_udis_permitido_tercero.getDato1())){
                                                                         */ message = message + " VALIDADO CON EXITO";
                                                                        /*}else{
                                                                          message = "El monto de su transferencia traspasa el permitido diario";  
                                                                        } */
                                                                    }

                                                                } else {
                                                                    message = "PRODUCTO NO CONFIGURADO PARA RECIBIR DEPOSITOS";
                                                                }
                                                            } else {
                                                                message = "GRUPO NO CONFIGURADO PARA DEPOSITOS";
                                                            }
                                                        } else {
                                                            message = "SOCIO NO PERTENECE AL GRUPO DE RETIRO";
                                                        }
                                                        ///Terminan validaciones para CSN
                                                    } else {
                                                        message = "VALIDADO CON EXITO";
                                                    }
                                                    /* } else {
                                                            message = "MONTO TRASPASA EL PERMITIDO DIARIO";
                                                        }
                                                    } else {
                                                        message = "EL SALDO QUE INTENTA TRANSFERIR ES " + minMax(monto).toUpperCase() + " AL PERMITIDO";
                                                    }*/
                                                } else {
                                                    message = "PRODUCTO DESTINO SOLO ACEPTA PAGOS(PRESTAMO)";
                                                }
                                            } else {
                                                message = "EL TIPO DE TRANSFERENCIA ES A TERCEROS PERO TU CUENTA DESTINO PERTENECE AL MISMO SOCIO";
                                            }

                                        } else {
                                            message = "PRODUCTO DESTINO NO OPERA PARA BANCA MOVIL";
                                        }
                                    } else {
                                        message = "PRODUCTO DESTINO ESTA INACTIVO";
                                    }

                                } else {
                                    message = "PRODUCTO DESTINO NO EXISTE";
                                }
                            } else {
                                message = "FONDOS INSUFICIENTES PARA COMPLETAR LA TRANSACCION";
                            }
                        } else {
                            message = "PRODUCTO ORIGEN INACTIVO";
                        }

                    } else {
                        message = "PRODUCTO ORIGEN NO PERMITE SOBRECARGOS";
                    }
                } else {
                    message = "PRODUCTO ORIGEN NO OPERA PARA BANCA MOVIL";
                }
            } else {
                message = "PRODUCTO OrigEN no pertenece al socio:" + clientBankIdentifier;
            }
        } catch (Exception e) {
            message = e.getMessage();
            System.out.println("Errro al validar transferencia a terceros:" + e.getMessage());
            return message;
        } finally {
            em.close();
        }
        return message.toUpperCase();
    }
    //Metodo para validar pago a prestamos

    public String validarPagoAPrestamos(int identificadorTr, String opaOrigen, Double monto, String opaDestino, String clientBankIdentifier) {
        EntityManager em = AbstractFacade.conexion();
        OpaDTO opaO = util.opa(opaOrigen);
        OpaDTO opaD = util.opa(opaDestino);
        OgsDTO ogs = util.ogs(clientBankIdentifier);

        boolean identificador_prestamo_propio = false;
        boolean identificador_prestamo_tercero = false;
        boolean validador_cuentas_destino = false;
        boolean bDestino = false;
        boolean banderaProductosDeposito = false;
        boolean banderaGrupo = false;
        Auxiliares ctaOrigen = null;
        Productos productoDestino = null;

        String cuentaOrigen = "SELECT * FROM auxiliares a WHERE idorigenp=" + opaO.getIdorigenp() + " AND idproducto=" + opaO.getIdproducto() + " AND idauxiliar=" + opaO.getIdauxiliar()
                + " AND  idorigen=" + ogs.getIdorigen() + " AND idgrupo=" + ogs.getIdgrupo() + " AND idsocio=" + ogs.getIdsocio();

        String cuentaDestino = "";
        if (identificadorTr == 3) {
            cuentaDestino = "SELECT * FROM auxiliares a WHERE idorigenp=" + opaD.getIdorigenp() + " AND idproducto=" + opaD.getIdproducto() + " AND idauxiliar=" + opaD.getIdauxiliar();
        } else if (identificadorTr == 4) {
            cuentaDestino = "SELECT * FROM auxiliares a WHERE idorigenp=" + opaD.getIdorigenp() + " AND idproducto=" + opaD.getIdproducto() + " AND idauxiliar=" + opaD.getIdauxiliar();
        }
        String message = "";
        try {
            boolean bOrigen = false;
            try {
                Query query = em.createNativeQuery(cuentaOrigen, Auxiliares.class);
                //Obtengo el producto origen
                ctaOrigen = (Auxiliares) query.getSingleResult();
                bOrigen = true;
            } catch (Exception e) {
                System.out.println("Error cuando se intento validar el origen");
            }
            if (bOrigen) {

                Double saldo = Double.parseDouble(ctaOrigen.getSaldo().toString());
                System.out.println("Saldo:" + saldo);
                if (util2.obtenerOrigen(em) == 30200) {
                    Tablas tablaProductoTDD = new TarjetaDeDebito().productoTddwebservice(em);
                    //Si el pago del prestamo se esta haciendo desde la TDD
                    if (ctaOrigen.getAuxiliaresPK().getIdproducto() == Integer.parseInt(tablaProductoTDD.getDato1())) {
                        WsSiscoopFoliosTarjetasPK1 foliosPK = new WsSiscoopFoliosTarjetasPK1(ctaOrigen.getAuxiliaresPK().getIdorigenp(), ctaOrigen.getAuxiliaresPK().getIdproducto(), ctaOrigen.getAuxiliaresPK().getIdauxiliar());
                        WsSiscoopFoliosTarjetas1 tarjetas = new TarjetaDeDebito().buscaTarjetaTDD(foliosPK.getIdorigenp(), foliosPK.getIdproducto(), foliosPK.getIdauxiliar(), em);
                        try {
                            System.out.println("consultando saldo para idtarjeta:" + tarjetas.getIdtarjeta());
                            BalanceQueryResponseDto saldoTDD = new TarjetaDeDebito().saldoTDD(tarjetas.getWsSiscoopFoliosTarjetasPK());
                            message = "TDD";
                            saldo = saldoTDD.getAvailableAmount();

                        } catch (Exception e) {
                            System.out.println("Error al buscar saldo de TDD:" + ctaOrigen.getAuxiliaresPK().getIdproducto());
                            return message = "ERROR AL CONSUMIR WS TDD Y OBTENER SALDO DEL PRODUCTO";

                        }
                    }

                }
                //Valido que el producto origen se trabaje en banca movil
                Productos_bankingly cuentasBankingly = em.find(Productos_bankingly.class, ctaOrigen.getAuxiliaresPK().getIdproducto());
                if (cuentasBankingly != null) {
                    //Busco descripcion del idproducto origen
                    Productos prOrigen = em.find(Productos.class, ctaOrigen.getAuxiliaresPK().getIdproducto());
                    //si el producto no es un prestamo            
                    if (prOrigen.getTipoproducto() == 0) {
                        //Verifico el estatus de la cuenta origen
                        if (ctaOrigen.getEstatus() == 2) {
                            //verifico que el saldo del producto origen es mayor o igual a lo que se intenta transferir
                            if (saldo >= monto) {
                                Auxiliares ctaDestino = null;
                                try {
                                    //Busco la cuenta destino
                                    Query queryDestino = em.createNativeQuery(cuentaDestino, Auxiliares.class);
                                    ctaDestino = (Auxiliares) queryDestino.getSingleResult();
                                    System.out.println("opaD:" + opaD.getIdproducto());
                                    productoDestino = em.find(Productos.class, opaD.getIdproducto());
                                    bDestino = true;
                                } catch (Exception e) {
                                    System.out.println("Error al buscar cuenta destino:" + e.getMessage());
                                }

                                if (bDestino) {
                                    //Busco el producto destino
                                    productoDestino = em.find(Productos.class, opaD.getIdproducto());
                                    //Valido que la cuenta destino este activa
                                    if (ctaDestino.getEstatus() == 2) {
                                        //Valido que cuenta destino pertenezca al mismo socio
                                        if (identificadorTr == 3) {
                                            if (ctaOrigen.getIdorigen() == ctaDestino.getIdorigen() && ctaOrigen.getIdgrupo() == ctaDestino.getIdgrupo() && ctaOrigen.getIdsocio() == ctaDestino.getIdsocio()) {
                                                identificador_prestamo_propio = true;
                                            } else {
                                                message = "CUENTA DESTINO NO PERTENECE AL MISMO SOCIO";
                                            }
                                        } else if (identificadorTr == 4) {
                                            if (ctaOrigen.getIdorigen() == ctaDestino.getIdorigen() && ctaOrigen.getIdgrupo() == ctaDestino.getIdgrupo() && ctaOrigen.getIdsocio() == ctaDestino.getIdsocio()) {
                                                message = "TU TIPO DE PAGO SE IDENTIFICA COMO TERCERO PERO LA CUENTA DESTINO PERTENECE AL MISMO SOCIO";
                                            } else {
                                                identificador_prestamo_tercero = true;
                                            }
                                        }

                                        if (identificador_prestamo_propio || identificador_prestamo_tercero) {
                                            validador_cuentas_destino = true;
                                        }

                                        if (validador_cuentas_destino) {
                                            //Valido que el producto destino tercero sea un prestamo
                                            if (productoDestino.getTipoproducto() == 2) {

                                                //valido el minimo o maximo para banca movil
                                                /*if (minMax(monto).toUpperCase().contains("VALIDO")) {
                                                    //Valido el monto maximo por dia
                                                    if (MaxPordia(opaOrigen, monto)) {
                                                 */
                                                Origenes origenMatriz = util2.busquedaMatriz();
                                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                                                String fecha_intento_liquidar = sdf.format(origenMatriz.getFechatrabajo());
                                                String sql_monto_liquidar = "SELECT monto_para_liquidar_prestamo(" + opaD.getIdorigenp() + ","
                                                        + opaD.getIdproducto() + ","
                                                        + opaD.getIdauxiliar() + ","
                                                        + "'" + fecha_intento_liquidar + "')";
                                                Query query_monto_liquidacion = em.createNativeQuery(sql_monto_liquidar);
                                                double monto_liquidacion = Double.parseDouble(String.valueOf(query_monto_liquidacion.getSingleResult()));
                                                System.out.println("El monto de liquidacion es :" + monto_liquidacion);
                                                if (monto <= monto_liquidacion) {
                                                    if (monto > 0) {
                                                        /*=======================REGLAS DE NEGOCIO==================================*/
                                                        //Valido que la cuenta origen para CSN esta un grupo de retiro configurado
                                                        if (util2.obtenerOrigen(em) == 30200) {
                                                            System.out.println("entro a csn");
                                                            //Buscamos que el producto origen pertenezca al grupo de retiro
                                                            Tablas tb = util2.busquedaTabla(em, "bankingly_banca_movil", "grupo_retiro");
                                                            if (ctaOrigen.getIdgrupo() == Integer.parseInt(tb.getDato1())) {
                                                                //Ahora verifico que el destino perteneneza al grupo de depositos
                                                                Tablas tbRetiro = util2.busquedaTabla(em, "bankingly_banca_movil", "grupo_deposito");
                                                                String cadena[] = tbRetiro.getDato1().split("\\|");
                                                                List list = Arrays.asList(cadena);
                                                                for (int i = 0; i < list.size(); i++) {
                                                                    if (ctaOrigen.getIdgrupo() == Integer.parseInt(String.valueOf(list.get(i)))) {
                                                                        banderaGrupo = true;
                                                                    }
                                                                }
                                                                if (banderaGrupo) {
                                                                    //valido que el producto acepte depositos
                                                                    Tablas tbDeposito = util2.busquedaTabla(em, "bankingly_banca_movil", "productos_deposito");

                                                                    String productos_deposito[] = tbDeposito.getDato2().split("\\|");
                                                                    List list_deposito = Arrays.asList(productos_deposito);
                                                                    for (int i = 0; i < list_deposito.size(); i++) {
                                                                        if (ctaDestino.getAuxiliaresPK().getIdproducto() == Integer.parseInt(String.valueOf(list_deposito.get(i)))) {
                                                                            banderaProductosDeposito = true;
                                                                        }
                                                                    }
                                                                    if (banderaProductosDeposito) {

                                                                        String fechaTrabajo = "SELECT date(fechatrabajo) FROM origenes limit 1";
                                                                        Query queryOrigenes = em.createNativeQuery(fechaTrabajo);
                                                                        String fechaTrabajoReal = String.valueOf(queryOrigenes.getSingleResult());
                                                                        String fecha[] = fechaTrabajoReal.split("-");
                                                                        LocalDate date = LocalDate.of(Integer.parseInt(fecha[0]), Integer.parseInt(fecha[1]), Integer.parseInt(fecha[2]));

                                                                        String sai_aux_cartera = "SELECT sai_auxiliar(" + ctaDestino.getAuxiliaresPK().getIdorigenp() + "," + ctaDestino.getAuxiliaresPK().getIdproducto() + "," + ctaDestino.getAuxiliaresPK().getIdauxiliar() + ",'" + date + "')";
                                                                        Query RsSai = em.createNativeQuery(sai_aux_cartera);
                                                                        String sai_aux = RsSai.getSingleResult().toString();
                                                                        String[] parts = sai_aux.split("\\|");
                                                                        List list_sai = Arrays.asList(parts);

                                                                        String cartera = list_sai.get(13).toString(); //String.valueOf(query_cartera.getSingleResult());
                                                                        // if (cartera.toUpperCase().equals("M") || cartera.toUpperCase().contains("V")) {
                                                                        //   message = "ESTATUS DE PRODUCTO:" + cartera;
                                                                        //} else {
                                                                        //Corro la funcion para verificar si es paralelo o no 
                                                                        String prestamo_paralelo_str = "SELECT primero_debe_pagar_prestamo_paralelo(" + opaD.getIdorigenp() + "," + opaD.getIdproducto() + "," + opaD.getIdauxiliar() + ")";
                                                                        String mensaje_paralelo = "";
                                                                        try {
                                                                            Query query_prestamo_paralelo = em.createNativeQuery(prestamo_paralelo_str);
                                                                            mensaje_paralelo = String.valueOf(query_prestamo_paralelo.getSingleResult());

                                                                        } catch (Exception e) {
                                                                        }

                                                                        if (!mensaje_paralelo.equals("")) {
                                                                            //Entra si es hipotecario
                                                                            if (ctaDestino.getTipoamortizacion() == 5) {
                                                                                String si_se_puede_aplicar = "select sai_bankingly_limite_adelanto (" + ctaDestino.getAuxiliaresPK().getIdorigenp() + ","
                                                                                        + "" + ctaDestino.getAuxiliaresPK().getIdproducto() + ","
                                                                                        + "" + ctaDestino.getAuxiliaresPK().getIdauxiliar() + ","
                                                                                        + "(SELECT date(fechatrabajo) FROM origenes limit 1),"
                                                                                        + monto + ",NULL)";
                                                                                Query query_se_puede_aplicar = em.createNativeQuery(si_se_puede_aplicar);
                                                                                Double se_puede_pagar = Double.parseDouble(String.valueOf(query_se_puede_aplicar.getSingleResult()));

                                                                                if (monto >= se_puede_pagar) {
                                                                                    message = message + "VALIDADO CON EXITO";
                                                                                } else {
                                                                                    message = "PUEDES ADELANTAR :" + se_puede_pagar;
                                                                                }
                                                                            } else {
                                                                                message = message + " VALIDADO CON EXITO";
                                                                            }
                                                                        } else {
                                                                            message = mensaje_paralelo;
                                                                        }
                                                                        // }
                                                                    } else {
                                                                        message = "PRODUCTO NO CONFIGURADO PARA RECIBIR DEPOSITOS";
                                                                    }
                                                                } else {
                                                                    message = "GRUPO NO CONFIGURADO PARA RECIBIR DEPOSITOS";
                                                                }
                                                            } else {
                                                                message = "SOCIO NO PERTENECE AL GRUPO DE RETIRO";
                                                            }

                                                        } else {
                                                            message = "VALIDADO CON EXITO";
                                                        }
                                                    } else {
                                                        message = "TU PAGO DEBE SER MAYOR A CERO";
                                                    }
                                                } else {
                                                    message = "EL SALDO QUE INTENTA PAGAR SOBREPASA A LA DEUDA";
                                                }

                                            } else {
                                                message = "PRODUCTO DESTINO NO ES UN PRESTAMO";
                                            }
                                        } else {
                                            message = message;
                                        }

                                    } else {
                                        message = "PRODUCTO DESTINO ESTA INACTIVO";
                                    }
                                } else {
                                    message = "NO SE ENCONTRO PRODUCTO DESTINO";
                                }
                            } else {
                                message = "FONDOS INSUFICIENTES PARA COMPLETAR LA TRANSACCION";
                            }
                        } else {
                            message = "PRODUCTO ORIGEN INACTIVO";
                        }

                    } else {
                        message = "PRODUCTO ORIGEN NO PERMITE SOBRECARGOS";
                    }
                } else {
                    message = "PRODUCTO ORIGEN NO OPERA PARA BANCA MOVIL";
                }
            } else {
                message = "PRODUCTO ORIGEN NO PERTENECE AL SOCIO:" + clientBankIdentifier;
            }
        } catch (Exception e) {
            message = e.getMessage();
            System.out.println("Error al realizar pago a prestamo:" + e.getMessage());
            return message;
        } finally {
            em.close();
        }
        System.out.println("el mensaje es:" + message);

        return message.toUpperCase();
    }
    //Metodo para validar pago orden SPEI

    public String validaOrdenSPEI(RequestDataOrdenPagoDTO orden /*@Context UriInfo ui*/) throws MalformedURLException {
        System.out.println("Entrando a validar las ordenes SPEI");
        EntityManager em = AbstractFacade.conexion();
        OpaDTO opa = util.opa(orden.getClienteClabe());
        OgsDTO ogs = util.ogs(orden.getOrdernante());
        String folio_origen_spei = "SELECT * FROM auxiliares a WHERE idorigenp=" + opa.getIdorigenp() + " AND idproducto=" + opa.getIdproducto() + " AND idauxiliar=" + opa.getIdauxiliar() + " AND estatus=2";
        System.out.println("Consulta de validaciones de cuenta :" + folio_origen_spei);
        String message = "";
        ResponseSPEIDTO dtoSPEI = new ResponseSPEIDTO();
        TablasPK urlTbPK = new TablasPK("bankingly_banca_movil", "speipath");

        //Busco en tablas,la tabla para url de SPEI en otro proyecto propio de Fenoreste que conecta a STP
        Tablas tablaSpeiPath = em.find(Tablas.class, urlTbPK);
        URL url = new URL(tablaSpeiPath.getDato1());
        System.out.println("urlFenoreste:" + url);

        try {
            //Hago ping a los servicios de SPEI alojado en otro proyecto
            if (pingURL(url, tablaSpeiPath.getDato3())) {
                System.out.println("Si hay Ping a spei");
                try {
                    Auxiliares folio_origen_ = null;
                    boolean bOrigen = false;
                    try {
                        Query query = em.createNativeQuery(folio_origen_spei, Auxiliares.class);
                        //Obtengo el folio origen para tarjeta de debito
                        folio_origen_ = (Auxiliares) query.getSingleResult();
                        bOrigen = true;

                    } catch (Exception e) {
                        System.out.println("Error al buscar producto origen:" + e.getMessage());
                        bOrigen = false;
                    }
                    //Si existe el producto origen
                    if (bOrigen) {
                        double saldo = 0.0;

                        if (util2.obtenerOrigen(em) == 30200) {
                            //Buscamos el producto para tarjeta de debito
                            Tablas tablaProductoTDD = new TarjetaDeDebito().productoTddwebservice(em);
                            System.out.println("El producto para Tarjeta de debito es:" + tablaProductoTDD);

                            //Si el retiro debe ser de Tarjeta de debito
                            if (folio_origen_.getAuxiliaresPK().getIdproducto() == Integer.parseInt(tablaProductoTDD.getDato1())) {
                                WsSiscoopFoliosTarjetasPK1 foliosPK = new WsSiscoopFoliosTarjetasPK1(folio_origen_.getAuxiliaresPK().getIdorigenp(), folio_origen_.getAuxiliaresPK().getIdproducto(), folio_origen_.getAuxiliaresPK().getIdauxiliar());
                                //Busco el folio de la tarjeta de debito
                                WsSiscoopFoliosTarjetas1 tarjeta = new TarjetaDeDebito().buscaTarjetaTDD(foliosPK.getIdorigenp(), foliosPK.getIdproducto(), foliosPK.getIdauxiliar(), em);
                                try {
                                    System.out.println("consultando saldo para idtarjeta:" + tarjeta.getIdtarjeta());
                                    //Obtengo el saldo desde ws de el idTarjeta encontrada
                                    BalanceQueryResponseDto saldo_tarjeta_de_debito = new TarjetaDeDebito().saldoTDD(tarjeta.getWsSiscoopFoliosTarjetasPK());
                                    message = "TDD";
                                    saldo = saldo_tarjeta_de_debito.getAvailableAmount();
                                    System.out.println("Saldo de la tarjeta de debito es:" + saldo);
                                } catch (Exception e) {
                                    System.out.println("Error al buscar saldo de TDD:" + folio_origen_.getAuxiliaresPK().getIdproducto());
                                    return message = "ERROR AL CONSUMIR WS TDD Y OBTENER SALDO DEL PRODUCTO";
                                }
                            }

                        }
                        //Valido que el producto de tarjeta de debito si deba operar para banca movil
                        Productos_bankingly cuentasBankingly = em.find(Productos_bankingly.class, folio_origen_.getAuxiliaresPK().getIdproducto());
                        if (cuentasBankingly != null) {
                            //Busco el producto del folio de tarjeta de debito en la tabla productos
                            Productos prOrigen = em.find(Productos.class, folio_origen_.getAuxiliaresPK().getIdproducto());
                            //si el producto no es tipo 2 es decir no es un prestamo            
                            if (prOrigen.getTipoproducto() == 0) {
                                //verifico que el socio le pertenezca ese producto
                                if (folio_origen_.getIdorigen() == ogs.getIdorigen() && folio_origen_.getIdgrupo() == ogs.getIdgrupo() && folio_origen_.getIdsocio() == ogs.getIdsocio()) {
                                    //Verifico el estatus de la cuenta origen
                                    if (folio_origen_.getEstatus() == 2) {
                                        //verifico que el saldo del producto origen es mayor o igual a lo que se intenta transferir(saldo obtenido desde WS de Alestra)
                                        //Sumamos el moto de comision+el monto a enviar
                                        //Buscamos el registro en tablas
                                        //Tablas tb_comision_spei = util2.busquedaTabla(em, "bankingly_banca_movil", "cuenta_spei_comisiones");
                                        double total_pago = orden.getMonto();//+ Double.parseDouble(tb_comision_spei.getDato2()) + ((Double.parseDouble(tb_comision_spei.getDato2())) * 0.16);//El monto de comision por el IVA
                                        if (saldo >= total_pago) {
                                            if (util2.obtenerOrigen(em) == 30200) {
                                                //Buscamos que el producto origen pertenezca al grupo de retiro
                                                Tablas tb = util2.busquedaTabla(em, "bankingly_banca_movil", "grupo_retiro");
                                                if (folio_origen_.getIdgrupo() == Integer.parseInt(tb.getDato1())) {
                                                    Tablas tb_minimo = util2.busquedaTabla(em, "bankingly_banca_movil", "spei_monto_minimo");
                                                    if (orden.getMonto() >= Integer.parseInt(tb_minimo.getDato1())) {
                                                        Tablas tb_maximo = util2.busquedaTabla(em, "bankingly_banca_movil", "spei_monto_maximo");
                                                        if (orden.getMonto() <= Integer.parseInt(tb_maximo.getDato1())) {
                                                            message = "VALIDADO CON EXITO";
                                                        } else {
                                                            message = "EL MONTO MAXIMO PERMITIDO ES :" + tb_maximo.getDato1();
                                                        }
                                                    } else {
                                                        message = "EL MONTO MINIMO PERMITIDO ES :" + tb_minimo.getDato1();
                                                    }
                                                } else {
                                                    message = "SOCIO NO PERTENECE AL GRUPO DE RETIRO";
                                                }
                                            } else {
                                                message = "VALIDADO CON EXITO";
                                            }
                                        } else {
                                            message = "FONDOS INSUFICIENTES PARA COMPLETAR LA TRANSACCION";
                                            dtoSPEI.setId(0);
                                            dtoSPEI.setError(message);
                                        }
                                    } else {
                                        message = "PRODUCTO ORIGEN INACTIVO";
                                        dtoSPEI.setId(0);
                                        dtoSPEI.setError(message);
                                    }
                                } else {
                                    message = "PRODUCTO ORIGEN NO PERTENECE AL SOCIO:" + orden.getOrdernante();

                                }

                            } else {
                                message = "PRODUCTO ORIGEN NO PERMITE SOBRECARGOS";

                            }
                        } else {
                            message = "PRODUCTO ORIGEN NO OPERA PARA BANCA MOVIL";

                        }
                    } else {
                        message = "NO SE ENCONTRO PRODUCTO ORIGEN";

                    }
                } catch (Exception e) {
                    System.out.println("Error al validar orden SPEI:" + e.getMessage());
                }
            } else {
                message = "SIN CONEXION AL HOST DESTINO:" + url;
            }

        } catch (Exception e) {
            message = e.getMessage();
            System.out.println("Error al realizar tranferenciasSPEI:" + e.getMessage());
            return message;
        } finally {
            em.close();
        }
        return message.toUpperCase();
    }

    //Se consume un servicio que yo desarrolle donde consumo API STP en caso de CSN si alguuien mas usara SPEI desarrollaria otro proyecto especficamente para la caja
    private ResponseSPEIDTO metodoEnviarSPEI(RequestDataOrdenPagoDTO orden) {

        URL urlEndpoint = null;
        String output = "";
        String salida = "";
        ResponseSPEIDTO response = new ResponseSPEIDTO();
        EntityManager em = AbstractFacade.conexion();
        try {
            org.json.JSONObject request = new JSONObject();
            //Preparo JSON para request en mi propio servicio
            request.put("clabeCliente", orden.getClienteClabe());
            request.put("monto", orden.getMonto());
            request.put("conceptoPago", orden.getConceptoPago());
            request.put("banco", orden.getInstitucionContraparte());
            request.put("beneficiario", orden.getNombreBeneficiario());
            request.put("rfcCurpBeneficiario", orden.getRfcCurpBeneficiario());
            request.put("cuentaBeneficiario", orden.getCuentaBeneficiario());

            //Busco la tabla para el proyecto SPEI 
            TablasPK urlTablaPK = new TablasPK("bankingly_banca_movil", "speipath");
            Tablas tablaSpeiPath = em.find(Tablas.class, urlTablaPK);
            //Obtengo los datos de la tabla para generar la URL de conexion
            URL url = new URL(tablaSpeiPath.getDato1() + tablaSpeiPath.getDato2());
            //Una ves generada la url contacteno el parametro para enviar orden(srvEnviarOrden)
            urlEndpoint = new URL(url + "srvEnviaOrden");
            //Se genera la conexion
            HttpURLConnection conn = (HttpURLConnection) urlEndpoint.openConnection();
            conn.setDoOutput(true);
            //El metodo que utilizo
            conn.setRequestMethod("POST");
            //Tipo de contenido aceptado por el WS
            conn.setRequestProperty("Content-Type", "application/json");
            //Obtengo el Stream
            OutputStream os = conn.getOutputStream();
            //Al stream le paso el request
            os.write(request.toString().getBytes());
            os.flush();

            JSONObject responseJSON = new JSONObject();
            //Obtengo el codigo de respuesta
            int codigoHTTP = conn.getResponseCode();

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            System.out.println("Output from Server .... \n");
            System.out.println("El codigo de respuesta es:" + codigoHTTP);
            if (codigoHTTP == 200) {
                while ((output = br.readLine()) != null) {
                    salida = output;
                    responseJSON = new JSONObject(salida);
                    response.setId(responseJSON.getInt("id"));
                    response.setError(responseJSON.getString("error"));
                }
            } else {
                while ((output = br.readLine()) != null) {
                    salida = output;
                    responseJSON = new JSONObject(salida);
                    response.setId(responseJSON.getInt("id"));
                    response.setError(responseJSON.getString("error"));
                }
            }
            conn.disconnect();
        } catch (Exception ex) {
            response.setId(-50);
            System.out.println("Error en conectar al EndPoint SPEI:" + ex.getMessage());
            response.setError("ERROR AL ENVIAR LA ORDEN SPEI");
            return response;
        } finally {
            em.close();
        }
        return response;
    }

    public String ejecutaOrdenSPEI(int idorden, String folio, String estado, String causadevolucion) {
        EntityManager em = AbstractFacade.conexion();
        Procesa_pago_movimientos pago = null;
        String mensaje = "";
        String cliente = "";
        boolean cancela = false;
        try {
            //Lo utlizon para los datos a procesar 
            long time = System.currentTimeMillis();
            Timestamp timestamp = new Timestamp(time);
            //Obtengo la sesion para los datos a procesar
            Query sesion = em.createNativeQuery("select text(pg_backend_pid())||'-'||trim(to_char(now(),'ddmmyy'))");
            String sesionc = String.valueOf(sesion.getSingleResult());

            //Obtengo un random que uso en conplemento con referencia
            int rn = (int) (Math.random() * 999999 + 1);

            //Obtener HH:mm:ss.microsegundos
            String fechaArray[] = timestamp.toString().substring(0, 10).split("-");
            String fReal = fechaArray[2] + "/" + fechaArray[1] + "/" + fechaArray[0];
            String referencia = String.valueOf(rn) + "" + String.valueOf(3) + String.valueOf(1) + "" + fReal.replace("/", "");
            int rechazo = 0;
            int a_rechazar = 0;
            boolean existe = false;
            boolean rechazos = false;

            try {
                String busqueda = "SELECT count(*) FROM bankingly_movimientos_spei WHERE idorden_spei=" + idorden;
                Query query_cancelados_ = em.createNativeQuery(busqueda);
                a_rechazar = Integer.parseInt(String.valueOf(query_cancelados_.getSingleResult()));
                if (a_rechazar > 0) {
                    existe = true;
                }
            } catch (Exception e) {
                System.out.println("Error al buscar registros de spei para rechazar:" + e.getMessage());
            }

            if (existe) {
                if (!estado.toUpperCase().contains("LIQUIDA")) {
                    //Extraigo registros de bankingly_movimientos_spei para moverlos a bankingly_movimientos_ca(Todo lo realiza la funcion)
                    String consulta_retroceso_spei = "SELECT sai_bankingly_spei_rechazado(" + idorden + ",'" + sesionc + "','" + referencia + "')";
                    Query query_retroceso = em.createNativeQuery(consulta_retroceso_spei);
                    rechazo = Integer.parseInt(String.valueOf(query_retroceso.getSingleResult()));
                    if (rechazo > 0) {//Si se movio el registro de manera exitosa
                        rechazos = true;
                    } else {
                        mensaje = "NO SE PUDIERON PREPARAR LOS REGISTROS A RECHAZAR";
                    }
                } else {//Si ya se aplico solo limpio registros
                    em.getTransaction().begin();
                    //limpio registros
                    em.createNativeQuery("DELETE FROM bankingly_movimientos_spei WHERE idorden_spei=" + idorden).executeUpdate();
                    em.getTransaction().commit();
                }
            } else {
                mensaje = "ORDEN NO EXISTE";
            }

            if (rechazos) {
                String consulta_lista_objetos = "SELECT * FROM bankingly_movimientos_ca WHERE idorden_spei=" + idorden + " AND cargoabono=1 LIMIT 1";
                Query query_lista_objetos = em.createNativeQuery(consulta_lista_objetos, Procesa_pago_movimientos.class);
                pago = (Procesa_pago_movimientos) query_lista_objetos.getSingleResult();
                //Solo consulto 1 registro de los 3 que se generan porque solo saco el ogs
                cliente = String.format("%06d", pago.getIdorigen()) + "" + String.format("%02d", pago.getIdgrupo()) + String.format("%06d", pago.getIdsocio());
                cancela = true;
            }

        } catch (Exception e) {
            System.out.println("Error que se produjo al intentar actualizar estatus de orden:" + idorden + " es:" + e.getMessage());
        }

        PreparaSMS envio_sms = new PreparaSMS();
        if (cancela) {
            try {
                if (!estado.toUpperCase().contains("LIQUIDA")) {
                    WsSiscoopFoliosTarjetas1 tarjeta_origen = new TarjetaDeDebito().buscaTarjetaTDD(pago.getAuxiliaresPK().getIdorigenp(), pago.getAuxiliaresPK().getIdproducto(), pago.getAuxiliaresPK().getIdauxiliar(), em);
                    int total_procesados = 0;
                    //Ajustamos el saldo de la TDD origen lo que se ya se retiro
                    System.out.println("Ajuste por:" + pago.getMonto());
                    boolean bandera_deposito = new TarjetaDeDebito().depositoTDD(tarjeta_origen, pago.getMonto());
                    //bandera_deposito=true;
                    if (bandera_deposito) {
                        try {
                            String consulta_datos_procesar = "SELECT sai_bankingly_aplica_transaccion('" + pago.getFecha().toString().substring(0, 10) + "'," + pago.getIdusuario() + ",'" + pago.getSesion() + "','" + pago.getReferencia() + "')";
                            Query procesa_movimiento = em.createNativeQuery(consulta_datos_procesar);
                            total_procesados = Integer.parseInt(String.valueOf(procesa_movimiento.getSingleResult()));
                        } catch (Exception e) {
                            System.out.println("Error al aplicar la transaccion en saicoop:"+e.getMessage());
                        }

                        if (total_procesados > 0) {
                            String consulta_termina_transaccion = "SELECT sai_bankingly_termina_transaccion('" + pago.getFecha().toString().substring(0, 10) + "'," + pago.getIdusuario() + ",'" + pago.getSesion() + "','" + pago.getReferencia() + "')";
                            Query termina_transaccion = em.createNativeQuery(consulta_termina_transaccion);

                            int registros_limpiados = Integer.parseInt(String.valueOf(termina_transaccion.getSingleResult()));
                            System.out.println("Registros Limpiados con exito:" + registros_limpiados);

                            em.getTransaction().begin();
                            //limpio registros
                            em.createNativeQuery("DELETE FROM bankingly_movimientos_spei WHERE idorden_spei=" + idorden).executeUpdate();
                            em.getTransaction().commit();

                            //Enviamos datos a preparar el sms indicando que debe obtener datos de mensaje a cuenta propia
                            mensaje = "recibido";
                            String envio_ok_sms = envio_sms.enviaSMSOrdenSpei(em, cliente, idorden, folio, estado, causadevolucion);
                        } else {
                            System.out.println("Falo el al generar poliza en csn pero fue recibido");
                            mensaje = "recibido";
                        }

                    } else {
                        mensaje = "Error al procesar el ajuste por ws Alestra tdd";//NO SE PUDO DEPOSITAR A LA TARJETA DE DEBITO";
                        em.getTransaction().begin();
                            //limpio registros
                        em.createNativeQuery("DELETE FROM bankingly_movimientos_ca WHERE idorden_spei=" + idorden).executeUpdate();
                        em.getTransaction().commit();
                    }

                }

            } catch (Exception e) {
                System.out.println("Error al procesar salida SPEI :" + e.getMessage());
                mensaje = e.getMessage();
            }
            

        }
        return mensaje.toLowerCase();

    }

    //valida el monto para banca movil total de transferencia
    public String minMax(Double amount) {
        EntityManager em = AbstractFacade.conexion();
        String mensaje = "";
        try {
            TablasPK tbPk = new TablasPK("bankingly_banca_movil", "montomaximominimo");
            Tablas tb = em.find(Tablas.class,
                    tbPk);
            if (amount > Double.parseDouble(tb.getDato1())) {
                mensaje = "MAYOR";
            } else if (amount < Double.parseDouble(tb.getDato2())) {
                mensaje = "MENOR";
            } else {
                mensaje = "VALIDO";
            }
        } catch (Exception e) {

            System.out.println("Error al validar monto min-max:" + e.getMessage());
        } finally {
            em.close();
        }
        return mensaje;
    }

    // REALIZA UN PING A LA URL DEL WSDL
    private boolean pingURL(URL url, String tiempo) {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(Integer.parseInt(tiempo));
            connection.setReadTimeout(Integer.parseInt(tiempo));
            int codigo = connection.getResponseCode();
            if (codigo == 200) {
                return true;
            }
        } catch (IOException ex) {
            System.out.println("Error al conectarse a URL SPEI: " + ex.getMessage());
        }
        return false;
    }

    public boolean actividad_horario() {
        EntityManager em = AbstractFacade.conexion();
        boolean bandera_ = false;
        try {
            if (util2.actividad(em)) {
                bandera_ = true;
            }
        } catch (Exception e) {
            System.out.println("Error al verificar el horario de actividad");
        } finally {
            em.close();
        }

        return bandera_;
    }

    public boolean actividad_horario_spei() {
        EntityManager em = AbstractFacade.conexion();
        boolean bandera_ = false;
        try {
            if (util2.actividad_spei(em)) {
                bandera_ = true;
            }
        } catch (Exception e) {
            System.out.println("Error al verificar el horario de actividad");
        } finally {
            em.close();
        }

        return bandera_;
    }

    //Metodo solo para CSN aplicando reglas
    public String validarTransferenciaCSN(TransactionToOwnAccountsDTO transactionOWN, int identificadorTransferencia, RequestDataOrdenPagoDTO SPEIOrden) {
        EntityManager em = AbstractFacade.conexion();
        String mensaje = "";
        try {
            System.out.println("El identificado de transferencia es:" + identificadorTransferencia);
            WsSiscoopFoliosTarjetas1 tarjeta = null;
            //Buscamos el producto para TDD en tablas 
            Tablas tablaProductoTDD = new TarjetaDeDebito().productoTddwebservice(em);
            System.out.println("Producto_para_tarjeta_de_debito:" + tablaProductoTDD.getDato1());
            //Busco el producto configurado para retiros
            Tablas tabla_retiro = util2.busquedaTabla(em, "bankingly_banca_movil", "producto_retiro");
            System.out.println("Producto para retiro es:" + tabla_retiro.getDato1());
            //Bandera que me sirve para decir si existe o no la tdd
            boolean tddEncontrada = false;

            OpaDTO opaOrigen = util.opa(transactionOWN.getDebitProductBankIdentifier());
            AuxiliaresPK auxpk = new AuxiliaresPK(opaOrigen.getIdorigenp(), opaOrigen.getIdproducto(), opaOrigen.getIdauxiliar());
            Auxiliares a = em.find(Auxiliares.class, auxpk);
            //Si el producto configurado para retiros no es la tdd entra aqui
            if (opaOrigen.getIdproducto() == Integer.parseInt(tabla_retiro.getDato1())) {
                try {
                    //Si la tdd es el producto conffgurado para              retiros
                    if (Integer.parseInt(tablaProductoTDD.getDato1()) == Integer.parseInt(tabla_retiro.getDato1())) {
                        //Verifico que la activar tdd este en 1--activado
                        Tablas activa_tdd = util2.busquedaTabla(em, "bankingly_banca_movil", "activa_tdd");
                        if (Integer.parseInt(activa_tdd.getDato2()) == 1) {
                            try {
                                //Buscando la tarjeta de debito 
                                tarjeta = new TarjetaDeDebito().buscaTarjetaTDD(opaOrigen.getIdorigenp(), opaOrigen.getIdproducto(), opaOrigen.getIdauxiliar(), em);
                                System.out.println("Los registros para el Folio son:" + tarjeta);

                                tddEncontrada = true;
                            } catch (Exception e) {
                                System.out.println("El folio para TDD no existe");
                            }
                            //si se encontro la Tarjeta de debito
                            if (tddEncontrada) {
                                //Verifico el estatuso de la TDD
                                //Si la tarjeta esta activa
                                if (tarjeta.getActiva()) {
                                    //Valido segun sea el tipo de transferencia
                                    //Cuentas propias
                                    if (identificadorTransferencia == 1) {
                                        System.out.println("Es una transferencia a cuenta propia");
                                        mensaje = validarTransferenciaEntreMisCuentas(transactionOWN.getDebitProductBankIdentifier(),
                                                transactionOWN.getAmount(),
                                                transactionOWN.getCreditProductBankIdentifier(),
                                                transactionOWN.getClientBankIdentifier());
                                    }
                                    //Terceros dentro de la entidad
                                    if (identificadorTransferencia == 2) {
                                        System.out.println("Es una transferencia a tercero dentro de la entidad");
                                        Query query_fecha_trabajo = em.createNativeQuery("SELECT to_char(fechatrabajo,'yyyymmdd') FROM origenes limit 1");
                                        String fecha_trabajo = String.valueOf(query_fecha_trabajo.getSingleResult());
                                        Tablas tb_precio_udi_periodo = util2.busquedaTabla(em, "valor_udi", fecha_trabajo.substring(0, 6));
                                        Tablas tb_udis_permitido_tercero = util2.busquedaTabla(em, "bankingly_banca_movil", "max_udis_por_transferencia");//fecha_trabajo.substring(0, 7).replace("\\/", ""));
                                        if (transactionOWN.getAmount() <= (Double.parseDouble(tb_precio_udi_periodo.getDato1()) * Double.parseDouble(tb_udis_permitido_tercero.getDato1()))) {
                                            mensaje = validarTransferenciaATerceros(transactionOWN.getDebitProductBankIdentifier(),
                                                    transactionOWN.getAmount(),
                                                    transactionOWN.getCreditProductBankIdentifier(),
                                                    transactionOWN.getClientBankIdentifier());
                                        } else {
                                            mensaje = "El monto de su transferencia traspasa el permitido a transferencia tercero";
                                        }

                                    }
                                    //Pago de prestamo propio ---Falta pago de prestamo tercero
                                    if (identificadorTransferencia == 3 || identificadorTransferencia == 4) {
                                        System.out.println("Es un prestamo");
                                        if (identificadorTransferencia == 4) {
                                            Query query_fecha_trabajo = em.createNativeQuery("SELECT to_char(fechatrabajo,'yyyymmdd') FROM origenes limit 1");
                                            String fecha_trabajo = String.valueOf(query_fecha_trabajo.getSingleResult());
                                            Tablas tb_precio_udi_periodo = util2.busquedaTabla(em, "valor_udi", fecha_trabajo.substring(0, 6));
                                            Tablas tb_udis_permitido_tercero = util2.busquedaTabla(em, "bankingly_banca_movil", "max_udis_por_transferencia");//fecha_trabajo.substring(0, 7).replace("\\/", ""));
                                            if (transactionOWN.getAmount() <= (Double.parseDouble(tb_precio_udi_periodo.getDato1()) * Double.parseDouble(tb_udis_permitido_tercero.getDato1()))) {
                                                mensaje = validarPagoAPrestamos(identificadorTransferencia, transactionOWN.getDebitProductBankIdentifier(),
                                                        transactionOWN.getAmount(),
                                                        transactionOWN.getCreditProductBankIdentifier(),
                                                        transactionOWN.getClientBankIdentifier());
                                            } else {
                                                mensaje = "El monto de su transferencia traspasa el permitido a transferencia tercero";
                                            }

                                        } else {
                                            mensaje = validarPagoAPrestamos(identificadorTransferencia, transactionOWN.getDebitProductBankIdentifier(),
                                                    transactionOWN.getAmount(),
                                                    transactionOWN.getCreditProductBankIdentifier(),
                                                    transactionOWN.getClientBankIdentifier());
                                        }

                                    }
                                    if (identificadorTransferencia == 5) {//Si es una orden SPEI  
                                        Query query_fecha_trabajo = em.createNativeQuery("SELECT to_char(fechatrabajo,'yyyymmdd') FROM origenes limit 1");
                                        String fecha_trabajo = String.valueOf(query_fecha_trabajo.getSingleResult());
                                        Tablas tb_precio_udi_periodo = util2.busquedaTabla(em, "valor_udi", fecha_trabajo.substring(0, 6));
                                        Tablas tb_udis_permitido_tercero = util2.busquedaTabla(em, "bankingly_banca_movil", "max_udis_por_transferencia");//fecha_trabajo.substring(0, 7).replace("\\/", ""));
                                        if (transactionOWN.getAmount() <= (Double.parseDouble(tb_precio_udi_periodo.getDato1()) * Double.parseDouble(tb_udis_permitido_tercero.getDato1()))) {
                                            //Validaremos el monto de transaccion acumulada mensual
                                            String consulta_movs = "SELECT (CASE WHEN sum(amount::numeric) > 0 THEN sum(amount::numeric) ELSE 0 END) FROM transferencias_bankingly WHERE clientbankidentifier='" + transactionOWN.getClientBankIdentifier()
                                                    + "' AND subtransactiontypeid='3' AND transactiontypeid='1' AND to_char(fechaejecucion,'yyyymmdd')='" + fecha_trabajo + "'";
                                            double total_acumulado_mes = 0.0;
                                            double maximo_perimitido_mes = 0.0;
                                            try {
                                                Query query_calculo_monto_mensual = em.createNativeQuery(consulta_movs);
                                                Double total_en_el_mes = Double.parseDouble(String.valueOf(query_calculo_monto_mensual.getSingleResult()));
                                                //Ahora buscamos la tabla donde esta parametrizado el maximo por mes
                                                Tablas tb_maximo_mxn_mes = util2.busquedaTabla(em, "bankingly_banca_movil", "max_mxn_mensual");
                                                total_acumulado_mes = total_en_el_mes;
                                                maximo_perimitido_mes = Double.parseDouble(tb_maximo_mxn_mes.getDato1());
                                            } catch (Exception e) {
                                                System.out.println("Error al buscar maximo en el mes:" + e.getMessage());
                                            }
                                            if ((total_acumulado_mes + transactionOWN.getAmount()) <= maximo_perimitido_mes) {
                                                mensaje = validaOrdenSPEI(SPEIOrden);
                                            } else {
                                                mensaje = "El monto de su transferencia spei traspasa el permitido por mes";
                                            }
                                        } else {
                                            mensaje = "El monto de su transferencia traspasa el permitido a transferencia tercero";
                                        }

                                    }
                                } else {
                                    mensaje = "ESTATUS TARJETA DE DEBITO:INACTIVA";
                                }
                            } else {
                                mensaje = "NO EXISTE FOLIO PARA LA TARJETA DE DEBITO";
                            }
                        } else {
                            mensaje = "POR FAVOR SOLICITE QUE SE ACTIVE EL USO DE TARJETA DE DEBITO";
                        }
                    } else {//Solo para pruebas
                        //Si no TDD de donde se esta transfiriendo
                        //Valido segun sea el tipo de transferencia
                        //Cuentas propias
                        if (identificadorTransferencia == 1) {
                            mensaje = validarTransferenciaEntreMisCuentas(transactionOWN.getDebitProductBankIdentifier(),
                                    transactionOWN.getAmount(),
                                    transactionOWN.getCreditProductBankIdentifier(),
                                    transactionOWN.getClientBankIdentifier());
                        }
                        //Terceros dentro de la entidad
                        if (identificadorTransferencia == 2) {
                            System.out.println("Es una transferencia a tercero dentro de la entidad");
                            Query query_fecha_trabajo = em.createNativeQuery("SELECT to_char(fechatrabajo,'yyyymmdd') FROM origenes limit 1");

                            String fecha_trabajo = String.valueOf(query_fecha_trabajo.getSingleResult());
                            Tablas tb_precio_udi_periodo = util2.busquedaTabla(em, "valor_udi", fecha_trabajo.substring(0, 6));
                            Tablas tb_udis_permitido_tercero = util2.busquedaTabla(em, "bankingly_banca_movil", "max_udis_por_transferencia");//fecha_trabajo.substring(0, 7).replace("\\/", ""));
                            if (transactionOWN.getAmount() <= (Double.parseDouble(tb_precio_udi_periodo.getDato1()) * Double.parseDouble(tb_udis_permitido_tercero.getDato1()))) {
                                mensaje = validarTransferenciaATerceros(transactionOWN.getDebitProductBankIdentifier(),
                                        transactionOWN.getAmount(),
                                        transactionOWN.getCreditProductBankIdentifier(),
                                        transactionOWN.getClientBankIdentifier());
                            } else {
                                mensaje = "El monto de su transferencia traspasa el permitido a transferencia tercero";
                            }

                        }
                        //Pago de prestamo propio ---Falta pago de prestamo tercero
                        if (identificadorTransferencia == 3 || identificadorTransferencia == 4) {
                            if (identificadorTransferencia == 4) {
                                Query query_fecha_trabajo = em.createNativeQuery("SELECT to_char(fechatrabajo,'yyyymmdd') FROM origenes limit 1");

                                String fecha_trabajo = String.valueOf(query_fecha_trabajo.getSingleResult());
                                Tablas tb_precio_udi_periodo = util2.busquedaTabla(em, "valor_udi", fecha_trabajo.substring(0, 6));
                                Tablas tb_udis_permitido_tercero = util2.busquedaTabla(em, "bankingly_banca_movil", "max_udis_por_transferencia");//fecha_trabajo.substring(0, 7).replace("\\/", ""));
                                if (transactionOWN.getAmount() <= (Double.parseDouble(tb_precio_udi_periodo.getDato1()) * Double.parseDouble(tb_udis_permitido_tercero.getDato1()))) {
                                    mensaje = validarPagoAPrestamos(identificadorTransferencia, transactionOWN.getDebitProductBankIdentifier(),
                                            transactionOWN.getAmount(),
                                            transactionOWN.getCreditProductBankIdentifier(),
                                            transactionOWN.getClientBankIdentifier());
                                } else {
                                    mensaje = "El monto de su transferencia traspasa el permitido a transferencia tercero";
                                }
                            } else {
                                mensaje = validarPagoAPrestamos(identificadorTransferencia, transactionOWN.getDebitProductBankIdentifier(),
                                        transactionOWN.getAmount(),
                                        transactionOWN.getCreditProductBankIdentifier(),
                                        transactionOWN.getClientBankIdentifier());
                            }
                        }
                        if (identificadorTransferencia == 5) {
                            mensaje = "SOLO SE PERMITEN ENVIAR ORDENES SPEI DESDE TARJETA DE DEBITO";
                        }
                    }
                } catch (Exception e) {
                    System.out.println("NO SE PUDIERON VALIDAR LOS DATOS PARA LA TRANSFERENCIA:" + e.getMessage());
                    mensaje = e.getMessage();
                }
            } else {
                mensaje = "PRODUCTO NO CONFIGURADO PARA RETIROS";
            }
        } catch (Exception e) {
            System.out.println("Error al validar transferencia a CSN:" + e.getMessage());
            mensaje = e.getMessage();

            return mensaje.toUpperCase();
        } finally {
            em.close();
        }
        return mensaje.toUpperCase();

    }

    public File construirHtmlVoucher(AuxiliaresD ad) throws FileNotFoundException {
        //Leyendo el .txt con estructura del html
        File fileTxt = new File(ruta() + "voucher.txt");
        //El nombre que se le dara al html
        String nombre_html = "voucher" + ad.getTransaccion().toString() + ad.getAuxiliaresDPK().getFecha().toString().replace("-", "").replace(":", "").replace(".", "");// =nombre_txt.replace(".txt",".html");
        String html = ruta() + nombre_html + ".html";
        File file_html = new File(html);

        FileOutputStream fs = new FileOutputStream(file_html);
        OutputStreamWriter out = new OutputStreamWriter(fs);
        try {
            FileReader fr = new FileReader(fileTxt);
            BufferedReader br = new BufferedReader(fr);
            String linea;
            String linea_contenedor = "";

            SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:MM:ss");
            String hoy = sdf.format(new Date());

            String mov = "";

            if (ad.getCargoabono() == 0) {
                mov = "Retiro";
            } else {
                mov = "Deposito";
            }
            String buscar_origen = "SELECT * FROM origenes WHERE matriz=0";
            Query bus_origen = AbstractFacade.conexion().createNativeQuery(buscar_origen, Origenes.class);
            Origenes origen = (Origenes) bus_origen.getSingleResult();
            //Leyendo el txt
            while ((linea = br.readLine()) != null) {
                if (linea.contains("@@direccion@@")) {
                    linea = linea.replace("@@direccion@@", origen.getNombre());
                } else if (linea.contains("/usr/local/saicoop/img_caratula_ahorros")) {
                    linea = linea.replace("/usr/local/saicoop/img_caratula_ahorros/", ruta());
                    System.out.println("linea:" + linea);
                } else if (linea.contains("@@opa@@")) {
                    linea = linea.replace("@@opa@@", ad.getAuxiliaresDPK().getIdorigenp() + "-" + ad.getAuxiliaresDPK().getIdproducto() + "-" + ad.getAuxiliaresDPK().getIdauxiliar());
                } else if (linea.contains("@@tipoMov@@")) {
                    linea = linea.replace("@@tipoMov@@", mov);
                } else if (linea.contains("@@fecha@@")) {
                    linea = linea.replace("@@fecha@@", ad.getAuxiliaresDPK().getFecha().toString().substring(0, 19));
                } else if (linea.contains("@@monto@@")) {
                    linea = linea.replace("@@monto@@", "$" + ad.getMonto().toString());
                } else if (linea.contains("@@saldoec@@")) {
                    linea = linea.replace("@@saldoec@@", "$" + ad.getSaldoec().toString());
                } else if (linea.contains("@@ticket@@")) {
                    linea = linea.replace("@@ticket@@", ad.getTransaccion().toString());
                } else if (linea.contains("@@hoy@@")) {
                    linea = linea.replace("@@hoy@@", hoy);
                }

                out.write(linea);
            }
            out.close();
        } catch (Exception e) {
            System.out.println("Excepcion leyendo txt" + ": " + e.getMessage());
        }
        return file_html;
    }

    public boolean crearPDF(String ruta, String nombreDelHTMLAConvertir) {
        try {
            // ruta donde esta el html a convertir
            String ficheroHTML = ruta + nombreDelHTMLAConvertir;
            String url = new File(ficheroHTML).toURI().toURL().toString();
            // ruta donde se almacenara el pdf y que nombre se le data
            String ficheroPDF = ruta + nombreDelHTMLAConvertir.replace(".html", ".pdf");
            File htmlSource = new File(ficheroHTML);
            File pdfDest = new File(ficheroPDF);
            // pdfHTML specific code
            ConverterProperties converterProperties = new ConverterProperties();
            HtmlConverter.convertToPdf(new FileInputStream(htmlSource), new FileOutputStream(pdfDest),
                    converterProperties);
            return true;
        } catch (Exception e) {
            System.out.println("Error al crear PDF:" + e.getMessage());
            return false;
        }

    }

    public static String ruta() {
        String home = System.getProperty("user.home");
        String separador = System.getProperty("file.separator");
        String actualRuta = home + separador + "Banca" + separador + "voucher" + separador;
        return actualRuta;
    }

}
