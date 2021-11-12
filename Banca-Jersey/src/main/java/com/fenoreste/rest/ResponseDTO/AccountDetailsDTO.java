/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.ResponseDTO;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 *
 * @author wilmer
 */
public class AccountDetailsDTO {

    private String AccountBankIdentifier;
    private String AccountOfficerName;
    private BigDecimal AccountCountableBalance;
    private BigDecimal AccountAvailableBalance;
    private BigDecimal AccountBalance24Hrs;
    private BigDecimal AccountBalance48Hrs;
    private BigDecimal AccountBalance48MoreHrs;
    private BigDecimal MonthlyAverageBalance;
    private int PendingChecks;
    private int ChecksToReleaseToday;
    private int ChecksToReleaseTomorrow;
    private int CancelledChecks;
    private int CertifiedChecks;
    private int RejectedChecks;
    private int BlockedAmount;
    private double MovementsOfTheMonth;
    private int ChecksDrawn;
    private Double Overdrafts;
    private String ProductBranchName;
    private String ProductOwnerName;
    private Boolean ShowCurrentAccountChecksInformation;

    public AccountDetailsDTO() {
    }

    public String getAccountBankIdentifier() {
        return AccountBankIdentifier;
    }

    public void setAccountBankIdentifier(String AccountBankIdentifier) {
        this.AccountBankIdentifier = AccountBankIdentifier;
    }

    public String getAccountOfficerName() {
        return AccountOfficerName;
    }

    public void setAccountOfficerName(String AccountOfficerName) {
        this.AccountOfficerName = AccountOfficerName;
    }

    public BigDecimal getAccountCountableBalance() {
        return AccountCountableBalance;
    }

    public void setAccountCountableBalance(BigDecimal AccountCountableBalance) {
        this.AccountCountableBalance = AccountCountableBalance;
    }

    public BigDecimal getAccountAvailableBalance() {
        return AccountAvailableBalance;
    }

    public void setAccountAvailableBalance(BigDecimal AccountAvailableBalance) {
        this.AccountAvailableBalance = AccountAvailableBalance;
    }

    public BigDecimal getAccountBalance24Hrs() {
        return AccountBalance24Hrs;
    }

    public void setAccountBalance24Hrs(BigDecimal AccountBalance24Hrs) {
        this.AccountBalance24Hrs = AccountBalance24Hrs;
    }

    public BigDecimal getAccountBalance48Hrs() {
        return AccountBalance48Hrs;
    }

    public void setAccountBalance48Hrs(BigDecimal AccountBalance48Hrs) {
        this.AccountBalance48Hrs = AccountBalance48Hrs;
    }

    public BigDecimal getAccountBalance48MoreHrs() {
        return AccountBalance48MoreHrs;
    }

    public void setAccountBalance48MoreHrs(BigDecimal AccountBalance48MoreHrs) {
        this.AccountBalance48MoreHrs = AccountBalance48MoreHrs;
    }

    public BigDecimal getMonthlyAverageBalance() {
        return MonthlyAverageBalance;
    }

    public void setMonthlyAverageBalance(BigDecimal MonthlyAverageBalance) {
        this.MonthlyAverageBalance = MonthlyAverageBalance;
    }

    public int getPendingChecks() {
        return PendingChecks;
    }

    public void setPendingChecks(int PendingChecks) {
        this.PendingChecks = PendingChecks;
    }

    public int getChecksToReleaseToday() {
        return ChecksToReleaseToday;
    }

    public void setChecksToReleaseToday(int ChecksToReleaseToday) {
        this.ChecksToReleaseToday = ChecksToReleaseToday;
    }

    public int getChecksToReleaseTomorrow() {
        return ChecksToReleaseTomorrow;
    }

    public void setChecksToReleaseTomorrow(int ChecksToReleaseTomorrow) {
        this.ChecksToReleaseTomorrow = ChecksToReleaseTomorrow;
    }

    public int getCancelledChecks() {
        return CancelledChecks;
    }

    public void setCancelledChecks(int CancelledChecks) {
        this.CancelledChecks = CancelledChecks;
    }

    public int getCertifiedChecks() {
        return CertifiedChecks;
    }

    public void setCertifiedChecks(int CertifiedChecks) {
        this.CertifiedChecks = CertifiedChecks;
    }

    public int getRejectedChecks() {
        return RejectedChecks;
    }

    public void setRejectedChecks(int RejectedChecks) {
        this.RejectedChecks = RejectedChecks;
    }

    public int getBlockedAmount() {
        return BlockedAmount;
    }

    public void setBlockedAmount(int BlockedAmount) {
        this.BlockedAmount = BlockedAmount;
    }

    public double getMovementsOfTheMonth() {
        return MovementsOfTheMonth;
    }

    public void setMovementsOfTheMonth(double MovementsOfTheMonth) {
        this.MovementsOfTheMonth = MovementsOfTheMonth;
    }

    public int getChecksDrawn() {
        return ChecksDrawn;
    }

    public void setChecksDrawn(int ChecksDrawn) {
        this.ChecksDrawn = ChecksDrawn;
    }

    public Double getOverdrafts() {
        return Overdrafts;
    }

    public void setOverdrafts(Double Overdrafts) {
        this.Overdrafts = Overdrafts;
    }

    public String getProductBranchName() {
        return ProductBranchName;
    }

    public void setProductBranchName(String ProductBranchName) {
        this.ProductBranchName = ProductBranchName;
    }

    public String getProductOwnerName() {
        return ProductOwnerName;
    }

    public void setProductOwnerName(String ProductOwnerName) {
        this.ProductOwnerName = ProductOwnerName;
    }

    public Boolean getShowCurrentAccountChecksInformation() {
        return ShowCurrentAccountChecksInformation;
    }

    public void setShowCurrentAccountChecksInformation(Boolean ShowCurrentAccountChecksInformation) {
        this.ShowCurrentAccountChecksInformation = ShowCurrentAccountChecksInformation;
    }

    @Override
    public String toString() {
        return "AccountDetailsDTO{" + "AccountBankIdentifier=" + AccountBankIdentifier + ", AccountOfficerName=" + AccountOfficerName + ", AccountCountableBalance=" + AccountCountableBalance + ", AccountAvailableBalance=" + AccountAvailableBalance + ", AccountBalance24Hrs=" + AccountBalance24Hrs + ", AccountBalance48Hrs=" + AccountBalance48Hrs + ", AccountBalance48MoreHrs=" + AccountBalance48MoreHrs + ", MonthlyAverageBalance=" + MonthlyAverageBalance + ", PendingChecks=" + PendingChecks + ", ChecksToReleaseToday=" + ChecksToReleaseToday + ", ChecksToReleaseTomorrow=" + ChecksToReleaseTomorrow + ", CancelledChecks=" + CancelledChecks + ", CertifiedChecks=" + CertifiedChecks + ", RejectedChecks=" + RejectedChecks + ", BlockedAmount=" + BlockedAmount + ", MovementsOfTheMonth=" + MovementsOfTheMonth + ", ChecksDrawn=" + ChecksDrawn + ", Overdrafts=" + Overdrafts + ", ProductBranchName=" + ProductBranchName + ", ProductOwnerName=" + ProductOwnerName + ", ShowCurrentAccountChecksInformation=" + ShowCurrentAccountChecksInformation + '}';
    }

  
    
}
