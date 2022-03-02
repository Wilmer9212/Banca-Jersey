package com.fenoreste.rest.dao;

import com.fenoreste.rest.Util.UtilidadesGenerales;
import com.fenoreste.rest.DTO.OpaDTO;
import com.fenoreste.rest.ResponseDTO.FeesDueData;
import com.fenoreste.rest.Util.AbstractFacade;
import com.fenoreste.rest.ResponseDTO.LoanDTO;
import com.fenoreste.rest.ResponseDTO.LoanFee;
import com.fenoreste.rest.ResponseDTO.LoanPayment;
import com.fenoreste.rest.ResponseDTO.LoanRate;
import com.fenoreste.rest.Util.Utilidades;
import com.fenoreste.rest.entidades.Amortizaciones;
import com.fenoreste.rest.entidades.AuxiliaresPK;
import com.fenoreste.rest.entidades.Auxiliares;
import com.fenoreste.rest.entidades.AuxiliaresD;
import com.fenoreste.rest.entidades.Catalog_Status_Bankingly;
import com.fenoreste.rest.entidades.Loan_Fee_Status;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import javax.persistence.EntityManager;
import javax.persistence.Query;

public abstract class FacadeLoan<T> {

    Utilidades util = new Utilidades();
    Calendar calendar = Calendar.getInstance();
    Date hoy = calendar.getTime();
    UtilidadesGenerales util2 = new UtilidadesGenerales();

    public FacadeLoan(Class<T> entityClass) {
    }

    public LoanDTO Loan(String productBankIdentifier) {
        EntityManager em = AbstractFacade.conexion();
        OpaDTO opa = util.opa(productBankIdentifier);
        LoanDTO dto = null;
        try {
            AuxiliaresPK auxpk = new AuxiliaresPK(opa.getIdorigenp(), opa.getIdproducto(), opa.getIdauxiliar());
            Auxiliares aux = em.find(Auxiliares.class, auxpk);
            if (aux.getEstatus() == 2) {
                Catalog_Status_Bankingly sts = em.find(Catalog_Status_Bankingly.class, Integer.parseInt(aux.getEstatus().toString()));
                Double currentBalance = Double.parseDouble(aux.getSaldo().toString());
                Double currentRate = Double.parseDouble(aux.getTasaio().toString());// + Double.parseDouble(aux.getTasaim().toString()) + Double.parseDouble(aux.getTasaiod().toString());
                int loanStatusId = sts.getProductstatusid();
                String buscar_amortizaciones_pagadas = "SELECT count(*) FROM amortizaciones WHERE idorigenp=" + opa.getIdorigenp()
                        + " AND idproducto=" + opa.getIdproducto()
                        + " AND idauxiliar=" + opa.getIdauxiliar()
                        + " AND todopag=true";
                Query query = em.createNativeQuery(buscar_amortizaciones_pagadas);
                int amortizaciones_pagadas = Integer.parseInt(String.valueOf(query.getSingleResult()));

                Query query1 = em.createNativeQuery("SELECT count(*) FROM amortizaciones WHERE idorigenp=" + opa.getIdorigenp()
                        + " AND idproducto=" + opa.getIdproducto()
                        + " AND idauxiliar=" + opa.getIdauxiliar());
                int tam = Integer.parseInt(String.valueOf(query1.getSingleResult()));

                dto = new LoanDTO();
                dto.setAccountBankIdentifier(productBankIdentifier);
                dto.setCurrentBalance(currentBalance);//Saldo del prestamo
                dto.setCurrentRate(currentRate);//Tasa del prestamo
                dto.setFeesDue(RSFeesDue(opa.getIdorigenp(), opa.getIdproducto(), opa.getIdauxiliar()));//Abonos vencidos
                dto.setFeesDueData(RSFeesDueData(opa.getIdorigenp(), opa.getIdproducto(), opa.getIdauxiliar(), aux.getTipoamortizacion()));//Vencimiento de cuotas
                dto.setLoanStatusId(loanStatusId);
                dto.setNextFee(nextFee(opa.getIdorigenp(), opa.getIdproducto(), opa.getIdauxiliar()));
                dto.setOriginalAmount(aux.getMontoprestado().doubleValue());
                dto.setOverdueDays(RSOverdueDays(opa.getIdorigenp(), opa.getIdproducto(), opa.getIdauxiliar()));//dias vencidos
                dto.setPaidFees(amortizaciones_pagadas);
                dto.setPayoffBalance(0.0);
                dto.setPrepaymentAmount(0.0);
                dto.setProductBankIdentifier(productBankIdentifier);
                dto.setTerm(tam);//Cantidad de amortizaciones para el prestamo
                dto.setShowPrincipalInformation(true);//para mostrar ajustes
                dto.setLoanFeesResult(null);//LoanFees(productBankIdentifier,1,0,10,""));
                dto.setLoanRateResult(null);
                dto.setLoanPaymentsResult(null);

            }
        } catch (Exception e) {
            System.out.println("Error en GetAccountDetails:" + e.getMessage());
        } finally {
            em.close();
        }

        return dto;//cuenta;

    }

