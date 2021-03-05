/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.ResponseDTO;

import java.util.Date;

/**
 *
 * @author Elliot
 */
public class GetProductsConsolidatePositionDTO {
      private String clientBankIdentifier;
      private String productBankIdentifier;
      private String productTypeId;
      private String productAlias;
      private String productNumber;
      private String localCurrencyId;
      private Double localBalance;
      private String internationalCurrencyId;
      private Double internationalBalance;
      private Double rate;
      private Date expirationDate;
      private Integer paidFees;
      private Double term;
      private Date nextFeeDueDate;
      private String productOwnerName;
      private String productBranchName;
      private Integer canTransact;
      private Integer subsidiaryId;
      private String subsidiaryName;
      private Integer backendId;

    public GetProductsConsolidatePositionDTO() {
    }

    public GetProductsConsolidatePositionDTO(String clientBankIdentifier, String productBankIdentifier, String productTypeId, String productAlias, String productNumber, String localCurrencyId, Double localBalance, String internationalCurrencyId, Double internationalBalance, Double rate, Date expirationDate, Integer paidFees, Double term, Date nextFeeDueDate, String productOwnerName, String productBranchName, Integer canTransact, Integer subsidiaryId, String subsidiaryName, Integer backendId) {
        this.clientBankIdentifier = clientBankIdentifier;
        this.productBankIdentifier = productBankIdentifier;
        this.productTypeId = productTypeId;
        this.productAlias = productAlias;
        this.productNumber = productNumber;
        this.localCurrencyId = localCurrencyId;
        this.localBalance = localBalance;
        this.internationalCurrencyId = internationalCurrencyId;
        this.internationalBalance = internationalBalance;
        this.rate = rate;
        this.expirationDate = expirationDate;
        this.paidFees = paidFees;
        this.term = term;
        this.nextFeeDueDate = nextFeeDueDate;
        this.productOwnerName = productOwnerName;
        this.productBranchName = productBranchName;
        this.canTransact = canTransact;
        this.subsidiaryId = subsidiaryId;
        this.subsidiaryName = subsidiaryName;
        this.backendId = backendId;
    }

    public String getClientBankIdentifier() {
        return clientBankIdentifier;
    }

    public void setClientBankIdentifier(String clientBankIdentifier) {
        this.clientBankIdentifier = clientBankIdentifier;
    }

    public String getProductBankIdentifier() {
        return productBankIdentifier;
    }

    public void setProductBankIdentifier(String productBankIdentifier) {
        this.productBankIdentifier = productBankIdentifier;
    }

    public String getProductTypeId() {
        return productTypeId;
    }

    public void setProductTypeId(String productTypeId) {
        this.productTypeId = productTypeId;
    }

    public String getProductAlias() {
        return productAlias;
    }

    public void setProductAlias(String productAlias) {
        this.productAlias = productAlias;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(String productNumber) {
        this.productNumber = productNumber;
    }

    public String getLocalCurrencyId() {
        return localCurrencyId;
    }

    public void setLocalCurrencyId(String localCurrencyId) {
        this.localCurrencyId = localCurrencyId;
    }

    public Double getLocalBalance() {
        return localBalance;
    }

    public void setLocalBalance(Double localBalance) {
        this.localBalance = localBalance;
    }

    public String getInternationalCurrencyId() {
        return internationalCurrencyId;
    }

    public void setInternationalCurrencyId(String internationalCurrencyId) {
        this.internationalCurrencyId = internationalCurrencyId;
    }

    public Double getInternationalBalance() {
        return internationalBalance;
    }

    public void setInternationalBalance(Double internationalBalance) {
        this.internationalBalance = internationalBalance;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Integer getPaidFees() {
        return paidFees;
    }

    public void setPaidFees(Integer paidFees) {
        this.paidFees = paidFees;
    }

    public Double getTerm() {
        return term;
    }

    public void setTerm(Double term) {
        this.term = term;
    }

    public Date getNextFeeDueDate() {
        return nextFeeDueDate;
    }

    public void setNextFeeDueDate(Date nextFeeDueDate) {
        this.nextFeeDueDate = nextFeeDueDate;
    }

    public String getProductOwnerName() {
        return productOwnerName;
    }

    public void setProductOwnerName(String productOwnerName) {
        this.productOwnerName = productOwnerName;
    }

    public String getProductBranchName() {
        return productBranchName;
    }

    public void setProductBranchName(String productBranchName) {
        this.productBranchName = productBranchName;
    }

    public Integer getCanTransact() {
        return canTransact;
    }

    public void setCanTransact(Integer canTransact) {
        this.canTransact = canTransact;
    }

    public Integer getSubsidiaryId() {
        return subsidiaryId;
    }

    public void setSubsidiaryId(Integer subsidiaryId) {
        this.subsidiaryId = subsidiaryId;
    }

    public String getSubsidiaryName() {
        return subsidiaryName;
    }

    public void setSubsidiaryName(String subsidiaryName) {
        this.subsidiaryName = subsidiaryName;
    }

    public Integer getBackendId() {
        return backendId;
    }

    public void setBackendId(Integer backendId) {
        this.backendId = backendId;
    }

    @Override
    public String toString() {
        return "GetProductsConsolidatePositionDTO{" + "clientBankIdentifier=" + clientBankIdentifier + ", productBankIdentifier=" + productBankIdentifier + ", productTypeId=" + productTypeId + ", productAlias=" + productAlias + ", productNumber=" + productNumber + ", localCurrencyId=" + localCurrencyId + ", localBalance=" + localBalance + ", internationalCurrencyId=" + internationalCurrencyId + ", internationalBalance=" + internationalBalance + ", rate=" + rate + ", expirationDate=" + expirationDate + ", paidFees=" + paidFees + ", term=" + term + ", nextFeeDueDate=" + nextFeeDueDate + ", productOwnerName=" + productOwnerName + ", productBranchName=" + productBranchName + ", canTransact=" + canTransact + ", subsidiaryId=" + subsidiaryId + ", subsidiaryName=" + subsidiaryName + ", backendId=" + backendId + '}';
    }
      
      
}
