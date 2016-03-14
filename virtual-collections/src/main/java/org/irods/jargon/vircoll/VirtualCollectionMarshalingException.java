/**
 * 
 */
package org.irods.jargon.vircoll;

import org.irods.jargon.vircoll.exception.VirtualCollectionException;

/**
 * Exception with marshaling, unmarshaling of virtual collections
 * 
 * @author Mike Conway - DICE
 * 
 */
public class VirtualCollectionMarshalingException extends
		VirtualCollectionException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3016324162282443403L;

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public VirtualCollectionMarshalingException(String message,
			int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public VirtualCollectionMarshalingException(String message,
			Throwable cause, int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public VirtualCollectionMarshalingException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public VirtualCollectionMarshalingException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public VirtualCollectionMarshalingException(Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 */
	public VirtualCollectionMarshalingException(Throwable cause) {
		super(cause);
	}

}