    public LoanFee LoanFee(String productBankIdentifier, int feeNumber) {
        EntityManager em = AbstractFacade.conexion();
        LoanFee loanFee = null;
        OpaDTO opa = util.opa(productBankIdentifier);
        try {
            AuxiliaresPK pk = new AuxiliaresPK(opa.getIdorigenp(), opa.getIdproducto(), opa.getIdauxiliar());
            Auxiliares aux = em.find(Auxiliares.class, pk);
            //Obtengo informacion con el sai_auxiliar hasta la fecha actual, si hay dudas checar el catalogo o atributos que devuelve la funcion
            String sai_auxiliar = "SELECT  sai_auxiliar(" + opa.getIdorigenp() + "," + opa.getIdproducto() + "," + opa.getIdauxiliar() + ",(SELECT date(fechatrabajo) FROM origenes limit 1))";
            Query RsSai = em.createNativeQuery(sai_auxiliar);
            String sai = RsSai.getSingleResult().toString();
            String[] parts = sai.split("\\|");
            List list = Arrays.asList(parts);
            //Obtengo la amortizacion que se vence
            String consultaA = "SELECT * FROM amortizaciones WHERE idorigenp=" + opa.getIdorigenp()
                    + " AND idproducto=" + opa.getIdproducto()
                    + " AND idauxiliar=" + opa.getIdauxiliar()
                    + " AND idorigenp+idproducto+idauxiliar+idamortizacion=" + feeNumber;

            System.out.println("La consulta es para la amortizacion es:" + consultaA);
            Query queryA = em.createNativeQuery(consultaA, Amortizaciones.class);
            Amortizaciones amm = (Amortizaciones) queryA.getSingleResult();
            Double iovencido = Double.parseDouble(list.get(6).toString()) + Double.parseDouble(list.get(17).toString());
            Double imvencido = Double.parseDouble(list.get(15).toString()) + Double.parseDouble(list.get(18).toString());

            int loanfeests = 0;
            if (Double.parseDouble(amm.getAbono().toString()) == Double.parseDouble(amm.getAbonopag().toString())) {
                loanfeests = 3;
            } else if (Double.parseDouble(amm.getAbono().toString()) > Double.parseDouble(amm.getAbonopag().toString())
                    && amm.getTodopag() == false) {
                loanfeests = 1;
            } else if (!list.get(14).toString().equals("C")) {
                loanfeests = 2;
            }

            Loan_Fee_Status loanf = em.find(Loan_Fee_Status.class, loanfeests);
            Double abonoT = Double.parseDouble(amm.getAbono().toString()) + iovencido + imvencido;
            String converted = String.valueOf(convertToLocalDateTimeViaInstant(amm.getVence()) + ":00.000Z");
            loanFee = new LoanFee(
                    Double.parseDouble(aux.getSaldo().toString()),//Saldo o balance del prestamo principal
                    amm.getAmortizacionesPK().getIdorigenp() + amm.getAmortizacionesPK().getIdproducto() + amm.getAmortizacionesPK().getIdauxiliar() + amm.getAmortizacionesPK().getIdamortizacion(),
                    Double.parseDouble(amm.getAbono().toString()),
                    converted,//String.valueOf(ldt),//amm.getVence().toString(),
                    iovencido,
                    imvencido,
                    loanf.getId(),
                    Double.parseDouble(amm.getAbono().toString()),
                    abonoT);
        } catch (Exception e) {
            System.out.println("Error en LoanFee:" + e.getMessage());
        } finally {
            em.close();
        }
        return loanFee;
    }

