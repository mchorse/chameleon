package mchorse.chameleon.geckolib;

import software.bernie.geckolib3.file.AnimationFile;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;

import java.util.ArrayList;
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

	public List<String> getBoneNames()
	{
		return this.getBoneNames(new ArrayList<String>(), this.model.topLevelBones);
	}

	public List<String> getBoneNames(List<String> boneNames, List<GeoBone> bones)
	{
		for (GeoBone bone : bones)
		{
			boneNames.add(bone.name);

			this.getBoneNames(boneNames, bone.childBones);
		}

		return boneNames;
	}
}
