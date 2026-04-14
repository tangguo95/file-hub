package com.tydic.filehub.service.impl;

/**
 * <br>
 * 标题：中台系统业务异常类<br>
 * 描述：用于中台信息各类业务异常的<br>
 * */
public class BusiException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8364826228386701060L;
	/** 异常编码 */
	private String exceptionCode;
	/** 异常描述 */
	private String exceptionDesc;

	public BusiException(String exceptionCode, String exceptionDesc) {
		super(exceptionDesc);
		this.exceptionCode = exceptionCode;
		this.exceptionDesc = exceptionDesc;
	}

	public BusiException(String exceptionCode, String exceptionDesc,
                         Throwable cause) {
		super(exceptionDesc, cause);
		this.exceptionCode = exceptionCode;
		this.exceptionDesc = exceptionDesc;
	}

	public String getExceptionCode() {
		return exceptionCode;
	}

	public String getExceptionDesc() {
		return exceptionDesc;
	}

	public String getExceptionInfo() {
		return "exceptionCode:" + this.exceptionCode + ",exceptionDesc:"
				+ this.exceptionDesc;
	}
}
