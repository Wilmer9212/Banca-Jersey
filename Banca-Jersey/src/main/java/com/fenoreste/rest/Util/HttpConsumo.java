/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.Util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author wilmer
 */
//Consumo a proyecto interno solo para metodos SPEI
public class HttpConsumo{

    String url = "", jsonRequest = "";

    public HttpConsumo(String url, String jsonRequest) {
        this.url = url;
        this.jsonRequest = jsonRequest;
    }

    public String consumo() {
        String salida = "";
        try {
            URL url_privada = new URL(url);
            //Se genera la conexion
            HttpURLConnection conn = (HttpURLConnection) url_privada.openConnection();
            conn.setDoOutput(true);
            //El metodo que utilizo
            conn.setRequestMethod("POST");
            //Tipo de contenido aceptado por el WS
            conn.setRequestProperty("Content-Type", "application/json");
            //Obtengo el Stream
            OutputStream os = conn.getOutputStream();
            //Al stream le paso el request
            os.write(jsonRequest.getBytes());
            os.flush();

            //Obtengo el codigo de respuesta
            int codigoHTTP = conn.getResponseCode();
            String output = "";
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            System.out.println("Output from Server .... \n");
            System.out.println("El codigo de respuesta es:" + codigoHTTP);
            if (codigoHTTP == 200) {
                while ((output = br.readLine()) != null) {
                    salida = output;
                }
            } else {
                while ((output = br.readLine()) != null) {
                    salida = output;
                }
            }
            conn.disconnect();
        } catch (Exception e) {
            System.out.println("Error al consumir proyecto interno para dispersion SPEI:" + e.getMessage());
        }
        return salida;
    }
}
