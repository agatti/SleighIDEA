// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import it.frob.sleighidea.SleighFileType;
import it.frob.sleighidea.SleighLanguage;
import org.jetbrains.annotations.NotNull;

public class SleighFile extends PsiFileBase {

    public SleighFile(@NotNull FileViewProvider viewProvider) {
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
}