    public LocalDateTime convertToLocalDateTimeViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public List<LoanFee> LoanFees(String productBankIdentifier, int feesStatus, int pageSize, int pageStartIndex, String order, int channelId) {
        EntityManager em = AbstractFacade.conexion();
        OpaDTO opa = util.opa(productBankIdentifier);
        List<LoanFee> lista_cuotas = new ArrayList();

        //Complemento para la consulta
        try {
            String complemento = "";
            if (feesStatus == 0 && order.toUpperCase().contains("FEENUMBER")) {
                complemento = "ORDER BY idamortizacion";
            } else if (feesStatus == 0 && order.equals("")) {
                complemento = "ORDER BY idamortizacion";
            } else if (feesStatus == 1 && order.toUpperCase().contains("FEENUMBER")) {
                complemento = " WHERE abonado<>abono AND date(vence) > (SELECT date(fechatrabajo) FROM origenes LIMIT 1) ORDER BY idamortizacion";
            } else if (feesStatus == 1 && order.equals("")) {
                complemento = " WHERE abonado<>abono AND date(vence) > (SELECT date(fechatrabajo) FROM origenes LIMIT 1) ORDER BY idamortizacion";
            } else if (feesStatus == 2 && order.toUpperCase().contains("FEENUMBER")) {
                complemento = " WHERE abonado=abono ORDER BY idamortizacion";
            } else if (feesStatus == 2 && order.equals("")) {
                complemento = " WHERE abonado=abono";
            }

            //Busco el auxiliar 
            AuxiliaresPK aux_pk = new AuxiliaresPK(opa.getIdorigenp(), opa.getIdproducto(), opa.getIdauxiliar());
            Auxiliares a = em.find(Auxiliares.class, aux_pk);

            // Obtengo informacion con el sai_auxiliar hasta la fecha actual, si hay dudas
            // checar el catalogo o atributos que devuelve la funcion
            String sai_auxiliar = "SELECT sai_auxiliar(" + opa.getIdorigenp() + "," + opa.getIdproducto() + "," + opa.getIdauxiliar() + ",(SELECT date(fechatrabajo) FROM origenes limit 1))";
            Query RsSai = em.createNativeQuery(sai_auxiliar);
            String cadena_sai_auxiliar = RsSai.getSingleResult().toString();
            String[] parametros_sai = cadena_sai_auxiliar.split("\\|");
            List lista_parametros_sai = Arrays.asList(parametros_sai);
            System.out.println("El sai auxiliar es:" + cadena_sai_auxiliar);

            //Obtenemos el iva para el producto segun la sucursal
            String consulta_iva_segun_sucursal = "SELECT sai_iva_segun_sucursal(" + opa.getIdorigenp() + ", idproducto," + a.getTipoamortizacion() + ") FROM productos WHERE idproducto=" + opa.getIdproducto();
            System.out.println("Consulta para iva_sucursal :" + consulta_iva_segun_sucursal);
            Query query_calculo_iva_sucursal = em.createNativeQuery(consulta_iva_segun_sucursal);
            String iva_segun_sucursal = String.valueOf(query_calculo_iva_sucursal.getSingleResult());

            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");

            BigDecimal iva = new BigDecimal(iva_segun_sucursal);
            BigDecimal imTotal = new BigDecimal(lista_parametros_sai.get(15).toString());

            //Traemos una lista de objetos de amortizaciones que se muestran en SAICoop
            String consulta_amortizaciones_saicoop = "SELECT * FROM sai_tabla_amortizaciones_t0_calculada('amortizaciones'," + opa.getIdorigenp() + "," + opa.getIdproducto() + "," + opa.getIdauxiliar() + ",(SELECT to_char(date(fechatrabajo),'yyyy-MM-dd') FROM origenes LIMIT 1)," + iva + "," + imTotal.doubleValue() + ",'" + lista_parametros_sai.get(10).toString() + "')" + complemento;
            System.out.println("Consulta amortizaciones saicoop:" + consulta_amortizaciones_saicoop);
            Query query_amortizaciones_saicoop = em.createNativeQuery(consulta_amortizaciones_saicoop);
            if (channelId != 5) {
                query_amortizaciones_saicoop.setFirstResult(pageStartIndex);
                query_amortizaciones_saicoop.setMaxResults(pageSize);
            }
            List<Object[]> lista_amortizaciones_saicoop = query_amortizaciones_saicoop.getResultList();

            double abono = 0.0, io = 0.0;
            for (Object[] objetos : lista_amortizaciones_saicoop) {
                abono = Double.parseDouble(String.valueOf(objetos[2].toString()));
                io = Double.parseDouble(String.valueOf(objetos[3].toString()));
                LoanFee loanFeeDTO = new LoanFee();
                int estatusCuota = 0;
                if (String.valueOf(objetos[11]).toUpperCase().contains("OK")) {
                    if (abono == Double.parseDouble(objetos[8].toString())) {                       
                            estatusCuota = 3;//Pagada                        
                    } else {
                        estatusCuota = 1;//El estatus de la amortizacion es Activa
                    }
                } else {
                    estatusCuota = 2;//El estatus de la amortizacion es Vencido
                }
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                Date vence = sdf.parse(objetos[1].toString().replace("/", "-"));
                String converted = String.valueOf(convertToLocalDateTimeViaInstant(vence) + ":00.000Z");
                loanFeeDTO.setCapitalBalance(a.getSaldo().doubleValue());
                loanFeeDTO.setFeeNumber(Integer.parseInt(objetos[0].toString()));
                loanFeeDTO.setPrincipalAmount(abono);
                loanFeeDTO.setDueDate(converted);
                loanFeeDTO.setInterestAmount(io);
                loanFeeDTO.setOverdueAmount(Double.parseDouble(objetos[10].toString()));
                loanFeeDTO.setFeeStatusId(estatusCuota);
                loanFeeDTO.setOthersAmount(Double.parseDouble(objetos[7].toString()));
                loanFeeDTO.setTotalAmount(abono);
                lista_cuotas.add(loanFeeDTO);

            }
            System.out.println("Total de cuotas encontradas : " + lista_cuotas.size());

        } catch (Exception e) {
            System.out.println("Error al buscar tabla de amortizaciones en metodo loan Fee result:" + e.getMessage());
        } finally {
            em.close();
        }
        return lista_cuotas;

    }

