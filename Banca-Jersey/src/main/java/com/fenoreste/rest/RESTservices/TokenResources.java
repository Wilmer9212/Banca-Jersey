/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.RESTservices;

import com.fenoreste.rest.ResponseDTO.BackendOperationResultDTO;
import com.fenoreste.rest.dao.TercerosDAO;
import com.github.cliftonlabs.json_simple.JsonObject;
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
@Path("/Authentication")
public class TokenResources {

    @POST
    @Path("/SendSmsToken")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response auth(String cadena) {
        JSONObject request = new JSONObject(cadena);
        JsonObject jsonResponse = new JsonObject();
        String numero = "";
        String token = "";
        String clientBankIdentifier = "";
        try {
            JSONObject input = request.getJSONObject("input");
            JSONArray lista = input.getJSONArray("clientBankIdentifiers");
            clientBankIdentifier = lista.getJSONObject(0).getString("value");
            numero = input.getString("phoneNumber");
            token = input.getString("token");
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }

        try {
            TercerosDAO serviciosTercero = new TercerosDAO();
            BackendOperationResultDTO resultado = serviciosTercero.tokenSend(clientBankIdentifier, numero, token);
            JsonObject backendOperationResult = new JsonObject();
            backendOperationResult.put("backendOperationResult",resultado);
            jsonResponse.put("response", backendOperationResult);
        } catch (Exception e) {
            System.out.println("Error al enviar token en servicio:"+e.getMessage());
        }

        return Response.status(Response.Status.OK).entity(jsonResponse).build();
    }

}
