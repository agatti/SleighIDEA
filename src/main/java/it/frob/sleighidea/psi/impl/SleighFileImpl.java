// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.psi.impl;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import it.frob.sleighidea.SleighFileType;
import it.frob.sleighidea.SleighLanguage;
import it.frob.sleighidea.model.ModelException;
import it.frob.sleighidea.model.Space;
import it.frob.sleighidea.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Concrete implementation of {@link SleighFile}.
 */
public class SleighFileImpl extends PsiFileBase implements SleighFile, PsiNameIdentifierOwner {

    /**
     * All the {@code token} elements in the file, wrapped in a cache-aware container.
     */
    private final CachedValue<List<SleighTokendef>> tokens = createCachedValue(
            new ValueProvider<List<SleighTokendef>>() {
                @Override
                protected @NotNull List<SleighTokendef> computeValue() {
                    return Collections.unmodifiableList(collectTokens());
                }
            }
    );

    /**
     * All the {@code macro} elements in the file, wrapped in a cache-aware container.
     */
    private final CachedValue<List<SleighMacrodef>> macros = createCachedValue(
            new ValueProvider<List<SleighMacrodef>>() {
                @Override
                protected @NotNull List<SleighMacrodef> computeValue() {
                    return Collections.unmodifiableList(collectMacros());
                }
            }
    );

    /**
     * All the {@code space} elements in the file, wrapped in a cache-aware container.
     */
    private final CachedValue<Map<String, Space>> spaces = createCachedValue(
            new ValueProvider<Map<String, Space>>() {
                @Override
                protected @NotNull Map<String, Space> computeValue() {
                    return Collections.unmodifiableMap(collectSpaces());
                }
            }
    );

    /**
     * Constructor.
     *
     * @param viewProvider the access class for the file's PSI elements.
     */
    public SleighFileImpl(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, SleighLanguage.INSTANCE);
    }

    @Override
    public @NotNull FileType getFileType() {
        return SleighFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Sleigh File";
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return null;
    }

    @Override
    public Collection<SleighTokendef> getTokens() {
        return tokens.getValue();
    }

    @Override
    public Collection<SleighMacrodef> getMacros() {
        return macros.getValue();
    }

    @Override
    public Map<String, Space> getSpaces() {
        return spaces.getValue();
    }

    /**
     * Create a cached value.
     *
     * @param provider the cache-aware value provider to get data from.
     * @param <T>      the class of the value to cache.
     * @return a {@link CachedValue} accessor instance.
     */
    @NotNull
    private <T> CachedValue<T> createCachedValue(@NotNull CachedValueProvider<T> provider) {
        return CachedValuesManager.getManager(getProject()).createCachedValue(provider, false);
    }

    /**
     * Cached value provider base class.
     *
     * @param <T> the class of the value that will be provided.
     */
    private abstract class ValueProvider<T> implements CachedValueProvider<T> {
        @NotNull
        @Override
        public final Result<T> compute() {
            return Result.create(computeValue(), SleighFileImpl.this);
        }

        /**
         * Compute the contained value.
         *
         * @return the current value to cache in the provider accessor methods.
         */
        @Nullable
        protected abstract T computeValue();
    }

    /**
     * Extract all {@code token} elements in the file.
     *
     * @return a list containing the {@link SleighTokendef} instances found in the file.
     */
    @NotNull
    private List<SleighTokendef> collectTokens() {
        return new ArrayList<>(PsiTreeUtil.collectElementsOfType(this, SleighTokendef.class));
    }

    /**
     * Extract all {@code macro} elements in the file.
     *
     * @return a list containing the {@link SleighMacrodef} instances found in the file.
     */
    @NotNull
    private List<SleighMacrodef> collectMacros() {
        return new ArrayList<>(PsiTreeUtil.collectElementsOfType(this, SleighMacrodef.class));
    }

    /**
     * Extract all {@code space} elements in the file.
     *
     * @return a map containing the {@link SleighSpacedef} instances found in the file, wrapped in their respective
     * {@link Space} model container classes, indexed by their name.
     */
    @NotNull
    private Map<String, Space> collectSpaces() {
        Map<String, Space> collectedSpaces = new HashMap<>();

        // TODO: Check for multiple default spaces.

        PsiTreeUtil.collectElementsOfType(this, SleighSpacedef.class)
                .stream()
                .map(space -> {
                    try {
                        return new Space(space);
                    } catch (ModelException ignored) {
                        // TODO: Figure out how to handle this case.
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .forEach(space -> {
                    if (collectedSpaces.containsKey(space.getName())) {
                        // TODO: Figure out how to handle duplicate spaces.
                        return;
                    }
                    collectedSpaces.put(space.getName(), space);
                });

        return collectedSpaces;
    }
}
