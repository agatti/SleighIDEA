// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.model.space;

import it.frob.sleighidea.model.ModelException;
import it.frob.sleighidea.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Memory space variables container class.
 */
public class VariablesContainer {

    /**
     * The {@link SleighVarnodedef} element for the container.
     */
    private final SleighVarnodedef definition;

    /**
     * A list of {@link Variable} instances that are present in the current container.
     */
    private final List<Variable> variables = new ArrayList<>();

    /**
     * Use the given {@link SleighVarnodedef} instance as a data source.
     *
     * @param definition the definition element to extract data from.
     * @throws ModelException if the extraction process failed.
     */
    public VariablesContainer(@NotNull SleighVarnodedef definition) throws ModelException {
        this.definition = definition;

        int offset = definition.getOffset();
        int size = definition.getSize();
        for (SleighIdOrWild element : definition.getIdentifierlist().getIdOrWildList()) {
            SleighWildcard wildcard = element.getWildcard();
            if (wildcard == null) {
                SleighIdentifier identifier = element.getIdentifier();
                assert identifier != null;
                variables.add(new Variable(identifier, identifier.getText().trim(), size, offset));
            }
            offset += size;
        }
    }

    /**
     * Get the variables container parse tree element.
     *
     * @return the variables container parse tree element.
     */
    public SleighVarnodedef getDefinition() {
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
