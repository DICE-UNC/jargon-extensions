/**
 * 
 */
package org.irods.jargon.vircoll;

import org.irods.jargon.vircoll.exception.VirtualCollectionException;

/**
 * Exception caused by an invalid query (missing parameters, malformed query
 * information)
 * 
 * @author Mike Conway - DICE
 *
 */
public class VirtualCollectionValidationException extends
		VirtualCollectionException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3496906201114547216L;

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public VirtualCollectionValidationException(String message,
			int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public VirtualCollectionValidationException(String message,
			Throwable cause, int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public VirtualCollectionValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public VirtualCollectionValidationException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public VirtualCollectionValidationException(Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 */
	public VirtualCollectionValidationException(Throwable cause) {
		super(cause);
	}

}
