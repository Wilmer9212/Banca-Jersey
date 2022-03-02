package com.fenoreste.rest.dao;

import com.fenoreste.rest.Util.UtilidadesGenerales;
import com.fenoreste.rest.DTO.OpaDTO;
import com.fenoreste.rest.Util.AbstractFacade;
import com.fenoreste.rest.ResponseDTO.AccountLast5MovementsDTO;
import com.fenoreste.rest.ResponseDTO.AccountDetailsDTO;
import com.fenoreste.rest.ResponseDTO.AccountMovementsDTO;
import com.fenoreste.rest.Util.Utilidades;
import com.fenoreste.rest.WsTDD.TarjetaDeDebito;
import com.fenoreste.rest.entidades.AuxiliaresPK;
import com.fenoreste.rest.entidades.Productos;
import com.fenoreste.rest.entidades.Auxiliares;
import com.fenoreste.rest.entidades.Tablas;
import com.fenoreste.rest.entidades.WsSiscoopFoliosTarjetasPK1;
import com.syc.ws.endpoint.siscoop.BalanceQueryResponseDto;
import java.math.BigDecimal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

public abstract class FacadeAccounts<T> {

    Utilidades util = new Utilidades();
    Calendar calendar = Calendar.getInstance();
    Date hoy = calendar.getTime();

    UtilidadesGenerales util2 = new UtilidadesGenerales();

    public FacadeAccounts(Class<T> entityClass) {
    }

    public AccountDetailsDTO GetAccountDetails(String accountId) {
        EntityManager em = AbstractFacade.conexion();
        OpaDTO opa = util.opa(accountId);
        AccountDetailsDTO cuenta = null;
        try {
            AuxiliaresPK auxpk = new AuxiliaresPK(opa.getIdorigenp(), opa.getIdproducto(), opa.getIdauxiliar());
            Auxiliares aux = em.find(Auxiliares.class, auxpk);
            String S24H = DateFormat.getDateInstance().format(substractDate(1));
            String S48H = DateFormat.getDateInstance().format(substractDate(2));

            String IntervalM = DateFormat.getDateInstance().format(subtractIntervalMonth());

            //Obtenemos saldo segun las horas pasadas 
            //Saldo 24 horas
            double saldo24 = 0.0, saldo48 = 0.0, saldo = 0.0;
            double saldosF[] = null;
            //Si el producto es TDD
            //Leemos ws de TDD Alestra
            TarjetaDeDebito wsTDD = new TarjetaDeDebito();
            Productos pr = em.find(Productos.class, aux.getAuxiliaresPK().getIdproducto());

            if (util2.obtenerOrigen(em) == 30200) {
                Tablas tb_producto_tdd = util2.busquedaTabla(em, "bankingly_banca_movil", "producto_tdd");
                if (aux.getAuxiliaresPK().getIdproducto() == Integer.parseInt(tb_producto_tdd.getDato1())) {
                    WsSiscoopFoliosTarjetasPK1 foliosPK = new WsSiscoopFoliosTarjetasPK1(aux.getAuxiliaresPK().getIdorigenp(), aux.getAuxiliaresPK().getIdproducto(), aux.getAuxiliaresPK().getIdauxiliar());
                    BalanceQueryResponseDto responseSaldo = wsTDD.saldoTDD(foliosPK);
                    saldo24 = responseSaldo.getAvailableAmount();
                    saldo48 = responseSaldo.getAvailableAmount();
                    saldo = responseSaldo.getAvailableAmount();
                } else {
                    saldosF = getSaldoAuxiliaresD(opa.getIdorigenp(), opa.getIdproducto(), opa.getIdauxiliar(), S24H, S48H);
                    if (pr.getTipoproducto() == 4 || pr.getTipoproducto() == 8) {
                        saldo24 = saldosF[2];
                        saldo48 = saldosF[2];
                        saldo = saldosF[2];
                    } else {
                        saldo24 = saldosF[0];
                        saldo48 = saldosF[1];
                        saldo = saldosF[2];
                        if (saldo24 > 0) {
                            saldo24 = saldo24;
                            if (saldo48 > 0) {
                                saldo48 = saldo48;
                                saldo = saldo48;
                            } else {
                                saldo48 = saldo;
                            }
                        } else {
                            if (saldo48 > 0) {
                                saldo24 = saldo48;
                            } else {
                                saldo24 = saldo;
                                saldo48 = saldo;
                            }
                        }
                    }

                }

            }
            String consulta_saldo_promedio_mensual = "SELECT sai_calcula_saldo_promedio_diario("
                    + aux.getAuxiliaresPK().getIdorigenp() + ","
                    + aux.getAuxiliaresPK().getIdproducto() + ","
                    + aux.getAuxiliaresPK().getIdauxiliar() + ",'" + IntervalM + "'," + "(SELECT date(fechatrabajo) FROM origenes limit 1)" + ",0)";

            Query saldo_promedio_mensual = em.createNativeQuery(consulta_saldo_promedio_mensual);
            String saldo_promedio_str = String.valueOf(saldo_promedio_mensual.getSingleResult());
            //replace(sai_calcula_saldo_promedio_diario(aux.idorigenp,aux.idproducto,aux.idauxiliar,fecha_inicial,fecha_final,0),',','')::numeric as "Promedio Diario" 
            //System.out.println("ad:"+adpk);
            //Productos pr = em.find(Productos.class, aux.getAuxiliaresPK().getIdproducto());
            String origen = util2.obtenerOrigen(aux.getAuxiliaresPK().getIdorigenp(), em);
            cuenta = new AccountDetailsDTO();
            cuenta.setAccountBankIdentifier(accountId);
            cuenta.setAccountOfficerName(pr.getNombre());
            cuenta.setAccountCountableBalance(aux.getSaldo());
            cuenta.setAccountAvailableBalance(aux.getSaldo());
            cuenta.setAccountBalance24Hrs(new BigDecimal(saldo24));
            cuenta.setAccountBalance48Hrs(new BigDecimal(saldo48));
            cuenta.setAccountBalance48MoreHrs(new BigDecimal(saldo48));
            cuenta.setMonthlyAverageBalance(new BigDecimal(saldo_promedio_str.replace(",", "")));
            cuenta.setPendingChecks(0);
            cuenta.setChecksToReleaseToday(0);
            cuenta.setCancelledChecks(0);
            cuenta.setCertifiedChecks(0);
            cuenta.setRejectedChecks(0);
            cuenta.setBlockedAmount(0);
            cuenta.setMovementsOfTheMonth(0);
            cuenta.setChecksDrawn(0);
            cuenta.setOverdrafts(0.0);

            cuenta.setProductBranchName(origen);
            cuenta.setProductOwnerName(origen);
            cuenta.setShowCurrentAccountChecksInformation(false);

        } catch (Exception e) {
            System.out.println("Error en GetAccountDetails:" + e.getMessage());
        } finally {
            em.close();
        }
        return cuenta;//cuenta;

    }

