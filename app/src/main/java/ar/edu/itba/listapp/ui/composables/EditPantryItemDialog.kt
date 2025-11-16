package ar.edu.itba.listapp.ui.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ar.edu.itba.listapp.R

@Composable
fun EditPantryItemDialog(
    emoji: String,
    name: String,
    currentQuantity: Double,
    currentUnit: String?,
    onDismiss: () -> Unit,
    onConfirm: (Double, String?) -> Unit
) {
    var quantity by remember { mutableStateOf(currentQuantity.toString()) }
    var unit by remember { mutableStateOf(currentUnit ?: "") }
    var quantityError by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = {
            // Only dismiss if there's no error being shown
            if (quantityError == null) {
                onDismiss()
            }
        }
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFEFEF)),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "Edit Item",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Product info (read-only)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 24.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Text(
                            text = name,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quantity input
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(id = R.string.quantity))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = {
                            quantity = it
                            quantityError = null
                        },
                        placeholder = { Text("1.0") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        isError = quantityError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (quantityError != null) Color.Red else Color.Gray,
                            unfocusedBorderColor = if (quantityError != null) Color.Red else Color.LightGray
                        )
                    )

                    // Error message
                    if (quantityError != null) {
                        Text(
                            text = quantityError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Unit input
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(id = R.string.unit_optional))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        placeholder = { Text(stringResource(id = R.string.unit_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Gray,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(id = R.string.cancel))
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            // Validate quantity first
                            if (quantity.isBlank()) {
                                quantityError = "Please enter a quantity"
                                return@Button
                            }

                            val qty = quantity.toDoubleOrNull()
                            if (qty == null || qty <= 0) {
                                quantityError = "Please enter a valid quantity"
                                return@Button
                            }

                            // Clear error and proceed
                            quantityError = null
                            val unitValue = unit.trim().takeIf { it.isNotEmpty() }
                            onConfirm(qty, unitValue)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(id = R.string.save))
                    }
                }
            }
        }
    }
}

