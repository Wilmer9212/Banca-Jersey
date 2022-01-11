/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.RESTservices;

import com.fenoreste.rest.DTO.OgsDTO;
import com.fenoreste.rest.DTO.OpaDTO;
import com.fenoreste.rest.ResponseDTO.BackendOperationResultDTO;
import com.fenoreste.rest.ResponseDTO.Bank;
import com.fenoreste.rest.ResponseDTO.ThirdPartyProductDTO;
import com.fenoreste.rest.ResponseDTO.userDocumentIdDTO;
import com.fenoreste.rest.Util.Utilidades;
import com.fenoreste.rest.Util.UtilidadesGenerales;
import com.fenoreste.rest.dao.TercerosDAO;
import com.github.cliftonlabs.json_simple.JsonObject;
import java.util.ArrayList;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author wilmer
 */
@Path("/Third")
public class TercerosResources {

    UtilidadesGenerales util = new UtilidadesGenerales();
    Utilidades util2 = new Utilidades();

    @Path("/Party/Product/Validate")
    @POST
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    @Consumes({javax.ws.rs.core.MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response validarTerceros(String cadena) {
        System.out.println("Request Terceros:" + cadena);

        ArrayList<String> clientBankIdentifiers_ = new ArrayList<>();
        String thirdPartyProductNumber_;
        String thirdPartyProductBankIdentifier_;
        String alias_;
        String currencyId_;
        Integer transactionSubType_;
        Integer thirdPartyProductType_;
        Integer productType_;
        String ownerName_;
        String ownerCountryId_;
        String ownerEmail_;
        String ownerCity_;
        String ownerAddress_;
        userDocumentIdDTO ownerDocumentId_ = new userDocumentIdDTO();
        String ownerPhoneNumber_;
        Bank bank_ = new Bank();
        Bank correspondentBank_ = new Bank();
        userDocumentIdDTO userDocumentId_ = new userDocumentIdDTO();
        ThirdPartyProductDTO dtoTercero = new ThirdPartyProductDTO();
        try {
            JSONObject request = new JSONObject(cadena);
            //Obtenemos la lista de clientes en el json
            JSONArray clientBankIdentifiers = request.getJSONArray("clientBankIdentifiers");
            for (int i = 0; i < clientBankIdentifiers.length(); i++) {
                clientBankIdentifiers_.add(clientBankIdentifiers.getString(i));
            }
            thirdPartyProductNumber_ = request.getString("thirdPartyProductNumber");
            thirdPartyProductBankIdentifier_ = request.getString("thirdPartyProductBankIdentifier");
            alias_ = request.getString("alias");
            currencyId_ = request.getString("currencyId");
            transactionSubType_ = request.getInt("transactionSubType");
            thirdPartyProductType_ = request.getInt("thirdPartyProductType");
            productType_ = request.getInt("productType");
            ownerName_ = request.getString("ownerName");
            ownerCountryId_ = request.getString("ownerCountryId");
            //ownerEmail_ = request.getString("ownerEmail");
            ownerCity_ = request.getString("ownerCity");
            ownerAddress_ = request.getString("ownerAddress");
            ownerPhoneNumber_ = request.getString("ownerPhoneNumber");

            //Obtenemos el objeto userDocumentId para ownerDocumentId
            JSONObject ownerDocumentId = request.getJSONObject("ownerDocumentId");
            ownerDocumentId_.setDocumentNumber(ownerDocumentId.getInt("documentNumber"));
            ownerDocumentId_.setDocumentType(ownerDocumentId.getInt("documentType"));
            ownerDocumentId_.setIntegrationProperties(ownerDocumentId.getString("integrationProperties"));

            //Obtenemos el objeto Bank para bank
            JSONObject bank = request.getJSONObject("bank");
            bank_.setBankId(bank.getInt("bankId"));
            bank_.setCountryId(bank.getString("countryId"));
            bank_.setDescription(bank.getString("description"));
            bank_.setHeadQuartersAddress(bank.getString("headQuartersAddress"));
            bank_.setRoutingCode(bank.getString("routingCode"));

            //Obtenemos el objeto Bank para correspondentBank
            JSONObject correspondentBank = request.getJSONObject("correspondentBank");
            correspondentBank_.setBankId(correspondentBank.getInt("bankId"));
            correspondentBank_.setCountryId(correspondentBank.getString("countryId"));
            correspondentBank_.setDescription(correspondentBank.getString("description"));
            correspondentBank_.setHeadQuartersAddress(correspondentBank.getString("headQuartersAddress"));
            correspondentBank_.setRoutingCode(correspondentBank.getString("routingCode"));

            //Obtenemos el objeto userDocumentId para ownerDocumentId
            JSONObject userDocumentId = request.getJSONObject("userDocumentId");
            userDocumentId_.setDocumentNumber(userDocumentId.getInt("documentNumber"));
            userDocumentId_.setDocumentType(userDocumentId.getInt("documentType"));
            userDocumentId_.setIntegrationProperties(userDocumentId.getString("integrationProperties"));

            dtoTercero.setClientBankIdentifiers(clientBankIdentifiers_);
            dtoTercero.setThirdPartyProductNumber(thirdPartyProductNumber_);
            dtoTercero.setThirdPartyProductBankIdentifier(thirdPartyProductBankIdentifier_);
            dtoTercero.setAlias(alias_);
            dtoTercero.setCurrencyId(currencyId_);
            dtoTercero.setTransactionSubType(transactionSubType_);
            dtoTercero.setOwnerName(ownerName_);
            dtoTercero.setOwnerCountryId(ownerCountryId_);
            dtoTercero.setOwnerEmail("NA");
            dtoTercero.setOwnerCity(ownerCity_);
            dtoTercero.setOwnerCity(ownerCity_);
            dtoTercero.setOwnerAddress(ownerAddress_);
            dtoTercero.setOwnerDocumentId(ownerDocumentId_);
            dtoTercero.setOwnerPhoneNumber(ownerPhoneNumber_);
            dtoTercero.setBank(bank_);
            dtoTercero.setCorrespondentBank(correspondentBank_);
            dtoTercero.setUserDocumentId(userDocumentId_);
            dtoTercero.setThirdPartyProductType(thirdPartyProductType_);
            dtoTercero.setProductType(productType_);
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
        com.github.cliftonlabs.json_simple.JsonObject jsonR = new JsonObject();
        TercerosDAO dao = new TercerosDAO();
        if (!dao.actividad_horario()) {
            jsonR.put("ERROR", "VERIFIQUE SU HORARIO DE ACTIVIDAD FECHA,HORA O CONTACTE A SU PROVEEEDOR");
            return Response.status(Response.Status.BAD_REQUEST).entity(jsonR).build();
        }

        OpaDTO opa = util2.opa(dtoTercero.getThirdPartyProductBankIdentifier());
        if (util.validacionSopar(opa.getIdorigenp(), opa.getIdproducto(), opa.getIdauxiliar(), 2)) {
            jsonR.put("ERROR", "SOCIO BLOQUEADO");
            return Response.status(Response.Status.BAD_REQUEST).entity(jsonR).build();
        }

        try {
            BackendOperationResultDTO response = dao.validarProductoTerceros(dtoTercero);
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.put("BackendOperationResult", response);
            return Response.status(Response.Status.OK).entity(jsonResponse).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();

        }
    }

    @Path("/Party/Account/ProductOwnerAndCurrency")
    @POST
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    @Consumes({javax.ws.rs.core.MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response altaTerceros(String cadena) {
        JSONObject jsonRequest = new JSONObject(cadena);
        String productNumber_ = "";
        Integer productTypeId_ = 0;
        Integer thirdPartyProductType_ = 0;
        //validamos que nuestro request este bien formado
        try {
            productNumber_ = jsonRequest.getString("productNumber");
            productTypeId_ = jsonRequest.getInt("productTypeId");
            //Otengo el objeto de productOwnerDocumentId
            JSONObject productOwnerDocumentId = jsonRequest.getJSONObject("productOwnerDocumentId");
            String documentNumber_ = productOwnerDocumentId.getString("documentNumber");
            String documentType_ = productOwnerDocumentId.getString("documentType");
            thirdPartyProductType_ = jsonRequest.getInt("thirdPartyProductType");
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }

        TercerosDAO dao = new TercerosDAO();

        JsonObject error = new JsonObject();
        if (!dao.actividad_horario()) {
            error.put("ERROR", "VERIFIQUE SU HORARIO DE ACTIVIDAD FECHA,HORA O CONTACTE A SU PROVEEEDOR");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        OpaDTO opa = util2.opa(productNumber_);
        if (util.validacionSopar(opa.getIdorigenp(), opa.getIdproducto(), opa.getIdauxiliar(), 2)) {
            error.put("ERROR", "SOCIO BLOQUEADO");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        userDocumentIdDTO documento = new userDocumentIdDTO();
        try {
            ThirdPartyProductDTO dto = dao.cosultaProductosTerceros(productNumber_, productTypeId_, documento, thirdPartyProductType_);
            JsonObject third = new JsonObject();
            third.put("productOwnerAndCurrency", dto);
            return Response.status(Response.Status.OK).entity(third).build();
        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage());

        }
        return null;
    }

}
