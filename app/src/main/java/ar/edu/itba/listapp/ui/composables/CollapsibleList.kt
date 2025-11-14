package ar.edu.itba.listapp.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.edu.itba.listapp.R
import ar.edu.itba.listapp.ui.theme.ListappTheme

@Composable
fun CollapsibleList(
    title: String,
    items: List<Pair<String, String>>,
    onAddItem: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onDeleteList: () -> Unit,
    onEditItem: (Pair<String, String>) -> Unit,
    onDeleteItem: (Pair<String, String>) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    var showEditTitle by remember { mutableStateOf(false) }
    var editableTitle by remember(title) { mutableStateOf(title) }
    val focusManager = LocalFocusManager.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp, horizontal = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFC8DCC5))
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFCFE8B6), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                if (showEditTitle) {
                    OutlinedTextField(
                        value = editableTitle,
                        onValueChange = { editableTitle = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            onTitleChanged(editableTitle)
                            showEditTitle = false
                            focusManager.clearFocus()
                        }),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color(0xFFA3C86D),
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    Text(
                        text = title,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F1F1F),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RoundIconButton(icon = Icons.Default.Add, onClick = onAddItem, backgroundColor = Color(0xFF9BD166))
                    RoundIconButton(
                        icon = Icons.Default.Edit,
                        onClick = { showEditTitle = true },
                        backgroundColor = Color(0xFF9BD166)
                    )
                    RoundIconButton(icon = Icons.Default.Delete, onClick = onDeleteList, backgroundColor = Color(0xFF9BD166))
                    RoundIconButton(
                        icon = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        onClick = { expanded = !expanded },
                        backgroundColor = Color(0xFF9BD166)
                    )
                }
            }
            AnimatedVisibility(visible = expanded && items.isNotEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                    items.forEach { item ->
                        ListItem(
                            item = item,
                            onEdit = { onEditItem(item) },
                            onDelete = { onDeleteItem(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoundIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(40.dp)
            .background(backgroundColor, CircleShape)
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF1F1F1F))
    }
}

@Composable
fun ListItem(item: Pair<String, String>, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBF2EE)),
        border = BorderStroke(2.dp, Color(0xFFC8DCC5))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(text = item.first, fontSize = 24.sp, modifier = Modifier.padding(end = 16.dp))
            Text(
                text = item.second,
                modifier = Modifier.weight(1f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SoftRoundIconButton(icon = Icons.Default.Edit, onClick = onEdit)
                SoftRoundIconButton(icon = Icons.Default.Delete, onClick = onDelete)
            }
        }
    }
}

@Composable
private fun SoftRoundIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(BorderStroke(2.dp, Color(0xFFC8DCC5)), CircleShape)

    ) {
        Icon(icon, contentDescription = null, tint = Color.Black)
    }
}


@Preview(showBackground = true)
@Composable
fun CollapsibleListPreview() {
    ListappTheme {
        var title by remember { mutableStateOf("Frutas") }
        CollapsibleList(
            title = title,
            items = listOf("🍎" to "Manzana", "🍌" to "Banana"),
            onAddItem = {},
            onTitleChanged = { title = it },
            onDeleteList = {},
            onEditItem = {},
            onDeleteItem = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CollapsibleListEmptyPreview() {
    ListappTheme {
         var title by remember { mutableStateOf("Verduras") }
        CollapsibleList(
            title = title,
            items = emptyList(),
            onAddItem = {},
            onTitleChanged = { title = it },
            onDeleteList = {},
            onEditItem = {},
            onDeleteItem = {}
        )
    }
}