    public List<LoanRate> LoanRates(String productBankIdentifier, int pageSize, int pageStartIndex) {
        EntityManager em = AbstractFacade.conexion();
        OpaDTO opa = util.opa(productBankIdentifier);
        List<LoanRate> listaRates = new ArrayList<>();
        //Consulto tasas
        try {
            AuxiliaresPK aux_pk = new AuxiliaresPK(opa.getIdorigenp(), opa.getIdproducto(), opa.getIdauxiliar());
            Auxiliares a = em.find(Auxiliares.class, aux_pk);

            String converted = "";//String.valueOf(convertToLocalDateTimeViaInstant(amm.getVence())+":00.000Z");
            LoanRate loanRate = new LoanRate();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            converted = a.getFechaactivacion() + "T00:00:00.000Z";////String.valueOf(convertToLocalDateTimeViaInstant(.getInitialdate()))+":00.000Z");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            converted = sdf.format(a.getFechaactivacion());
            converted = converted + "T00:00:00.000Z";

            loanRate.setId(1);
            loanRate.setRate(a.getTasaio().doubleValue());
            loanRate.setInitialDate(converted);
            listaRates.add(loanRate);
            //}

        } catch (Exception e) {

            System.out.println("Error en LoanRates:" + e.getMessage());
        }
        em.close();

        return listaRates;
    }

    public int numero() {
        Random r = new Random();
        int valorDado = r.nextInt(6) + 1;
        return valorDado;
    }

    public List<LoanPayment> loanPayments(String productBankIdentifier, int pageSize, int startPageIndex, int channelId) {
        EntityManager em = AbstractFacade.conexion();
        OpaDTO opa = util.opa(productBankIdentifier);
        List<LoanPayment> listPayment = new ArrayList();
        try {
            AuxiliaresPK aux_pk = new AuxiliaresPK(opa.getIdorigenp(), opa.getIdproducto(), opa.getIdauxiliar());
            Auxiliares a = em.find(Auxiliares.class, aux_pk);

            String fechaActivacion = dateToString(a.getFechaAutorizacion());

            List<Object[]> lista_objetos = null;
            //Ejecuto la funcion para devolver los pagos en libretas
            String consulta_pagos = "SELECT * FROM sai_estado_de_cuenta_libretas(" + opa.getIdorigenp() + "," + opa.getIdproducto() + "," + opa.getIdauxiliar() + ",'" + fechaActivacion + "',(SELECT date(fechatrabajo) FROM origenes LIMIT 1),0) WHERE replace(cargo,',','')::numeric=0";
            System.out.println("Consulta:" + consulta_pagos);
            //Ejecuto la funcion
            Query query_funcion_pagos = em.createNativeQuery(consulta_pagos);
            if (channelId != 5) {
                query_funcion_pagos.setFirstResult(startPageIndex);
                query_funcion_pagos.setMaxResults(pageSize);
                lista_objetos = query_funcion_pagos.getResultList();
            } else {
                lista_objetos = query_funcion_pagos.getResultList();
            }

            for (Object[] lista_pagos : lista_objetos) {

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String fecha = lista_pagos[0].toString().replace("-", "");
                Date f = sdf.parse(fecha);
                String fechaReal = sdf.format(f);

                Timestamp tss = new Timestamp(f.getTime());
                String consulta_aux_por_fecha_poliza = "SELECT * FROM auxiliares_d WHERE idorigenp=" + opa.getIdorigenp()
                        + " AND idproducto=" + opa.getIdproducto() + " AND idauxiliar=" + opa.getIdauxiliar()
                        + " AND date(fecha) ='" + fechaReal + "' AND replace(to_char(idorigenc,'099999')||to_char(idtipo,'09')||to_char(idpoliza,'09999'),' ','')='" + lista_pagos[1].toString().replace("-", "") + "'";

                Query query_auxiliard_fecha_poliza = em.createNativeQuery(consulta_aux_por_fecha_poliza, AuxiliaresD.class);
                AuxiliaresD aux_d = (AuxiliaresD) query_auxiliard_fecha_poliza.getSingleResult();

                String converted = "";
                LoanPayment pago = new LoanPayment();
                System.out.println("Fecha pago:" + lista_pagos[0]);
                if (aux_d.getCargoabono() == 1) {

                    Timestamp ts = new Timestamp(f.getTime());
                    converted = String.valueOf(convertToLocalDateTimeViaInstant(ts) + ":00.000Z");
                    pago.setCapitalBalance(Double.parseDouble(lista_pagos[4].toString().replace(",", "")));
                    pago.setFeeNumber(aux_d.getTransaccion());
                    pago.setMovementType(2);
                    pago.setOthersAmount(Double.parseDouble(lista_pagos[6].toString().replace(",", "")) + Double.parseDouble(lista_pagos[8].toString().replace(",", "")) + Double.parseDouble(lista_pagos[10].toString().replace(",", ""))); //Ivaio + IvaIm+ monto_v;
                    pago.setOverdueInterestAmount(Double.parseDouble(lista_pagos[7].toString().replace(",", "")));//im
                    pago.setNormalInterestAmount(Double.parseDouble(lista_pagos[5].toString().replace(",", "")));//io
                    pago.setPaymentDate(converted);//fecha
                    pago.setPrincipalAmount(Double.parseDouble(lista_pagos[3].toString().replace(",", "")));//abono
                    pago.setTotalAmount(Double.parseDouble(lista_pagos[3].toString().replace(",", ""))//Abono
                            + Double.parseDouble(lista_pagos[5].toString().replace(",", ""))//io
                            + Double.parseDouble(lista_pagos[6].toString().replace(",", ""))//ivaio
                            + Double.parseDouble(lista_pagos[7].toString().replace(",", ""))//im
                            + Double.parseDouble(lista_pagos[8].toString().replace(",", ""))//ivaim
                            + Double.parseDouble(lista_pagos[10].toString().replace(",", "")));//monto vencido
                    listPayment.add(pago);
                }
            }

        } catch (Exception e) {
            System.out.println("Error al buscar auxiliares d:" + e.getMessage());
        }
        return listPayment;
    }

