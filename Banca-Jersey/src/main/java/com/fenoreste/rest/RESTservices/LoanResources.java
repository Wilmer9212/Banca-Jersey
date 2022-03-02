/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.RESTservices;

import com.fenoreste.rest.DTO.OpaDTO;
import com.fenoreste.rest.ResponseDTO.LoanDTO;
import com.fenoreste.rest.ResponseDTO.LoanFee;
import com.fenoreste.rest.ResponseDTO.LoanPayment;
import com.fenoreste.rest.ResponseDTO.LoanRate;
import com.fenoreste.rest.Util.Utilidades;
import com.fenoreste.rest.Util.UtilidadesGenerales;
import com.fenoreste.rest.dao.LoanDAO;
import com.github.cliftonlabs.json_simple.JsonObject;
import java.util.List;
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
@Path("/Loan")
public class LoanResources {

    UtilidadesGenerales util = new UtilidadesGenerales();
    Utilidades util2 = new Utilidades();

    @POST
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    @Consumes({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response loan(String cadena) {

        String productBankIdentifier = "";
        JsonObject Error = new JsonObject();
        LoanDAO dao = new LoanDAO();
        if (!dao.actividad_horario()) {
            Error.put("ERROR", "VERIFIQUE SU HORARIO DE ACTIVIDAD FECHA,HORA O CONTACTE A SU PROVEEEDOR");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Error).build();
        }
        try {
            JSONObject jsonRecibido = new JSONObject(cadena);
            productBankIdentifier = jsonRecibido.getString("productBankIdentifier");
            int o = Integer.parseInt(productBankIdentifier.substring(0, 6));
            int p = Integer.parseInt(productBankIdentifier.substring(6, 11));
            int a = Integer.parseInt(productBankIdentifier.substring(11, 19));
            System.out.println("" + o + "-" + p + "-" + a);
            if (dao.tipoproducto(p) != 2) {
                Error.put("Error", "Producto no valido para LOANS");
                return Response.status(Response.Status.BAD_REQUEST).entity(Error).build();
            }

            OpaDTO opa = util2.opa(productBankIdentifier);
            if (util.validacionSopar(opa.getIdorigenp(), opa.getIdproducto(), opa.getIdauxiliar(), 2)) {
                Error.put("ERROR", "SOCIO BLOQUEADO");
                return Response.status(Response.Status.UNAUTHORIZED).entity(Error).build();
            }

        } catch (Exception e) {
            Error.put("Error", "Error en parametros JSON:" + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Error).build();
        }

        try {
            LoanDTO loan = dao.Loan(productBankIdentifier);
            JsonObject j = new JsonObject();
            System.out.println("Loan : " + loan);
            j.put("Loan", loan);
            return Response.status(Response.Status.OK).entity(j).build();
        } catch (Exception e) {
            Error.put("Error", "SOCIOS NO ENCONTRADOS");
            System.out.println("Error al convertir cadena a JSON:" + e.getMessage());
            return Response.status(Response.Status.NO_CONTENT).entity(Error).build();

        }

    }

    @POST
    @Path("/Fee")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response loanFee(String cadena) {
        String productBankIdentifier = "";
        JsonObject Error = new JsonObject();
        int feeNumber = 0;
        LoanDAO dao = new LoanDAO();
        if (!dao.actividad_horario()) {
            Error.put("ERROR", "VERIFIQUE SU HORARIO DE ACTIVIDAD FECHA,HORA O CONTACTE A SU PROVEEEDOR");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Error).build();
        }
        try {
            JSONObject jsonRecibido = new JSONObject(cadena);
            productBankIdentifier = jsonRecibido.getString("productBankIdentifier");
            feeNumber = jsonRecibido.getInt("feeNumber");
            int o = Integer.parseInt(productBankIdentifier.substring(0, 6));
            int p = Integer.parseInt(productBankIdentifier.substring(6, 11));
            int a = Integer.parseInt(productBankIdentifier.substring(11, 19));
            if (dao.tipoproducto(p) != 2) {
                Error.put("Error", "Producto no valido para LOANS");
                return Response.status(Response.Status.BAD_REQUEST).entity(Error).build();
            }
        } catch (Exception e) {
            Error.put("Error", "Error en parametros JSON");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Error).build();
        }

