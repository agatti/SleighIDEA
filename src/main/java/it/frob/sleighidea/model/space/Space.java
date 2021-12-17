// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.model.space;

import it.frob.sleighidea.model.ModelException;
import it.frob.sleighidea.psi.SleighSpaceDefinition;
import it.frob.sleighidea.psi.SleighVariablesNodeDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnegative;
import java.util.*;

/**
 * Memory space class container.
 */
public class Space {

    /**
     * The memory space definition element in the parse tree.
     */
    private final SleighSpaceDefinition definition;

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
     * The memory space variables definition nodes.
     */
    private final List<VariablesContainer> containers = new ArrayList<>();

    /**
     * The variables bound to the memory space.
     */
    private final Map<String, Variable> variables = new HashMap<>();

    /**
     * Use the given {@link SleighSpaceDefinition} instance as a data source.
     *
     * @param definition the definition element to extract data from.
     * @throws ModelException if the extraction process failed.
     */
    public Space(@NotNull SleighSpaceDefinition definition, @NotNull Collection<SleighVariablesNodeDefinition> variableDeclarations)
            throws ModelException {
        SpaceVisitor visitor = new SpaceVisitor();
        definition.acceptChildren(visitor);

        if (!visitor.isValid()) {
            throw new ModelException("Invalid space definition found.");
        }

        for (SleighVariablesNodeDefinition declaration : variableDeclarations) {
            if (declaration.getSymbol().getValue().equals(visitor.getName())) {
                VariablesContainer variablesContainer = new VariablesContainer(declaration);

                for (Variable variable : variablesContainer.getVariables()) {
                    if (variables.containsKey(variable.getName())) {
                        // TODO: Mark the node with an error - duplicate variable.
                        throw new ModelException("Duplicate variable name found.");
                    }

                    variables.put(variable.getName(), variable);
                }

                containers.add(variablesContainer);
            }
        }

        this.definition = definition;
        name = visitor.getName();
        type = visitor.getType();
        size = Objects.requireNonNull(visitor.getSize());
        wordSize = visitor.getWordSize();
        isDefault = visitor.isDefault();
    }

    /**
     * Get the memory space definition parse tree element.
     *
     * @return the memory space definition parse tree element.
     */
    @NotNull
    public SleighSpaceDefinition getDefinition() {
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
     * Get the variable definitions containers for the memory space.
     *
     * @return a list of {@link VariablesContainer} instances.
     */
    public List<VariablesContainer> getContainers() {
        return containers;
    }

    /**
     * Get a map of all defined variables in the memory space.
     *
     * @return a map of {@code Variable} instances indexed by their name.
     */
    public Map<String, Variable> getVariables() {
        return variables;
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
