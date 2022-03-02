package com.fenoreste.rest.RESTservices;

import com.fenoreste.rest.DTO.OpaDTO;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.fenoreste.rest.ResponseDTO.AccountDetailsDTO;
import com.fenoreste.rest.ResponseDTO.AccountLast5MovementsDTO;
import com.fenoreste.rest.ResponseDTO.AccountMovementsDTO;
import com.fenoreste.rest.Util.Utilidades;
import com.fenoreste.rest.Util.UtilidadesGenerales;
import com.fenoreste.rest.dao.AccountsDAO;
import com.github.cliftonlabs.json_simple.JsonObject;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.HeaderParam;
import org.json.JSONObject;

@Path("/Account")
public class AccountsResources {

    UtilidadesGenerales util = new UtilidadesGenerales();
    Utilidades util2 = new Utilidades();

    @POST
    @Path("/Details")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    @Consumes({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response getAccountDetails(String cadenaJson) {

        String accountId = "";
        JsonObject Json_De_Error = new JsonObject();
        AccountsDAO metodos = new AccountsDAO();
        if (!metodos.actividad_horario()) {
            Json_De_Error.put("ERROR", "VERIFIQUE SU HORARIO DE ACTIVIDAD FECHA,HORA O CONTACTE A SU PROVEEEDOR");

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Json_De_Error).build();
        }
        try {
            JSONObject jsonRecibido = new JSONObject(cadenaJson);
            accountId = jsonRecibido.getString("productBankIdentifier");
        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage());
        }

        OpaDTO opa = util2.opa(accountId);
        if (util.validacionSopar(opa.getIdorigenp(), opa.getIdproducto(), opa.getIdauxiliar(), 2)) {
            Json_De_Error.put("ERROR", "SOCIO BLOQUEADO");
            return Response.status(Response.Status.UNAUTHORIZED).entity(Json_De_Error).build();
        }
        try {
            boolean bande = true;
            //Reccorremos el accountId para veru que solo sean numeros que trae
            for (int i = 0; i < accountId.length(); i++) {
                if (Character.isLetter(accountId.charAt(i))) {
                    bande = false;
                    System.out.println("Charat:" + accountId.charAt(i));
                }
            }
            System.out.println("Bande:" + bande);
            //Si no trae letras en Identificador de producto(OPA) y la longitud es igual a lo que se maneja en la caja 
            if (bande == true && accountId.length() == 19) {
                AccountDetailsDTO cuenta = null;
                try {
                    cuenta = metodos.GetAccountDetails(accountId);
                    if (cuenta != null) {
                        return Response.status(Response.Status.OK).entity(cuenta).build();
                    } else {
                        Json_De_Error.put("Error", "ERROR PRODUCTO NO ENCONTRADO");
                        return Response.status(Response.Status.BAD_REQUEST).entity(Json_De_Error).build();
                    }
                } catch (Exception e) {
                    return Response.status(Response.Status.BAD_REQUEST).entity(Json_De_Error).build();
                }

            } else {
                Json_De_Error.put("Error", "FORMATO DE INDETIFICADOR INVALIDO");
                return Response.status(Response.Status.BAD_REQUEST).entity(Json_De_Error).build();
            }
        } catch (Exception e) {

            return null;
        }

    }