        OpaDTO opa = util2.opa(productBankIdentifier);
        if (util.validacionSopar(opa.getIdorigenp(), opa.getIdproducto(), opa.getIdauxiliar(), 2)) {
            Error.put("ERROR", "SOCIO BLOQUEADO");
            return Response.status(Response.Status.UNAUTHORIZED).entity(Error).build();
        }

        try {
            LoanFee loan = dao.LoanFee(productBankIdentifier, feeNumber);
            JsonObject j = new JsonObject();
            j.put("Fee", loan);
            return Response.status(Response.Status.OK).entity(j).build();
        } catch (Exception e) {
            Error.put("Error", "SOCIOS NO ENCONTRADOS");
            System.out.println("Error al convertir cadena a JSON:" + e.getMessage());
            return Response.status(Response.Status.NO_CONTENT).entity(Error).build();

        }
    }

    @POST
    @Path("/Fees")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response loanFees(String cadena) {
        System.out.println("Peticion loanFeesResult:" + cadena);
        String productBankIdentifier = "";
        JsonObject Error = new JsonObject();
        int feeStatus = 0, pageSize = 0, pageStartIndex = 0;

        LoanDAO dao = new LoanDAO();
        if (!dao.actividad_horario()) {
            Error.put("ERROR", "VERIFIQUE SU HORARIO DE ACTIVIDAD FECHA,HORA O CONTACTE A SU PROVEEEDOR");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Error).build();
        }
        try {
            String order = "";
            int ChannelId = 0;
            try {
                JSONObject jsonRecibido = new JSONObject(cadena);
                productBankIdentifier = jsonRecibido.getString("productBankIdentifier");
                feeStatus = jsonRecibido.getInt("feesStatus");
                JSONObject json = jsonRecibido.getJSONObject("paging");
                ChannelId = jsonRecibido.getInt("ChannelId");
                if (!json.toString().contains("null")) {
                    pageSize = json.getInt("pageSize");
                    pageStartIndex = json.getInt("pageStartIndex");
                    order = json.getString("orderByField");
                }else{
                    pageSize = 10;
                    pageStartIndex = 0;
                }
            } catch (Exception e) {
                Error.put("Error", "Error en parametros JSON");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Error).build();
            }

            OpaDTO opa = util2.opa(productBankIdentifier);
            if (util.validacionSopar(opa.getIdorigenp(), opa.getIdproducto(), opa.getIdauxiliar(), 2)) {
                Error.put("ERROR", "SOCIO BLOQUEADO");
                return Response.status(Response.Status.UNAUTHORIZED).entity(Error).build();
            }
            System.out.println("loanFeeStatus:" + feeStatus + ",pageSize:" + pageSize + ",pageStartIndex:" + pageStartIndex + ",orderByField:" + order);
            try {
                List<LoanFee> loan = dao.LoanFees(productBankIdentifier, feeStatus, pageSize, pageStartIndex, order,ChannelId);
                JsonObject j = new JsonObject();
                 int t = dao.contadorFeesPayments(productBankIdentifier,1,feeStatus);
                    j.put("Fees", loan);
                    j.put("LoanFeesCount", t);                
                return Response.status(Response.Status.OK).entity(j).build();
                
            } catch (Exception e) {
                Error.put("Error", "SOCIOS NO ENCONTRADOS");
                System.out.println("Error al convertir cadena a JSON:" + e.getMessage());
                return Response.status(Response.Status.NO_CONTENT).entity(Error).build();

            }
        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage());
        }
        return null;
    }

    @POST
    @Path("/Rates")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response loanRates(String cadena) {
        System.out.println("Cadena:" + cadena);
        String productBankIdentifier = "";
        JsonObject Error = new JsonObject();
        int pageSize = 0, pageStartIndex = 0;
        LoanDAO dao = new LoanDAO();
        if (!dao.actividad_horario()) {
            Error.put("ERROR", "VERIFIQUE SU HORARIO DE ACTIVIDAD FECHA,HORA O CONTACTE A SU PROVEEEDOR");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Error).build();
        }
        try {
            JSONObject jsonRecibido = new JSONObject(cadena);
            productBankIdentifier = jsonRecibido.getString("productBankIdentifier");
            JSONObject paging = jsonRecibido.getJSONObject("paging");
            if (!paging.toString().contains("null")) {
                pageSize = paging.getInt("pageSize");
                pageStartIndex = paging.getInt("pageStartIndex");
            }

            int o = Integer.parseInt(productBankIdentifier.substring(0, 6));
            int p = Integer.parseInt(productBankIdentifier.substring(6, 11));
            int a = Integer.parseInt(productBankIdentifier.substring(11, 19));
            if (dao.tipoproducto(p) != 2) {
                Error.put("Error", "Producto no valido para LOANS");
                return Response.status(Response.Status.BAD_REQUEST).entity(Error).build();
            }
        } catch (Exception e) {
            Error.put("Error", "Error en parametros JSON:" + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Error).build();
        }

        OpaDTO opa = util2.opa(productBankIdentifier);
        if (util.validacionSopar(opa.getIdorigenp(), opa.getIdproducto(), opa.getIdauxiliar(), 2)) {
            Error.put("ERROR", "SOCIO BLOQUEADO");
            return Response.status(Response.Status.UNAUTHORIZED).entity(Error).build();
        }

        try {
            List<LoanRate> loan = dao.LoanRates(productBankIdentifier, pageSize, pageStartIndex);
            JsonObject j = new JsonObject();
            j.put("Rates", loan);
            j.put("LoanRatesCount", loan.size());
            return Response.status(Response.Status.OK).entity(j).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NO_CONTENT).entity(Error).build();
        }
    }

    @POST
    @Path("/Payments")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response loanPayments(String cadena) {
        System.out.println("Cadena:" + cadena);
        String productBankIdentifier = "";
        JsonObject Error = new JsonObject();
        int pageSize = 0, pageStartIndex = 0,channelId = 0;
        LoanDAO dao = new LoanDAO();
        if (!dao.actividad_horario()) {
            Error.put("ERROR", "VERIFIQUE SU HORARIO DE ACTIVIDAD FECHA,HORA O CONTACTE A SU PROVEEEDOR");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Error).build();
        }
        try {
            JSONObject jsonRecibido = new JSONObject(cadena);
            productBankIdentifier = jsonRecibido.getString("productBankIdentifier");
            JSONObject paging = jsonRecibido.getJSONObject("paging");
            channelId = jsonRecibido.getInt("ChannelId");
            if(!paging.toString().contains("null")){
                pageSize = paging.getInt("pageSize");
                pageStartIndex = paging.getInt("pageStartIndex");
            }            
            int o = Integer.parseInt(productBankIdentifier.substring(0, 6));
            int p = Integer.parseInt(productBankIdentifier.substring(6, 11));
            int a = Integer.parseInt(productBankIdentifier.substring(11, 19));
            if (dao.tipoproducto(p) != 2) {
                Error.put("Error", "Producto no valido para LOANS");
                return Response.status(Response.Status.BAD_REQUEST).entity(Error).build();
            }
        } catch (Exception e) {
            Error.put("Error", "Error en parametros JSON:" + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Error).build();
        }

        OpaDTO opa = util2.opa(productBankIdentifier);
        if (util.validacionSopar(opa.getIdorigenp(), opa.getIdproducto(), opa.getIdauxiliar(), 2)) {
            Error.put("ERROR", "SOCIO BLOQUEADO");
            return Response.status(Response.Status.UNAUTHORIZED).entity(Error).build();
        }

        try {
            List<LoanPayment> ListPayment = dao.loanPayments(productBankIdentifier, pageSize, pageStartIndex,channelId);
            JsonObject j = new JsonObject();
            int t = dao.contadorFeesPayments(productBankIdentifier,2,0);
            j.put("Payments", ListPayment);
            j.put("LoanPaymentsCount", t);
            return Response.status(Response.Status.OK).entity(j).build();
        } catch (Exception e) {
            Error.put("Error", "SOCIOS NO ENCONTRADOS");
            System.out.println("Error al convertir cadena a JSON:" + e.getMessage());
            return Response.status(Response.Status.NO_CONTENT).entity(Error).build();

        }
    }

}
