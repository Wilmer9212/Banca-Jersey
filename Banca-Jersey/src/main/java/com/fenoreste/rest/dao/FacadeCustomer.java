package com.fenoreste.rest.dao;

import com.fenorest.rest.EnviarSMS.PreparaSMS;
import com.fenoreste.rest.Util.UtilidadesGenerales;
import com.fenoreste.rest.ResponseDTO.ClientByDocumentDTO;
import com.fenoreste.rest.entidades.Persona;
import com.fenoreste.rest.Util.AbstractFacade;
import com.fenoreste.rest.Util.HttpConsumo;
import com.fenoreste.rest.WsTDD.TarjetaDeDebito;
import com.fenoreste.rest.entidades.Auxiliares;
import com.fenoreste.rest.entidades.AuxiliaresPK;
import com.fenoreste.rest.entidades.Clabes_Interbancarias;
import com.fenoreste.rest.entidades.Tablas;
import com.fenoreste.rest.entidades.TablasPK;
import com.fenoreste.rest.entidades.WsClabeActivacion;
import com.fenoreste.rest.entidades.WsSiscoopFoliosTarjetasPK1;
import com.fenoreste.rest.entidades.Banca_movil_usuarios;
import com.syc.ws.endpoint.siscoop.BalanceQueryResponseDto;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.json.JSONObject;

public abstract class FacadeCustomer<T> {

    List<Object[]> lista = null;
    UtilidadesGenerales util = new UtilidadesGenerales();

    public FacadeCustomer(Class<T> entityClass) {
    }

    public ClientByDocumentDTO getClientByDocument(Persona p, String username) {
        EntityManager em = AbstractFacade.conexion();
        ClientByDocumentDTO client = null;
        try {
            int clientType = 0;
            if (p.getRazonSocial() == null) {
                clientType = 0;
            } else {
                clientType = 1;
            }

            //Para persisitir desccomenta las lineas
            if (saveDatos(username, p)) {
            client = new ClientByDocumentDTO();
            client.setClientBankIdentifier(String.format("%06d", p.getPersonasPK().getIdorigen()) + "" + String.format("%02d", p.getPersonasPK().getIdgrupo()) + "" + String.format("%06d", p.getPersonasPK().getIdsocio()));
            client.setClientName(p.getNombre() + " " + p.getAppaterno() + " " + p.getApmaterno());
            client.setClientType(String.valueOf(clientType));
            client.setDocumentId(p.getCurp());
            }

        } catch (Exception e) {
            System.out.println("Error leer socio:" + e.getMessage());
            return client;
        } finally {
            em.close();
        }
        return client;
    }

    //Metodo para saber si la personas realmente existe en la base de datos
    public Persona BuscarPersona(int clientType, String documentId, String Name, String LastName, String Mail,String CellPhone) throws UnsupportedEncodingException, IOException {
        EntityManager em = AbstractFacade.conexion();//emf.createEntityManager()EntityManager em = emf.createEntityManager();        EntityManager em = emf.createEntityManager();
        String IdentClientType = "";
        /*Identificamos el tipo de cliente si es 
        1.- persona fisica buscamos por Curp
        2.- persona moral RFC
         */
        if (LastName.replace(" ", "").toUpperCase().contains("Ñ")) {
            LastName = LastName.toUpperCase().replace("Ñ", "%");
        }
        if (clientType == 1) {
            IdentClientType = "curp";
        } else if (clientType == 2) {
            IdentClientType = "rfc";
        }

        Persona persona = new Persona();
        String consulta = "";
        persona.setNombre("SIN DATOS ");
        consulta = "SELECT * FROM personas p WHERE "
                + "  replace(upper(p." + IdentClientType.toUpperCase() + "),' ','')='" + documentId.replace(" ", "").trim().toUpperCase() + "'"
                + " AND UPPER(REPLACE(p.nombre,' ',''))='" + valida_caracteres_speciales(Name.toUpperCase().replace(" ", "").trim()).toUpperCase() + "'"
                + " AND UPPER(replace(appaterno,' ',''))||''||UPPER(replace(p.apmaterno,' ','')) LIKE ('%" + valida_caracteres_speciales(LastName.toUpperCase().trim()).replace(" ", "").toUpperCase() + "%')"
                + " AND (CASE WHEN email IS NULL THEN '' ELSE trim(upper(email)) END)='" + Mail.toUpperCase().trim() + "'"
                + " AND (CASE WHEN celular IS NULL THEN '' ELSE trim(celular) END)='" + CellPhone.trim() + "' LIMIT 1";
        System.out.println("Consulta para busqueda de personas :"+consulta);
        
        try {   //Se deberia buscar por telefono,celular,email pero Mitras solicito que solo sea x curp y nombre esta en prueba            
            Query query = em.createNativeQuery(consulta, Persona.class);
            persona = (Persona) query.getSingleResult();
        } catch (Exception e) {
            System.out.println("Error al Buscar personas:" + e.getMessage());
            return persona;
        } finally {
            em.close();
        }
        return persona;
    }

