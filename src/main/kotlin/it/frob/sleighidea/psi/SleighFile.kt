// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiTreeUtil
import it.frob.sleighidea.*
import java.util.*

/**
 * Concrete implementation of [PsiFile] for Sleigh files.
 *
 * @param viewProvider the access class for the file's PSI elements.
 */
class SleighFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, SleighLanguage.INSTANCE), PsiFile,
    PsiNameIdentifierOwner {

    /**
     * All the `token` elements in the file, wrapped in a cache-aware container.
     */
    private val _tokens = createCachedValue(
        object : ValueProvider<List<SleighTokenDefinition>>() {
            override fun computeValue(): List<SleighTokenDefinition> = simpleCollector()
        }
    )
    val tokens: List<SleighTokenDefinition>
        get() = _tokens.value

    /**
     * All the `macro` elements in the file, wrapped in a cache-aware container.
     */
    private val _macros = createCachedValue(
        object : ValueProvider<List<SleighMacroDefinition>>() {
            override fun computeValue(): List<SleighMacroDefinition> = simpleCollector()
        }
    )
    val macros: List<SleighMacroDefinition>
        get() = _macros.value

    /**
     * All the `space` elements in the file, wrapped in a cache-aware linear container.
     */
    private val _spaces = createCachedValue(
        object : ValueProvider<List<SleighSpaceDefinition>>() {
            override fun computeValue(): List<SleighSpaceDefinition> = simpleCollector()
        }
    )
    val spaces: List<SleighSpaceDefinition>
        get() = _spaces.value

    /**
     * All the `include` elements in the file, wrapped in a cache-aware container.
     */
    private val _includes = createCachedValue(
        object : ValueProvider<List<SleighInclude>>() {
            override fun computeValue(): List<SleighInclude> = simpleCollector()
        }
    )
    val includes: List<SleighInclude>
        get() = _includes.value

    /**
     * All the variables node definition elements in the file, wrapped in a cache-aware container.
     */
    private val _variableDefinitions = createCachedValue(
        object : ValueProvider<List<SleighVariablesNodeDefinition>>() {
            override fun computeValue(): List<SleighVariablesNodeDefinition> = collectVariablesNodeDefinitions()
        }
    )
    val variableDefinitions: List<SleighVariablesNodeDefinition>
        get() = _variableDefinitions.value

    /**
     * All the constructor start elements in the file, wrapped in a cache-aware container.
     */
    private val _constructorStarts = createCachedValue(
        object : ValueProvider<List<SleighConstructorStart>>() {
            override fun computeValue(): List<SleighConstructorStart> = simpleCollector()
        }
    )
    val constructorStarts: List<SleighConstructorStart>
        get() = _constructorStarts.value

    /**
     * All the pcodeop elements in the file, wrapped in a cache-aware container.
     */
    private val _pcodeops = createCachedValue(
        object : ValueProvider<List<SleighPcodeopDefinition>>() {
            override fun computeValue(): List<SleighPcodeopDefinition> = simpleCollector()
        }
    )
    val pcodeops: List<SleighPcodeopDefinition>
        get() = _pcodeops.value

    /**
     * All the @define elements in the file, wrapped in a cache-aware container.
     */
    private val _defines = createCachedValue(
        object : ValueProvider<List<SleighDefine>>() {
            override fun computeValue(): List<SleighDefine> = simpleCollector()
        }
    )
    val defines: List<SleighDefine>
        get() = _defines.value

    override fun getFileType(): FileType = SleighFileType.INSTANCE

    override fun toString(): String = "Sleigh File"

    override fun getNameIdentifier(): PsiElement? = null

    /**
     * Create a cached value.
     *
     * @param provider the cache-aware value provider to get data from.
     * @param <T>      the class of the value to cache.
     * @return a [CachedValue] accessor instance.
    </T> */
    private fun <T> createCachedValue(provider: CachedValueProvider<T>): CachedValue<T> =
        CachedValuesManager.getManager(project).createCachedValue(provider, false)

    /**
     * Cached value provider base class.
     *
     * @param <T> the class of the value that will be provided.
    </T> */
    private abstract inner class ValueProvider<T> : CachedValueProvider<T> {
        override fun compute(): CachedValueProvider.Result<T> =
            CachedValueProvider.Result.create(computeValue(), this@SleighFile)

        /**
         * Compute the contained value.
         *
         * @return the current value to cache in the provider accessor methods.
         */
        protected abstract fun computeValue(): T?
    }

    /**
     * Extract all elements of the given [PsiElement]-derived type in the file.
     *
     * @param T the type of the elements to collect.
     * @return a list containing the [PsiElement]-derived instances found in the file.
     */
    private inline fun <reified T : PsiElement> simpleCollector(): List<T> =
        ArrayList(PsiTreeUtil.collectElementsOfType(this, T::class.java))

    /**
     * Extract all variables node definition elements in the file.
     *
     * @return a list containing the [SleighVariablesNodeDefinition] instances found in the file.
     */
    private fun collectVariablesNodeDefinitions(): List<SleighVariablesNodeDefinition> =
        simpleCollector<SleighDefinition>()
            .map { child: SleighDefinition? ->
                PsiTreeUtil.getChildrenOfTypeAsList(child, SleighVariablesNodeDefinition::class.java)
            }
            .flatten()
            .toList()
}