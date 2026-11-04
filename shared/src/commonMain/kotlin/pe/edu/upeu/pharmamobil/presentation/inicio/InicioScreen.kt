package pe.edu.upeu.pharmamobil.presentation.inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.pharmamobil.navigation.Screen
import pe.edu.upeu.pharmamobil.presentation.sesion.SesionViewModel

@Composable
fun InicioScreen(
    onNavegar: (Screen) -> Unit,
    modifier: Modifier = Modifier,
    sesionViewModel: SesionViewModel = koinViewModel()
) {
    val sesionState by sesionViewModel.estado.collectAsStateWithLifecycle()
    val sesion = sesionState.sesion

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Portada()

        Text(
            text = "Qué puedes hacer",
            style = MaterialTheme.typography.titleMedium
        )

        // Módulo Productos: visible si el rol tiene permiso CATALOGO_LEER
        if (sesion?.puede("CATALOGO_LEER") == true) {
            val puedeEscribir = sesion.puede("CATALOGO_ESCRIBIR")
            AccesoRapido(
                icono = Icons.Default.Medication,
                titulo = if (puedeEscribir) "Registrar productos" else "Ver catálogo de productos",
                descripcion = if (puedeEscribir) "Da de alta medicamentos con su precio y su stock." else "Consulta la lista de productos disponibles.",
                onClick = { onNavegar(Screen.Productos) }
            )
        }

        // Módulo Clientes: visible únicamente si el rol tiene permiso CLIENTES_LEER
        if (sesion?.puede("CLIENTES_LEER") == true) {
            AccesoRapido(
                icono = Icons.Default.Person,
                titulo = "Registrar clientes",
                descripcion = "Guarda los datos de contacto para la boleta.",
                onClick = { onNavegar(Screen.Clientes) }
            )
        }

        // Módulo Pedidos
        AccesoRapido(
            icono = Icons.Default.ShoppingCart,
            titulo = "Revisar pedidos",
            descripcion = "Disponible en una próxima sesión del curso.",
            onClick = { onNavegar(Screen.Pedidos) }
        )
    }
}

@Composable
private fun Portada() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocalPharmacy,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )

            Text(
                text = "PharmaMobil",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Sistema de gestión farmacéutica para tu cadena de boticas.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun AccesoRapido(
    icono: ImageVector,
    titulo: String,
    descripcion: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(22.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall
                )

                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}