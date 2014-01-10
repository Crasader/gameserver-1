package com.xinqihd.sns.gameserver.script;

import java.util.List;

/**
 * The result returned by a script object.
 * 
 * @author wangqi
 *
 */
public class ScriptResult {
	
	//The result't type
	private Type type;
	
	//The result's content in map.
	private List result;
	
	//The error message.
	private String error;
	
	//The error occurred when running.
	//It is null if the script runs OK.
	private Throwable cause;
	
	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * @return the result
	 */
	public List getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(List result) {
		this.result = result;
	}

	/**
	 * @return the cause
	 */
	public Throwable getCause() {
		return cause;
	}

	/**
	 * @param cause the cause to set
	 */
	public void setCause(Throwable cause) {
		this.cause = cause;
	}

	/**
	 * @return the error
	 */
	public String getError() {
		return error;
	}

	/**
	 * @param error the error to set
	 */
	public void setError(String error) {
		this.error = error;
	}

	/**
	 * The result's type.
	 * @author wangqi
	 *
	 */
	public static enum Type {
		/**
		 * The script run successfully without any result to return.
		 */
		SUCCESS,
		/**
		 * The script returned a result to the caller.
		 * The normal process in the caller should be continued to run.
		 */
		SUCCESS_RETURN,
		/**
		 * The caller should abort the normal process and return,
		 * because the script already process the request.
		 */
		CALLER_ABORT,
		/**
		 * The script failed to run. The caller can get the occurred exceptions
		 * or just ignore it and run normally.
		 */
		SCRIPT_FAIL,
	}
}
