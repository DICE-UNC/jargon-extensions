/**
 * 
 */
package org.irods.jargon.vircoll;

import org.irods.jargon.core.exception.JargonException;

/**
 * General exception in virtual collection processing. This deals more with CRUD
 * of virtual collections versus executing queries on them
 * 
 * @author Mike Conway - DICE
 * 
 */
public class VirtualCollectionException extends JargonException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7870998677444041033L;

	public VirtualCollectionException(String message,
			int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	public VirtualCollectionException(String message, Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public VirtualCollectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public VirtualCollectionException(String message) {
		super(message);
	}

	public VirtualCollectionException(Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public VirtualCollectionException(Throwable cause) {
		super(cause);
	}

}
