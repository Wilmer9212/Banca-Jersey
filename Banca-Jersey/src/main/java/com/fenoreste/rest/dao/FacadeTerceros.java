/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.dao;

import com.fenorest.rest.EnviarSMS.PreparaSMS;
import com.fenoreste.rest.DTO.OgsDTO;
import com.fenoreste.rest.Util.UtilidadesGenerales;
import com.fenoreste.rest.ResponseDTO.BackendOperationResultDTO;
import com.fenoreste.rest.ResponseDTO.Bank;
import com.fenoreste.rest.ResponseDTO.ThirdPartyProductDTO;
import com.fenoreste.rest.ResponseDTO.userDocumentIdDTO;
import com.fenoreste.rest.Util.AbstractFacade;
import com.fenoreste.rest.Util.Utilidades;
import com.fenoreste.rest.entidades.Auxiliares;
import com.fenoreste.rest.entidades.AuxiliaresPK;
import com.fenoreste.rest.entidades.Colonias;
import com.fenoreste.rest.entidades.Estados;
import com.fenoreste.rest.entidades.Municipios;
import com.fenoreste.rest.entidades.Origenes;
import com.fenoreste.rest.entidades.Paises;
import com.fenoreste.rest.entidades.Persona;
import com.fenoreste.rest.entidades.PersonasPK;
import com.fenoreste.rest.entidades.Productos;
import com.fenoreste.rest.entidades.ProductosTercero;
import com.fenoreste.rest.entidades.ProductosTerceros;
import com.fenoreste.rest.entidades.Productos_bankingly;
import com.fenoreste.rest.entidades.Tablas;
import java.util.ArrayList;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author wilmer
 */
public abstract class FacadeTerceros<T> {

    UtilidadesGenerales util2 = new UtilidadesGenerales();
    Utilidades util = new Utilidades();
   

    public FacadeTerceros(Class<T> entityClass) {
    }

