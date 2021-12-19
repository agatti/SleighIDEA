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
import it.frob.sleighidea.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Concrete implementation of {@link SleighFile}.
 */
public class SleighFileImpl extends PsiFileBase implements SleighFile, PsiNameIdentifierOwner {

    /**
     * All the {@code token} elements in the file, wrapped in a cache-aware container.
     */
    private final CachedValue<List<SleighTokenDefinition>> tokens = createCachedValue(
            new ValueProvider<>() {
                @Override
                protected @NotNull List<SleighTokenDefinition> computeValue() {
                    return Collections.unmodifiableList(collectTokens());
                }
            }
    );

    /**
     * All the {@code macro} elements in the file, wrapped in a cache-aware container.
     */
    private final CachedValue<List<SleighMacroDefinition>> macros = createCachedValue(
            new ValueProvider<>() {
                @Override
                protected @NotNull List<SleighMacroDefinition> computeValue() {
                    return Collections.unmodifiableList(collectMacros());
                }
            }
    );

    /**
     * All the {@code space} elements in the file, wrapped in a cache-aware linear container.
     */
    private final CachedValue<List<SleighSpaceDefinition>> spaces = createCachedValue(
            new ValueProvider<>() {
                @Override
                protected @NotNull List<SleighSpaceDefinition> computeValue() {
                    return Collections.unmodifiableList(collectSpaces());
                }
            }
    );

    /**
     * All the {@code include} elements in the file, wrapped in a cache-aware container.
     */
    private final CachedValue<List<SleighInclude>> includes = createCachedValue(
            new ValueProvider<>() {
                @Override
                protected @NotNull List<SleighInclude> computeValue() {
                    return Collections.unmodifiableList(collectIncludes());
                }
            }
    );

    /**
     * All the variables node definition elements in the file, wrapped in a cache-aware container.
     */
    private final CachedValue<List<SleighVariablesNodeDefinition>> variableDefinitions = createCachedValue(
            new ValueProvider<>() {
                @Override
                protected @NotNull List<SleighVariablesNodeDefinition> computeValue() {
                    return Collections.unmodifiableList(collectVariablesNodeDefinitions());
                }
            }
    );

    /**
     * All the constructor start elements in the file, wrapped in a cache-aware container.
     */
    private final CachedValue<List<SleighConstructorStart>> constructorStarts = createCachedValue(
            new ValueProvider<>() {
                @Override
                protected @NotNull List<SleighConstructorStart> computeValue() {
                    return Collections.unmodifiableList(collectConstructorStarts());
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
    public Collection<SleighTokenDefinition> getTokens() {
        return tokens.getValue();
    }

    @Override
    public Collection<SleighMacroDefinition> getMacros() {
        return macros.getValue();
    }

    @Override
    public Collection<SleighSpaceDefinition> getSpaces() {
        return spaces.getValue();
    }

    @Override
    public Collection<SleighInclude> getIncludes() {
        return includes.getValue();
    }

    @Override
    public Collection<SleighVariablesNodeDefinition> getVariablesNodeDefinitions() {
        return variableDefinitions.getValue();
    }

    @Override
    public Collection<SleighConstructorStart> getConstructorStarts() {
        return constructorStarts.getValue();
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
     * @return a list containing the {@link SleighTokenDefinition} instances found in the file.
     */
    @NotNull
    private List<SleighTokenDefinition> collectTokens() {
        return new ArrayList<>(PsiTreeUtil.collectElementsOfType(this, SleighTokenDefinition.class));
    }

    /**
     * Extract all {@code macro} elements in the file.
     *
     * @return a list containing the {@link SleighMacroDefinition} instances found in the file.
     */
    @NotNull
    private List<SleighMacroDefinition> collectMacros() {
        return new ArrayList<>(PsiTreeUtil.collectElementsOfType(this, SleighMacroDefinition.class));
    }

    /**
     * Extract all {@code space} elements in the file.
     *
     * @return a list containing the {@link SleighSpaceDefinition} instances found in the file.
     */
    @NotNull
    private List<SleighSpaceDefinition> collectSpaces() {
        return new ArrayList<>(PsiTreeUtil.collectElementsOfType(this, SleighSpaceDefinition.class));
    }

    /**
     * Extract all {@code include} elements in the file.
     *
     * @return a list containing the {@link SleighInclude} instances found in the file.
     */
    @NotNull
    private List<SleighInclude> collectIncludes() {
        return new ArrayList<>(PsiTreeUtil.collectElementsOfType(this, SleighInclude.class));
    }

    /**
     * Extract all variables node definition elements in the file.
     *
     * @return a list containing the {@link SleighVariablesNodeDefinition} instances found in the file.
     */
    @NotNull
    private List<SleighVariablesNodeDefinition> collectVariablesNodeDefinitions() {
        return PsiTreeUtil.collectElementsOfType(this, SleighDefinition.class).stream()
                .map(child -> PsiTreeUtil.getChildrenOfTypeAsList(child, SleighVariablesNodeDefinition.class))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Extract all constructor start elements in the file.
     *
     * @return a list containing the {@link SleighConstructorStart} instances found in the file.
     */
    @NotNull
    private List<SleighConstructorStart> collectConstructorStarts() {
        return new ArrayList<>(PsiTreeUtil.collectElementsOfType(this, SleighConstructorStart.class));
    }
}
