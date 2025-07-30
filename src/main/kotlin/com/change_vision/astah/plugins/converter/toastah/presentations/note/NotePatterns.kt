package com.change_vision.astah.plugins.converter.toastah.presentations.note

/**
 * ノート関連の正規表現パターン
 */
object NotePatterns {
    // クラス定義の検出パターン
    val CLASS_PATTERN = Regex("""^\s*(class|interface|enum|abstract)\s+("?)(\w+)\2""")
    
    // ① note <pos> of <entity> : <body…>
    val NOTE_OF_PATTERN = Regex("""^\s*note\s+(top|bottom|left|right)\s+of\s+("?)([^":]+?)\2\s*:\s*(.+)$""",
        RegexOption.IGNORE_CASE)
    
    // ② note "<body…>" as <id>
    val NOTE_AS_PATTERN = Regex("""^\s*note\s+"(.+?)"\s+as\s+([\p{L}\w.]+)\s*$""",
        RegexOption.IGNORE_CASE)
    
    // ③ note <pos> : <body…>
    val NOTE_INLINE_PATTERN = Regex("""^\s*note\s+(top|bottom|left|right)\s*:\s*(.+)$""",
        RegexOption.IGNORE_CASE)
} 