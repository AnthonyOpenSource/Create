package com.simibubi.create.modules.contraptions.relays.gauge;

import java.util.List;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.config.AllConfigs;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.contraptions.GogglesItem;
import com.simibubi.create.modules.contraptions.base.IRotate.SpeedLevel;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;

public class SpeedGaugeTileEntity extends GaugeTileEntity{

	public SpeedGaugeTileEntity() {
		super(AllTileEntities.SPEEDOMETER.type);
	}

	@Override
	public void onSpeedChanged(float prevSpeed) {
		super.onSpeedChanged(prevSpeed);
		float speed = Math.abs(getSpeed());
		float medium = AllConfigs.SERVER.kinetics.mediumSpeed.get().floatValue();
		float fast = AllConfigs.SERVER.kinetics.fastSpeed.get().floatValue();
		float max = AllConfigs.SERVER.kinetics.maxRotationSpeed.get().floatValue();
		color = ColorHelper.mixColors(SpeedLevel.of(speed).getColor(), 0xffffff, .25f);

		if (speed == 69)
			AllTriggers.triggerForNearbyPlayers(AllTriggers.SPEED_READ, world, pos, 6,
					GogglesItem::canSeeParticles);
		if (speed == 0) {
			dialTarget = 0;
			color = 0x333333;
		} else if (speed < medium) {
			dialTarget = MathHelper.lerp(speed / medium, 0, .45f);
		} else if (speed < fast) {
			dialTarget = MathHelper.lerp((speed - medium) / (fast - medium), .45f, .75f);
		} else {
			dialTarget = MathHelper.lerp((speed - fast) / (max - fast), .75f, 1.125f);
		}
		
		markDirty();
	}

	@Override
	public boolean addToGoggleTooltip(List<String> tooltip, boolean isPlayerSneaking) {
		super.addToGoggleTooltip(tooltip, isPlayerSneaking);

		tooltip.add(spacing + TextFormatting.GRAY + Lang.translate("gui.speed_gauge.title"));
		tooltip.add(spacing + SpeedLevel.getFormattedSpeedText(speed, overStressed));
		if (overStressed)
			tooltip.add(spacing + TextFormatting.DARK_RED + Lang.translate("gui.stress_gauge.overstressed"));

		return true;
	}
}
