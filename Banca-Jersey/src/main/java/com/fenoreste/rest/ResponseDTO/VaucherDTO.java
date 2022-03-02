/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.ResponseDTO;

/**
 *
 * @author wilmer
 */
public class VaucherDTO {
    
    String  productBankStatementFile;
	String productBankStatementFileName;
	
	public VaucherDTO() {
		// TODO Auto-generated constructor stub
	}

	public String getProductBankStatementFile() {
		return productBankStatementFile;
	}

	public void setProductBankStatementFile(String productBankStatementFile) {
		this.productBankStatementFile = productBankStatementFile;
	}

	public String getProductBankStatementFileName() {
		return productBankStatementFileName;
	}

	public void setProductBankStatementFileName(String productBankStatementFileName) {
		this.productBankStatementFileName = productBankStatementFileName;
	}
}
