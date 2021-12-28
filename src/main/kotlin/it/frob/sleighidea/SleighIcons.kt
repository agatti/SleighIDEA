// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea

import com.intellij.openapi.util.IconLoader

object SleighIcons {
    @JvmField
    val FILE = IconLoader.getIcon("/it/frob/sleighidea/ghidra_icon.png", SleighIcons::class.java)
    val TABLE = IconLoader.getIcon("/it/frob/sleighidea/table.png", SleighIcons::class.java)
    val TABLE_GO = IconLoader.getIcon("/it/frob/sleighidea/table_go.png", SleighIcons::class.java)
    @JvmField
    val TOKEN_VIEWER_ICON = IconLoader.getIcon("/it/frob/sleighidea/token_viewer_icon.png", SleighIcons::class.java)
}