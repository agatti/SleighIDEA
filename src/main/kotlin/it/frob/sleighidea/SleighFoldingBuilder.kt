// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import it.frob.sleighidea.psi.*
import it.frob.sleighidea.syntax.SLEIGH_PREFIX_STRING

class SleighFoldingBuilder : FoldingBuilderEx(), DumbAware {

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors: MutableList<FoldingDescriptor> = mutableListOf()

        descriptors.addAll(PsiTreeUtil.findChildrenOfType(root, SleighMacroDefinition::class.java).map { element ->
            FoldingDescriptor(
                element.parent.node, element.parent.textRange, FoldingGroup.newGroup(SLEIGH_PREFIX_STRING)
            )
        })

        descriptors.addAll(PsiTreeUtil.findChildrenOfType(root, SleighTokenDefinition::class.java).map { element ->
            FoldingDescriptor(
                element.parent.node, element.parent.textRange, FoldingGroup.newGroup(SLEIGH_PREFIX_STRING)
            )
        })

        descriptors.addAll(PsiTreeUtil.findChildrenOfType(root, SleighConstructor::class.java).filter { element ->
            element.constructorStart.identifier == null
        }.map { element ->
            FoldingDescriptor(
                element.node,
                element.textRange,
                FoldingGroup.newGroup(SLEIGH_PREFIX_STRING),
                element.constructorStart.display.placeholderText
            )
        })

        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String? = null

    override fun isCollapsedByDefault(node: ASTNode): Boolean = false
}