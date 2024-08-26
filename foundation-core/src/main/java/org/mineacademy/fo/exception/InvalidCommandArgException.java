package org.mineacademy.fo.exception;

import org.mineacademy.fo.model.SimpleComponent;

/**
 * Thrown when a command has invalid argument
 */
public final class InvalidCommandArgException extends CommandException {

	private static final long serialVersionUID = 1L;

	public InvalidCommandArgException() {
	}

	public InvalidCommandArgException(SimpleComponent message) {
		super(message);
	}
}