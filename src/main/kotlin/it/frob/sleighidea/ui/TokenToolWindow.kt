// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.psi.PsiManager
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBInsets
import com.intellij.util.ui.JBUI
import it.frob.sleighidea.model.Endianness
import it.frob.sleighidea.psi.*
import java.awt.*
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.*
import javax.swing.border.LineBorder

private const val MARGIN = 5

open class AbstractMouseListener : MouseListener {
    override fun mouseClicked(event: MouseEvent?) = Unit

    override fun mousePressed(event: MouseEvent?) = Unit

    override fun mouseReleased(event: MouseEvent?) = Unit

    override fun mouseEntered(event: MouseEvent?) = Unit

    override fun mouseExited(event: MouseEvent?) = Unit
}

internal interface TokenClickListener {
    fun tokenClicked(token: SleighTokenDefinition)

    fun fieldClicked(token: SleighTokenDefinition, field: SleighTokenFieldDefinition)
}

private fun buildLabel(text: String, clickable: Boolean = false): JBLabel {
    val label = JBLabel(text, SwingConstants.CENTER)
    label.border = LineBorder(JBUI.CurrentTheme.Label.foreground(), 1)
    label.verticalAlignment = SwingConstants.CENTER
    if (clickable) {
        label.cursor = Cursor(Cursor.HAND_CURSOR)
    }

    return label
}

class TokenTableComponent(private val token: SleighTokenDefinition) : JPanel(GridBagLayout()) {

    private var listener: TokenClickListener? = null

    internal fun setListener(clickListener: TokenClickListener?) {
        listener = clickListener
    }

    init {
        val constraints = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
        }

        constraints.apply {
            (0 until token.size!!).forEach { index ->
                gridx = token.size!! - index
                gridy = 0
                insets = JBInsets(MARGIN, 0, MARGIN, 0)

                this@TokenTableComponent.add(buildLabel(index.toString()), this)
            }
        }

        constraints.apply {
            gridx = 0
            gridy = 1
            gridwidth = token.size!! + 1
            insets = JBInsets(0, 0, MARGIN, 0)

            val endianness = when (endiannessResolver(token.containingFile as SleighFile, token.endian)) {
                Endianness.BIG -> "BE"
                Endianness.LITTLE -> "LE"
                Endianness.EXTERNAL -> "EXT"
                Endianness.DEFAULT -> "?"
                Endianness.UNKNOWN -> "?"
            }

            @Suppress("HtmlRequiredLangAttribute")
            // language=HTML
            val label = buildLabel(
                "<html><a href=''>${token.symbol.value}<sub>$endianness</sub> (${token.size!!})</a></html>",
                clickable = true
            )
            label.font = JBFont.h4().asBold()
            label.addMouseListener(object : AbstractMouseListener() {
                override fun mouseClicked(event: MouseEvent?) {
                    listener?.tokenClicked(token)
                }
            })

            this@TokenTableComponent.add(label, this)
        }

        constraints.insets = JBInsets(0, 0, 0, 0)

        token.tokenFieldDefinitionList.forEachIndexed { index, field ->
            constraints.apply {
                val endBit = field.bitEnd.toInteger() ?: token.size!!
                val startBit = field.bitStart.toInteger() ?: 0

                gridy = index + 2
                gridx = token.size!! - endBit
                gridwidth = endBit - startBit + 1

                @Suppress("HtmlRequiredLangAttribute")
                // language=HTML
                val label = buildLabel(
                    "<html><a href=''>${field.symbol.value}<sub>${field.baseString}</sub> ($startBit, $endBit)</a></html>",
                    clickable = true
                )
                label.addMouseListener(object : AbstractMouseListener() {
                    override fun mouseClicked(event: MouseEvent?) {
                        listener?.fieldClicked(token, field)
                    }
                })

                this@TokenTableComponent.add(label, this)
            }
        }
    }
}

class TokenToolWindow(private val project: Project, rootToolWindow: ToolWindow) : Disposable,
    TokenClickListener {
    val content: JPanel = JPanel(BorderLayout())
    private val scrollPane: JBScrollPane =
        JBScrollPane(JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
    private var currentFile: SleighFile? = null

    private fun refreshWithFile(sleighFile: SleighFile) {
        currentFile = sleighFile
        val panel = JPanel()
        val layout = BoxLayout(panel, BoxLayout.PAGE_AXIS)
        panel.layout = layout

        var lastFiller: Component? = null
        sleighFile.tokens.forEach { token ->
            val table = TokenTableComponent(token)
            table.setListener(this)
            panel.add(table)

            val filler = Box.createVerticalGlue()
            filler.minimumSize.height = JBUIScale.scale(MARGIN)
            panel.add(filler)
            lastFiller = filler
        }
        lastFiller?.let { filler -> panel.remove(filler) }
        scrollPane.viewport.removeAll()
        scrollPane.viewport.add(panel)
    }

    private fun refreshIfNeeded(file: VirtualFile?) {
        file?.let { virtualFile ->
            currentFile = null
            scrollPane.viewport.removeAll()
            if (!virtualFile.isValid) return
            PsiManager.getInstance(project).findFile(virtualFile)?.let { psiFile ->
                if (psiFile is SleighFile) {
                    ApplicationManager.getApplication().invokeLater {
                        this@TokenToolWindow.refreshWithFile(psiFile)
                    }
                }
            }
        }
    }

    override fun dispose() {
        scrollPane.removeAll()
    }

    override fun tokenClicked(token: SleighTokenDefinition) {
        if (currentFile == null) return
        FileEditorManager.getInstance(project).selectedTextEditor?.let { editor ->
            editor.caretModel.moveToOffset(token.textOffset)
            editor.scrollingModel.scrollToCaret(ScrollType.CENTER)
        }
    }

    override fun fieldClicked(token: SleighTokenDefinition, field: SleighTokenFieldDefinition) {
        if (currentFile == null) return
        FileEditorManager.getInstance(project).selectedTextEditor?.let { editor ->
            editor.caretModel.moveToOffset(field.textOffset)
            editor.scrollingModel.scrollToCaret(ScrollType.CENTER)
        }
    }

    init {
        content.add(scrollPane, BorderLayout.CENTER)

        rootToolWindow.activate {
            refreshIfNeeded(FileEditorManager.getInstance(project).selectedEditor?.file)
        }

        project.messageBus.connect(this)
            .subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
                override fun selectionChanged(event: FileEditorManagerEvent) {
                    refreshIfNeeded(event.newFile)
                }
            })
    }
}

class TokenToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, rootToolWindow: ToolWindow) {
        val toolWindow = TokenToolWindow(project, rootToolWindow)
        val contentFactory = ContentFactory.SERVICE.getInstance()
        rootToolWindow.contentManager.addContent(contentFactory.createContent(toolWindow.content, "", true))
    }
}