## Version 1.2.1

This update was made by Chryfi, exceptions are mentioned at the respective items.

**Compatible** with McLib `2.4.1`, Metamorph `1.3.1`. It doesn't mean that future versions of McLib or Metamorph would be incompatible, but older versions are most likely incompatible.

* Added local / global translation mode
* Fixed NPE crash in getCurrentPose due to missing model (by MiaoNLI)
* Fixed the problem that chameleon does not support CTRL + LMB to penetrate transparent pixels (by MiaoNLI)

## Version 1.2

This update was made by MiaoNLI. It adds cool features like limb glow, color animation and more.

**Compatible** with McLib `2.4`, Metamorph `1.3`. It doesn't mean that future versions of McLib or Metamorph would be incompatible, but older versions are most likely incompatible.

* Morph
    * Added limb glow and color animation (suggested by MaiZhi)
    * Added Animation action that can be controlled by animation (pauseable)
    * Fixed some NPE bug
    * Optimized animation
* File Encoding
    * UTF-8 file encoding is used by default

## Version 1.1

This is a major internal rewrite whose purpose is to make Chameleon independent from GeckoLib. [GeckoLib](https://www.curseforge.com/minecraft/mc-mods/geckolib) is still an awesome library that I would recommend anyone who is doing animated entities and blocks, however, it doesn't personally satisfy me anymore.

The motivation behind this update is to be able to fix bugs on my end, rather than on GeckoLib's end. 

**Compatible** with McLib `2.3.5`, Metamorph `1.2.7`. It doesn't mean that future versions of McLib or Metamorph would be incompatible, but older versions are most likely incompatible.

* Added support for Bedrock Smooth keyframes (`catmullrom`)
* Added new base code (written from scratch) that replaces GeckoLib's animation and model loading code
* Fixed the crash with missing faces in per-face UV mode
* Removed `geckolib` from dependencies

I tried to replace everything that GeckoLib was offering, however there are a couple of features that are missing: step interpolation, and support for overshoot/bounciness factor for back, elastic and bounce interpolations. Beside that, everything else should be the same.

## Version 1.0.3

This is a small patch has 2 features and bug fixes.

**Compatible** with McLib `2.3`, Metamorph `1.2.7` and GeckoLib `3.0.0`. It doesn't mean that future versions of Metamorph, Aperture and GeckoLib would be incompatible, but older versions are most likely incompatible.

* Added copy/paste context menu to Chameleon's pose editor
* Added support for folders (nested models) in Chameleon's models folder (suggested by The Minebox)
* Fixed Chameleon morphs disappearing when changing animations
* Fixed face alpha culling due to global OpenGL leaking (reported by Moris)

## Version 1.0.2

This is a small patch that is required for new versions of McLib.

**Compatible** with McLib `2.2`, Metamorph `1.2.5` and GeckoLib `3.0.0`. It doesn't mean that future versions of Metamorph, Aperture and GeckoLib would be incompatible, but older versions are most likely incompatible.

* Fixed configuration ID (due to mod ID change in previous patch) being broken
* Fixed multi-part not updating when closing morph editor with texture picker opened
* Fixed animations were not updating on present morphs (reported by Silverx)
* Fixed dedicated server crashing (reported by Nazzy)

## Version 1.0.1

Quick hotfix to fix Chameleon mod to work with another more popular Chameleon mod.

**Compatible** with McLib `2.1.2`, Metamorph `1.2.4` and GeckoLib `3.0.0`. It doesn't mean that future versions of Metamorph, Aperture and GeckoLib would be incompatible, but older versions are most likely incompatible.

* Changed the mod ID from `chameleon` to `chameleon_morph`

## Version 1.0

First version of Chameleon mod. This version provides essential tools to work with Chameleon animated morphs.

**Compatible** with McLib `2.1.2`, Metamorph `1.2.4` and GeckoLib `3.0.0`. It doesn't mean that future versions of Metamorph, Aperture and GeckoLib would be incompatible, but older versions are most likely incompatible.

<a href="https://youtu.be/9vKWH2r6wFI"><img src="https://img.youtube.com/vi/9vKWH2r6wFI/0.jpg"></a> 

* Added loading of `.geo.json` and `.animation.json` Bedrock models and animations (powered by GeckoLib)
* Added `chameleon.*` morph that allows using static and animated Bedrock models and animations
	* Added skin property, which lets to change the skin of the morph
	* Added body part compatibility, which allows to add morphs to limbs
	* Added pose editor, which allows to change the pose of the model and additionaly still bones (or entire skeleton)
	* Added action editor, which lets remapping action state to different action names, additionally changing properties like speed and fade factor
	* Added global scale and GUI scale, which allow to change the scale of the model in the world or in menus
* Added custom resource pack `c.s` which allows referencing textures out of `config/chameleon/models/` folder
* Added config section to McLib's config system that includes utility buttons
* Added Chinese localization (thanks to Chunk7)