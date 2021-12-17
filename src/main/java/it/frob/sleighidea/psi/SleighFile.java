// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.psi;

import com.intellij.psi.PsiFile;
import it.frob.sleighidea.model.space.Space;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * Get the {@link Space} instance registered under the given name.
     *
     * @param name the memory space name to look up.
     * @return the matching {@link Space} instance or {@code null} if none is found.
     */
    @Nullable Space getSpaceForName(@NotNull String name);

    /**
     * Get an immutable collection containing all the {@link Space} instances in the file.
     *
     * @return the {@link Space} instances present in the file.
     */
    Collection<Space> getSpaces();

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
