// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.psi;

import com.intellij.psi.PsiFile;
import it.frob.sleighidea.model.Space;

import java.util.Collection;
import java.util.Map;

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
     * Get an immutable map containing all the valid {@code space} definition tokens.
     *
     * @return the {@link Space} instances present in the file.
     */
    Map<String, Space> getSpaces();

    /**
     * Get an immutable collection containing all the valid {@code token} definition tokens.
     *
     * @return the {@link SleighTokendef} instances present in the file.
     */
    Collection<SleighTokendef> getTokens();
}