    public List<AccountLast5MovementsDTO> getAccountLast5Movements(String accountId) {
        OpaDTO opa = util.opa(accountId);
        AccountLast5MovementsDTO cuenta;
        boolean isDC = false;
        String Description = "";
        List<AccountLast5MovementsDTO> ListaDTO = new ArrayList<AccountLast5MovementsDTO>();
        EntityManager em = AbstractFacade.conexion();
        try {
            String consulta = " SELECT m.* "
                    + "         FROM auxiliares_d m"
                    + "         WHERE idorigenp=" + opa.getIdorigenp() + " AND idproducto=" + opa.getIdproducto() + " AND idauxiliar=" + opa.getIdauxiliar() + " ORDER BY fecha DESC LIMIT 5";
            Query last5Movements = em.createNativeQuery(consulta);
            int movementTypeId = 0;
            int idmovimiento = 0;
            List<Object[]> milista = last5Movements.getResultList();
            for (int i = 0; i < milista.size(); i++) {
                Object[] as = milista.get(i);
                if (Integer.parseInt(as[4].toString()) == 1) {
                    Description = "Deposito";
                    movementTypeId = 2;
                    isDC = false;
                } else if (Integer.parseInt(as[4].toString()) == 0) {
                    Description = "Retiro";
                    movementTypeId = 3;
                    isDC = true;
                }
                idmovimiento = Integer.parseInt(as[15].toString());//Integer.parseInt(as[9].toString()) + Integer.parseInt(as[10].toString()) + Integer.parseInt(as[11].toString()) + Integer.parseInt(as[12].toString());
                //Productos productos = em.find(Productos.class, Integer.parseInt(as[1].toString()));

                cuenta = new AccountLast5MovementsDTO();
                cuenta.setMovementId(idmovimiento);
                cuenta.setAccountBankIdentifier(accountId);
                cuenta.setMovementDate(as[3].toString());
                cuenta.setDescription(Description);
                cuenta.setAmount(Double.parseDouble(as[5].toString()));
                cuenta.setIsDebit(isDC);
                cuenta.setBalance(Double.parseDouble(as[14].toString()));
                cuenta.setMovementTypeId(movementTypeId);
                cuenta.setTypeDescription(Description);
                cuenta.setCheckId(null);
                cuenta.setVoucherId(String.valueOf(idmovimiento));

                ListaDTO.add(cuenta);
            }
            System.out.println("SALIENDO DE LAS 5 MOVEMENTS ======================");

        } catch (Exception e) {
            System.out.println("Error en GetAccountLast5Movements:" + e.getMessage());
        } finally {
            em.close();
        }
        return ListaDTO;
    }

