package org.polyfrost.oneconfig.internal.ui.hud

import androidx.compose.runtime.Composable
import java.util.Locale
import org.polyfrost.compose.composables.PolyBox
import org.polyfrost.compose.composables.PolyMcText
import org.polyfrost.compose.composables.padding
import org.polyfrost.compose.layout.PolyInsets
import org.polyfrost.compose.render.PolyColor
import org.polyfrost.oneconfig.api.hud.v1.Hud
import org.polyfrost.oneconfig.internal.ui.zordyx.ZordyxAimAssist

/**
 * In-world AimAssist feedback.
 *
 * The HUD only draws while AimAssist is enabled and has a live target. It reports the target's name,
 * distance and angular error, with an arrow showing which way the crosshair is from the target. It is a
 * readout: it never draws an aim line and never changes the camera.
 */
class AimAssistHud : Hud("aim_assist_hud", "AimAssist", Category.COMBAT) {
    init {
    }

    override fun update(): Boolean = true

    override fun shouldShow(): Boolean =
        ZordyxAimAssist.enabled && ZordyxAimAssist.analysis?.target != null

    override fun showByDefault(): Boolean = true

    override fun defaultPosition(): Pair<Float, Float> = 8f to 8f

    @Composable
    override fun Content() {
        val candidate = ZordyxAimAssist.analysis?.target ?: return
        val target = candidate.target
        // Yaw increases clockwise in Minecraft, so a positive delta means the target is to the right.
        val direction = when {
            candidate.yawDelta > 0.75f -> "→"
            candidate.yawDelta < -0.75f -> "←"
            else -> "•"
        }
        val line = String.format(
            Locale.ROOT,
            "AimAssist %s %s  %.1fm  %.1f\u00B0",
            direction,
            target.name,
            candidate.distance,
            candidate.angle,
        )

        PolyBox(modifier = hudBackground().padding(PolyInsets(4f, 3f, 4f, 3f))) {
            PolyMcText(
                text = line,
                color = PolyColor(textColor, textChroma, textChromaSpeed),
                shadow = showShadow,
                scale = textScale,
            )
        }
    }
}
