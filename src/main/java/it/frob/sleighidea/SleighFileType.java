// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SleighFileType extends LanguageFileType {

    public static final String FILE_EXTENSION = "slaspec";
    public static final SleighFileType INSTANCE = new SleighFileType();

    protected SleighFileType() {
        super(SleighLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Sleigh File";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Sleigh definition file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return FILE_EXTENSION;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return SleighIcons.FILE;
    }
}