    public FeesDueData RSFeesDueData(int o, int p, int a, int tipoamortizacion) {
        EntityManager em = AbstractFacade.conexion();
        String sai_auxiliar = "SELECT * FROM sai_auxiliar(" + o + "," + p + "," + a + ",(SELECT date(fechatrabajo) FROM origenes limit 1))";
        FeesDueData FeesDueDataRS = null;
        try {
            Query RsSai = em.createNativeQuery(sai_auxiliar);
            String sai = RsSai.getSingleResult().toString();
            String[] parts = sai.split("\\|");
            List pocisiones_sai = Arrays.asList(parts);

            String sai_bankingly_prestamo_cuanto = "SELECT sai_bankingly_prestamo_cuanto(" + o + ","
                    + p + ","
                    + a + ","
                    + "(SELECT date(fechatrabajo) FROM origenes limit 1)" + ","
                    + tipoamortizacion + ",'" + sai + "')";

            Double interestAmount = 0.0, OverDueAmount = 0.0, principalAmount = 0.0, othersAmount = 0.0, principalAmountTotal = 0.0, intereses_creciente_adelanto = 0.0;
            try {
                Query query_sai_bankingly_prestamo_cuanto = em.createNativeQuery(sai_bankingly_prestamo_cuanto);
                String cadena_cuanto_prestamo = query_sai_bankingly_prestamo_cuanto.getSingleResult().toString();
                String[] partes_cuanto_prestamo = cadena_cuanto_prestamo.split("\\|");
                List lista_posiciones_cuanto_prestamo = Arrays.asList(partes_cuanto_prestamo);

                if (!pocisiones_sai.get(13).toString().toUpperCase().equals("C")) {
                    interestAmount = Double.parseDouble(String.valueOf(lista_posiciones_cuanto_prestamo.get(3)));
                    othersAmount = Double.parseDouble(String.valueOf(lista_posiciones_cuanto_prestamo.get(4))) + Double.parseDouble(String.valueOf(lista_posiciones_cuanto_prestamo.get(6)));;
                    OverDueAmount = Double.parseDouble(String.valueOf(lista_posiciones_cuanto_prestamo.get(5)));
                    principalAmount = Double.parseDouble(String.valueOf(lista_posiciones_cuanto_prestamo.get(2)));

                    principalAmountTotal = interestAmount + othersAmount + OverDueAmount + principalAmount;
                }
            } catch (Exception e) {

            }

            FeesDueDataRS = new FeesDueData();
            FeesDueDataRS.setFeesDueInterestAmount(interestAmount);//interestAmount);(Solo interes mortios
            FeesDueDataRS.setFeesDueOthersAmount(othersAmount);//(iva de io + iva de im)
            FeesDueDataRS.setFeesDueOverdueAmount(OverDueAmount);//(moratorio
            FeesDueDataRS.setFeesDuePrincipalAmount(principalAmount);//monto vencido);(
            FeesDueDataRS.setFeesDueTotalAmount(principalAmountTotal);//principalAmount);(monto vencido)

            System.out.println("Detalle vencido :" + FeesDueDataRS);
        } catch (Exception e) {
            System.out.println("Error en FeesDueData:" + e.getMessage());

        } finally {
            em.close();
        }
        System.out.println("FeesDueData:" + FeesDueDataRS);
        return FeesDueDataRS;
    }