    public List<AccountMovementsDTO> getAccountMovements(String productBankIdentifier, String dateFromFilter, String dateToFilter, int pageSize, int pageStartIndex, String orderBy, int canal) {
        AccountMovementsDTO cuenta;
        boolean isDC = false;
        String Description = "";
        List<AccountMovementsDTO> ListaDTO = new ArrayList<AccountMovementsDTO>();
        String complemento = "";
        OpaDTO opa = util.opa(productBankIdentifier);
        EntityManager em = AbstractFacade.conexion();
        try {
            switch (orderBy.toUpperCase()) {
                case "MOVEMENTDATE ASC":
                    complemento = "ORDER BY fecha ASC LIMIT 5";
                    break;
                case "MOVEMENTDATE DESC":
                    complemento = "ORDER BY fecha DESC LIMIT 5";
                    break;
                case "MOVEMENTDATE":
                    complemento = "ORDER BY fecha LIMIT 5";
                    break;
                case "ID ASC":
                    complemento = "ORDER BY idpoliza ASC LIMIT 5";
                    break;
                case "ID DESC":
                    complemento = "ORDER BY idpoliza DESC LIMIT 5";
                    break;
                case "ID":
                    complemento = "ORDER BY idpolizaLIMIT 5";
                    break;
                case "DESCRIPTION ASC":
                    complemento = "ORDER BY cargoabono ASC LIMIT 5";
                    break;
                case "DESCRIPTION DESC":
                    complemento = "ORDER BY cargoabono DESC LIMIT 5";
                    break;
                case "DESCRIPTION":
                    complemento = "ORDER BY cargoabono LIMIT 5";
                    break;
                case "AMOUNT ASC":
                    complemento = "ORDER BY monto ASC LIMIT 5";
                    break;
                case "AMOUNT DESC":
                    complemento = "ORDER BY monto DESC LIMIT 5";
                    break;
                case "AMOUNT":
                    complemento = "ORDER BY monto LIMIT 5";
                    break;
                case "BALANCE ASC":
                    complemento = "ORDER BY saldoec ASC LIMIT 5";
                    break;
                case "BALANCE DESC":
                    complemento = "ORDER BY saldoec DESC LIMIT 5";
                    break;
                case "BALANCE":
                    complemento = "ORDER BY saldoec LIMIT 5";
                    break;
                case "":
                    break;

            }

            int pageNumber = pageStartIndex;
            int pageSizes = pageSize;
            int inicioB = 0;
            //Query query = em.createNativeQuery("SELECT * FROM personas order by idsocio ASC",Persona.class);
            String consulta = "";
            if (!dateFromFilter.equals("") && !dateToFilter.equals("")) {
                consulta = " SELECT * "
                        + "         FROM auxiliares_d"
                        + "         WHERE date(fecha) between '" + dateFromFilter + "'"
                        + "         AND '" + dateToFilter + "' AND idorigenp=" + opa.getIdorigenp() + " AND idproducto=" + opa.getIdproducto() + " AND idauxiliar=" + opa.getIdauxiliar() + " " + complemento;
            } else if (!dateFromFilter.equals("") && dateToFilter.equals("")) {
                consulta = " SELECT * "
                        + "         FROM auxiliares_d"
                        + "         WHERE date(fecha) > '" + dateFromFilter + "' AND idorigenp=" + opa.getIdorigenp() + " AND idproducto=" + opa.getIdproducto() + " AND idauxiliar=" + opa.getIdauxiliar() + " " + complemento;

            } else if (dateFromFilter.equals("") && !dateToFilter.equals("")) {
                consulta = " SELECT * "
                        + "         FROM auxiliares_d"
                        + "         WHERE date(fecha) < '" + dateToFilter + "' AND idorigenp=" + opa.getIdorigenp() + " AND idproducto=" + opa.getIdproducto() + " AND idauxiliar=" + opa.getIdauxiliar() + " " + complemento;

            } else {
                consulta = " SELECT * "
                        + "         FROM auxiliares_d"
                        + "         WHERE  idorigenp=" + opa.getIdorigenp() + " AND idproducto=" + opa.getIdproducto() + " AND idauxiliar=" + opa.getIdauxiliar() + " " + complemento;

            }
            /*if (pageNumber == 1 || pageNumber == 0) {
            inicioB = 0;
            } else if (pageNumber > 1) {
            inicioB = ((pageNumber * pageSizes) - pageSizes);
            }*/
            System.out.println("La consulta es:" + consulta);
            inicioB = ((pageNumber * pageSizes) - pageSizes);
            if (inicioB < 0) {
                inicioB = 0;
            }
            try {
                Query queryE = em.createNativeQuery(consulta);

                queryE.setFirstResult(pageStartIndex);
                queryE.setMaxResults(pageSizes);

                List<Object[]> MiLista = queryE.getResultList();
                int movementTypeId = 0;
                for (Object[] as : MiLista) {
                    if (Integer.parseInt(as[4].toString()) == 1) {
                        Description = "Deposito";
                        movementTypeId = 2;
                        isDC = false;
                    } else if (Integer.parseInt(as[4].toString()) == 0) {
                        Description = "Retiro";
                        movementTypeId = 3;
                        isDC = true;
                    }

                    int idmovimiento = Integer.parseInt(as[15].toString());
                    cuenta = new AccountMovementsDTO();

                    cuenta.setMovementId(idmovimiento);
                    cuenta.setAccountBankIdentifier(productBankIdentifier);
                    cuenta.setMovementDate(as[3].toString());
                    cuenta.setDescription(Description);
                    cuenta.setAmount(Double.parseDouble(as[5].toString()));
                    cuenta.setIsDebit(isDC);
                    cuenta.setBalance(Double.parseDouble(as[14].toString()));
                    cuenta.setMovementTypeId(movementTypeId);
                    cuenta.setTypeDescription(Description);
                    cuenta.setCheckId(null);
                    cuenta.setVoucherId(String.valueOf(idmovimiento));

                    ListaDTO.add(cuenta);
                }
            } catch (Exception e) {
                System.out.println("Error:" + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Error en account movements:" + e.getMessage());
        } finally {
            em.close();
        }
        return ListaDTO;
    }

    public static Date substractDate(int numeroDias) {
        SimpleDateFormat d = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;
        Calendar calendar = Calendar.getInstance();

        //Si vamos a usar la fecha en tiempo real date=fechaActual
        //date = fechaActual;
        date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, -numeroDias);
        return cal.getTime();
    }

    public static Date subtractDay24H() {
        SimpleDateFormat d = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return cal.getTime();
    }

    public static Date subtractIntervalMonth() {
        SimpleDateFormat d = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, -1);
        return cal.getTime();
    }

