// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.model.space;

import it.frob.sleighidea.psi.SleighIdentifier;
import it.frob.sleighidea.psi.SleighSymbol;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnegative;

/**
 * Memory space variable class container.
 */
public class Variable {

    /**
     * The memory space variable element in the parse tree.
     */
    private final SleighSymbol symbol;

    /**
     * The variable name.
     */
    private final String name;

    /**
     * Create a {@link Variable} instance.
     *
     * @param element the containing element.
     * @param name    the variable name.
     */
    public Variable(SleighSymbol element, String name) {
        this.symbol = element;
        this.name = name;
    }

    /**
     * Get the variable name.
     *
     * @return the variable name.
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Get the PSI element referencing the variable data.
     *
     * @return the {@link SleighSymbol} element for the variable.
     */
    public SleighSymbol getElement() {
        return symbol;
    }
}
