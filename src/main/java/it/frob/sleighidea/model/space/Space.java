// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.model.space;

import it.frob.sleighidea.model.ModelException;
import it.frob.sleighidea.psi.SleighSpacedef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnegative;

/**
 * Memory space class container.
 */
public class Space {

    /**
     * The memory space definition element in the parse tree.
     */
    private final SleighSpacedef definition;

    /**
     * The memory space name.
     */
    private final String name;

    /**
     * The memory space type.
     */
    private final String type;

    /**
     * The memory space size.
     */
    private final int size;

    /**
     * The memory space word size.
     */
    private final int wordSize;

    /**
     * The memory space default flag.
     */
    private final boolean isDefault;

    /**
     * Use the given {@link SleighSpacedef} instance as a data source.
     *
     * @param definition the definition element to extract data from.
     * @throws ModelException if the extraction process failed.
     */
    public Space(@NotNull SleighSpacedef definition) throws ModelException {
        SpaceVisitor visitor = new SpaceVisitor();
        definition.acceptChildren(visitor);

        if (!visitor.isValid()) {
            throw new ModelException("Invalid space definition found.");
        }

        this.definition = definition;
        name = visitor.getName();
        type = visitor.getType();
        size = visitor.getSize();
        wordSize = visitor.getWordSize();
        isDefault = visitor.isDefault();
    }

    /**
     * Get the memory space definition parse tree element.
     *
     * @return the memory space definition parse tree element.
     */
    @NotNull
    public SleighSpacedef getDefinition() {
        return definition;
    }

    /**
     * Get the memory space name.
     *
     * @return the memory space name.
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Get the memory space type.
     *
     * @return the memory space type, or {@code null} if no type is present.
     */
    @Nullable
    public String getType() {
        return type;
    }

    /**
     * Get the memory space size.
     *
     * @return the memory space size.
     */
    @Nonnegative
    public int getSize() {
        return size;
    }

    /**
     * Get the memory space word size.
     *
     * @return the memory space word size.
     */
    @Nonnegative
    public int getWordSize() {
        return wordSize;
    }

    /**
     * Get the memory space default flag.
     *
     * @return the memory space default flag.
     */
    public boolean isDefault() {
        return isDefault;
    }
}
