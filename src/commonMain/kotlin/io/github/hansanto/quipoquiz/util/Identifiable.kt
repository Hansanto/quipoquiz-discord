package io.github.hansanto.quipoquiz.util

/**
 * Separator for creating id from multiple strings.
 * If the parent id is "parent" and the child id is "child", the combined id will be "parent-child".
 */
private const val ID_SEPARATOR = "-"

/**
 * Create an ID from the parent and child IDs.
 * @param parentId Parent ID.
 * @param childId Child ID.
 * @return Combined ID.
 */
fun createId(parentId: String, childId: String): String {
    return "$parentId$ID_SEPARATOR$childId"
}

interface Identifiable {
    /**
     * Unique identifier.
     */
    val id: String
}