    //Metodo para devolver abonos vencidos
    public int RSFeesDue(int o, int p, int a) {
        EntityManager em = AbstractFacade.conexion();
        String sai_auxiliar = "SELECT * FROM sai_auxiliar(" + o + "," + p + "," + a + ",(SELECT date(fechatrabajo) FROM origenes limit 1))";
        int abonosVencidos = 0;
        try {
            Query RsSai = em.createNativeQuery(sai_auxiliar);
            String sai = RsSai.getSingleResult().toString();
            String[] parts = sai.split("\\|");
            List list = Arrays.asList(parts);
            double redondeado = Math.ceil(Double.parseDouble(list.get(2).toString()));
            abonosVencidos = (int) redondeado;
        } catch (Exception e) {
            System.out.println("Error en FeesDueData:" + e.getMessage());
        } finally {
            em.close();
        }
        System.out.println("El total de abonos vencidos es:" + abonosVencidos);
        return abonosVencidos;
    }

    //Devuelve LoanFee para apoyo en GetLoanPrincipal para obtener la proxima amortizacion
    public LoanFee nextFee(int o, int p, int a) {
        EntityManager em = AbstractFacade.conexion();
        //Obejeto para cuota
        LoanFee loanFee = new LoanFee();
        try {
            AuxiliaresPK pk = new AuxiliaresPK(o, p, a);
            Auxiliares aux = em.find(Auxiliares.class, pk);
            //Obtengo informacion con el sai_auxiliar hasta la fecha actual, si hay dudas checar el catalogo o atributos que devuelve la funcion(
            //Porque con el sai auxiliar obtengo fecha de la proxima amortizacion y voy a buscarla a la tabla de amortizaciones
            String sai_auxiliar = "SELECT * FROM sai_auxiliar(" + o + "," + p + "," + a + ",(SELECT date(fechatrabajo) FROM origenes limit 1))";
            Query RsSai = em.createNativeQuery(sai_auxiliar);
            String sai = RsSai.getSingleResult().toString();
            String[] parts = sai.split("\\|");
            List posiciones_sai = Arrays.asList(parts);
            Query query_next_fee = null;
            Amortizaciones amm = null;
            Double imvencido = 0.0, iovencido = 0.0, montoCuota = 0.0, intereses_creciente_adelanto = 0.0;
            Double interestAmount = 0.0, OverDueAmount = 0.0, principalAmount = 0.0, othersAmount = 0.0;

            //Calculo adelanto de interese para prestamos crecientes y si es hipotecario me devuelve 0 la funcion
            String sai_adelanto_de_interes = "SELECT sai_bankingly_monto_adelanto_interes(" + o + "," + p + "," + a + ",(SELECT date(fechatrabajo) FROM origenes limit 1),'" + sai + "')";
            try {
                Query calculo_adelanto_intereses = em.createNativeQuery(sai_adelanto_de_interes);
                intereses_creciente_adelanto = Double.parseDouble(String.valueOf(calculo_adelanto_intereses.getSingleResult()));
            } catch (Exception e) {
            }

            int idamortizacion = 0;
            int estatus_amortizacion = 0;//El estatus de la amortizacion
            if (posiciones_sai.get(13).toString().equals("C")) {
                //Obtengo la amortizacion que se vence
                String consultaA = "SELECT * FROM amortizaciones WHERE idorigenp=" + o
                        + " AND idproducto=" + p
                        + " AND idauxiliar=" + a
                        + " AND vence='" + posiciones_sai.get(10) + "' limit 1";
                query_next_fee = em.createNativeQuery(consultaA, Amortizaciones.class);
                amm = (Amortizaciones) query_next_fee.getSingleResult();

                if (Double.parseDouble(amm.getAbono().toString()) == Double.parseDouble(amm.getAbonopag().toString())) {
                    estatus_amortizacion = 3;
                } else if (Double.parseDouble(amm.getAbono().toString()) > Double.parseDouble(amm.getAbonopag().toString()) && amm.getTodopag() == false) {
                    estatus_amortizacion = 1;
                } else if (!posiciones_sai.get(13).toString().toUpperCase().equals("C")) {//Si esta vencido
                    estatus_amortizacion = 2;
                }
                idamortizacion = amm.getAmortizacionesPK().getIdamortizacion();
                //Double montovencido = Double.parseDouble(list.get(4).toString());
            } else {
                //Obtengo el idamortizacion que no hay que cubrir
                String consulta_id_amortizaciones = "SELECT idamortizacion FROM amortizaciones WHERE idorigenp=" + o
                        + " AND idproducto=" + p
                        + " AND idauxiliar=" + a
                        + " AND todopag=false ORDER BY vence limit 1";
                System.out.println("consulta_amortizacion_moroso:" + consulta_id_amortizaciones);
                Query id_amortizacion = em.createNativeQuery(consulta_id_amortizaciones);
                idamortizacion = Integer.parseInt(String.valueOf(id_amortizacion.getSingleResult()));

            }

            //Para obtener el total de la proxima cuota  total corro la funcion de pago completo
            String sai_bankingly_prestamo_cuanto = "SELECT sai_bankingly_prestamo_cuanto(" + aux.getAuxiliaresPK().getIdorigenp() + ","
                    + aux.getAuxiliaresPK().getIdproducto() + ","
                    + aux.getAuxiliaresPK().getIdauxiliar() + ","
                    + "(SELECT date(fechatrabajo) FROM origenes limit 1)" + ","
                    + aux.getTipoamortizacion() + ",'" + sai + "')";
            try {
                Query query_sai_bankingly_prestamo_cuanto = em.createNativeQuery(sai_bankingly_prestamo_cuanto);
                String cadena_cuanto_prestamo = query_sai_bankingly_prestamo_cuanto.getSingleResult().toString();
                String[] partes_cuanto_prestamo = cadena_cuanto_prestamo.split("\\|");
                List lista_posiciones_cuanto_prestamo = Arrays.asList(partes_cuanto_prestamo);
                montoCuota = Double.parseDouble(String.valueOf(lista_posiciones_cuanto_prestamo.get(1)))
                        + Double.parseDouble(String.valueOf(lista_posiciones_cuanto_prestamo.get(2)))
                        + Double.parseDouble(String.valueOf(lista_posiciones_cuanto_prestamo.get(3)))
                        + Double.parseDouble(String.valueOf(lista_posiciones_cuanto_prestamo.get(4)))
                        + Double.parseDouble(String.valueOf(lista_posiciones_cuanto_prestamo.get(5)))
                        + Double.parseDouble(String.valueOf(lista_posiciones_cuanto_prestamo.get(6)))
                        + Double.parseDouble(String.valueOf(lista_posiciones_cuanto_prestamo.get(7)))
                        + intereses_creciente_adelanto;

                interestAmount = Double.parseDouble(String.valueOf(lista_posiciones_cuanto_prestamo.get(3))) + Double.parseDouble(String.valueOf(lista_posiciones_cuanto_prestamo.get(4)));
                OverDueAmount = Double.parseDouble(String.valueOf(lista_posiciones_cuanto_prestamo.get(5))) + Double.parseDouble(String.valueOf(lista_posiciones_cuanto_prestamo.get(6)));
                principalAmount = Double.parseDouble(String.valueOf(lista_posiciones_cuanto_prestamo.get(2))) + Double.parseDouble(String.valueOf(lista_posiciones_cuanto_prestamo.get(7)));
                othersAmount = Double.parseDouble(String.valueOf(lista_posiciones_cuanto_prestamo.get(1))) + intereses_creciente_adelanto;
            } catch (Exception e) {

            }

            //montoCuota = Double.parseDouble(String.valueOf(list.get(11))) + Double.parseDouble(String.valueOf(list.get(4))) + iovencido + imvencido + seguros_hipotecarios;
            //Double abonoT = Double.parseDouble(amm.getAbono().toString()) + iovencido + imvencido;
            loanFee.setCapitalBalance(Double.parseDouble(aux.getSaldo().toString()));//Saldo o balance actual del prestamo
            loanFee.setFeeNumber(idamortizacion);//(amm.getAmortizacionesPK().getIdorigenp() + amm.getAmortizacionesPK().getIdproducto() + amm.getAmortizacionesPK().getIdauxiliar() + amm.getAmortizacionesPK().getIdamortizacion());//Numero de cuota
            loanFee.setPrincipalAmount(principalAmount);//monto de la cuota
            loanFee.setDueDate(String.valueOf(posiciones_sai.get(10)));//fecha de vencimiento
            loanFee.setInterestAmount(interestAmount);//Monto de interes io + ivaio
            loanFee.setOverdueAmount(OverDueAmount);//Monto im+ivaim
            loanFee.setFeeStatusId(estatus_amortizacion);//Estado de la amortizacion
            loanFee.setOthersAmount(othersAmount);//Otros conceptos asociados seguros si tiene hipotecario
            loanFee.setTotalAmount(montoCuota);//Monto total de la cuota

            System.out.println("Proxima cuota : " + loanFee);
        } catch (Exception e) {
            System.out.println("Error en obtener la prxima cuota:" + e.getMessage());
        } finally {
            em.close();
        }

        return loanFee;
    }

