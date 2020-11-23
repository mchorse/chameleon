package mchorse.chameleon.geckolib;

import software.bernie.geckolib3.file.AnimationFile;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;

import java.util.List;

public class ChameleonModel
{
	public GeoModel model;
	public AnimationFile animation;
	public long lastUpdate;

	public ChameleonModel(GeoModel model, AnimationFile file, long lastUpdate)
	{
		this.model = model;
		this.animation = file;
		this.lastUpdate = lastUpdate;

		/* This is VERY IMPORTANT, if initial bone snapshots won't be saved,
		 * there will be rotation inconsistencies! */
		this.saveInitialBoneSnapshots(model.topLevelBones);
	}

	private void saveInitialBoneSnapshots(List<GeoBone> bones)
	{
		for (GeoBone bone : bones)
		{
			bone.saveInitialSnapshot();

			this.saveInitialBoneSnapshots(bone.childBones);
		}
	}
}
