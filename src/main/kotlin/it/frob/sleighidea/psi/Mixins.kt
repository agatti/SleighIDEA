// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.psi

import com.intellij.ide.projectView.PresentationData
import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.util.PlatformIcons

abstract class SleighTokenDefinitionMixin(node: ASTNode) : SleighNamedElementImpl(node), SleighTokenDefinition {
    override fun setName(name: String): PsiElement {
        nameIdentifier?.replace(SleighElementFactory.createSleighSymbol(project, name))
        return this
    }

    override fun getName(): String? = symbol.value

    override fun getNameIdentifier(): PsiElement? = symbol

    override fun getIdentifyingElement(): PsiElement? = symbol

    override fun getTextOffset(): Int = symbol.textOffset

    override fun getPresentation(): ItemPresentation = PresentationData(
        "$placeholderText (${size ?: "?"})",
        getContainingFile(this),
        PlatformIcons.CLASS_ICON,
        null
    )
}

abstract class SleighTokenFieldDefinitionMixin(node: ASTNode) : SleighNamedElementImpl(node),
    SleighTokenFieldDefinition {
    override fun setName(name: String): PsiElement {
        nameIdentifier?.replace(SleighElementFactory.createSleighSymbol(project, name))
        return this
    }

    override fun getName(): String? = symbol.value

    override fun getNameIdentifier(): PsiElement? = symbol

    override fun getIdentifyingElement(): PsiElement? = symbol

    override fun getTextOffset(): Int = symbol.textOffset

    override fun getPresentation(): ItemPresentation = PresentationData(
        "${symbol.value} (${bitStart.toInteger() ?: "?"}, ${bitEnd.toInteger() ?: "?"})",
        getContainingFile(this), PlatformIcons.CLASS_ICON,
        null
    )
}

abstract class SleighSpaceDefinitionMixin(node: ASTNode) : SleighNamedElementImpl(node), SleighSpaceDefinition {
    override fun setName(name: String): PsiElement {
        nameIdentifier?.replace(SleighElementFactory.createSleighSymbol(project, name))
        return this
    }

    override fun getName(): String? = symbol.value

    override fun getNameIdentifier(): PsiElement? = symbol

    override fun getIdentifyingElement(): PsiElement? = symbol

    override fun getTextOffset(): Int = symbol.textOffset

    override fun getPresentation(): ItemPresentation =
        PresentationData(placeholderText, getContainingFile(this), PlatformIcons.ANONYMOUS_CLASS_ICON, null)
}

abstract class SleighPcodeopDefinitionMixin(node: ASTNode) : SleighNamedElementImpl(node), SleighPcodeopDefinition {
    override fun setName(name: String): PsiElement {
        nameIdentifier?.replace(SleighElementFactory.createSleighSymbol(project, name))
        return this
    }

    override fun getName(): String? = symbol.value

    override fun getNameIdentifier(): PsiElement? = symbol

    override fun getIdentifyingElement(): PsiElement? = symbol

    override fun getTextOffset(): Int = symbol.textOffset

    override fun getPresentation(): ItemPresentation = PresentationData(
        name,
        getContainingFile(this),
        PlatformIcons.ABSTRACT_CLASS_ICON,
        null
    )
}

abstract class SleighVariablesNodeDefinitionMixin(node: ASTNode) : SleighNamedElementImpl(node),
    SleighVariablesNodeDefinition {
    override fun setName(name: String): PsiElement {
        nameIdentifier?.replace(SleighElementFactory.createSleighSymbol(project, name))
        return this
    }

    override fun getName(): String? = symbol.value

    override fun getNameIdentifier(): PsiElement? = symbol

    override fun getIdentifyingElement(): PsiElement? = symbol

    override fun getTextOffset(): Int = symbol.textOffset
}

abstract class SleighContextDefinitionMixin(node: ASTNode) : SleighNamedElementImpl(node), SleighContextDefinition {
    override fun setName(name: String): PsiElement {
        nameIdentifier?.replace(SleighElementFactory.createSleighSymbol(project, name))
        return this
    }

    override fun getName(): String? = symbol.value

    override fun getNameIdentifier(): PsiElement? = symbol

    override fun getIdentifyingElement(): PsiElement? = symbol

    override fun getTextOffset(): Int = symbol.textOffset
}

