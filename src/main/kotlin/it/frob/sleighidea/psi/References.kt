// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.psi

import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.io.OSAgnosticPathUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import it.frob.sleighidea.isBuiltInSymbol
import it.frob.sleighidea.isStandardLibraryCall

private fun findFirstAssignmentInLocalScopeForVariable(element: PsiElement): PsiElement? {
    (PsiTreeUtil.findFirstParent(element, true) { psiElement ->
        psiElement is SleighSemanticBody
    } as? SleighSemanticBody)?.let { semanticBody ->
        semanticBody.statementList
            .map { statement -> statement.firstChild }
            .filterIsInstance<SleighAssignment>()
            .map { assignment -> assignment.lvalue }
            .find { lvalue ->
                return when {
                    lvalue.identifier?.text == element.text -> lvalue.identifier
                    lvalue.sizedstar?.identifier?.text == element.text -> lvalue.sizedstar
                    else -> null
                }
            }
    }

    return null
}

private fun findFirstDeclarationInLocalScopeForVariable(element: PsiElement): PsiElement? {
    (PsiTreeUtil.findFirstParent(element, true) { psiElement ->
        psiElement is SleighSemanticBody
    } as? SleighSemanticBody)?.let { semanticBody ->
        semanticBody.statementList
            .map { statement -> statement.firstChild }
            .filterIsInstance<SleighDeclaration>()
            .map { declaration -> declaration.identifier }
            .find { identifier -> identifier.text == element.text }
    }

    return null
}

private fun findVariableDeclaration(element: PsiElement): PsiElement? =
    (element.containingFile as SleighFile).variableDefinitions.map { definitions -> definitions.symbolOrWildcardList }
        .flatten()
        .mapNotNull { symbol -> symbol.symbol }
        .firstOrNull { symbol -> symbol.value == element.text }

private fun findFirstTableEntry(element: PsiElement): PsiElement? =
    (element.containingFile as SleighFile).constructorStarts.firstOrNull { constructor -> constructor.identifier?.text == element.text }

private fun findTokenField(element: PsiElement): PsiElement? =
    (element.containingFile as SleighFile).tokens.map { token -> token.tokenFieldDefinitionList }
        .flatten()
        .firstOrNull { field -> field.symbol.value == element.text }

/**
 * PSI reference class for function calls (macros and pcodeops).
 *
 * @param element the PSI element that contains a yet-to-be resolved function call reference.
 */
class FunctionCallReference(element: PsiElement) :
    PsiReferenceBase<PsiElement?>(element, TextRange(0, element.textLength)) {

    override fun resolve(): PsiElement? {
        val file = element.containingFile as SleighFile
        val callValue = (element as SleighExpressionApplyName).symbol.value
        return file.macros.firstOrNull { macro -> macro.symbol.value == callValue }
            ?: file.pcodeops.firstOrNull { pcodeop -> pcodeop.symbol.value == callValue }
    }
}

/**
 * PSI reference class for file inclusion statements.
 *
 * @param element the PSI element that contains a yet-to-be resolved file reference.
 */
class IncludeReference(element: PsiElement) :
    PsiReferenceBase<PsiElement>(element, (element as SleighInclude).quotedString.textRangeInParent) {

    override fun resolve(): PsiElement? = (element as SleighInclude).fileName?.let { targetFile ->
        return (if (OSAgnosticPathUtil.isAbsolute(targetFile)) LocalFileSystem.getInstance()
            .findFileByPath(targetFile) else VfsUtil.findRelativeFile(
            targetFile,
            element.containingFile.virtualFile.parent
        ))?.let { virtualFile ->
            PsiManager.getInstance(element.project).findFile(virtualFile)
        }
    } ?: run { null }
}

// TODO: Turn this into a multi-element reference (tables can have more than one entry after all).

class VariableReference(element: PsiElement, textRange: TextRange) :
    PsiReferenceBase<PsiElement?>(element, textRange) {
    override fun resolve(): PsiElement? =
        findVariableDeclaration(element) ?: findFirstTableEntry(element) ?: findTokenField(element)
        ?: findFirstAssignmentInLocalScopeForVariable(element) ?: findFirstDeclarationInLocalScopeForVariable(element)
}

class ExternalDefinitionReference(element: PsiElement, textRange: TextRange) :
    PsiReferenceBase<PsiElement?>(element, textRange) {
    override fun resolve(): PsiElement? = (element.containingFile as SleighFile).defines.find { definition ->
        definition.defineName.text == (element as? SleighExternalDefinition)?.symbolString?.text
    }
}

class VariableReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> =
        arrayOf(VariableReference(element, element.textRangeInParent))

    override fun acceptsHints(element: PsiElement, hints: PsiReferenceService.Hints): Boolean =
        !isBuiltInSymbol((element as SleighExpressionApplyName).symbol.value)
}

class ExternalDefinitionReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> =
        arrayOf(ExternalDefinitionReference(element, element.textRangeInParent))
}

class SleighReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(SleighExpressionApplyName::class.java)
                .inFile(PlatformPatterns.psiFile(SleighFile::class.java)), object : PsiReferenceProvider() {

                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> = arrayOf(FunctionCallReference(element))

                override fun acceptsHints(element: PsiElement, hints: PsiReferenceService.Hints): Boolean =
                    // Skip built-in calls.
                    !isStandardLibraryCall((element as SleighExpressionApplyName).symbol.value)
            }
        )

        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(SleighVariableNode::class.java)
                .withChild(PlatformPatterns.psiElement(SleighSymbol::class.java))
                .inFile(PlatformPatterns.psiFile(SleighFile::class.java)), VariableReferenceProvider()
        )

        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(SleighLvalue::class.java)
                .withParent(SleighAssignment::class.java)
                .inFile(PlatformPatterns.psiFile(SleighFile::class.java)), VariableReferenceProvider()
        )

        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(SleighExternalDefinition::class.java)
                .inFile(PlatformPatterns.psiFile(SleighFile::class.java)), ExternalDefinitionReferenceProvider()
        )

        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(SleighInclude::class.java)
                .inFile(PlatformPatterns.psiFile(SleighFile::class.java)), object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> = arrayOf(IncludeReference(element))
            }
        )
    }
}
