// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea

import com.intellij.lang.Commenter

class SleighCommenter : Commenter {

    override fun getLineCommentPrefix(): String = "#"

    override fun getBlockCommentPrefix(): String? = null

    override fun getBlockCommentSuffix(): String? = null

    override fun getCommentedBlockCommentPrefix(): String? = null

    override fun getCommentedBlockCommentSuffix(): String? = null
}