// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.model;

/**
 * Default exception thrown in case of model inconsistencies.
 */
public class ModelException extends Exception {

    /**
     * Create a model exception with no message.
     */
    public ModelException() {

    }

    /**
     * Create a model exception with a custom message.
     *
     * @param message the exception message.
     */
    public ModelException(String message) {
        super(message);
    }
}
