package com.fenoreste.rest.dao;

import com.fenoreste.rest.Util.UtilidadesGenerales;
import com.fenoreste.rest.ResponseDTO.ClientByDocumentDTO;
import com.fenoreste.rest.ResponseDTO.usuarios_banca_bankinglyDTO;
import com.fenoreste.rest.entidades.Persona;
import com.fenoreste.rest.Util.AbstractFacade;
import com.fenoreste.rest.WsTDD.TarjetaDeDebito;
import com.fenoreste.rest.entidades.Auxiliares;
import com.fenoreste.rest.entidades.AuxiliaresPK;
import com.fenoreste.rest.entidades.Clabes_Interbancarias;
import com.fenoreste.rest.entidades.Tablas;
import com.fenoreste.rest.entidades.TablasPK;
import com.fenoreste.rest.entidades.WsSiscoopFoliosTarjetasPK1;
import com.fenoreste.rest.entidades.banca_movil_usuarios;
import com.syc.ws.endpoint.siscoop.BalanceQueryResponseDto;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

public abstract class FacadeCustomer<T> {

    List<Object[]> lista = null;
    UtilidadesGenerales util = new UtilidadesGenerales();

    public FacadeCustomer(Class<T> entityClass) {
    }

    public ClientByDocumentDTO getClientByDocument(Persona p, String username) {
        EntityManager em = AbstractFacade.conexion();
        ClientByDocumentDTO client = null;
        usuarios_banca_bankinglyDTO socio = null;
        /*String c="SELECT sai_convierte_caracteres_especiales_iso88591_utf8(appaterno) FROM personas WHERE idorigen="+p.getPersonasPK().getIdorigen()+
                                                                                                      " AND idgrupo="+p.getPersonasPK().getIdgrupo()+
                                                                                                      " AND idsocio="+p.getPersonasPK().getIdsocio();
         */
        try {
            System.out.println("esta es la persona:" + p.getNombre());
            int clientType = 0;
            System.out.println("Razon social:" + p.getRazonSocial());
            if (p.getRazonSocial() == null) {
                clientType = 0;
            } else {
                clientType = 1;
            }
            //Para persisitir desccomenta las lineas
            //if (saveDatos(username, p)) {
                client = new ClientByDocumentDTO();
                client.setClientBankIdentifier(String.format("%06d", p.getPersonasPK().getIdorigen()) + "" + String.format("%02d", p.getPersonasPK().getIdgrupo()) + "" + String.format("%06d", p.getPersonasPK().getIdsocio()));
                client.setClientName(p.getNombre() + " " + p.getAppaterno() + " " + p.getApmaterno());
                client.setClientType(String.valueOf(clientType));
                client.setDocumentId(p.getCurp());
            //}
            
        } catch (Exception e) {
            System.out.println("Error leer socio:" + e.getMessage());
            return client;
        } finally {
            em.close();
        }
        return client;
    }