    //Metodo para devolver dias vencidos
    public int RSOverdueDays(int o, int p, int a) {
        EntityManager em = AbstractFacade.conexion();
        String sai_auxiliar = "SELECT * FROM sai_auxiliar(" + o + "," + p + "," + a + ",(SELECT date(fechatrabajo) FROM origenes limit 1))";
        int diasVencidos = 0;
        try {
            Query RsSai = em.createNativeQuery(sai_auxiliar);
            String sai = RsSai.getSingleResult().toString();
            String[] parts = sai.split("\\|");
            List list = Arrays.asList(parts);
            diasVencidos = Integer.parseInt(list.get(3).toString());
        } catch (Exception e) {
            System.out.println("Error en FeesDueData:" + e.getMessage());
        } finally {
            em.close();
        }

        return diasVencidos;
    }

    public int tipoproducto(int idproducto) {
        EntityManager em = AbstractFacade.conexion();
        int tipoproducto = 0;
        try {
            String consulta = "SELECT tipoproducto FROM productos WHERE idproducto=" + idproducto;
            Query query = em.createNativeQuery(consulta);
            tipoproducto = Integer.parseInt(String.valueOf(query.getSingleResult()));
        } catch (Exception e) {

            System.out.println("Error en buscar tipoproducto:" + e.getMessage());
        } finally {
            em.close();
        }
        return tipoproducto;
    }

