package app.dto

import kotlinx.serialization.Serializable

@Serializable
data class MatchEditorAdditionRequest(
    val editorEmail: String,
)