package com.nan.nearbystorefinder.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun CategorySection(
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf(
        Triple("FOOD", Icons.Default.Fastfood, "FOOD"),
        Triple("GROCERY", Icons.Default.LocalGroceryStore, "GROCERY"),
        Triple("COFFEE", Icons.Default.Coffee, "COFFEE"),
        Triple("PHARMACY", Icons.Default.MedicalServices, "PHARMACY"),
        Triple("MALL", Icons.Default.ShoppingBag, "MALL"),
        Triple("CLOTHING", Icons.Default.Checkroom, "CLOTHING")
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories) { (label, icon, category) ->
            CategoryChip(
                label = label,
                icon = icon,
                isSelected = selectedCategory == category
            ) {
                onCategorySelected(category)
            }
        }
    }
}

@Composable
fun CategoryChip(label: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFF8A7CFF) else Color(0xFF1A1A1E),
        modifier = Modifier
            .height(45.dp)
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
