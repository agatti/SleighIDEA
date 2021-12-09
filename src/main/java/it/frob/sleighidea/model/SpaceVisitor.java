// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.model;

import com.intellij.psi.util.PsiTreeUtil;
import it.frob.sleighidea.psi.*;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Optional;

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
    private int size;

    /**
     * The collected memory space word size.
     */
    private int wordSize;

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
    public void visitSpacemods(@NotNull SleighSpacemods visited) {
        SpaceModsVisitor visitor = new SpaceModsVisitor();
        visited.acceptChildren(visitor);

        if (!visitor.isValid) {
            isValid = false;
            return;
        }

        if (!visitor.size.isPresent()) {
            isValid = false;
            // TODO: Mark the node with an error - no size found.
            return;
        }

        type = visitor.type.orElse(null);
        size = visitor.size.get();
        wordSize = visitor.wordSize.orElse(1);
        isDefault = visitor.isDefault;
    }

    /**
     * Get the extracted memory space name.
     *
     * @return the memory space name.
     */
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
    public int getSize() {
        return size;
    }

    /**
     * Get the extracted memory space word size.
     *
     * @return the memory space word size.
     */
    public int getWordSize() {
        return wordSize;
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
        return isValid;
    }

    /**
     * Inner class visitor for parsing {@code space} parameters.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static class SpaceModsVisitor extends SleighVisitor {
        /**
         * The visited memory space type.
         */
        private Optional<String> type = Optional.empty();

        /**
         * The visited memory space size.
         */
        private Optional<Integer> size = Optional.empty();

        /**
         * The visited memory space word size.
         */
        private Optional<Integer> wordSize = Optional.empty();

        /**
         * The visited memory space default flag.
         */
        private boolean isDefault = false;

        /**
         * Flag indicating whether the visit found issues with the element being parsed.
         */
        private boolean isValid = true;

        @Override
        public void visitTypemod(@NotNull SleighTypemod visited) {
            SleighType typeNode = PsiTreeUtil.findChildOfType(visited, SleighType.class);
            assert typeNode != null;

            if (type.isPresent()) {
                isValid = false;
                // TODO: Mark the node with an error - multiple types found.
                return;
            }

            type = Optional.of(typeNode.getText());
        }

        @Override
        public void visitSizemod(@NotNull SleighSizemod visited) {
            SleighInteger sizeNode = PsiTreeUtil.findChildOfType(visited, SleighInteger.class);
            assert sizeNode != null;

            if (size.isPresent()) {
                isValid = false;
                // TODO: Mark the node with an error - multiple sizes found.
                return;
            }

            int value = Integer.parseInt(sizeNode.getText());
            if (value <= 0) {
                isValid = false;
                // TODO: Mark the node with an error - size cannot be zero.
                return;
            }

            size = Optional.of(value);
        }

        @Override
        public void visitWordsizemod(@NotNull SleighWordsizemod visited) {
            SleighInteger wordSizeNode = PsiTreeUtil.findChildOfType(visited, SleighInteger.class);
            assert wordSizeNode != null;

            if (wordSize.isPresent()) {
                isValid = false;
                // TODO: Mark the node with an error - multiple word sizes found.
                return;
            }

            int value = Integer.parseInt(wordSizeNode.getText());
            if (value <= 0) {
                isValid = false;
                // TODO: Mark the node with an error - word size cannot be zero.
                return;
            }

            wordSize = Optional.of(value);
        }

        @Override
        public void visitSpacemod(@NotNull SleighSpacemod visited) {
            SleighTypemod typeModifier = visited.getTypemod();
            if (typeModifier != null) {
                typeModifier.accept(this);
                return;
            }

            SleighSizemod sizeModifier = visited.getSizemod();
            if (sizeModifier != null) {
                sizeModifier.accept(this);
                return;
            }

            SleighWordsizemod wordSizeModifier = visited.getWordsizemod();
            if (wordSizeModifier != null) {
                wordSizeModifier.accept(this);
                return;
            }

            if (SleighTypes.KEY_DEFAULT.toString().equals(visited.getText())) {
                isDefault = true;
            }
        }
    }
}
