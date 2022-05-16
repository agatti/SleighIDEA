// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea

import com.intellij.ide.highlighter.XmlLikeFileType
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.module.Module
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.xml.XmlFile
import com.intellij.xml.XmlSchemaProvider
import java.net.URL
import javax.swing.Icon

open class SleighProcessorSpecificationsFileType protected constructor() : XmlLikeFileType(XMLLanguage.INSTANCE) {

    override fun getName(): String = "Sleigh Processor Specification File"

    override fun getDescription(): String = "Sleigh Processor specification file"

    override fun getDefaultExtension(): String = FILE_EXTENSION

    override fun getIcon(): Icon = SleighIcons.FILE

    companion object {
        const val FILE_EXTENSION = "cspec"

        @JvmField
        val INSTANCE = SleighProcessorSpecificationsFileType()
    }
}

class SleighProcessorSpecificationsSchemaProvider : XmlSchemaProvider() {
    override fun getSchema(url: String, module: Module?, baseFile: PsiFile): XmlFile? {
        val resource: URL =
            SleighLanguageDefinitionsSchemaProvider::class.java.getResource("it/frob/sleighidea/schemas/pspec.xsd")
        VfsUtil.findFileByURL(resource)?.let { file ->
            module?.project?.let { project ->
                return PsiManager.getInstance(project).findFile(file)?.copy() as XmlFile
            }
        }

        return null
    }
}