    //Buscamos que el socio no aparezca con otro usuario
    public String validaciones_datos(int idorigen, int idgrupo, int idsocio, String username) {
        String mensaje = "";
        EntityManager em = AbstractFacade.conexion();
        boolean bandera = false;
        String consulta = "";
        try {
            //Busco la tabla donde guarda el producto para banca movil
            TablasPK tablasPK = new TablasPK("bankingly_banca_movil", "producto_banca_movil");
            Tablas tablaProducto = em.find(Tablas.class, tablasPK);
            //Buscamos que el socio tenga el producto para banca movil aperturado en auxiliares            
            //Reglas CSN,Mitras
            String busquedaFolio = "SELECT * FROM auxiliares WHERE idorigen=" + idorigen + " AND idgrupo=" + idgrupo + " AND idsocio=" + idsocio + " AND idproducto=" + tablaProducto.getDato1() + " AND estatus=0";
            Query busquedaFolioQuery = em.createNativeQuery(busquedaFolio, Auxiliares.class);
            Auxiliares a = (Auxiliares) busquedaFolioQuery.getSingleResult();

            //Si ya tiene el producto para banca movil activo
            if (a != null) {
                //Regla especifica para CSN 
                //-- Debe tener activo producto 133 y tener minimo de saldo 50 pesos

                //1.-30200 CSN
                if (util.obtenerOrigen(em) == 30200) {
                    //Buscamos que el socio tenga el producto 133 y con el saldo de 50 pesos
                    Tablas tb_producto_tdd = util.busquedaTabla(em, "bankingly_banca_movil", "producto_tdd");
                    try {
                        //Buscamos que en la tdd tenga minimo 50 pesos de saldo leemos el ws de Alestra
                        String busqueda133 = "SELECT * FROM auxiliares a WHERE idorigen=" + idorigen
                                + " AND idgrupo=" + idgrupo
                                + " AND idsocio=" + idsocio
                                + " AND idproducto=" + Integer.parseInt(tb_producto_tdd.getDato1()) + " AND estatus=2";
                        Query auxiliar = em.createNativeQuery(busqueda133, Auxiliares.class);
                        Auxiliares a_tdd = (Auxiliares) auxiliar.getSingleResult();

                        WsSiscoopFoliosTarjetasPK1 foliosPK = new WsSiscoopFoliosTarjetasPK1(a_tdd.getAuxiliaresPK().getIdorigenp(), a_tdd.getAuxiliaresPK().getIdproducto(), a_tdd.getAuxiliaresPK().getIdauxiliar());

                        double saldo = 0.0;
                        BalanceQueryResponseDto saldoWS = new TarjetaDeDebito().saldoTDD(foliosPK);
                        if (saldoWS.getCode() >= 1) {
                            saldo = saldoWS.getAvailableAmount();
                            // SaldoTddPK saldoTddPK = new SaldoTddPK(a.getAuxiliaresPK().getIdorigenp(), a.getAuxiliaresPK().getIdproducto(), a.getAuxiliaresPK().getIdauxiliar());
                            // new TarjetaDeDebito().actualizarSaldoTDD(saldoTddPK, saldo, em);
                        } else {
                            System.out.println("Error al consumir web service para saldo de TDD");
                        }
                        if (saldo >= Double.parseDouble(tb_producto_tdd.getDato2())) {
                            //S tiene el saldoque se encesita en la tdd
                            //Ahora verificamos que no se un socio bloqueado buscamos en la lista sopar
                           if (!util.validacionSopar(a_tdd.getAuxiliaresPK().getIdorigenp(),a_tdd.getAuxiliaresPK().getIdproducto(),a_tdd.getAuxiliaresPK().getIdauxiliar(),2)) {
                                //Ahora verifico que el socio tenga clabe para SPEI 
                                AuxiliaresPK clave_llave = new AuxiliaresPK(a_tdd.getAuxiliaresPK().getIdorigenp(), a_tdd.getAuxiliaresPK().getIdproducto(), a_tdd.getAuxiliaresPK().getIdauxiliar());
                                Clabes_Interbancarias clabe_folio = em.find(Clabes_Interbancarias.class, clave_llave);
                                if (clabe_folio != null) {
                                    if (clabe_folio.isActiva()) {
                                        String cuenta = String.format("%06d", clabe_folio.getAux_pk().getIdorigenp()) + "" + String.format("%05d", clabe_folio.getAux_pk().getIdproducto()) + "" + String.format("%08d", clabe_folio.getAux_pk().getIdauxiliar());                                                                               
                                        Tablas tb_activar_registra_cuenta_persona_fisica=util.busquedaTabla(em, "bankingly_banca_movil","activa_desactiva_registra_cuenta");
                                        if(Integer.parseInt(tb_activar_registra_cuenta_persona_fisica.getDato1())==1){
                                        JSONObject request = new JSONObject();
                                        request.put("productBankIdentifier", cuenta);
                                        Tablas tb_path = util.busquedaTabla(em, "bankingly_banca_movil", "registra_cuenta_spei");
                                        HttpConsumo consumo = new HttpConsumo(tb_path.getDato2(), request.toString());
                                        String respuesta_consumo = consumo.consumo();
                                        JSONObject respuesta_registra_orden = new JSONObject(respuesta_consumo);                                        
                                        bandera=true;
                                        System.out.println("Respuesta registra orden:" + respuesta_registra_orden);
                                        }else{
                                            bandera = true;
                                        }                                        
                                    } else {
                                        mensaje = "CLABE INTERBANCARIA INACTIVA";
                                    }
                                } else {
                                    mensaje = "SOCIO NO CUENTA CON CLABE INTERBANCARIA";
                                }
                            } else {
                                mensaje = "SOCIO ESTA BLOQUEADO";
                            }
                        } else {
                            mensaje = "PRODUCTO " + tb_producto_tdd.getDato1() + " NO CUMPLE CON EL SALDO MINIMO";
                        }
                    } catch (Exception e) {
                        mensaje = "PRODUCTO " + tablaProducto.getDato1() + " NO ESTA ACTIVO";
                        System.out.println("Error al buscar el producto 133:" + e.getMessage());
                    }
                } else {
                    bandera = true;
                }

                if (bandera) {
                    /*String consulta_usuarios_banca = "SELECT count(*) FROM banca_movil_usuarios_bankingly WHERE idorigen=" + idorigen + " AND idgrupo=" + idgrupo + " AND idsocio=" + idsocio;
                    System.out.println("consulta_username:" + consulta);
                    int count_usuarios = 0;*/
                    //Para validar registro solo descomentar estas lineas
                    /*try {
                        Query query = em.createNativeQuery(consulta_usuarios_banca);
                        count_usuarios = Integer.parseInt(query.getSingleResult().toString());
                    } catch (Exception e) {
                    } */                   
                        mensaje = "VALIDADO CON EXITO";
                    

                }
            }
        } catch (Exception e) {
            mensaje = "El usuario no tiene habilitado el producto para banca movil";
            System.out.println("Error en metodo para validar datos:" + e.getMessage());
            return mensaje.toUpperCase();
        } finally {
            em.close();
        }

        return mensaje.toUpperCase();
    }