    public static Date stringTodate(String fecha) {
        Date date = null;
        try {
            SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
            date = formato.parse(fecha);
        } catch (ParseException ex) {
            System.out.println("Error al convertir fecha:" + ex.getMessage());
        }
        System.out.println("date:" + date);
        return date;
    }

    public LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public int contadorFeesPayments(String productBankIdentifier, int identificador, int feesStatus) {
        EntityManager em = AbstractFacade.conexion();
        int cont = 0;
        OpaDTO opa = util.opa(productBankIdentifier);
        //Identificador 1 es fees y 2 es payments

        try {
            AuxiliaresPK aux_pk = new AuxiliaresPK(opa.getIdorigenp(), opa.getIdproducto(), opa.getIdauxiliar());
            Auxiliares a = em.find(Auxiliares.class, aux_pk);
            String consulta = "";
            Query query = null;
            List<Object[]> lista_objetos = null;
            if (identificador == 1) {
                String complemento = "";
                if (feesStatus == 0) {
                    complemento = "ORDER BY idamortizacion";
                } else if (feesStatus == 1) {
                    complemento = " WHERE abonado<>abono AND date(vence) > (SELECT date(fechatrabajo) FROM origenes LIMIT 1) ORDER BY idamortizacion";
                } else if (feesStatus == 2) {
                    complemento = " WHERE abonado=abono ORDER BY idamortizacion";
                }

                // checar el catalogo o atributos que devuelve la funcion
                String sai_auxiliar = "SELECT sai_auxiliar(" + opa.getIdorigenp() + "," + opa.getIdproducto() + "," + opa.getIdauxiliar() + ",(SELECT date(fechatrabajo) FROM origenes limit 1))";
                Query RsSai = em.createNativeQuery(sai_auxiliar);
                String cadena_sai_auxiliar = RsSai.getSingleResult().toString();
                String[] parametros_sai = cadena_sai_auxiliar.split("\\|");
                List lista_parametros_sai = Arrays.asList(parametros_sai);
                System.out.println("El sai auxiliar es:" + cadena_sai_auxiliar);

                //Obtenemos el iva para el producto segun la sucursal
                String consulta_iva_segun_sucursal = "SELECT sai_iva_segun_sucursal(" + opa.getIdorigenp() + ", idproducto," + a.getTipoamortizacion() + ") FROM productos WHERE idproducto=" + opa.getIdproducto();
                System.out.println("Consulta para iva_sucursal :" + consulta_iva_segun_sucursal);
                Query query_calculo_iva_sucursal = em.createNativeQuery(consulta_iva_segun_sucursal);
                String iva_segun_sucursal = String.valueOf(query_calculo_iva_sucursal.getSingleResult());

                SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");

                BigDecimal iva = new BigDecimal(iva_segun_sucursal);
                BigDecimal imTotal = new BigDecimal(lista_parametros_sai.get(15).toString());

                //Traemos una lista de objetos de amortizaciones que se muestran en SAICoop
                consulta = "SELECT * FROM sai_tabla_amortizaciones_t0_calculada('amortizaciones'," + opa.getIdorigenp() + "," + opa.getIdproducto() + "," + opa.getIdauxiliar() + ",(SELECT to_char(date(fechatrabajo),'yyyy-MM-dd') FROM origenes LIMIT 1)," + iva + "," + imTotal.doubleValue() + ",'" + lista_parametros_sai.get(10).toString() + "')" + complemento;
                
                query = em.createNativeQuery(consulta);                
                lista_objetos = query.getResultList();
                cont = lista_objetos.size();

            } else if (identificador == 2) {
                String fechaActivacion = dateToString(a.getFechaAutorizacion());
                consulta = "SELECT * FROM sai_estado_de_cuenta_libretas(" + opa.getIdorigenp() + "," + opa.getIdproducto() + "," + opa.getIdauxiliar() + ",'" + fechaActivacion + "',(SELECT date(fechatrabajo) FROM origenes LIMIT 1),0) WHERE replace(cargo,',','')::numeric=0";
                query = em.createNativeQuery(consulta);
                lista_objetos = query.getResultList();
                cont = lista_objetos.size();
            }

            System.out.println("cont:" + cont);
        } catch (Exception e) {
            System.out.println("Error al obtener contador general:" + e.getMessage());
            return 0;
        } finally {
            em.close();
        }
        return cont;
    }

    public String dateToString(Date cadena) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String cadenaStr = sdf.format(cadena);
        return cadenaStr;
    }

    public boolean actividad_horario() {
        EntityManager em = AbstractFacade.conexion();
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