    @POST
    @Path("/Last5Movements")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    @Consumes({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response getAccountLast5Movements(String cadenaJson) {
        System.out.println("Cadena Json:" + cadenaJson);
        JSONObject jsonRecibido = new JSONObject(cadenaJson);
        JsonObject Json_De_Error = new JsonObject();
        String accountId = jsonRecibido.getString("productBankIdentifier");
        AccountsDAO metodos = new AccountsDAO();
        if (!metodos.actividad_horario()) {
            Json_De_Error.put("ERROR", "VERIFIQUE SU HORARIO DE ACTIVIDAD FECHA,HORA O CONTACTE A SU PROVEEEDOR");
            return Response.status(Response.Status.BAD_REQUEST).entity(Json_De_Error).build();
        }
        try {
            boolean bandera = true;
            for (int i = 0; i < accountId.length(); i++) {
                if (Character.isLetter(accountId.charAt(i))) {
                    bandera = false;
                }
            }
            OpaDTO opa = util2.opa(accountId);
            if (util.validacionSopar(opa.getIdorigenp(), opa.getIdproducto(), opa.getIdauxiliar(), 2)) {
                Json_De_Error.put("ERROR", "SOCIO BLOQUEADO");
                return Response.status(Response.Status.UNAUTHORIZED).entity(Json_De_Error).build();
            }

            List<AccountLast5MovementsDTO> cuentas = new ArrayList<AccountLast5MovementsDTO>();
            if (bandera) {
                
                    cuentas = metodos.getAccountLast5Movements(accountId);
                    if (cuentas.size() > 0) {
                        JsonObject cuentasJson = new JsonObject();
                        cuentasJson.put("Last5Movements", cuentas);
                        return Response.status(Response.Status.OK).entity(cuentasJson).build();
                    } else {
                        Json_De_Error.put("Error", "PRODUCTO NO ENCONTRADO");
                        return Response.status(Response.Status.BAD_REQUEST).entity(Json_De_Error).build();
                    }


            } else {
                Json_De_Error.put("Error", "CARACTERES INVALIDOS EN ENTRADA");
                return Response.status(Response.Status.BAD_REQUEST).entity(Json_De_Error).build();
            }
        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/Movements")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    @Consumes({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response getAccountMovements(String cadenaJson) {
        System.out.println("===============ENTRANDO A MOVEMENTS=================");
        AccountsDAO dao = new AccountsDAO();
        String ProductBankIdentifier = "";
        String DateFromFilter = null;
        String DateToFilter = null;
        int PageSize = 0;
        int PageStartIndex = 0;
        JsonObject Error = new JsonObject();
        String orderBy = "";
        int canal = 0;
        if (!dao.actividad_horario()) {
            Error.put("ERROR", "VERIFIQUE SU HORARIO DE ACTIVIDAD FECHA,HORA O CONTACTE A SU PROVEEEDOR");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Error).build();
        }
        //System.out.println("Peticion recibida:"+cadenaJson);

        try {
            try {
                JSONObject jsonRecibido = new JSONObject(cadenaJson);
                ProductBankIdentifier = jsonRecibido.getString("productBankIdentifier");
                DateFromFilter = jsonRecibido.getString("dateFromFilter");
                DateToFilter = jsonRecibido.getString("dateToFilter");
                JSONObject paging = jsonRecibido.getJSONObject("paging");
                if (!paging.toString().contains("null")) {
                    PageSize = paging.getInt("pageSize");
                    PageStartIndex = paging.getInt("pageStartIndex");
                    orderBy = paging.getString("orderByField");
                    canal = jsonRecibido.getInt("ChannelId");
                }

            } catch (Exception e) {
                Error.put("Error", "Error en parametros JSON:" + e.getMessage());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Error).build();
            }
            int count = 0;

            OpaDTO opa = util2.opa(ProductBankIdentifier);
            if (util.validacionSopar(opa.getIdorigenp(), opa.getIdproducto(), opa.getIdauxiliar(), 2)) {
                Error.put("ERROR", "SOCIO BLOQUEADO");
                return Response.status(Response.Status.UNAUTHORIZED).entity(Error).build();
            }

            try {
                List<AccountMovementsDTO> MiListaDTO = null;
                System.out.println("MOVEMENTS fechas:" + DateFromFilter + " - " + DateToFilter);
                MiListaDTO = dao.getAccountMovements(ProductBankIdentifier, DateFromFilter, DateToFilter, PageSize, PageStartIndex, orderBy, canal);
                com.github.cliftonlabs.json_simple.JsonObject j = new com.github.cliftonlabs.json_simple.JsonObject();

                count = dao.contadorAuxD(ProductBankIdentifier, DateFromFilter, DateToFilter);
                if (count > 0) {
                    j.put("MovementsCount", count);
                    j.put("Movements", MiListaDTO);
                } else {
                    Error.put("Error", "SIN REGISTROS PARA CUENTA:" + ProductBankIdentifier);
                    return Response.status(Response.Status.NO_CONTENT).entity(Error).build();
                }

                return Response.status(Response.Status.OK).entity(j).build();
            } catch (Exception e) {
                Error.put("Error", "SOCIOS NO ENCONTRADOS");
                System.out.println("Error al convertir cadena a JSON:" + e.getMessage());
                return Response.status(Response.Status.NO_CONTENT).entity(Error).build();

            }
        } catch (Exception e) {
            System.out.println("Error al consumir:" + e.getMessage());

        }
        return null;
    }
}
