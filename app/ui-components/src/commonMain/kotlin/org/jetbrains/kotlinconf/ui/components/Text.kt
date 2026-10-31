package org.jetbrains.kotlinconf.ui.components

import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import org.jetbrains.kotlinconf.ui.theme.AppTheme
import org.jetbrains.kotlinconf.ui.theme.PreviewHelper
import androidx.compose.ui.tooling.preview.PreviewLightDark

/**
 * A simple text component that uses defaults from [AppTheme],
 * and accepts a simple [color] parameter to set the text color.
 */
@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AppTheme.colors.primaryText,
    style: TextStyle = AppTheme.typography.text1,
    maxLines: Int = Int.MAX_VALUE,
    selectable: Boolean = false,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
) {
    Text(
        text = AnnotatedString(text),
        modifier = modifier,
        color = color,
        style = style,
        maxLines = maxLines,
        selectable = selectable,
        onTextLayout = onTextLayout,
    )
}

@Composable
fun Text(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = AppTheme.colors.primaryText,
    style: TextStyle = AppTheme.typography.text1,
    maxLines: Int = Int.MAX_VALUE,
    selectable: Boolean = false,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
) {
    val content = @Composable {
        BasicText(
            text = text,
            modifier = modifier,
            style = style,
            color = { color },
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            inlineContent = inlineContent,
            onTextLayout = onTextLayout,
        )
    }

    if (selectable) {
        SelectionContainer { content() }
    } else {
        content()
    }
}

private data class TypographyStylePreviewParams(
    val name: String,
    val style: @Composable () -> TextStyle,
)

private class TypographyStylePreviewProvider :
    PreviewParameterProvider<TypographyStylePreviewParams> {
    override val values = sequenceOf(
        TypographyStylePreviewParams("h1") { AppTheme.typography.h1 },
        TypographyStylePreviewParams("h2") { AppTheme.typography.h2 },
        TypographyStylePreviewParams("h3") { AppTheme.typography.h3 },
        TypographyStylePreviewParams("h4") { AppTheme.typography.h4 },
        TypographyStylePreviewParams("text1") { AppTheme.typography.text1 },
        TypographyStylePreviewParams("text2") { AppTheme.typography.text2 },
    )

    override fun getDisplayName(index: Int) = values.elementAt(index).name
}

@PreviewLightDark
@Composable
private fun TextStylePreview(
    @PreviewParameter(TypographyStylePreviewProvider::class) params: TypographyStylePreviewParams,
) = PreviewHelper {
    Text(
        text = "This is a text demo in the kotlinconf-app codebase",
        style = params.style(),
    )
}