    //Metodo que no ayuda a obtener saldo por rango de fechas, se utilizo en el metodo de getAccountDetails
    public double[] getSaldoAuxiliaresD(int o, int p, int a, String fecha, String fecha2) {
        double saldo1 = 0.0;
        double saldo2 = 0.0;
        double saldo3 = 0.0;
        double saldos[] = new double[3];
        EntityManager em = AbstractFacade.conexion();
        try {
            if (!fecha.equals("") && !fecha2.equals("")) {
                String consulta = "SELECT (CASE WHEN saldoec > 0 THEN saldoec ELSE 0.0 END) FROM auxiliares_d WHERE " + "idorigenp=" + o
                        + " AND idproducto=" + p
                        + " AND idauxiliar=" + a
                        + " AND date(fecha) ='" + fecha + "'";

                String consulta2 = "SELECT (CASE WHEN saldoec > 0 THEN saldoec ELSE 0.0 END) FROM auxiliares_d WHERE " + "idorigenp=" + o
                        + " AND idproducto=" + p
                        + " AND idauxiliar=" + a
                        + " AND date(fecha) ='" + fecha2 + "'";

                String consulta3 = "SELECT (CASE WHEN saldoec > 0 THEN saldoec ELSE 0.0 END)  FROM auxiliares_d WHERE " + "idorigenp=" + o
                        + " AND idproducto=" + p
                        + " AND idauxiliar=" + a + " ORDER BY fecha DESC limit 1";

                try {
                    Query query = em.createNativeQuery(consulta);
                    saldo1 = Double.parseDouble(String.valueOf(query.getSingleResult()));
                } catch (Exception e) {
                }
                try {
                    Query query2 = em.createNativeQuery(consulta2);
                    saldo2 = Double.parseDouble(String.valueOf(query2.getSingleResult()));
                } catch (Exception e) {
                }
                try {
                    Query query3 = em.createNativeQuery(consulta3);
                    saldo3 = Double.parseDouble(String.valueOf(query3.getSingleResult()));
                } catch (Exception e) {
                }
                saldos[0] = saldo1;
                saldos[1] = saldo2;
                saldos[2] = saldo3;
            } else {
                System.out.println("Defina fechas por favor");
            }
        } catch (Exception e) {
            System.out.println("Error en obtener auxiliares_d:" + e.getMessage());
        } finally {
            em.close();
        }
        return saldos;
    }

