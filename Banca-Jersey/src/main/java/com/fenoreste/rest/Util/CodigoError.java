/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.Util;

/**
 *
 * @author gerardo
 */
public enum CodigoError {
    
    // ---- ERRORES GENERALES ---  073
    CE000("000", "Transacción exitosa.", "Transacción exitosa"),
    CE050("050", "Usuario no autorizado.", "El usuario no esta autorizado para utilizar la aplicacion"),
    CE073("073", "POSIBLE ASUNTO EN DEMANDA. No es posible hacer movimientos a su cuenta. Favor de acudir al depto. de Cobranza en la sucursal Matriz para más información.", "No es posible hacer movimientos a su cuenta. Favor de acudir al depto. de Cobranza en la sucursal Matriz para más información."),
    CE074("074", "Error no se inserto el total de seguros y comisiones.", "Error no se inserto el total de seguros y comisiones."),
    CE075("075", "Error al validar que se insertaron los temporales.", "Error al validar que se insertaron los temporales."),
    CE076("076", "El monto tiene que ser menor o igual a la mensualidad asiganda.", "El monto tiene que ser menor o igual a la mensualidad asiganda."),
    CE077("077", "El monto sobrepasa el valor de udis configurado.", "El monto sobrepasa el valor de udis configurado."),
    CE078("078", "El monto sobrepasa el limite menusal permitido.", "El monto sobrepasa el limite menusal permitido."),
    CE079("079", "No esta configurada la tabla udis_maximo_menores.", "No esta configurada la tabla."),
    CE080("080", "Usuario no existe.", "El usuario no existe en la base"),
    CE081("081", "Origen cerrado del usuario.", "Origen cerrado del usuario."),
    CE082("082", "Socio no registrado al sistema.", "Socio no registrado al sistema."),
    CE083("083", "Socio bloqueado del sistema.", "Socio bloqueado del sistema."),
    CE084("084", "No essta configurada la lista de productos validos.", "No essta configurada la lista de productos validos."),
    CE085("085", "No puede retirar de un prestamo.", "No puede retirar de un prestamo."),
    CE086("086", "El movimiento ya fue aplicado.", "El movimiento ya fue aplicado."),
    CE087("087", "El monto tiene que ser mayor a 0.", "El monto tiene que ser mayor a 0."),
    CE088("088", "Fondos insuficientes.", "Fondo insuficiente para retirarle al producto."),
    CE089("089", "No existe el folio.", "No existe el folio registrado en la base."),
    CE090("090", "El socio no tiene folios.", "El socio no tiene folios."),
    CE091("091", "Numero de socio incorrecto.", "El numero de socio no existe o esta mal generado."),
    CE092("092", "Numero de folio incorrecto.", "El folio no existe o esta mal generado."),
    CE093("093", "Una o mas tablas no estan configuradas.", "La tabla movimientos_de_cuentas y/o ws_get_account_txs_req no estan configuradas"),
    CE094("094", "El folio de origen no puede ser igual al de destino.", "El folio del origen tiene que ser diferente al de destino"),
    CE095("095", "Transacción fuera de horario.", "Transacción fuera de horario."),
    CE096("096", "Error desconocido.", "Llamar a sistemas"),
    CE097("097", "Usuario no activo.", "El usuario no esta activo en este momento"),
    CE098("098", "El monto a transferir excede al monto disponible.", "Error al validar los montos a depositar"),
    CE099("099", "Producto no disponible para efectuar movimiento", "Producto no disponible para efectuar movimiento"),
    // ---- ERRORES TDD ---
    CE100("100", "Error al obtener saldo de la TDD del origen", "Error al obtener saldo de la TDD del origen"),
    CE101("101", "Error al obtener saldo de la TDD del destino", "Error al obtener saldo de la TDD del destino"),
    CE102("102", "Error en el retiro de la TDD", "Error en el retiro de la TDD"),
    CE103("103", "Error en el deposito de la TDD", "Error en el deposito de la TDD"),
    CE104("104", "Error la tarjeta TDD esta inactiva o cancelada", "Error la tarjeta TDD esta inactiva o cancelada"),
    // ---- ERRORES SPEI ---
    SPEI000("000", "Transacción exitosa.", "Transacción exitosa."),
    SPEI057("057", "Error en SPEI.", "Error en SPEI."),
    SPEI050("050", "Error no existe codigo SPEI.", "Error en SPEI codigo no existente.");
    
    private String idError;
    private String mensaje;
    private String descripcion;

    private CodigoError(String idError, String mensaje, String descripcion) {
        this.idError = idError;
        this.mensaje = mensaje;
        this.descripcion = descripcion;
    }

    public String getIdError() {
        return idError;
    }

    public void setIdError(String idError) {
        this.idError = idError;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return "CodigoError: " + idError + ": " + mensaje + " - " + descripcion;
    }

}
