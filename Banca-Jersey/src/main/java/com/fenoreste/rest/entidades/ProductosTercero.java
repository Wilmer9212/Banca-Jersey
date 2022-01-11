/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.entidades;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author wilmer
 */
@Entity
@Table(name = "bankingly_informacion_terceros")
public class ProductosTercero implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @Column(name = "productnumber")
    private String productnumber;
    @Column(name = "producttypeid")
    private Integer producttypeid;   
    @Column(name="alias")
    private String alias;
    @Column(name = "beneficiaryemail")
    private String beneficiaryemail;

    public ProductosTercero() {
    }

    public Integer getProducttypeid() {
        return producttypeid;
    }

    public void setProducttypeid(Integer producttypeid) {
        this.producttypeid = producttypeid;
    }

    public String getProductnumber() {
        return productnumber;
    }

    public void setProductnumber(String productnumber) {
        this.productnumber = productnumber;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getBeneficiaryemail() {
        return beneficiaryemail;
    }

    public void setBeneficiaryemail(String beneficiaryemail) {
        this.beneficiaryemail = beneficiaryemail;
    }

    @Override
    public String toString() {
        return "ProductosTercero{" + "producttypeid=" + producttypeid + ", productnumber=" + productnumber + ", alias=" + alias + ", beneficiaryemail=" + beneficiaryemail + '}';
    }
    
    
    
    
}
