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