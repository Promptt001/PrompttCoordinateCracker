package io.github.promptt001.coordinatecracker.data;

import java.util.StringJoiner;

/**
 * Coordinate-randomized block profiles supported by the 1.21.11 GUI workflow.
 *
 * A profile is a visible face interpretation, not just a Minecraft block id.
 * Several vanilla blocks have four coordinate-selected model entries, but those
 * entries collapse to fewer readable states on a particular face. Sand-style
 * side faces, for example, are stored as one-state profiles because y-rotating a
 * cube-like model usually does not produce a readable wall rotation.
 */
public enum EnumBlockType {

	DEEPSLATE(0, "Deepslate", "deepslate", "DSP", 4, 2, "deepslate.png", 1, "deepslate_side", "deepslate_axis_y"),
	DEEPSLATE_TOP_BOTTOM(13, "Deepslate", "deepslate_top", "DSPT", 4, 4, "deepslate_top.png", 2 | 4, "deepslate_bottom", "deepslate_end", "deepslate_top_bottom", "deepslate_axis_y_top"),
	INFESTED_DEEPSLATE(20, "Infested Deepslate", "infested_deepslate", "IDSP", 4, 2, "deepslate.png", 1, "infested_deepslate_side", "infested_deepslate_axis_y"),
	INFESTED_DEEPSLATE_TOP_BOTTOM(21, "Infested Deepslate", "infested_deepslate_top", "IDSPT", 4, 4, "deepslate_top.png", 2 | 4, "infested_deepslate_bottom", "infested_deepslate_end", "infested_deepslate_top_bottom", "infested_deepslate_axis_y_top"),
	STONE(1, "Stone", "stone", "STN", 4, 2, "stone.png", 1, "stone_side"),
	STONE_TOP_BOTTOM(22, "Stone", "stone_top", "STNT", 4, 4, "stone.png", 2 | 4, "stone_bottom", "stone_top_bottom"),
	INFESTED_STONE(23, "Infested Stone", "infested_stone", "ISTN", 4, 2, "stone.png", 1, "infested_stone_side"),
	INFESTED_STONE_TOP_BOTTOM(24, "Infested Stone", "infested_stone_top", "ISTNT", 4, 4, "stone.png", 2 | 4, "infested_stone_bottom", "infested_stone_top_bottom"),
	SCULK(11, "Sculk", "sculk", "SCLK", 4, 2, "sculk.png", 1, "sculk_side"),
	SCULK_TOP_BOTTOM(25, "Sculk", "sculk_top", "SCLKT", 4, 4, "sculk.png", 2 | 4, "sculk_bottom", "sculk_top_bottom"),
	BEDROCK(12, "Bedrock", "bedrock", "BDR", 4, 2, "bedrock.png", 1, "bedrock_side"),
	BEDROCK_TOP_BOTTOM(26, "Bedrock", "bedrock_top", "BDRT", 4, 4, "bedrock.png", 2 | 4, "bedrock_bottom", "bedrock_top_bottom"),
	DIRT(2, "Dirt", "dirt", "DRT", 4, 1, "dirt.png", 1, "dirt_side"),
	DIRT_TOP_BOTTOM(32, "Dirt", "dirt_top", "DRTT", 4, 4, "dirt.png", 2 | 4, "dirt_bottom", "dirt_top_bottom"),
	SAND(5, "Sand", "sand", "SND", 4, 1, "sand.png", 1, "sand_side"),
	SAND_TOP_BOTTOM(35, "Sand", "sand_top", "SNDT", 4, 4, "sand.png", 2 | 4, "sand_bottom", "sand_top_bottom"),
	RED_SAND(6, "Red Sand", "red_sand", "RSND", 4, 1, "red_sand.png", 1, "red_sand_side"),
	RED_SAND_TOP_BOTTOM(36, "Red Sand", "red_sand_top", "RSNDT", 4, 4, "red_sand.png", 2 | 4, "red_sand_bottom", "red_sand_top_bottom"),
	ROOTED_DIRT(7, "Rooted Dirt", "rooted_dirt", "RDT", 4, 1, "rooted_dirt.png", 1, "rooted_dirt_side"),
	ROOTED_DIRT_TOP_BOTTOM(37, "Rooted Dirt", "rooted_dirt_top", "RDTT", 4, 4, "rooted_dirt.png", 2 | 4, "rooted_dirt_bottom", "rooted_dirt_top_bottom"),
	GRASS_BLOCK(8, "Grass Block", "grass_block", "GRS", 4, 1, "grass_block_side.png", 1, "grass_block_side"),
	GRASS_BLOCK_TOP(48, "Grass Block", "grass_block_top", "GRST", 4, 4, "grass_block_top.png", 2),
	GRASS_BLOCK_BOTTOM(68, "Grass Block", "grass_block_bottom", "GRSB", 4, 4, "dirt.png", 4),
	PODZOL(9, "Podzol", "podzol", "PDZ", 4, 1, "podzol_side.png", 1, "podzol_side"),
	PODZOL_TOP(49, "Podzol", "podzol_top", "PDZT", 4, 4, "podzol_top.png", 2),
	PODZOL_BOTTOM(69, "Podzol", "podzol_bottom", "PDZB", 4, 4, "dirt.png", 4),
	MYCELIUM(27, "Mycelium", "mycelium", "MYC", 4, 1, "mycelium_side.png", 1, "mycelium_side"),
	MYCELIUM_TOP(67, "Mycelium", "mycelium_top", "MYCT", 4, 4, "mycelium_top.png", 2),
	MYCELIUM_BOTTOM(87, "Mycelium", "mycelium_bottom", "MYCB", 4, 4, "dirt.png", 4),
	DIRT_PATH(10, "Dirt Path", "dirt_path", "PATH", 4, 1, "dirt_path_side.png", 1, "dirt_path_side"),
	DIRT_PATH_TOP(50, "Dirt Path", "dirt_path_top", "PATHT", 4, 4, "dirt_path_top.png", 2),
	DIRT_PATH_BOTTOM(70, "Dirt Path", "dirt_path_bottom", "PATHB", 4, 4, "dirt.png", 4),
	WHITE_CONCRETE_POWDER(100, "White Concrete Powder", "white_concrete_powder", "WCP", 4, 1, "white_concrete_powder.png", 1, "white_concrete_powder_side"),
	WHITE_CONCRETE_POWDER_TOP_BOTTOM(101, "White Concrete Powder", "white_concrete_powder_top", "WCPT", 4, 4, "white_concrete_powder.png", 2 | 4, "white_concrete_powder_bottom", "white_concrete_powder_top_bottom"),
	ORANGE_CONCRETE_POWDER(102, "Orange Concrete Powder", "orange_concrete_powder", "OCP", 4, 1, "orange_concrete_powder.png", 1, "orange_concrete_powder_side"),
	ORANGE_CONCRETE_POWDER_TOP_BOTTOM(103, "Orange Concrete Powder", "orange_concrete_powder_top", "OCPT", 4, 4, "orange_concrete_powder.png", 2 | 4, "orange_concrete_powder_bottom", "orange_concrete_powder_top_bottom"),
	MAGENTA_CONCRETE_POWDER(104, "Magenta Concrete Powder", "magenta_concrete_powder", "MCP", 4, 1, "magenta_concrete_powder.png", 1, "magenta_concrete_powder_side"),
	MAGENTA_CONCRETE_POWDER_TOP_BOTTOM(105, "Magenta Concrete Powder", "magenta_concrete_powder_top", "MCPT", 4, 4, "magenta_concrete_powder.png", 2 | 4, "magenta_concrete_powder_bottom", "magenta_concrete_powder_top_bottom"),
	LIGHT_BLUE_CONCRETE_POWDER(106, "Light Blue Concrete Powder", "light_blue_concrete_powder", "LBCP", 4, 1, "light_blue_concrete_powder.png", 1, "light_blue_concrete_powder_side"),
	LIGHT_BLUE_CONCRETE_POWDER_TOP_BOTTOM(107, "Light Blue Concrete Powder", "light_blue_concrete_powder_top", "LBCPT", 4, 4, "light_blue_concrete_powder.png", 2 | 4, "light_blue_concrete_powder_bottom", "light_blue_concrete_powder_top_bottom"),
	YELLOW_CONCRETE_POWDER(108, "Yellow Concrete Powder", "yellow_concrete_powder", "YCP", 4, 1, "yellow_concrete_powder.png", 1, "yellow_concrete_powder_side"),
	YELLOW_CONCRETE_POWDER_TOP_BOTTOM(109, "Yellow Concrete Powder", "yellow_concrete_powder_top", "YCPT", 4, 4, "yellow_concrete_powder.png", 2 | 4, "yellow_concrete_powder_bottom", "yellow_concrete_powder_top_bottom"),
	LIME_CONCRETE_POWDER(110, "Lime Concrete Powder", "lime_concrete_powder", "LCP", 4, 1, "lime_concrete_powder.png", 1, "lime_concrete_powder_side"),
	LIME_CONCRETE_POWDER_TOP_BOTTOM(111, "Lime Concrete Powder", "lime_concrete_powder_top", "LCPT", 4, 4, "lime_concrete_powder.png", 2 | 4, "lime_concrete_powder_bottom", "lime_concrete_powder_top_bottom"),
	PINK_CONCRETE_POWDER(112, "Pink Concrete Powder", "pink_concrete_powder", "PCP", 4, 1, "pink_concrete_powder.png", 1, "pink_concrete_powder_side"),
	PINK_CONCRETE_POWDER_TOP_BOTTOM(113, "Pink Concrete Powder", "pink_concrete_powder_top", "PCPT", 4, 4, "pink_concrete_powder.png", 2 | 4, "pink_concrete_powder_bottom", "pink_concrete_powder_top_bottom"),
	GRAY_CONCRETE_POWDER(114, "Gray Concrete Powder", "gray_concrete_powder", "GCP", 4, 1, "gray_concrete_powder.png", 1, "gray_concrete_powder_side"),
	GRAY_CONCRETE_POWDER_TOP_BOTTOM(115, "Gray Concrete Powder", "gray_concrete_powder_top", "GCPT", 4, 4, "gray_concrete_powder.png", 2 | 4, "gray_concrete_powder_bottom", "gray_concrete_powder_top_bottom"),
	LIGHT_GRAY_CONCRETE_POWDER(116, "Light Gray Concrete Powder", "light_gray_concrete_powder", "LGCP", 4, 1, "light_gray_concrete_powder.png", 1, "light_gray_concrete_powder_side"),
	LIGHT_GRAY_CONCRETE_POWDER_TOP_BOTTOM(117, "Light Gray Concrete Powder", "light_gray_concrete_powder_top", "LGCPT", 4, 4, "light_gray_concrete_powder.png", 2 | 4, "light_gray_concrete_powder_bottom", "light_gray_concrete_powder_top_bottom"),
	CYAN_CONCRETE_POWDER(118, "Cyan Concrete Powder", "cyan_concrete_powder", "CCP", 4, 1, "cyan_concrete_powder.png", 1, "cyan_concrete_powder_side"),
	CYAN_CONCRETE_POWDER_TOP_BOTTOM(119, "Cyan Concrete Powder", "cyan_concrete_powder_top", "CCPT", 4, 4, "cyan_concrete_powder.png", 2 | 4, "cyan_concrete_powder_bottom", "cyan_concrete_powder_top_bottom"),
	PURPLE_CONCRETE_POWDER(120, "Purple Concrete Powder", "purple_concrete_powder", "PCP2", 4, 1, "purple_concrete_powder.png", 1, "purple_concrete_powder_side"),
	PURPLE_CONCRETE_POWDER_TOP_BOTTOM(121, "Purple Concrete Powder", "purple_concrete_powder_top", "PCP2T", 4, 4, "purple_concrete_powder.png", 2 | 4, "purple_concrete_powder_bottom", "purple_concrete_powder_top_bottom"),
	BLUE_CONCRETE_POWDER(122, "Blue Concrete Powder", "blue_concrete_powder", "BCP", 4, 1, "blue_concrete_powder.png", 1, "blue_concrete_powder_side"),
	BLUE_CONCRETE_POWDER_TOP_BOTTOM(123, "Blue Concrete Powder", "blue_concrete_powder_top", "BCPT", 4, 4, "blue_concrete_powder.png", 2 | 4, "blue_concrete_powder_bottom", "blue_concrete_powder_top_bottom"),
	BROWN_CONCRETE_POWDER(124, "Brown Concrete Powder", "brown_concrete_powder", "BRCP", 4, 1, "brown_concrete_powder.png", 1, "brown_concrete_powder_side"),
	BROWN_CONCRETE_POWDER_TOP_BOTTOM(125, "Brown Concrete Powder", "brown_concrete_powder_top", "BRCPT", 4, 4, "brown_concrete_powder.png", 2 | 4, "brown_concrete_powder_bottom", "brown_concrete_powder_top_bottom"),
	GREEN_CONCRETE_POWDER(126, "Green Concrete Powder", "green_concrete_powder", "GRCP", 4, 1, "green_concrete_powder.png", 1, "green_concrete_powder_side"),
	GREEN_CONCRETE_POWDER_TOP_BOTTOM(127, "Green Concrete Powder", "green_concrete_powder_top", "GRCPT", 4, 4, "green_concrete_powder.png", 2 | 4, "green_concrete_powder_bottom", "green_concrete_powder_top_bottom"),
	RED_CONCRETE_POWDER(128, "Red Concrete Powder", "red_concrete_powder", "RCP", 4, 1, "red_concrete_powder.png", 1, "red_concrete_powder_side"),
	RED_CONCRETE_POWDER_TOP_BOTTOM(129, "Red Concrete Powder", "red_concrete_powder_top", "RCPT", 4, 4, "red_concrete_powder.png", 2 | 4, "red_concrete_powder_bottom", "red_concrete_powder_top_bottom"),
	BLACK_CONCRETE_POWDER(130, "Black Concrete Powder", "black_concrete_powder", "BKCP", 4, 1, "black_concrete_powder.png", 1, "black_concrete_powder_side"),
	BLACK_CONCRETE_POWDER_TOP_BOTTOM(131, "Black Concrete Powder", "black_concrete_powder_top", "BKCPT", 4, 4, "black_concrete_powder.png", 2 | 4, "black_concrete_powder_bottom", "black_concrete_powder_top_bottom");