    public BackendOperationResultDTO validarProductoTerceros(ThirdPartyProductDTO dtoInput) {
        System.out.println("si." + dtoInput.getOwnerDocumentId().getDocumentNumber());
        EntityManager em = AbstractFacade.conexion();//EntityManager em = emf.createEntityManager();
        BackendOperationResultDTO dtoResult = new BackendOperationResultDTO();
        try {
            String backendMessage = "";
            ProductosTerceros productosTerceros = new ProductosTerceros();

            System.out.println("size:" + dtoInput.getClientBankIdentifiers().size());
            boolean b = false;
            ProductosTerceros prod = null;
            for (int i = 0; i < dtoInput.getClientBankIdentifiers().size(); i++) {
                String cvalidar = "SELECT * FROM productos_terceros_bankingly WHERE thirdpartyproductbankidentifier='" + dtoInput.getThirdPartyProductBankIdentifier() + "'";
                Query query = em.createNativeQuery(cvalidar, ProductosTerceros.class);
                try {
                    prod = (ProductosTerceros) query.getSingleResult();
                } catch (Exception e) {
                    System.out.println("Message:" + e.getMessage());
                }
                if (prod != null) {
                    backendMessage = "Error,producto ya esta registrado...";
                    System.out.println("back:" + backendMessage);
                } else {
                    productosTerceros.setThirdPartyProductBankIdentifier(dtoInput.getThirdPartyProductBankIdentifier());
                    productosTerceros.setClientBankIdentifiers(dtoInput.getClientBankIdentifiers().get(i));
                    productosTerceros.setThirdPartyProductNumber(dtoInput.getThirdPartyProductNumber());
                    productosTerceros.setAlias(dtoInput.getAlias());
                    productosTerceros.setCurrencyId(dtoInput.getCurrencyId());
                    productosTerceros.setTransactionSubType(dtoInput.getTransactionSubType());
                    productosTerceros.setThirdPartyProductType(dtoInput.getThirdPartyProductType());
                    productosTerceros.setProductType(dtoInput.getProductType());
                    productosTerceros.setOwnerName(dtoInput.getOwnerName());
                    productosTerceros.setOwnerCountryId(dtoInput.getOwnerCountryId());
                    productosTerceros.setOwnerEmail(dtoInput.getOwnerEmail());
                    productosTerceros.setOwnerCity(dtoInput.getOwnerCity());
                    productosTerceros.setOwnerAddress(dtoInput.getOwnerAddress());
                    productosTerceros.setOwnerDocumentId_integrationProperties(dtoInput.getOwnerDocumentId().getIntegrationProperties());
                    productosTerceros.setOwnerDocumentId_documentNumber(String.valueOf(dtoInput.getOwnerDocumentId().getDocumentNumber()));
                    productosTerceros.setOwnerDocumentId_documentType(String.valueOf(dtoInput.getOwnerDocumentId().getDocumentType()));
                    productosTerceros.setOwnerPhoneNumber(dtoInput.getOwnerPhoneNumber());
                    productosTerceros.setBank_bankId(dtoInput.getBank().getBankId());
                    productosTerceros.setBank_countryId(dtoInput.getBank().getCountryId());
                    productosTerceros.setBank_description(dtoInput.getBank().getDescription());
                    productosTerceros.setBank_headQuartersAddress(dtoInput.getBank().getHeadQuartersAddress());
                    productosTerceros.setBank_routingCode(dtoInput.getBank().getRoutingCode());
                    productosTerceros.setCorrespondentBank_bankId(dtoInput.getCorrespondentBank().getBankId());
                    productosTerceros.setCorrespondentBank_countryId(dtoInput.getCorrespondentBank().getCountryId());
                    productosTerceros.setCorrespondentBank_description(dtoInput.getCorrespondentBank().getDescription());
                    productosTerceros.setCorrespondentBank_headQuartersAddress(dtoInput.getCorrespondentBank().getHeadQuartersAddress());
                    productosTerceros.setCorrespondentBank_routingCode(dtoInput.getCorrespondentBank().getRoutingCode());
                    productosTerceros.setUserDocumentId_documentNumber(String.valueOf(dtoInput.getUserDocumentId().getDocumentNumber()));
                    productosTerceros.setUserDocumentId_documentType(String.valueOf(dtoInput.getUserDocumentId().getDocumentType()));
                    productosTerceros.setUserDocumentId_integrationProperties(dtoInput.getUserDocumentId().getIntegrationProperties());
                    try {
                        em.getTransaction().begin();
                        em.persist(productosTerceros);
                        em.getTransaction().commit();
                        /*if (!em.getTransaction().isActive()) {
                            em.clear();
                            em.getTransaction().begin();
                            em.persist(productosTerceros);

                        }
                        em.getTransaction().commit();*/
                    } catch (Exception e) {
                        /*if (em.getTransaction().isActive()) {
                            em.getTransaction().rollback();
                        }*/
                    }
                }
                if (i == dtoInput.getClientBankIdentifiers().size() - 1 && backendMessage.equals("")) {
                    b = true;
                    backendMessage = "Producto registrado con exito...";
                }

            }
            if (b == true) {
                dtoResult.setBackendCode("1");
                dtoResult.setBackendMessage(backendMessage);
                dtoResult.setBackendReference("null");
                dtoResult.setIntegrationProperties("null");
                dtoResult.setIsError(false);
                dtoResult.setTransactionIdenty(productosTerceros.getThirdPartyProductNumber());
            } else {
                dtoResult.setBackendCode("1");
                dtoResult.setBackendMessage(backendMessage);
                dtoResult.setBackendReference("null");
                dtoResult.setIntegrationProperties("null");
                dtoResult.setIsError(true);
                dtoResult.setTransactionIdenty(productosTerceros.getThirdPartyProductNumber());

            }

        } catch (Exception e) {
            e.printStackTrace();

            System.out.println("Error:" + e.getMessage());
        } finally {
            em.close();
        }
        return dtoResult;

    }

