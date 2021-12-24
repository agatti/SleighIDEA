package it.frob.sleighidea.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import it.frob.sleighidea.psi.*

abstract class SleighTokenDefinitionMixin(node: ASTNode) : SleighNamedElementImpl(node), SleighTokenDefinition {
    override fun setName(name: String): PsiElement {
        nameIdentifier?.replace(SleighElementFactory.createSleighSymbol(project, name))
        return this
    }

    override fun getName(): String? {
        return symbol.value
    }

    override fun getNameIdentifier(): PsiElement? = symbol

    override fun getIdentifyingElement(): PsiElement? = symbol

    override fun getTextOffset(): Int = symbol.textOffset
}

abstract class SleighTokenFieldDefinitionMixin(node: ASTNode) : SleighNamedElementImpl(node),
    SleighTokenFieldDefinition {
    override fun setName(name: String): PsiElement {
        nameIdentifier?.replace(SleighElementFactory.createSleighSymbol(project, name))
        return this
    }

    override fun getName(): String? {
        return symbol.value
    }

    override fun getNameIdentifier(): PsiElement? = symbol

    override fun getIdentifyingElement(): PsiElement? = symbol

    override fun getTextOffset(): Int = symbol.textOffset
}

abstract class SleighSpaceDefinitionMixin(node: ASTNode) : SleighNamedElementImpl(node), SleighSpaceDefinition {
    override fun setName(name: String): PsiElement {
        nameIdentifier?.replace(SleighElementFactory.createSleighSymbol(project, name))
        return this
    }

    override fun getName(): String? {
        return symbol.value
    }

    override fun getNameIdentifier(): PsiElement? = symbol

    override fun getIdentifyingElement(): PsiElement? = symbol

    override fun getTextOffset(): Int = symbol.textOffset
}

abstract class SleighPcodeDefinitionMixin(node: ASTNode) : SleighNamedElementImpl(node), SleighPcodeopDefinition {
    override fun setName(name: String): PsiElement {
        nameIdentifier?.replace(SleighElementFactory.createSleighSymbol(project, name))
        return this
    }

    override fun getName(): String? {
        return symbol.value
    }

    override fun getNameIdentifier(): PsiElement? = symbol

    override fun getIdentifyingElement(): PsiElement? = symbol

    override fun getTextOffset(): Int = symbol.textOffset
}