    public int contadorAuxD(String productBankIdentifier, String dateFromFilter, String dateToFilter) {
        String consulta = "";
        int count = 0;
        EntityManager em = AbstractFacade.conexion();//emf.createEntityManager()EntityManager em = emf.createEntityManager();EntityManager em = emf.createEntityManager();
        try {
            if (!dateFromFilter.equals("") && !dateToFilter.equals("")) {
                consulta = " SELECT count(*) "
                        + "         FROM auxiliares_d"
                        + "         WHERE date(fecha) between '" + dateFromFilter + "'"
                        + "         AND '" + dateToFilter + "' AND replace((to_char(idorigenp,'099999')||to_char(idproducto,'09999')||to_char(idauxiliar,'09999999')),' ','')='" + productBankIdentifier + "'";
            } else if (!dateFromFilter.equals("") && dateToFilter.equals("")) {
                consulta = " SELECT count(*) "
                        + "         FROM auxiliares_d"
                        + "         WHERE date(fecha) > '" + dateFromFilter + "' AND replace((to_char(idorigenp,'099999')||to_char(idproducto,'09999')||to_char(idauxiliar,'09999999')),' ','')='" + productBankIdentifier + "'";

            } else if (dateFromFilter.equals("") && !dateToFilter.equals("")) {
                consulta = " SELECT count(*) "
                        + "         FROM auxiliares_d"
                        + "         WHERE date(fecha) < '" + dateToFilter + "' AND replace((to_char(idorigenp,'099999')||to_char(idproducto,'09999')||to_char(idauxiliar,'09999999')),' ','')='" + productBankIdentifier + "'";

            } else {
                consulta = " SELECT count(*) "
                        + "         FROM auxiliares_d"
                        + "         WHERE replace((to_char(idorigenp,'099999')||to_char(idproducto,'09999')||to_char(idauxiliar,'09999999')),' ','')='" + productBankIdentifier + "'";

            }
            System.out.println("DE LASTMOVEMENTS CONSULTA:" + consulta);
            Query query = em.createNativeQuery(consulta);
            int c = 0;
            c = Integer.parseInt(String.valueOf(query.getSingleResult()));
            count = c;
        } catch (Exception e) {
            System.out.println("Error al contar registros:" + e.getMessage());
        } finally {
            em.close();
        }
        return count;
    }

    public boolean actividad_horario() {
        EntityManager em = AbstractFacade.conexion();//emf.createEntityManager()EntityManager em = emf.createEntityManager();EntityManager em = emf.createEntityManager();
        boolean bandera_ = false;
        try {
            if (util2.actividad(em)) {
                bandera_ = true;
            }
        } catch (Exception e) {
            System.out.println("Error al verificar el horario de actividad");
        } finally {
            em.close();
        }

        return bandera_;
    }

}
