// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.model.space;

import it.frob.sleighidea.psi.SleighIdentifier;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnegative;

/**
 * Memory space variable class container.
 */
public class Variable {

    /**
     * The memory space variable element in the parse tree.
     */
    private final SleighIdentifier element;

    /**
     * The variable name.
     */
    private final String name;

    /**
     * The variable size.
     */
    private final int size;

    /**
     * The variable offset in the source memory space.
     */
    private final int offset;

    /**
     * Create a {@link Variable} instance.
     *
     * @param element the containing element.
     * @param name    the variable name.
     * @param size    the variable size.
     * @param offset  the variable offset.
     */
    public Variable(SleighIdentifier element, String name, int size, int offset) {
        this.element = element;
        this.name = name;
        this.size = size;
        this.offset = offset;
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
     * Get the variable size.
     *
     * @return the variable size.
     */
    @Nonnegative
    public int getSize() {
        return size;
    }

    /**
     * Get the variable offset.
     *
     * @return the variable offset.
     */
    @Nonnegative
    public int getOffset() {
        return offset;
    }

    /**
     * Get the PSI element referencing the variable data.
     *
     * @return the {@link SleighIdentifier} element for the variable.
     */
    public SleighIdentifier getElement() {
        return element;
    }
}
