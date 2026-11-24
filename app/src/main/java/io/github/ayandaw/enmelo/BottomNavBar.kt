package io.github.ayandaw.enmelo

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.ayandaw.enmelo.ui.theme.EnmeloTheme
import kotlin.math.roundToInt

data class NavTab(val label: String, val icon: Int)

@Composable
fun BottomNavBar(tabs: List<NavTab>, selected: Int, onSelect: (Int) -> Unit) {
    if (tabs.isEmpty()) return
    val lefts = remember(tabs.size) { mutableStateMapOf<Int, Float>() }
    val widths = remember(tabs.size) { mutableStateMapOf<Int, Float>() }




    Surface(
        modifier = Modifier
            .padding(vertical = 5.dp)
            .height(50.dp)
            .dropShadow(
                RoundedCornerShape(28.dp),
                Shadow(
                    radius = 15.dp,
                    spread = 0.dp,
                    color = Color.Black.copy(alpha = 0.30f),
                    offset = DpOffset.Zero
                )
            ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(Modifier.fillMaxHeight().padding(horizontal = 5.dp, vertical = 5.dp)) {
            val shadeLeft = lefts[selected]
            val shadeWidth = widths[selected]
            if (shadeLeft != null && shadeWidth != null) {
                val shadeX by animateFloatAsState(
                    targetValue = shadeLeft,
                    animationSpec = tween(300, easing = EaseInOut),
                    label = "shadeX"
                )
                val shadeW by animateFloatAsState(
                    targetValue = shadeWidth,
                    animationSpec = tween(300, easing = EaseInOut),
                    label = "shadeW"
                )
                Box(
                    Modifier
                        .offset { IntOffset(shadeX.roundToInt(), 0) }
                        .width(with(LocalDensity.current) { shadeW.toDp() })
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(28.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
                )
            }
            Row(Modifier.fillMaxHeight()) {
                tabs.forEachIndexed { i, (label, icon) ->
                    val tint = if (i == selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                    Column(
                        Modifier
                            .onGloballyPositioned {
                                lefts[i] = it.positionInParent().x
                                widths[i] = it.size.width.toFloat()
                            }
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(28.dp))
                            .tapNoRipple { onSelect(i) }
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(painterResource(icon), contentDescription = label, tint = tint)
                        Text(label, style = MaterialTheme.typography.labelSmall, color = tint)
                    }
                }
            }
        }
    }
}

private val previewTabs = listOf(
    NavTab("Home", R.drawable.ic_home),
    NavTab("Library", R.drawable.ic_library),
    NavTab("Search", R.drawable.ic_search)
)

@Preview(showBackground = true, widthDp = 400)
@Composable
fun BottomNavBarPreview() {
    var selected by remember { mutableStateOf(1) }
    EnmeloTheme { BottomNavBar(previewTabs, selected) { selected = it } }
}