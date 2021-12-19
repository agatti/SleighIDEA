// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea

import com.intellij.openapi.fileTypes.LanguageFileType
import it.frob.sleighidea.SleighLanguage
import it.frob.sleighidea.SleighFileType
import it.frob.sleighidea.SleighIcons
import javax.swing.Icon

class SleighFileType protected constructor() : LanguageFileType(SleighLanguage.INSTANCE) {

    override fun getName(): String = "Sleigh File"

    override fun getDescription(): String = "Sleigh definition file"

    override fun getDefaultExtension(): String = FILE_EXTENSION

    override fun getIcon(): Icon = SleighIcons.FILE

    companion object {
        const val FILE_EXTENSION = "slaspec"
        @JvmField
        val INSTANCE = SleighFileType()
    }
}