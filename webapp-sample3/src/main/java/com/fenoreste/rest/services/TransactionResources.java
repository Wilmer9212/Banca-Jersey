/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.services;

import com.fenoreste.rest.ResponseDTO.TransactionToOwnAccountsDTO;
import com.fenoreste.rest.ResponseDTO.destinationDocumentIdDTO;
import com.fenoreste.rest.ResponseDTO.sourceDocumentIdDTO;
import com.fenoreste.rest.ResponseDTO.userDocumentIdDTO;
import com.fenoreste.rest.dao.TransactionDAO;
import com.google.gson.JsonObject;
import com.sun.rowset.internal.InsertRow;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.json.Json;
import javax.json.JsonValue;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.JSONObject;

/**
 *
 * @author Elliot
 */
@Path("/Transaction")
public class TransactionResources {

    @Path("/Insert")
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response insertTransaction(String cadena) {
        JSONObject jsonRecibido = new JSONObject(cadena);
        TransactionDAO dao=new TransactionDAO();

        int transactionTypeId = 0;
        JSONObject insertTransaction=new JSONObject();
        JSONObject destinationDocumentId=new JSONObject();
        JSONObject sourceDocumentId=new JSONObject();
        JSONObject userDocumentId=new JSONObject();
        
        try {
            insertTransaction=jsonRecibido.getJSONObject("inserTransactionInput");
            transactionTypeId=insertTransaction.getInt("transactionTypeId");
            destinationDocumentId = insertTransaction.getJSONObject("destinationDocumentId");
            sourceDocumentId =insertTransaction.getJSONObject("sourceDocumentId");
            userDocumentId=insertTransaction.getJSONObject("userDocumentId");
            System.out.println("llego");
        } catch (Exception e) {
            System.out.println("Error al leer json:" + e.getMessage());
        }

        try {
            if (transactionTypeId == 1) {
                destinationDocumentIdDTO dto1 = new destinationDocumentIdDTO();
                dto1.setIntegrationProperties("{}");
                dto1.setDocumentNumber(destinationDocumentId.getString("documentNumber"));
                dto1.setDocumentType(destinationDocumentId.getString("documentType"));
                
                sourceDocumentIdDTO dto2=new sourceDocumentIdDTO();
                dto2.setDocumentNumber(sourceDocumentId.getString("documentNumber"));
                dto2.setDocumentType(sourceDocumentId.getString("documentType"));
                dto2.setIntegrationProperties("{}");
                
                userDocumentIdDTO dto3=new userDocumentIdDTO();
                dto3.setDocumentNumber(userDocumentId.getString("documentNumber"));
                dto3.setDocumentType(userDocumentId.getString("documentType"));
                dto3.setIntegrationProperties("{}");
                
                System.out.println("dto1:" + dto1);
                System.out.println("dto2:" + dto2);
                System.out.println("dto3:" + dto3);

                TransactionToOwnAccountsDTO dto = new TransactionToOwnAccountsDTO();
                dto.setSubTransactionTypeId(Integer.parseInt(insertTransaction.getString("subTransactionTypeId")));
                dto.setCurrencyId(insertTransaction.getString("currencyId"));
                dto.setValueDate(stringTodate(insertTransaction.getString("valueDate")));
                dto.setTransactionTypeId(insertTransaction.getInt("transactionTypeId"));
                dto.setTransactionStatusId(insertTransaction.getInt("transactionStatusId"));
                dto.setClientBankIdentifier(insertTransaction.getString("clientBankIdentifier"));
                dto.setDebitProductBankIdentifier(insertTransaction.getString("debitProductBankIdentifier"));
                dto.setDebitProductTypeId(insertTransaction.getInt("debitProductTypeId"));
                dto.setDebitCurrencyId(insertTransaction.getString("debitCurrencyId"));
                dto.setCreditProductBankIdentifier(insertTransaction.getString("creditProductBankIdentifier"));
                dto.setCreditProductTypeId(insertTransaction.getInt("creditProductTypeId"));
                dto.setCreditCurrencyId(insertTransaction.getString("creditCurrencyId"));
                dto.setAmount(insertTransaction.getDouble("amount"));
                dto.setNotifyTo(insertTransaction.getString("notifyTo"));
                dto.setNotificationChannelId(insertTransaction.getInt("notificationChannelId"));
                dto.setTransactionId(insertTransaction.getInt("transactionId"));
                dto.setDestinationDocumentId(dto1);
                dto.setDestinationName(insertTransaction.getString("destinationName"));
                dto.setDestinationBank(insertTransaction.getString("destinationBank"));
                dto.setDescription(insertTransaction.getString("description"));
                dto.setBankRoutingNumber(insertTransaction.getString("bankRoutingNumber"));
                dto.setSourceName(insertTransaction.getString("sourceName"));
                dto.setSourceBank(insertTransaction.getString("sourceBank"));
                dto.setSourceDocumentId(dto2);
                dto.setRegulationAmountExceeded(insertTransaction.getBoolean("regulationAmountExceeded"));
                dto.setSourceFunds(insertTransaction.getString("sourceFunds"));
                dto.setDestinationFunds(insertTransaction.getString("destinationFunds"));
                dto.setUserDocumentId(dto3);
                dto.setTransactionCost(insertTransaction.getDouble("transactionCost"));
                dto.setTransactionCostCurrencyId(insertTransaction.getString("transactionCostCurrencyId"));
                dto.setExchangeRate(insertTransaction.getDouble("exchangeRate"));
                dto.setCountryIntermediaryInstitution(insertTransaction.getString("countryIntermediaryInstitution"));
                dto.setRouteNumberIntermediaryInstitution("{}");
                dto.setIntegrationParameters("{}");
                
                
                if(dao.buscar(dto.getDebitProductBankIdentifier(),dto.getClientBankIdentifier(),4.00,dto.getCreditProductBankIdentifier())){
                  String[]arr=dao.transferenciasEtreMisCuentas(dto);
                  JsonObject json=new JsonObject();
                  JsonObject json1=new JsonObject();
                  
                  javax.json.JsonObject build=null;
                  
                  build=Json.createObjectBuilder().add("InsertTransactionResult",Json.createObjectBuilder()
                                                                                     .add("backendOperationResult",Json.createObjectBuilder()
                                                                                                                       .add("integrationProperties",Json.createObjectBuilder().build())
                                                                                                                       .add("backendCode",Json.createObjectBuilder().build())
                                                                                                                       .add("backendMessage",Json.createObjectBuilder().build())
                                                                                                                       .add("backendReference",Json.createObjectBuilder().build())
                                                                                                                       .add("isError",arr[0])
                                                                                                                       .add("idTransaction",arr[1])
                                                                                     ).build())
                          .build();
                                                                                                                        
                                                  
                                                 
                  
                  
                  return Response.status(Response.Status.OK).entity(build).build();
                }
                System.out.println("dto:"+dto);
            }
        } catch (Exception e) {
            System.out.println("Error:"+e.getMessage());
        }
        return null;
    }

    public static Date stringTodate(String fecha) {
        Date date = null;
        try {
            SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
            date = formato.parse(fecha);
        } catch (ParseException ex) {
            System.out.println("Error al convertir fecha:" + ex.getMessage());
        }
        System.out.println("date:" + date);
        return date;
    }
}
