// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.model.space;

import it.frob.sleighidea.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Memory space variables container class.
 */
public class VariablesContainer {

    /**
     * The {@link SleighVariablesNodeDefinition} element for the container.
     */
    private final SleighVariablesNodeDefinition definition;

    /**
     * A list of {@link Variable} instances that are present in the current container.
     */
    private final List<Variable> variables = new ArrayList<>();

    /**
     * Use the given {@link SleighVariablesNodeDefinition} instance as a data source.
     *
     * @param definition the definition element to extract data from.
     */
    public VariablesContainer(@NotNull SleighVariablesNodeDefinition definition) {
        this.definition = definition;

        variables.addAll(definition.getSymbolOrWildcardList().stream()
                .filter(symbol -> symbol.getSymbol() != null)
                .map(symbol -> new Variable(symbol.getSymbol(), symbol.getSymbol().getValue()))
                .collect(Collectors.toList()));
    }

    /**
     * Get the variables container parse tree element.
     *
     * @return the variables container parse tree element.
     */
    public SleighVariablesNodeDefinition getDefinition() {
        return definition;
    }

    /**
     * Get the variables found in this container.
     *
     * @return a list of {@link Variable} elements that are inside the container.
     */
    public List<Variable> getVariables() {
        return variables;
    }
}
