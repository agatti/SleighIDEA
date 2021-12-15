// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.model.space;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.util.PsiTreeUtil;
import it.frob.sleighidea.psi.*;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Tree visitor for collecting {@code space} parameters.
 */
public class SpaceVisitor extends SleighVisitor {

    /**
     * The collected memory space name.
     */
    private String name;

    /**
     * The collected memory space type.
     */
    private String type;

    /**
     * The collected memory space size.
     */
    private Integer size;

    /**
     * The collected memory space word size.
     */
    private Integer wordSize;

    /**
     * Flag indicating if the memory space acts as the default or not.
     */
    private boolean isDefault;

    /**
     * Flag indicating whether the {@code space} parsing succeeded or not.
     */
    private boolean isValid = true;

    @Override
    public void visitIdentifier(@NotNull SleighIdentifier visited) {
        this.name = visited.getText().trim();
    }

    @Override
    public void visitSpaceTypeModifier(@NotNull SleighSpaceTypeModifier visited) {
        SleighSpaceType typeNode = PsiTreeUtil.findChildOfType(visited, SleighSpaceType.class);
        assert typeNode != null;

        if (type != null) {
            isValid = false;
            // TODO: Mark the node with an error - multiple types found.
            return;
        }

        type = typeNode.getText();
    }

    @Override
    public void visitSpaceSizeModifier(@NotNull SleighSpaceSizeModifier visited) {
        SleighInteger sizeNode = PsiTreeUtil.findChildOfType(visited, SleighInteger.class);
        if (sizeNode == null) {
            // No integer is attached if the value was invalid to begin with.
            return;
        }

        if (size != null) {
            isValid = false;
            // TODO: Mark the node with an error - multiple sizes found.
            return;
        }

        size = sizeNode.toInteger();
    }

    @Override
    public void visitSpaceWordsizeModifier(@NotNull SleighSpaceWordsizeModifier visited) {
        SleighInteger wordSizeNode = PsiTreeUtil.findChildOfType(visited, SleighInteger.class);
        if (wordSizeNode == null) {
            // No integer is attached if the value was invalid to begin with.
            return;
        }

        if (wordSize != null) {
            isValid = false;
            // TODO: Mark the node with an error - multiple word sizes found.
            return;
        }

        wordSize = wordSizeNode.toInteger();
    }

    @Override
    public void visitSpaceModifier(@NotNull SleighSpaceModifier visited) {
        SleighSpaceTypeModifier typeModifier = visited.getSpaceTypeModifier();
        if (typeModifier != null) {
            typeModifier.accept(this);
            return;
        }

        SleighSpaceSizeModifier sizeModifier = visited.getSpaceSizeModifier();
        if (sizeModifier != null) {
            sizeModifier.accept(this);
            return;
        }

        SleighSpaceWordsizeModifier wordSizeModifier = visited.getSpaceWordsizeModifier();
        if (wordSizeModifier != null) {
            wordSizeModifier.accept(this);
            return;
        }

        if ("default".equals(visited.getText())) {
            isDefault = true;
        }
    }

    /**
     * Get the extracted memory space name.
     *
     * @return the memory space name.
     */
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * Get the extracted memory space type.
     *
     * @return the memory space type, or {@code null} if no type was found.
     */
    @Nullable
    public String getType() {
        return type;
    }

    /**
     * Get the extracted memory space size.
     *
     * @return the memory space size.
     */
    @Nullable
    public Integer getSize() {
        return size;
    }

    /**
     * Get the extracted memory space word size.
     *
     * @return the memory space word size.
     */
    public int getWordSize() {
        return wordSize == null ? 1 : wordSize;
    }

    /**
     * Get the extracted memory space default flag.
     *
     * @return the memory space default flag.
     */
    public boolean isDefault() {
        return isDefault;
    }

    /**
     * Get whether the memory space extraction succeeded.
     *
     * @return {@code true} if the operation succeeded, {@code false} otherwise.
     */
    public boolean isValid() {
        return isValid && (size != null) && StringUtil.isNotEmpty(name);
    }
}
