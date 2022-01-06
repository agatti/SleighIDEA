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
import it.frob.sleighidea.Endianness
import it.frob.sleighidea.psi.*
import java.awt.*
import java.awt.event.*
import javax.swing.*
import javax.swing.border.LineBorder
import kotlin.math.roundToInt

private const val MARGIN = 5

/**
 * Abstract implementation of [MouseListener], providing default empty methods.
 */
open class AbstractMouseListener : MouseListener {
    override fun mouseClicked(event: MouseEvent?) = Unit

    override fun mousePressed(event: MouseEvent?) = Unit

    override fun mouseReleased(event: MouseEvent?) = Unit

    override fun mouseEntered(event: MouseEvent?) = Unit

    override fun mouseExited(event: MouseEvent?) = Unit
}

internal interface TokenClickListener {
    fun tokenClicked(token: SleighTokenDefinition)

    fun fieldClicked(field: SleighTokenFieldDefinition)
}

/**
 * A label which will display tick marks across the horizontal border.
 *
 * @param text the text to display in the label.
 * @param segments how many segments the label is made up of.
 */
private open class TickLabel(text: String, private val segments: Int) : JBLabel(text, SwingConstants.CENTER) {
    override fun paintComponent(g: Graphics?) {
        (g as? Graphics2D)?.let { graphics ->
            graphics.color = JBUI.CurrentTheme.Label.foreground()
            graphics.drawRect(0, 0, width, height)
            val segmentWidth = width / segments

            (1 until segments).forEach { index ->
                graphics.drawLine(segmentWidth * index, 0, segmentWidth * index, height / 6)
                graphics.drawLine(segmentWidth * index, (height / 6) * 5, segmentWidth * index, height)
            }
        }

        super.paintComponent(g)
    }
}

private fun buildLabel(text: String): JBLabel = JBLabel(text, SwingConstants.CENTER).apply {
    border = LineBorder(JBUI.CurrentTheme.Label.foreground(), 1)
    verticalAlignment = SwingConstants.CENTER
    cursor = Cursor(Cursor.HAND_CURSOR)
}

private fun buildTokenTableTitle(token: SleighTokenDefinition): JBLabel {
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
    )
    label.font = JBFont.h4().asBold()

    return label
}

class TokenTableHeader(private val bits: Int) : JComponent() {
    init {
        // TODO: Update the whole lot when the label font changes.
        font = JBFont.label()
    }

    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)

        (g as? Graphics2D)?.let { graphics ->
            val cellWidth = width.toFloat() / bits.toFloat()
            graphics.color = JBUI.CurrentTheme.Label.foreground()
            graphics.drawRect(0, 0, width, height)
            (bits downTo 0).forEach { index ->
                val bitIndex = index - 1
                graphics.drawLine((cellWidth * index).roundToInt(), 0, (cellWidth * index).roundToInt(), height)
                // TODO: Cache string bounds if the font stays the same.
                val stringBounds = font.getStringBounds(bitIndex.toString(), graphics.fontRenderContext)
                graphics.drawString(
                    bitIndex.toString(),
                    ((cellWidth * (bits - index)) + ((cellWidth - stringBounds.width) / 2)).toInt(),
                    (stringBounds.height + graphics.fontMetrics.descent).toInt()
                )
            }
        }
    }

    override fun preferredSize(): Dimension = Dimension(0, getFontMetrics(font).height + (MARGIN * 2))
}

private class TokenFieldComponent(private val field: SleighTokenFieldDefinition, clickListener: TokenClickListener?) :
    JPanel() {

    private var label: TickLabel
    private var parentWidth: Int

    init {
        // TODO: Update the whole lot when the label font changes.
        font = JBFont.label()

        @Suppress("HtmlRequiredLangAttribute")
        // language=HTML
        label = TickLabel(
            "<html><a href=''><nobr>${field.symbol.value}<sub>${field.baseString}</sub></nobr></a></html>",
            field.bitWidth!!
        ).apply {
            cursor = Cursor(Cursor.HAND_CURSOR)
            addMouseListener(object : AbstractMouseListener() {
                override fun mouseClicked(event: MouseEvent?) {
                    clickListener?.fieldClicked(field)
                }
            })
        }
        add(label)
        parentWidth = (field.parent as SleighTokenDefinition).size!!
    }

    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)

        (g as? Graphics2D)?.let {
            val cellWidth = width.toFloat() / parentWidth.toFloat()

            val labelHeight = height
            val labelWidth = (field.bitWidth!! * cellWidth).roundToInt()
            label.size = Dimension(labelWidth, labelHeight)
            label.location = Point((cellWidth * (parentWidth - field.bitEnd.toInteger()!! - 1)).roundToInt(), 0)
        }
    }

    override fun preferredSize(): Dimension = Dimension(label.width, getFontMetrics(label.font).height + (MARGIN * 2))
}

private class TokenTable(private val token: SleighTokenDefinition, private val clickListener: TokenClickListener?) :
    JPanel(GridBagLayout()) {

    init {
        val constraints = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
            gridx = GridBagConstraints.REMAINDER
        }

        buildTokenTableTitle(token).run {
            addMouseListener(object : AbstractMouseListener() {
                override fun mouseClicked(event: MouseEvent?) {
                    clickListener?.tokenClicked(token)
                }
            })
            this@TokenTable.add(this, constraints)
        }

        // TODO: Add a notice if the token size cannot be figured out.
        token.size?.let { tokenBits ->
            add(TokenTableHeader(tokenBits), constraints)
            token.tokenFieldDefinitionList.forEach { field ->
                add(TokenFieldComponent(field, clickListener), constraints)
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
        val panel = JPanel(GridBagLayout())
        val constraints = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
            gridx = GridBagConstraints.REMAINDER
            ipady = JBUIScale.scale(INTRA_TABLE_PADDING)
            insets = JBInsets(0, MARGIN, 0, MARGIN)
        }
        scrollPane.viewport.removeAll()
        sleighFile.tokens.map { token -> TokenTable(token, this) }
            .forEach { table -> panel.add(table, constraints) }
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

    override fun fieldClicked(field: SleighTokenFieldDefinition) {
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

    companion object {
        private const val INTRA_TABLE_PADDING = 15
    }
}

class TokenToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, rootToolWindow: ToolWindow) {
        val toolWindow = TokenToolWindow(project, rootToolWindow)
        val contentFactory = ContentFactory.SERVICE.getInstance()
        rootToolWindow.contentManager.addContent(contentFactory.createContent(toolWindow.content, "", true))
    }
}