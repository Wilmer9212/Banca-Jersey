/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.RESTservices;

import com.fenoreste.rest.Request.RequestDataOrdenPagoDTO;
import com.fenoreste.rest.ResponseDTO.BackendOperationResultDTO;
import com.fenoreste.rest.ResponseDTO.DocumentIdTransaccionesDTO;
import com.fenoreste.rest.ResponseDTO.TransactionToOwnAccountsDTO;
import com.fenoreste.rest.Util.Authorization;
import com.fenoreste.rest.dao.TransactionDAO;
import com.github.cliftonlabs.json_simple.JsonObject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.util.Base64;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
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

    Authorization auth = new Authorization();

    //BasePath SPEI
    String basePath = "";

    @Path("/Insert")
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response insertTransaction(String cadena, @HeaderParam("authorization") String authCredentials /*, @Context UriInfo urlPath*/) throws IOException {
        BackendOperationResultDTO backendOperationResult = new BackendOperationResultDTO();
        backendOperationResult.setBackendCode("2");
        backendOperationResult.setBackendMessage("Error en transaccion");
        backendOperationResult.setBackendReference(null);
        backendOperationResult.setIntegrationProperties("{}");
        backendOperationResult.setIsError(true);
        backendOperationResult.setTransactionIdenty("0");

        JsonObject response_json_principal = new JsonObject();
        /*================================================================
                Validamos las credenciales mediante la utenticacion basica
        =================================================================*/
 /*if (!auth.isUserAuthenticated(authCredentials)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Credenciales incorrectas").build();
        }*/

        JSONObject jsonRecibido = new JSONObject(cadena.replace("null", "nulo"));
        /*================================================================
                Obtenemos el request y lo pasamos a DTO
        =================================================================*/
        TransactionToOwnAccountsDTO dto = new TransactionToOwnAccountsDTO();
        JsonObject response_json_secundario = new JsonObject();
        JsonObject response_json_3 = new JsonObject();

        try {

            JSONObject insertTransaction = jsonRecibido.getJSONObject("inserTransactionInput");
            JSONObject destinationDocumentId = insertTransaction.getJSONObject("destinationDocumentId");

            DocumentIdTransaccionesDTO dto1 = new DocumentIdTransaccionesDTO();
            dto1.setDocumentNumber(destinationDocumentId.getString("documentNumber"));
            dto1.setDocumentType(destinationDocumentId.getString("documentType"));

            DocumentIdTransaccionesDTO dto2 = new DocumentIdTransaccionesDTO();
            dto1.setDocumentNumber(destinationDocumentId.getString("documentNumber"));
            dto1.setDocumentType(destinationDocumentId.getString("documentType"));

            DocumentIdTransaccionesDTO dto3 = new DocumentIdTransaccionesDTO();
            dto1.setDocumentNumber(destinationDocumentId.getString("documentNumber"));
            dto1.setDocumentType(destinationDocumentId.getString("documentType"));

            System.out.println("fechaaaaaaa:" + insertTransaction.getString("valueDate"));
            dto.setSubTransactionTypeId(Integer.parseInt(insertTransaction.getString("subTransactionTypeId")));
            dto.setCurrencyId(insertTransaction.getString("currencyId"));
            dto.setValueDate(insertTransaction.getString("valueDate"));
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
        } catch (Exception e) {
            backendOperationResult.setBackendCode("2");
            backendOperationResult.setBackendMessage(e.getMessage());
            response_json_3.put("integrationProperties", backendOperationResult.getIntegrationProperties());
            response_json_3.put("backendCode", backendOperationResult.getBackendCode());
            response_json_3.put("backendMessage", backendOperationResult.getBackendMessage());
            response_json_3.put("backendReference", null);
            response_json_3.put("isError", backendOperationResult.isIsError());
            response_json_3.put("transactionType", backendOperationResult.getTransactionIdenty());

            response_json_secundario.put("backendOperationResult", response_json_3);
            response_json_principal.put("InsertTransactionResult", response_json_secundario);
            return Response.status(Response.Status.BAD_REQUEST).entity(response_json_principal).build();
        }

        /*======================================================================
                Si el request que nos llego es el correcto procedemos
          ======================================================================*/
        TransactionDAO dao = new TransactionDAO();
        if (!dao.actividad_horario()) {
            JsonObject obje = new JsonObject();
            obje.put("ERROR", "VERIFIQUE SU HORARIO DE ACTIVIDAD FECHA,HORA O CONTACTE A SU PROVEEEDOR");
            backendOperationResult.setBackendMessage("VERIFIQUE SU HORARIO DE ACTIVIDAD FECHA,HORA O CONTACTE A SU PROVEEEDOR");
            response_json_3.put("integrationProperties", null);
            response_json_3.put("backendCode", backendOperationResult.getBackendCode());
            response_json_3.put("backendMessage", backendOperationResult.getBackendMessage());
            response_json_3.put("backendReference", null);
            response_json_3.put("isError", backendOperationResult.isIsError());
            response_json_3.put("transactionType", backendOperationResult.getTransactionIdenty());

            response_json_secundario.put("backendOperationResult", response_json_3);
            response_json_principal.put("InsertTransactionResult", response_json_secundario);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response_json_principal).build();
        }
        try {
            System.out.println("Accediendo a trasnferencias con subTransactionType=" + dto.getSubTransactionTypeId() + ",TransactionId:" + dto.getTransactionTypeId());

            //Si subtransactionType es 1 y transactionType es 1: El tipo de transaccion es es entre mis cuentas
            if (dto.getSubTransactionTypeId() == 1 && dto.getTransactionTypeId() == 1) {
                backendOperationResult = dao.transferencias(dto, 1, null);
            }
            //Si subtransactionType es 2 y transactionType es 1: El tipo de transaccion es a terceros
            if (dto.getSubTransactionTypeId() == 2 && dto.getTransactionTypeId() == 1) {
                backendOperationResult = dao.transferencias(dto, 2, null);
            }
            //Si subtransactionType es 9 y transactionType es 6: El tipo de transaccion es es un pago a prestamos 
            if (dto.getSubTransactionTypeId() == 9 && dto.getTransactionTypeId() == 6) {
                backendOperationResult = dao.transferencias(dto, 3, null);
            }
            //Si es un pago a prestamo tercero
            if (dto.getSubTransactionTypeId() == 10 && dto.getTransactionTypeId() == 6) {
                backendOperationResult = dao.transferencias(dto, 4, null);
            }
            //Si es una trasnferencia SPEI
            if (dto.getSubTransactionTypeId() == 3 && dto.getTransactionTypeId() == 1) {

                if (!dao.actividad_horario_spei()) {
                    backendOperationResult.setBackendMessage("VERIFIQUE SU HORARIO DE ACTIVIDAD FECHA,HORA O CONTACTE A SU PROVEEEDOR");
                    response_json_3.put("integrationProperties", null);
                    response_json_3.put("backendCode", backendOperationResult.getBackendCode());
                    response_json_3.put("backendMessage", backendOperationResult.getBackendMessage());
                    response_json_3.put("backendReference", null);
                    response_json_3.put("isError", backendOperationResult.isIsError());
                    response_json_3.put("transactionType", backendOperationResult.getTransactionIdenty());

                    response_json_secundario.put("backendOperationResult", response_json_3);
                    response_json_principal.put("InsertTransactionResult", response_json_secundario);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response_json_principal).build();
                }
                
                
                //Consumimos mis servicios de SPEI que tengo en otro proyecto(CSN0)
                RequestDataOrdenPagoDTO ordenReque = new RequestDataOrdenPagoDTO();
                ordenReque.setClienteClabe(dto.getDebitProductBankIdentifier());//Opa origen como cuenta clabe en el metodo spei se busca la clave
                ordenReque.setConceptoPago(dto.getDescription());
                ordenReque.setCuentaBeneficiario(dto.getCreditProductBankIdentifier());//La clabe del beneficiario
                ordenReque.setInstitucionContraparte(dto.getDestinationBank());
                ordenReque.setMonto(dto.getAmount());
                ordenReque.setNombreBeneficiario(dto.getDestinationName());
                ordenReque.setRfcCurpBeneficiario(dto.getDestinationDocumentId().getDocumentNumber());
                ordenReque.setOrdernante(dto.getClientBankIdentifier());

                backendOperationResult = dao.transferencias(dto, 5, ordenReque);

            }

            response_json_3.put("integrationProperties", null);
            response_json_3.put("backendCode", backendOperationResult.getBackendCode());
            response_json_3.put("backendMessage", backendOperationResult.getBackendMessage());
            response_json_3.put("backendReference", null);
            response_json_3.put("isError", backendOperationResult.isIsError());
            response_json_3.put("transactionType", backendOperationResult.getTransactionIdenty());

            response_json_secundario.put("backendOperationResult", response_json_3);
            response_json_principal.put("InsertTransactionResult", response_json_secundario);

        } catch (Exception e) {
            backendOperationResult.setBackendMessage(e.getMessage());
            response_json_3.put("integrationProperties", null);
            response_json_3.put("backendCode", backendOperationResult.getBackendCode());
            response_json_3.put("backendMessage", backendOperationResult.getBackendMessage());
            response_json_3.put("backendReference", null);
            response_json_3.put("isError", backendOperationResult.isIsError());
            response_json_3.put("transactionType", backendOperationResult.getTransactionIdenty());

            response_json_secundario.put("backendOperationResult", response_json_3);
            response_json_principal.put("InsertTransactionResult", response_json_secundario);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response_json_principal).build();
        }
        return Response.status(Response.Status.OK).entity(response_json_principal).build();
    }

    @POST
    @Path("/Voucher")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response voucher(String cadena) {
        JSONObject request = new JSONObject(cadena);
        String idTransaccion = "";
        try {
            idTransaccion = request.getString("transactionVoucherIdentifier");
        } catch (Exception e) {
            System.out.println("Error al obtener Json Request:" + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
        TransactionDAO dao = new TransactionDAO();
        JsonObject jsonMessage = new JsonObject();
        try {
            String fileRoute = dao.voucherFileCreate(idTransaccion);
            if (!fileRoute.equals("")) {
                File file = new File(fileRoute);
                if (file.exists()) {
                    byte[] input_file = Files.readAllBytes(Paths.get(fileRoute));
                    byte[] encodedBytesFile = Base64.getEncoder().encode(input_file);
                    String bytesFileId = new String(encodedBytesFile);
                    jsonMessage.put("productBankStatementFile", bytesFileId);
                    jsonMessage.put("productBankStatementFileName", file.getName());

                } else {
                    jsonMessage.put("Error", "EL ARCHIVO QUE INTENTA DESCARGAR NO EXISTE");
                }
            }
        } catch (Exception e) {
            jsonMessage.put("Error", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(jsonMessage).build();
        }
        return Response.status(Response.Status.OK).entity(jsonMessage).build();
    }

    @POST
    @Path("/ejecutaSpei")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response ejecutarOrdenSPei(String cadena) {
        JSONObject request_json = new JSONObject(cadena);
        int idorden = request_json.getInt("id");
        String folio = request_json.getString("folioOrigen");
        String estado = request_json.getString("estado");
        String causa = request_json.getString("causaDevolucion");
        System.out.println("Cadena :" + cadena);
        TransactionDAO dao = new TransactionDAO();
        if (!dao.actividad_horario_spei()) {
            JsonObject obje = new JsonObject();
            obje.put("mensaje", "VERIFIQUE SU HORARIO DE ACTIVIDAD FECHA,HORA O CONTACTE A SU PROVEEEDOR");
            return Response.ok(obje).build();
        }

        String mensaje = dao.ejecutaOrdenSPEI(idorden, folio, estado, causa);
        System.out.println("mensaje:" + mensaje);
        JsonObject response = new JsonObject();
        response.put("mensaje", mensaje);
        return Response.ok(response).build();

    }

    public static Timestamp stringTodate(String fecha) {
        Timestamp time = null;

        Timestamp tm = Timestamp.valueOf(fecha);
        time = tm;
        System.out.println("date:" + time);
        return time;
    }

    
      public static String limpiarAcentos(String cadena) {
        String limpio = null;
        if (cadena != null) {
            String valor = cadena;
            valor = valor.toUpperCase();
            // Normalizar texto para eliminar acentos, dieresis, cedillas y tildes
            limpio = Normalizer.normalize(valor, Normalizer.Form.NFD);
            // Quitar caracteres no ASCII excepto la enie, interrogacion que abre, exclamacion que abre, grados, U con dieresis.
            limpio = limpio.replaceAll("[^\\p{ASCII}(ñ\u0303)(n\u0303)(\u00A1)(\u00BF)(\u00B0)(U\u0308)(u\u0308)]", "");
            // Regresar a la forma compuesta, para poder comparar la enie con la tabla de valores
            limpio = Normalizer.normalize(limpio, Normalizer.Form.NFC);
        }
        return limpio.toLowerCase();
    }

}
