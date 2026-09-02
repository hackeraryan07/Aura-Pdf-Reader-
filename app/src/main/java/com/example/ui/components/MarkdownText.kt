package com.example.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified
) {
    val annotatedString = parseMarkdown(text, MaterialTheme.colorScheme.primary)
    Text(
        text = annotatedString,
        modifier = modifier,
        style = style,
        color = color
    )
}

fun parseMarkdown(text: String, primaryColor: Color): AnnotatedString {
    return buildAnnotatedString {
        var currentIndex = 0
        val regex = Regex("(\\*\\*.*?\\*\\*|\\*.*?\\*|`.*?`|#+\\s.*?\n|\\[.*?\\]\\(.*?\\)|\\n)")
        
        val matches = regex.findAll(text)
        
        for (match in matches) {
            val start = match.range.first
            val end = match.range.last + 1
            val matchText = match.value

            // Append normal text before the match
            if (start > currentIndex) {
                append(text.substring(currentIndex, start))
            }

            when {
                matchText.startsWith("**") && matchText.endsWith("**") -> {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(matchText.drop(2).dropLast(2))
                    pop()
                }
                matchText.startsWith("*") && matchText.endsWith("*") -> {
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    append(matchText.drop(1).dropLast(1))
                    pop()
                }
                matchText.startsWith("`") && matchText.endsWith("`") -> {
                    pushStyle(SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = primaryColor.copy(alpha = 0.1f)
                    ))
                    append(matchText.drop(1).dropLast(1))
                    pop()
                }
                matchText.startsWith("#") -> {
                    val headerLevel = matchText.takeWhile { it == '#' }.length
                    val fontSize = when (headerLevel) {
                        1 -> 24.sp
                        2 -> 20.sp
                        3 -> 18.sp
                        else -> 16.sp
                    }
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = fontSize))
                    append(matchText.dropWhile { it == '#' }.trimStart())
                    pop()
                }
                matchText.startsWith("[") && matchText.contains("](") && matchText.endsWith(")") -> {
                    val label = matchText.substringAfter("[").substringBefore("]")
                    pushStyle(SpanStyle(color = primaryColor, textDecoration = TextDecoration.Underline))
                    append(label)
                    pop()
                }
                matchText == "\n" -> {
                    append("\n")
                }
                else -> {
                    append(matchText)
                }
            }
            currentIndex = end
        }
        
        // Append remaining text
        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }
}
