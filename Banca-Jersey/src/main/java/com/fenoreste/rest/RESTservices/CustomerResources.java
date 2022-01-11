/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.RESTservices;

import com.fenoreste.rest.ResponseDTO.ClientByDocumentDTO;
import com.fenoreste.rest.dao.CustomerDAO;
import com.fenoreste.rest.entidades.Persona;
import com.github.cliftonlabs.json_simple.JsonObject;
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
@Path("/Clients")
public class CustomerResources {

    @POST
    @Path("/auth")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    @Consumes({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response credencialesCliente(String cadena) {
        /*   boolean bandera = false;
        JSONObject request = new JSONObject(cadena);
        CustomerDAO dao = new CustomerDAO();
        try {String user = request.getString("username");
            String ogs = dao.findOGS(user);
            JsonObject json = new JsonObject();
            json.put("ogs", ogs);
            return Response.status(Response.Status.OK).entity(ogs).build();
        } catch (Exception e) {
            System.out.println("error:" + e.getMessage());
        } finally {
            dao.cerrar();
        }*/
        return null;
    }

    /*===============================================================================
          METODO PARA BUSCAR SOCIO CON BASE A DOCUMENTOS QUE SE TRAE EN EL REQUEST
    =================================================================================*/
    @POST
    @Path("/ByDocuments")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    @Consumes({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response getClientsByDocument(String cadenaJson) throws Throwable {

        //Verificamos el horario de actividad
        JSONObject JsonRequest_ = new JSONObject(cadenaJson);
        JsonObject JsonError_ = new JsonObject();
        JsonObject JsonResponse_ = new JsonObject();
        String DocumentId = JsonRequest_.getString("documentId");
        int ClientType = JsonRequest_.getInt("clientType");
        String Name = JsonRequest_.getString("name");
        String LastName = JsonRequest_.getString("lastName");
        String Mail = JsonRequest_.getString("mail");
        String Phone = JsonRequest_.getString("phone");
        String CellPhone = JsonRequest_.getString("cellPhone");
        String UserName = JsonRequest_.getString("userName");
        CustomerDAO metodos = new CustomerDAO();
        
        String mensaje_error="";
        if (!metodos.actividad_horario()) {
            JsonError_.put("ERROR", "VERIFIQUE SU HORARIO DE ACTIVIDAD FECHA,HORA O CONTACTE A SU PROVEEDOR");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(JsonError_).build();
        }
        try {
            Persona persona = null;
            try {
                //Buscamos a la persona con los datos que se esta enviando
                persona = metodos.BuscarPersona(ClientType, DocumentId, Name, LastName, Mail,CellPhone);
                
            } catch (Exception e) {
                mensaje_error="Persona no existe";
            }

            //Si la persona existe 
            if (persona.getPersonasPK() != null) {
                //Validamos que el socio ya halla asistido a sucursal a aperturar el producto para banca movil
                String validaciones_datos_ = metodos.validaciones_datos(persona.getPersonasPK().getIdorigen(), persona.getPersonasPK().getIdgrupo(), persona.getPersonasPK().getIdsocio(), UserName);
                System.out.println("validaciones datos:" + validaciones_datos_);
                if (validaciones_datos_.contains("EXITO")) {
                    ClientByDocumentDTO cliente = null;
                    //Buscamos que la persona no se halla registrado antes
                    //-----String mensajeValidacionUsuario = metodos.BuscarSocioRegistrado(persona.getPersonasPK().getIdorigen(), persona.getPersonasPK().getIdgrupo(), persona.getPersonasPK().getIdsocio());

                    //Retornamos el dto para GetClientByDocument y en ese mismo metodo buscamos el socio en la tabla banca_movil_usuarios 
                    cliente = metodos.getClientByDocument(persona, UserName);
                    if (cliente != null) {
                        JsonResponse_.put("customers", cliente);
                        return Response.ok(JsonResponse_).build();
                    } else {
                        JsonResponse_.put("Error", "NO SE PUDIERON CARGAR DATOS,CONTACTE AL PROVEEDOR");
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(JsonResponse_).build();
                    }

                } else {
                    JsonResponse_.put("Error", validaciones_datos_);
                    return Response.status(Response.Status.OK).entity(JsonResponse_).build();
                }
            } else {
                JsonResponse_.put("Error", "SOCIO NO EXISTE,VERIFIQUE DATOS");
                return Response.status(Response.Status.OK).entity(JsonResponse_).build();
            }
        } catch (Exception e) {
            System.out.println("Mensaje:"+mensaje_error);
            System.out.println("Error:" + e.getMessage());
            JsonError_.put("Error", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(JsonError_).build();
        }
    }
    
    
    @POST
    @Path("/notificaEstadoCuenta")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    @Consumes({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response notificaEstadoCuenta(String cadenaJson){
    JSONObject json_request = new JSONObject(cadenaJson);
    CustomerDAO dao=new CustomerDAO();
    String cuenta=json_request.getString("cuenta");
    String estado = json_request.getString("estado");
    String observaciones = json_request.getString("observaciones");
    String empresa = json_request.getString("empresa");
    
    String mensaje_notificacion = dao.notificaCreacionCuenta(cuenta, estado, observaciones,empresa);
    
    JsonObject jsonResponse=new JsonObject();
    jsonResponse.put("mensaje",mensaje_notificacion);       
    return Response.ok(jsonResponse).build();
    }
}
