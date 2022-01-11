/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.RESTservices;

import com.fenoreste.rest.Util.AbstractFacade;
import com.fenoreste.rest.dao.CustomerDAO;
import com.fenoreste.rest.entidades.Auxiliares;
import com.fenoreste.rest.entidades.Persona;
import com.fenoreste.rest.entidades.Banca_movil_usuarios;
import com.github.cliftonlabs.json_simple.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
@Path("/Test")
public class TestResources {

    @POST
    @Path("/auth")    
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response auth(String cadenaJson) {
        JSONObject request=new JSONObject(cadenaJson);
        JsonObject jsonOb=new JsonObject();
        //EntityManagerFactory emf=AbstractFacade.conexion();
        //EntityManagerFactory em=AbstractFacade.conexion();
        try {
            EntityManager em=AbstractFacade.conexion();
            String usuario=request.getString("username");
            String c="SELECT * FROM usuarios_bancam_bankinglyTest WHERE username='"+usuario+"' and estatus=true";
            Query q=em.createNativeQuery(c,Banca_movil_usuarios.class);
            Banca_movil_usuarios user=(Banca_movil_usuarios) q.getSingleResult();
            /*jsonOb.put("user",user.getUsername());
            jsonOb.put("id",user.getSocio());*/
            em.close();
        } catch (Exception e) {
            jsonOb.put("user","null");
            System.out.println("Error al buscar user:"+e.getMessage());
            //emf.close();            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(jsonOb).build();
        }finally{
            //emf.close();
        }
        return Response.status(Response.Status.OK).entity(jsonOb).build();
        
    }
    

}