    //Metodo para saber si la personas realmente existe en la base de datos
    public Persona BuscarPersona(int clientType, String documentId, String Name, String LastName, String Mail, String Phone, String CellPhone) throws UnsupportedEncodingException, IOException {
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
        System.out.println("LastNAME:" + LastName);
        persona.setNombre("SIN DATOS ");
        /*if (util.obtenerOrigen(em).contains("MITRAS") && LastName.contains("%")) {
            consulta = "SELECT "
                    + "idorigen,"
                    + "idgrupo,"
                    + "idsocio,"
                    + "calle,"
                    + "numeroext,"
                    + "numeroint,"
                    + "entrecalles,"
                    + "fechanacimiento,"
                    + "lugarnacimiento,"
                    + "efnacimiento,"
                    + "sexo,"
                    + "telefono,"
                    + "telefonorecados,"
                    + "listanegra,"
                    + "estadocivil,"
                    + "idcoop,"
                    + "idsector,"
                    + "estatus,"
                    + "aceptado,"
                    + "fechaingreso,"
                    + "fecharetiro,"
                    + "fechaciudad,"
                    + "regimen_Mat,"
                    + "nombre,"
                    + "medio_Inf,"
                    + "requisitos,"
                    + "sai_convierte_caracteres_especiales_iso88591_utf8(appaterno) as appaterno,"
                    + "sai_convierte_caracteres_especiales_iso88591_utf8(apmaterno) as apmaterno,"
                    + "nacionalidad,"
                    + "grado_Estudios,"
                    + "categoria,"
                    + "rfc,"
                    + "curp,"
                    + "email,"
                    + "razon_Social,"
                    + "causa_Baja,"
                    + "nivel_Riesgo,"
                    + "celular,"
                    + "rfc_Valido,"
                    + "curp_Valido,"
                    + "idcolonia"
                    + " FROM personas p WHERE "
                    + " replace((p." + IdentClientType.toUpperCase() + "),' ','')='" + documentId.replace(" ", "").trim() + "'"
                    + " AND UPPER(REPLACE(p.nombre,' ',''))='" + Name.toUpperCase().replace(" ", "").trim() + "'"
                    + " AND UPPER(p.appaterno)||''||UPER(p.apmaterno) LIKE ('%" + LastName.toUpperCase().replace(" ", "") + "%')"
                    + " AND (CASE WHEN email IS NULL THEN '' ELSE trim(UPPER(email)) END)='" + Mail.toUpperCase() + "'"
                    + " AND (CASE WHEN telefono IS NULL THEN '' ELSE trim(telefono) END)='" + Phone + "'"
                    + " AND (CASE WHEN celular IS NULL THEN '' ELSE trim(celular) END)='" + CellPhone + "' LIMIT 1";

        } else {*/
        consulta = "SELECT * FROM personas p WHERE "
                + "  replace(upper(p." + IdentClientType.toUpperCase() + "),' ','')='" + documentId.replace(" ", "").trim().toUpperCase() + "'"
                + " AND UPPER(REPLACE(p.nombre,' ',''))='" + Name.toUpperCase().replace(" ", "").trim() + "'"
                + " AND UPPER(appaterno)||''||UPPER(p.apmaterno) LIKE ('%" + LastName.toUpperCase().replace(" ", "") + "%')"
                + " AND (CASE WHEN email IS NULL THEN '' ELSE trim(email) END)='" + Mail + "'"
                + " AND (CASE WHEN telefono IS NULL THEN '' ELSE trim(telefono) END)='" + Phone + "'"
                + " AND (CASE WHEN celular IS NULL THEN '' ELSE trim(celular) END)='" + CellPhone + "' LIMIT 1";
        //}
        System.out.println("Consulta:" + consulta);
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
                if (util.obtenerOrigen(em).replace(" ", "").contains("SANNICOLAS")) {
                    //Buscamos que el socio tenga el producto 133 y con el saldo de 50 pesos
                    Tablas tb_producto_tdd = util.busquedaTabla(em, "bankingly_banca_movil", "producto_tdd");                    try {
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
                            Tablas tb_sopar = util.busquedaTabla(em, "bankingly_banca_movil", "sopar");
                            String consulta_sopar = "SELECT count(*) FROM sopar WHERE idorigen=" + idorigen + " AND idgrupo=" + idgrupo + " AND idsocio=" + idsocio + " AND tipo='" + tb_sopar.getDato2() + "'";
                            Query query_sopar = em.createNativeQuery(consulta_sopar);
                            int count_sopar = Integer.parseInt(String.valueOf(query_sopar.getSingleResult()));
                            if (count_sopar == 0) {
                                //Ahora verifico que el socio tenga clabe para SPEI 
                                AuxiliaresPK clave_llave = new AuxiliaresPK(a_tdd.getAuxiliaresPK().getIdorigenp(),a_tdd.getAuxiliaresPK().getIdproducto(),a_tdd.getAuxiliaresPK().getIdauxiliar());
                                Clabes_Interbancarias clabe_folio = em.find(Clabes_Interbancarias.class,clave_llave);
                                if(clabe_folio != null){
                                    if(clabe_folio.isActiva()){
                                      bandera = true;   
                                    }else{
                                      mensaje  = "CLABE INTERBANCARIA INACTIVA";
                                    }                                    
                                }else{
                                    mensaje  = "SOCIO NO CUENTA CON CLABE INTERBANCARIA";   
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
                    String consulta_usuarios_banca = "SELECT count(*) FROM banca_movil_usuarios_bankingly WHERE idorigen=" + idorigen + " AND idgrupo=" + idgrupo + " AND idsocio=" + idsocio;
                    System.out.println("consulta_username:" + consulta);
                    int count_usuarios = 0;
                    //Para validar registro solo descomentar estas lineas
                    /*try {
                        Query query = em.createNativeQuery(consulta_usuarios_banca);
                        count_usuarios = Integer.parseInt(query.getSingleResult().toString());
                    } catch (Exception e) {
                    }*/
                    if (count_usuarios == 0) {
                        mensaje = "VALIDADO CON EXITO";
                    } else {
                        mensaje = "YA EXISTE UN REGISTRO PARA EL SOCIO";
                    }

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
            banca_movil_usuarios userDB = new banca_movil_usuarios();
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
            em.persist(userDB);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            System.out.println("Error al persistir usuario:" + username + ":" + e.getMessage());
            return false;
        } finally {
            em.close();
        }
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

}