    public ThirdPartyProductDTO cosultaProductosTerceros(String productNumber, Integer productTypeId, userDocumentIdDTO documento, Integer thirdPartyProductType) {
        EntityManager em = AbstractFacade.conexion();
        ThirdPartyProductDTO dto = new ThirdPartyProductDTO();
        try {
            Auxiliares a = validarTercero(productNumber, productTypeId);
            if (a != null) {
                if (a.getEstatus() == 2) {
                    userDocumentIdDTO userDocument = new userDocumentIdDTO();
                    Bank bancoProductoTercero = new Bank();
                    Bank corresponsalBank = new Bank();
                    Productos pr = em.find(Productos.class, a.getAuxiliaresPK().getIdproducto());
                    String ogs = String.format("%06d", a.getIdorigen()) + String.format("%02d", a.getIdgrupo()) + String.format("%06d", a.getIdsocio());
                    ArrayList<String> listaPt = new ArrayList<>();
                    listaPt.add(ogs);
                    dto.setClientBankIdentifiers(listaPt);
                    dto.setThirdPartyProductNumber(String.valueOf(thirdPartyProductType));
                    dto.setThirdPartyProductBankIdentifier(productNumber);
                    dto.setAlias(pr.getNombre());
                    dto.setCurrencyId("484");//Identificador de moneda 1 es local
                    dto.setTransactionSubType(2);
                    dto.setThirdPartyProductType(1);

                    Productos_bankingly prod = em.find(Productos_bankingly.class, a.getAuxiliaresPK().getIdproducto());
                    dto.setProductType(prod.getProductTypeId());//el tipo de producto

                    PersonasPK personaPK = new PersonasPK(a.getIdorigen(), a.getIdgrupo(), a.getIdsocio());
                    Persona p = em.find(Persona.class, personaPK);
                    dto.setOwnerName(p.getNombre() + " " + p.getAppaterno() + " " + p.getApmaterno());

                    //Otenemos el nombre del pais de la persona
                    Colonias c = em.find(Colonias.class, p.getIdcolonia());
                    Municipios m = em.find(Municipios.class, c.getIdmunicipio());
                    Estados e = em.find(Estados.class, m.getIdestado());
                    Paises pa = em.find(Paises.class, e.getIdpais());
                    dto.setOwnerCountryId("484");//Moneda nacional interncional cambia de codigo a 840
                    
                    String email = "";
                    //Buscamos en la tabla infomracion de terceros para obtener el email
                    try {
                        //Para evitar cualquier error usamos el ciclo por si el tercero no esta en la lista es nuevo 
                         ProductosTercero tercero = em.find(ProductosTercero.class,productNumber);
                         email = tercero.getBeneficiaryemail();
                    } catch (Exception ex) {
                        System.out.println("Error al buscar el tercero en el archivo bakingly:"+ex.getMessage());
                    }
                    if(!email.equals("")){
                      dto.setOwnerEmail(email);//p.getEmail()); 
                    }else{
                        dto.setOwnerEmail(p.getEmail());
                    }
                    dto.setOwnerCity(c.getNombre());
                    dto.setOwnerAddress(c.getNombre() + "," + p.getNumeroext() + "," + p.getNumeroint());
                    //Creamos y llenamos documento para el titular del producto de tercero
                    userDocumentIdDTO ownerDocumentId = new userDocumentIdDTO();
                    ownerDocumentId.setDocumentNumber(p.getPersonasPK().getIdorigen() + p.getPersonasPK().getIdgrupo() + p.getPersonasPK().getIdsocio());//Se a solicitado a Bankingly
                    ownerDocumentId.setDocumentType(3);//Se a solicitado a Bankingly
                    dto.setOwnerDocumentId(ownerDocumentId);
                    dto.setOwnerPhoneNumber(p.getCelular());
                    //Llenamos user document Id
                    userDocument.setDocumentNumber(p.getPersonasPK().getIdorigen() + p.getPersonasPK().getIdgrupo() + p.getPersonasPK().getIdsocio());//
                    userDocument.setDocumentType(3);
                    dto.setUserDocumentId(userDocument);
                    //Llenamos el banco de tercero
                    bancoProductoTercero.setBankId(a.getAuxiliaresPK().getIdorigenp());
                    bancoProductoTercero.setCountryId("484");
                    Origenes o = em.find(Origenes.class, a.getAuxiliaresPK().getIdorigenp());
                    bancoProductoTercero.setDescription(o.getNombre());
                    bancoProductoTercero.setRoutingCode(null);
                    bancoProductoTercero.setHeadQuartersAddress(o.getCalle() + "," + o.getNumeroint() + "," + o.getNumeroext());
                    dto.setBank(bancoProductoTercero);
                    dto.setCorrespondentBank(corresponsalBank);
                }
            }

        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage());

        } finally {
            em.close();
        }
        return dto;

    }

    public Auxiliares validarTercero(String opaTercero, int productType) {
        System.out.println("productTypeId:" + productType);
        EntityManager em = AbstractFacade.conexion();
        String message = "";
        try {
            int o = Integer.parseInt(opaTercero.substring(0, 6));
            int p = Integer.parseInt(opaTercero.substring(6, 11));
            int a = Integer.parseInt(opaTercero.substring(11, 19));
            System.out.println(o + "-" + p + "-" + a);
            AuxiliaresPK auxiPK = new AuxiliaresPK(o, p, a);
            Auxiliares auxiliar = em.find(Auxiliares.class, auxiPK);
            if (auxiliar != null) {
                if (auxiliar.getEstatus() != null) {
                    Productos_bankingly pr = em.find(Productos_bankingly.class, p);
                    if (pr.getProductTypeId() == productType) {
                        return auxiliar;
                    } else {
                        message = "Tipo de producto tercero no coincide";
                    }
                } else {
                    message = "Producto no activo";
                }
            } else {
                message = "Producto tercero no encontrado para local";
            }
        } catch (Exception e) {
            System.out.println("Error validando producto de tercero :" + e.getMessage());
        } finally {
            em.close();
        }
        return null;

    }
    
    public BackendOperationResultDTO tokenSend(String clientBankIdentifier,String numero,String token){
        BackendOperationResultDTO dto = new BackendOperationResultDTO();
        dto.setIsError(true);
        dto.setBackendMessage("Error");
        dto.setBackendReference(null);
        dto.setBackendCode("2");
        dto.setIntegrationProperties("");
        dto.setTransactionIdenty(null);
        
        try {
            EntityManager em = AbstractFacade.conexion();
            //Busco a la persona para comparar que los numeros sea el mismo
            OgsDTO ogs = util.ogs(clientBankIdentifier);
            PersonasPK personaPK = new PersonasPK(ogs.getIdorigen(),ogs.getIdgrupo(),ogs.getIdsocio());
            Persona p = em.find(Persona.class, personaPK);
            if(p.getCelular().trim().equals(numero.trim())){               
                Tablas tb_activo_sms = util2.busquedaTabla(em,"bankingly_banca_movil","smsactivo");
                if(tb_activo_sms.getDato1().trim().equals("1")){
                  PreparaSMS sendSms = new PreparaSMS();
                  String respuesta_envio_token = sendSms.enviarTokenAltaTerceros(em,numero, token);  
                  dto.setIsError(false);
                  dto.setBackendMessage("Token enviado");
                  dto.setBackendCode("1");                  
                }
            }else{
                dto.setBackendMessage("El numero no coincide con nuestros registros");
            }
            
        } catch (Exception e) {
            System.out.println("Error al enviar sms token : "+e.getMessage());
        }
        return dto;
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

}
