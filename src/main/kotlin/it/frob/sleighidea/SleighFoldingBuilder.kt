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

    override fun buildFoldRegions(
        root: PsiElement, document: Document,
        quick: Boolean
    ): Array<FoldingDescriptor> {
        val descriptors: MutableList<FoldingDescriptor> = mutableListOf()
        root.acceptChildren(object : SleighVisitor() {
            override fun visitDefinition(visited: SleighDefinition) {
                PsiTreeUtil.findChildOfType(visited, SleighTokenDefinition::class.java)?.let { container ->
                    PsiTreeUtil.findChildOfType(container, SleighIdentifier::class.java)?.let {
                        descriptors.add(
                            FoldingDescriptor(
                                visited.node,
                                visited.textRange,
                                FoldingGroup.newGroup(SLEIGH_PREFIX_STRING),
                                container.placeholderText
                            )
                        )
                    }
                }
            }

            override fun visitConstructorLike(visited: SleighConstructorLike) {
                visited.acceptChildren(object : SleighVisitor() {
                    override fun visitMacrodef(visited: SleighMacrodef) {
                        descriptors.add(
                            FoldingDescriptor(
                                visited.parent.node,
                                visited.parent.textRange,
                                FoldingGroup.newGroup(SLEIGH_PREFIX_STRING),
                                visited.placeholderText!!
                            )
                        )
                    }

                    override fun visitConstructor(visited: SleighConstructor) {
                        val container = PsiTreeUtil.findChildOfType(visited, SleighConstructorStart::class.java) ?: return
                        if (container.firstChild is SleighDisplay) {
                            descriptors.add(
                                FoldingDescriptor(
                                    visited.parent.node,
                                    visited.parent.textRange,
                                    FoldingGroup.newGroup(SLEIGH_PREFIX_STRING),
                                    (container.firstChild as SleighDisplay).placeholderText
                                )
                            )
                        }
                    }
                })
            }
        })
        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String? = null

    override fun isCollapsedByDefault(node: ASTNode): Boolean = false
}