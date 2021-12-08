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
     * Get an immutable collection containing all the valid {@code token} definition tokens.
     *
     * @return the {@link SleighTokendef} instances present in the file.
     */
    Collection<SleighTokendef> getTokens();
}