	public static final int SURFACE_WALL = 1;
	public static final int SURFACE_FLOOR = 2;
	public static final int SURFACE_CEILING = 4;
	public static final int SURFACE_ANY = SURFACE_WALL | SURFACE_FLOOR | SURFACE_CEILING;

	private final int id;
	private final String displayName;
	private final String fileToken;
	private final String abbreviation;
	private final int modelVariantCount;
	private final int guiStateCount;
	private final String textureFileName;
	private final int surfaceMask;
	private final String[] aliases;

	EnumBlockType(int id, String displayName, String fileToken, String abbreviation, int modelVariantCount, int guiStateCount, String textureFileName, int surfaceMask, String... aliases) {
		this.id = id;
		this.displayName = displayName;
		this.fileToken = fileToken;
		this.abbreviation = abbreviation;
		this.modelVariantCount = modelVariantCount;
		this.guiStateCount = guiStateCount;
		this.textureFileName = textureFileName;
		this.surfaceMask = surfaceMask;
		this.aliases = aliases == null ? new String[0] : aliases;
	}

	public int getId() {
		return this.id;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public String getFileToken() {
		return this.fileToken;
	}

	public String getAbbreviation() {
		return this.abbreviation;
	}

	/** Raw vanilla model-pool entries sampled from the coordinate rendering seed. */
	public int getModelVariantCount() {
		return this.modelVariantCount;
	}

	/** Visible GUI states that should be entered for this block profile. */
	public int getGuiStateCount() {
		return this.guiStateCount;
	}

	/** Backward-compatible accessor retained for older code paths. */
	public int getVariantCount() {
		return this.modelVariantCount;
	}

	public String getTextureFileName() {
		return this.textureFileName;
	}

	public int getSurfaceMask() {
		return this.surfaceMask;
	}

	public boolean isCompatibleWithSurface(int selectedSurfaceMask) {
		return (this.surfaceMask & selectedSurfaceMask) != 0;
	}

	public boolean isFaceSpecific() {
		return this.surfaceMask != SURFACE_ANY;
	}

	public String getGuiStateDescription() {
		return this.guiStateCount == 1 ? "1 state" : this.guiStateCount + " states";
	}

	public String getDropdownDescription() {
		return this.displayName + " — " + getSurfaceDescription() + " — " + getGuiStateDescription();
	}

	public String getSurfaceDescription() {
		switch(this.surfaceMask) {
		case SURFACE_WALL:
			return "side";
		case SURFACE_FLOOR:
			return "top";
		case SURFACE_CEILING:
			return "bottom";
		case SURFACE_FLOOR | SURFACE_CEILING:
			return "top/bottom";
		case SURFACE_ANY:
		default:
			return "any surface";
		}
	}

	@Override
	public String toString() {
		return getDropdownDescription();
	}

	/** One-state profiles are still parseable for older pattern files, but hidden from the block-type dropdown. */
	public boolean isSelectableInGui() {
		return this.guiStateCount > 1;
	}

	public static EnumBlockType[] selectableValues() {
		int count = 0;
		for(EnumBlockType type : values()) {
			if(type.isSelectableInGui()) count++;
		}

		EnumBlockType[] selectable = new EnumBlockType[count];
		int index = 0;
		for(EnumBlockType type : values()) {
			if(type.isSelectableInGui()) selectable[index++] = type;
		}
		return selectable;
	}

	public static EnumBlockType fromId(int id) {
		for(EnumBlockType type : values()) {
			if(type.getId() == id) return type;
		}
		throw new IllegalArgumentException("Unknown block type id: " + id);
	}

	public static EnumBlockType fromToken(String token) {
		EnumBlockType type = tryFromToken(token);
		if(type == null) {
			String unsupportedReason = unsupportedReasonForToken(token);
			if(unsupportedReason != null) throw new IllegalArgumentException(unsupportedReason);
			throw new IllegalArgumentException("Unsupported block type '" + token + "'. Supported block types: " + supportedTokenList() + ".");
		}
		return type;
	}

	public static EnumBlockType tryFromToken(String token) {
		if(token == null) return null;
		String normalized = normalizeToken(token);
		for(EnumBlockType type : values()) {
			if(type.getFileToken().equals(normalized)) return type;
			if(type.name().toLowerCase().equals(normalized)) return type;
			if(type.getAbbreviation().toLowerCase().equals(normalized)) return type;
			for(String alias : type.aliases) {
				if(normalizeToken(alias).equals(normalized)) return type;
			}
		}
		return null;
	}

	public static boolean isRemovedStaticToken(String token) {
		String normalized = normalizeToken(token);
		String[] removedTokens = new String[] {
			"tuff", "tuf",
			"gravel", "grv",
			"granite", "grt",
			"diorite", "dio",
			"andesite", "and",
			"blackstone", "blk"
		};
		for(String removedToken : removedTokens) {
			if(removedToken.equals(normalized)) return true;
		}
		return false;
	}

	public static String unsupportedReasonForToken(String token) {
		String normalized = normalizeToken(token);
		if(normalized.equals("netherrack") || normalized.equals("neth")) {
			return "Netherrack is disabled for 1.21.11 because it uses a 16-entry randomized model pool that is not represented by this tool's four-state GUI mapping yet.";
		}
		String[] objectModeTokens = new String[] {"lily_pad", "turtle_egg", "sea_pickle", "bamboo", "chorus_plant", "fire", "soul_fire"};
		for(String unsupported : objectModeTokens) {
			if(unsupported.equals(normalized)) return "Unsupported non-cube/object randomized block '" + token + "'. This workflow supports normal cube-face observations only.";
		}
		return null;
	}

	private static String normalizeToken(String token) {
		return token == null ? "" : token.trim().toLowerCase().replace('-', '_').replace(' ', '_');
	}

	public static String supportedTokenList() {
		StringJoiner joiner = new StringJoiner(", ");
		for(EnumBlockType type : selectableValues()) {
			joiner.add(type.getFileToken());
		}
		return joiner.toString();
	}
}
