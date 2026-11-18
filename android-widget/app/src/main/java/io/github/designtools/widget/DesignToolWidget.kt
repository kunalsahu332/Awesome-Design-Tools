package io.github.designtools.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import java.time.LocalDate
import kotlin.random.Random

private val ToolIndexKey = intPreferencesKey("tool_index")
private val DayKey = longPreferencesKey("epoch_day")

class DesignToolWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Responsive(setOf(SMALL, MEDIUM, LARGE))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val tools = ToolRepository.tools(context)

        // Roll over to a new "tool of the day" when the date changes.
        val today = LocalDate.now()
        val state = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
        if (state[DayKey] != today.toEpochDay() || state[ToolIndexKey] == null) {
            updateAppWidgetState(context, id) {
                it[DayKey] = today.toEpochDay()
                it[ToolIndexKey] = ToolRepository.indexForDay(today, tools.size)
            }
        }

        provideContent {
            val index = currentState<Preferences>()[ToolIndexKey] ?: 0
            GlanceTheme {
                ToolCard(tools[index.coerceIn(tools.indices)])
            }
        }
    }

    companion object {
        val SMALL = DpSize(110.dp, 50.dp)
        val MEDIUM = DpSize(180.dp, 110.dp)
        val LARGE = DpSize(260.dp, 180.dp)
    }
}

@Composable
private fun ToolCard(tool: DesignTool) {
    val context = LocalContext.current
    val size = LocalSize.current
    val compact = size.height < DesignToolWidget.MEDIUM.height || size.width < DesignToolWidget.MEDIUM.width
    val large = size.height >= DesignToolWidget.LARGE.height && size.width >= DesignToolWidget.LARGE.width
    val openIntent = Intent(Intent.ACTION_VIEW, Uri.parse(tool.url))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(24.dp)
            .padding(horizontal = 16.dp, vertical = if (compact) 8.dp else 14.dp)
            .clickable(actionStartActivity(openIntent))
            .semantics { contentDescription = context.getString(R.string.open_tool, tool.name) },
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                if (!compact) {
                    Text(
                        text = tool.category.uppercase(),
                        maxLines = 1,
                        style = TextStyle(
                            color = GlanceTheme.colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                }
                Text(
                    text = tool.name,
                    maxLines = if (compact) 1 else 2,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = if (large) 24.sp else if (compact) 16.sp else 20.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
            CircleIconButton(
                imageProvider = ImageProvider(R.drawable.ic_shuffle),
                contentDescription = context.getString(R.string.shuffle),
                onClick = actionRunCallback<ShuffleAction>(),
                backgroundColor = GlanceTheme.colors.secondaryContainer,
                contentColor = GlanceTheme.colors.onSecondaryContainer,
            )
        }

        if (!compact && tool.description.isNotBlank()) {
            Spacer(GlanceModifier.height(6.dp))
            Text(
                text = tool.description.replaceFirstChar { it.uppercase() },
                maxLines = if (large) 6 else 3,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = if (large) 15.sp else 13.sp,
                ),
            )
        }
    }
}

/** Picks a different random tool for this widget instance. */
class ShuffleAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val size = ToolRepository.tools(context).size
        updateAppWidgetState(context, glanceId) { prefs ->
            val current = prefs[ToolIndexKey] ?: -1
            var next = Random.nextInt(size)
            if (size > 1) while (next == current) next = Random.nextInt(size)
            prefs[ToolIndexKey] = next
        }
        DesignToolWidget().update(context, glanceId)
    }
}

class DesignToolWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DesignToolWidget()
}
