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