abstract class SleighContextFieldDefinitionMixin(node: ASTNode) : SleighNamedElementImpl(node),
    SleighContextFieldDefinition {
    override fun setName(name: String): PsiElement {
        nameIdentifier?.replace(SleighElementFactory.createSleighSymbol(project, name))
        return this
    }

    override fun getName(): String? = symbol.value

    override fun getNameIdentifier(): PsiElement? = symbol

    override fun getIdentifyingElement(): PsiElement? = symbol

    override fun getTextOffset(): Int = symbol.textOffset
}

abstract class SleighBitRangeMixin(node: ASTNode) : SleighNamedElementImpl(node), SleighBitRange {
    override fun setName(name: String): PsiElement {
        nameIdentifier?.replace(SleighElementFactory.createSleighSymbol(project, name))
        return this
    }

    override fun getName(): String? = symbolList.first().value

    override fun getNameIdentifier(): PsiElement? = symbolList.first()

    override fun getIdentifyingElement(): PsiElement? = symbolList.first()

    override fun getTextOffset(): Int = symbolList.first().textOffset
}

abstract class SleighMacroDefinitionMixin(node: ASTNode) : SleighNamedElementImpl(node), SleighMacroDefinition {
    override fun setName(name: String): PsiElement {
        nameIdentifier?.replace(SleighElementFactory.createSleighSymbol(project, name))
        return this
    }

    override fun getName(): String? = symbol.value

    override fun getNameIdentifier(): PsiElement? = symbol

    override fun getIdentifyingElement(): PsiElement? = symbol

    override fun getTextOffset(): Int = symbol.textOffset

    override fun getPresentation(): ItemPresentation = PresentationData(
        placeholderText,
        getContainingFile(this),
        PlatformIcons.FUNCTION_ICON,
        null
    )
}

abstract class SleighDefineMixin(node: ASTNode) : SleighNamedElementImpl(node), SleighDefine {
    override fun setName(name: String): PsiElement {
        nameIdentifier?.replace(SleighElementFactory.createSleighLiteralSymbol(project, name))
        return this
    }

    override fun getName(): String? = defineName.text

    override fun getNameIdentifier(): PsiElement? = defineName

    override fun getIdentifyingElement(): PsiElement? = defineName

    override fun getTextOffset(): Int = defineName.textOffset

    override fun getPresentation(): ItemPresentation = PresentationData(
        "$name \u2192 \"${defineValue.string}\"",
        getContainingFile(this),
        PlatformIcons.ANNOTATION_TYPE_ICON,
        null
    )
}

abstract class SleighExpressionApplyNameMixin(node: ASTNode) : SleighNamedElementImpl(node), SleighExpressionApplyName {
    override fun setName(name: String): PsiElement {
        nameIdentifier?.replace(SleighElementFactory.createSleighSymbol(project, name))
        return this
    }

    override fun getNameIdentifier(): PsiElement? = symbol

    override fun getIdentifyingElement(): PsiElement? = symbol

    override fun getTextOffset(): Int = symbol.textOffset

    override fun getReferences(): Array<PsiReference> = ReferenceProvidersRegistry.getReferencesFromProviders(this)
}

abstract class SleighVariableNodeMixin(node: ASTNode) : SleighNamedElementImpl(node), SleighVariableNode {
    override fun setName(name: String): PsiElement {
        nameIdentifier?.replace(SleighElementFactory.createSleighSymbol(project, name))
        return this
    }

    override fun getNameIdentifier(): PsiElement? = symbol

    override fun getIdentifyingElement(): PsiElement? = symbol

    override fun getTextOffset(): Int = symbol?.textOffset ?: 0

    override fun getReferences(): Array<PsiReference> = ReferenceProvidersRegistry.getReferencesFromProviders(this)
}

abstract class SleighLvalueMixin(node: ASTNode) : SleighNamedElementImpl(node), SleighLvalue {
    override fun setName(name: String): PsiElement {
        nameIdentifier?.replace(SleighElementFactory.createSleighSymbol(project, name))
        return this
    }

    override fun getNameIdentifier(): PsiElement? = identifier

    override fun getIdentifyingElement(): PsiElement? = identifier

    override fun getTextOffset(): Int = identifier?.textOffset ?: 0

    override fun getReferences(): Array<PsiReference> = ReferenceProvidersRegistry.getReferencesFromProviders(this)
}
