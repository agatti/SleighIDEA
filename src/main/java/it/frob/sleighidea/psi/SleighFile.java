// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.psi;

import com.intellij.psi.PsiFile;

import java.util.Collection;

/**
 * Public interface for accessing Sleigh files' structured content.
 */
public interface SleighFile extends PsiFile {

    /**
     * Get an immutable collection containing all the valid {@code macro} definition tokens.
     *
     * @return the {@link SleighMacrodef} instances present in the file.
     */
    Collection<SleighMacrodef> getMacros();

    /**
     * Get an immutable collection containing all the {@link SleighSpaceDefinition} instances in the file.
     *
     * @return the {@link SleighSpaceDefinition} instances present in the file.
     */
    Collection<SleighSpaceDefinition> getSpaces();

    /**
     * Get an immutable collection containing all the valid {@code token} definition tokens.
     *
     * @return the {@link SleighTokenDefinition} instances present in the file.
     */
    Collection<SleighTokenDefinition> getTokens();

    /**
     * Get an immutable collection containing all the valid {@code import} definitions.
     *
     * @return the {@link SleighInclude} instances present in the file.
     */
    Collection<SleighInclude> getIncludes();

    /**
     * Get an immutable collection containing all the valid variable definition nodes.
     *
     * @return the {@link SleighVariablesNodeDefinition} instances present in the file.
     */
    Collection<SleighVariablesNodeDefinition> getVariablesNodeDefinitions();
}