    //Guardamos el usuario en la base de datos
    public boolean saveDatos(String username, Persona p) {
        EntityManager em = AbstractFacade.conexion();
        try {
            Banca_movil_usuarios userDB = new Banca_movil_usuarios();
            userDB.setPersonasPK(p.getPersonasPK());
            userDB.setAlias_usuario(username);
            //Para insertar opa buscamos su producto configurado en tablas
            Tablas tb = util.busquedaTabla(em, "bankingly_banca_movil", "producto_banca_movil");
            String b_auxiliares = "SELECT * FROM auxiliares a WHERE "
                    + "idorigen=" + p.getPersonasPK().getIdorigen()
                    + " AND idgrupo=" + p.getPersonasPK().getIdgrupo()
                    + " AND idsocio=" + p.getPersonasPK().getIdsocio()
                    + " AND idproducto=" + Integer.parseInt(tb.getDato1()) + " AND estatus=0";

            Query query_auxiliar = em.createNativeQuery(b_auxiliares, Auxiliares.class
            );
            Auxiliares a = (Auxiliares) query_auxiliar.getSingleResult();

            
            userDB.setPersonasPK(p.getPersonasPK());
            userDB.setEstatus(true);
            userDB.setAlias_usuario(username);
            userDB.setIdorigenp(a.getAuxiliaresPK().getIdorigenp());
            userDB.setIdproducto(a.getAuxiliaresPK().getIdproducto());
            userDB.setIdauxiliar(a.getAuxiliaresPK().getIdauxiliar());
            em.getTransaction().begin();
            em.merge(userDB);
            em.getTransaction().commit();
            return true;
            
        } catch (Exception e) {
            System.out.println("Error al persistir usuario:" + username + ":" + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    public String notificaCreacionCuenta(String cuenta, String estado, String observacion, String empresa) {
        PreparaSMS enviar_sms = new PreparaSMS();
        EntityManager em = AbstractFacade.conexion();
        String mensaje = "";
        boolean bandera_existe = false;

        try {
            String busqueda_folio = "SELECT * FROM ws_siscoop_clabe_interbancaria WHERE clabe='" + cuenta + "'";
            System.out.println("Busqueda Folio:" + busqueda_folio);
            Auxiliares a = null;
            try {
                Query query = em.createNativeQuery(busqueda_folio, Clabes_Interbancarias.class);
                Clabes_Interbancarias clabe_folio = (Clabes_Interbancarias) query.getSingleResult();
                AuxiliaresPK a_pk = new AuxiliaresPK(clabe_folio.getAux_pk().getIdorigenp(), clabe_folio.getAux_pk().getIdproducto(), clabe_folio.getAux_pk().getIdauxiliar());
                a = em.find(Auxiliares.class, a_pk);
                if (clabe_folio.isActiva()) {
                    bandera_existe = true;
                }

            } catch (Exception e) {
                System.out.println("Error al notificar creacion de estado de cuenta spei:" + e.getMessage());
                mensaje = "CUENTA NO EXISTE";
                bandera_existe = false;
            }

            Date hoy = new Date();

            if (bandera_existe) {
                String cliente = String.format("%06d", a.getIdorigen()) + "" + String.format("%02d", a.getIdgrupo()) + "" + String.format("%06d", a.getIdsocio());
                bandera_existe = false;
                WsClabeActivacion clave = null;
                try {
                    String b = "SELECT * FROM ws_bankingly_clabe_activacion WHERE clabe='" + cuenta + "'";
                    Query query = em.createNativeQuery(b, WsClabeActivacion.class);
                    clave = (WsClabeActivacion) query.getSingleResult();
                    bandera_existe = true;
                } catch (Exception e) {
                }
                if (bandera_existe) {
                    if (!estado.toUpperCase().contains("A")) {
                        clave.setActiva(false);
                    }else{
                        clave.setActiva(true);
                    }
                    clave.setEstado(estado);
                } else {
                    clave = new WsClabeActivacion();
                    clave.setClabe(cuenta);
                    if (!estado.toUpperCase().contains("A")) {
                        clave.setActiva(false);
                    }else{
                       clave.setActiva(true); 
                    }
                    clave.setFecha_hora(hoy);
                    clave.setObservacion(observacion);
                    clave.setEmpresa(empresa);
                    System.out.println("7.-");
                    clave.setEstado(estado);
                }

                em.getTransaction().begin();
                em.persist(clave);
                em.getTransaction().commit();

                mensaje = "recibido";
                String enviar_ = enviar_sms.enviaSMSNotificaEstadoCuenta(em, cliente, cuenta, estado, observacion);

            } else {
                mensaje = "cuenta no existe";
            }

        } catch (Exception e) {
            System.out.println("Error al guardar una clabe interbancaria:" + e.getMessage());
            mensaje = "YA SE REGISTRO UN ESTATUS PARA LA CUENTA:" + cuenta;
        }
        return mensaje.toUpperCase();

    }

    public String Random() {
        String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
        String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        String NUMBER = "0123456789";

        String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;
        SecureRandom random = new SecureRandom();

        String cadena = "";
        for (int i = 0; i < 15; i++) {
            int rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING.length());
            char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);
            cadena = cadena + rndChar;
        }
        System.out.println("Cadena:" + cadena);
        return cadena;
    }

    public boolean actividad_horario() {
        EntityManager em = AbstractFacade.conexion();
        boolean bandera_ = false;
        try {
            if (util.actividad(em)) {
                bandera_ = true;
            }
        } catch (Exception e) {
            System.out.println("Error al verificar el horario de actividad");
        } finally {
            em.close();
        }
        return bandera_;
    }

     public String valida_caracteres_speciales(String cadena){
        cadena = cadena.toLowerCase();
        for(int i=0;i<cadena.length();i++){
            int ascii = cadena.charAt(i);
            char c = cadena.charAt(i);
            if(cadena.charAt(i) == ' ' || Character.isLetter(c) || Character.isDigit(c)){               
               
                switch(c){
                    case 'á':
                    cadena = cadena.replace(String.valueOf(c),"a");
                    break;
                    case 'é':
                    cadena = cadena.replace(String.valueOf(c),"e");
                    break;
                    case 'í':
                    cadena = cadena.replace(String.valueOf(c),"a");
                    break;
                    case 'ó':
                    cadena = cadena.replace(String.valueOf(c),"o");
                    break;
                    case 'ú':
                    cadena = cadena.replace(String.valueOf(c),"u");
                    break;
                    /*case 'ñ':
                    cadena = cadena.replace(String.valueOf(c),"n");
                    break;*/
                //áéíóúñ    
                }                
            }else{
               cadena = cadena.replace(String.valueOf(c),"");
            }
           
            //System.out.println("El caracter en la posicion "+i+"es:"+cadena.charAt(i)+" y su valor ascii es:"+ascii);
        }
        System.out.println("la cadena es:"+cadena.trim());
        
        return cadena;
    }

}
