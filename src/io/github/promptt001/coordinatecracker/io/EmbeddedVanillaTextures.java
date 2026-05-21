package io.github.promptt001.coordinatecracker.io;

import java.awt.image.BufferedImage;

/**
 * Exact vanilla texture pixels embedded as Java source data.
 *
 * The constants below are decoded ARGB pixels from the vanilla block texture
 * PNGs used by the previous texture-bundled build. They deliberately avoid
 * shipping or loading the original PNG files while preserving the same pixel
 * values for GUI screenshot transcription.
 */
final class EmbeddedVanillaTextures {
    private EmbeddedVanillaTextures() {}

    static BufferedImage get(String fileName) {
        if(fileName == null) return null;
        switch(fileName) {
        case "bedrock.png":
            return image(16, 16, BEDROCK);
        case "black_concrete_powder.png":
            return image(16, 16, BLACK_CONCRETE_POWDER);
        case "blue_concrete_powder.png":
            return image(16, 16, BLUE_CONCRETE_POWDER);
        case "brown_concrete_powder.png":
            return image(16, 16, BROWN_CONCRETE_POWDER);
        case "cyan_concrete_powder.png":
            return image(16, 16, CYAN_CONCRETE_POWDER);
        case "deepslate.png":
            return image(16, 16, DEEPSLATE);
        case "deepslate_top.png":
            return image(16, 16, DEEPSLATE_TOP);
        case "dirt.png":
            return image(16, 16, DIRT);
        case "dirt_path_side.png":
            return image(16, 16, DIRT_PATH_SIDE);
        case "dirt_path_top.png":
            return image(16, 16, DIRT_PATH_TOP);
        case "grass_block_side.png":
            return image(16, 16, GRASS_BLOCK_SIDE);
        case "grass_block_top.png":
            return image(16, 16, GRASS_BLOCK_TOP);
        case "gray_concrete_powder.png":
            return image(16, 16, GRAY_CONCRETE_POWDER);
        case "green_concrete_powder.png":
            return image(16, 16, GREEN_CONCRETE_POWDER);
        case "light_blue_concrete_powder.png":
            return image(16, 16, LIGHT_BLUE_CONCRETE_POWDER);
        case "light_gray_concrete_powder.png":
            return image(16, 16, LIGHT_GRAY_CONCRETE_POWDER);
        case "lime_concrete_powder.png":
            return image(16, 16, LIME_CONCRETE_POWDER);
        case "magenta_concrete_powder.png":
            return image(16, 16, MAGENTA_CONCRETE_POWDER);
        case "mycelium_side.png":
            return image(16, 16, MYCELIUM_SIDE);
        case "mycelium_top.png":
            return image(16, 16, MYCELIUM_TOP);
        case "netherrack.png":
            return image(16, 16, NETHERRACK);
        case "orange_concrete_powder.png":
            return image(16, 16, ORANGE_CONCRETE_POWDER);
        case "pink_concrete_powder.png":
            return image(16, 16, PINK_CONCRETE_POWDER);
        case "podzol_side.png":
            return image(16, 16, PODZOL_SIDE);
        case "podzol_top.png":
            return image(16, 16, PODZOL_TOP);
        case "purple_concrete_powder.png":
            return image(16, 16, PURPLE_CONCRETE_POWDER);
        case "red_concrete_powder.png":
            return image(16, 16, RED_CONCRETE_POWDER);
        case "red_sand.png":
            return image(16, 16, RED_SAND);
        case "rooted_dirt.png":
            return image(16, 16, ROOTED_DIRT);
        case "sand.png":
            return image(16, 16, SAND);
        case "sculk.png":
            return image(16, 64, SCULK);
        case "stone.png":
            return image(16, 16, STONE);
        case "white_concrete_powder.png":
            return image(16, 16, WHITE_CONCRETE_POWDER);
        case "yellow_concrete_powder.png":
            return image(16, 16, YELLOW_CONCRETE_POWDER);
        default:
            return null;
        }
    }

    private static BufferedImage image(int width, int height, String hexPixels) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = new int[width * height];
        for(int i = 0; i < pixels.length; i++) {
            int offset = i * 8;
            pixels[i] = (int) Long.parseLong(hexPixels.substring(offset, offset + 8), 16);
        }
        image.setRGB(0, 0, width, height, pixels, 0, width);
        return image;
    }

    private static final String BEDROCK =
        "ff636363ff333333ff333333ff333333ff333333ff575757ff979797ff636363" +
        "ff575757ff979797ff575757ff333333ff333333ff575757ff333333ff575757" +
        "ff333333ff636363ff636363ff979797ff636363ff979797ff636363ff979797" +
        "ff333333ff979797ff979797ff636363ff222222ff333333ff575757ff333333" +
        "ff333333ff222222ff333333ff333333ff222222ff333333ff333333ff333333" +
        "ff979797ff333333ff575757ff575757ff575757ff979797ff575757ff636363" +
        "ff575757ff979797ff636363ff636363ff575757ff222222ff575757ff979797" +
        "ff575757ff575757ff333333ff333333ff333333ff333333ff333333ff575757" +
        "ff222222ff333333ff979797ff333333ff333333ff333333ff333333ff575757" +
        "ff333333ff333333ff636363ff979797ff979797ff636363ff636363ff333333" +
        "ff333333ff575757ff575757ff575757ff333333ff575757ff575757ff333333" +
        "ff333333ff222222ff575757ff575757ff333333ff333333ff222222ff333333" +
        "ff979797ff979797ff636363ff575757ff636363ff979797ff979797ff636363" +
        "ff575757ff636363ff979797ff979797ff636363ff979797ff333333ff222222" +
        "ff575757ff575757ff575757ff333333ff575757ff575757ff575757ff333333" +
        "ff333333ff222222ff333333ff333333ff575757ff333333ff636363ff575757" +
        "ff636363ff979797ff979797ff575757ff333333ff333333ff575757ff575757" +
        "ff636363ff979797ff979797ff636363ff979797ff979797ff979797ff979797" +
        "ff575757ff333333ff333333ff222222ff333333ff575757ff333333ff575757" +
        "ff333333ff636363ff575757ff333333ff333333ff333333ff979797ff636363" +
        "ff636363ff636363ff979797ff333333ff636363ff979797ff979797ff333333" +
        "ff222222ff333333ff979797ff979797ff979797ff636363ff333333ff333333" +
        "ff222222ff575757ff575757ff575757ff333333ff333333ff575757ff636363" +
        "ff979797ff979797ff575757ff636363ff636363ff979797ff979797ff222222" +
        "ff575757ff333333ff333333ff333333ff636363ff575757ff333333ff333333" +
        "ff636363ff979797ff333333ff333333ff333333ff333333ff333333ff575757" +
        "ff636363ff979797ff636363ff979797ff575757ff222222ff575757ff333333" +
        "ff333333ff333333ff222222ff333333ff575757ff636363ff979797ff979797" +
        "ff636363ff636363ff575757ff333333ff333333ff575757ff333333ff333333" +
        "ff575757ff636363ff979797ff979797ff575757ff333333ff333333ff575757" +
        "ff333333ff222222ff333333ff333333ff222222ff979797ff979797ff636363" +
        "ff333333ff575757ff636363ff222222ff333333ff333333ff575757ff636363";

    private static final String BLACK_CONCRETE_POWDER =
        "ff121519ff232328ff232528ff29292dff1e1f23ff1b1c21ff13161cff15161c" +
        "ff1e1f23ff1b1c21ff13151bff121219ff2c2c30ff1e1f25ff121519ff13161b" +
        "ff12131bff15161cff191b1fff212225ff101318ff19191fff18191eff1e1f23" +
        "ff222326ff37373bff12151cff191b1fff12131bff18191fff15161eff3d3d42" +
        "ff2d2d32ff2b2b2fff12151cff1f2125ff26262bff15161cff101219ff1e1e23" +
        "ff101219ff13151bff232328ff18191fff16181eff13151bff1c1c22ff3d3d42" +
        "ff0f1218ff222226ff13161bff12131bff0f1018ff232328ff18181cff101219" +
        "ff131319ff18191eff12151bff13131bff18191fff16181eff0f1218ff1f1f23" +
        "ff15161eff18191eff16181eff19191fff12151bff1c1e22ff232528ff232528" +
        "ff15181cff0f1018ff1c1c22ff1f2125ff1b1c21ff131519ff12151bff16181e" +
        "ff1b1c21ff101218ff15181bff232529ff2d2d32ff13161bff13161cff1c1e22" +
        "ff18191eff101319ff1c1e22ff222226ff121319ff252529ff191b1fff191b1f" +
        "ff16161cff16181fff13161bff15161cff212125ff16191fff191b1fff121319" +
        "ff1b1b1fff1e1e23ff12151bff101319ff191b1fff212225ff16181eff101219" +
        "ff101218ff28282cff191b1fff16181eff1c1e22ff222326ff16181fff13161e" +
        "ff1e1e23ff101219ff101219ff212125ff1e1e23ff18191fff15161eff12151b" +
        "ff12131bff252529ff232328ff26262bff16191fff1c1e22ff1e1f23ff13151c" +
        "ff1c1e22ff15161eff121519ff212125ff1f2125ff191b1fff101218ff2b2b2f" +
        "ff15151bff1c1e22ff1c1e22ff10131bff13151bff101219ff0f1018ff16181c" +
        "ff16181eff0f1218ff101219ff15161cff222226ff232528ff1b1b1fff26262b" +
        "ff12131bff191b1fff121218ff15151cff121319ff121319ff13161bff252529" +
        "ff13161bff121319ff1e1f23ff15161cff12151bff12151bff15181cff1c1e22" +
        "ff191b1fff1e1f23ff121519ff191b1fff15161eff13161bff15181cff12151b" +
        "ff222226ff232328ff222328ff1f2125ff1f2125ff1b1c21ff1c1c22ff0e1015" +
        "ff15181cff16181eff1f1f25ff13151bff232328ff121519ff3b3b41ff222226" +
        "ff15161cff16191eff1e1f23ff12151bff1c1e22ff191b1fff15181bff101319" +
        "ff1b1c21ff1e1f23ff252529ff222226ff34343aff212125ff18191eff26262b" +
        "ff252529ff18191fff1b1c21ff222326ff13161cff12151bff1b1c21ff10131b" +
        "ff101319ff121219ff0f1018ff18181eff13161cff1e1e23ff1c1e22ff1c1e22" +
        "ff222326ff26282cff131319ff18191eff13161cff121319ff191b1fff212225" +
        "ff1c1e22ff101319ff16181eff0f1216ff1b1c22ff1f2125ff121519ff13161b" +
        "ff101219ff1c1e22ff1b1c21ff1e1e22ff12131bff121219ff18191eff131519";

    private static final String BLUE_CONCRETE_POWDER =
        "ff3f419aff4d51afff5053b2ff4d51afff4c50b0ff4a4eabff4346a3ff4548a5" +
        "ff4143a2ff4143a1ff4a4eabff4246a4ff4e53b2ff555abfff3e419aff4245a1" +
        "ff4143a2ff494daaff4d50afff4245a4ff42459dff41429eff3e419bff4649a5" +
        "ff4a4daeff494cabff565ac3ff4143a1ff4246a4ff494da9ff4e52b1ff4142a3" +
        "ff4649a7ff5055b6ff4649acff41429eff41439fff3f429cff5055b3ff4548a5" +
        "ff4348a4ff494caaff4d51b2ff4548a7ff4245a1ff484aa6ff41429eff4548aa" +
        "ff4649a4ff4649a6ff494da9ff4246a3ff3f419bff4245a4ff494ca7ff4649a7" +
        "ff3d3e93ff4548a5ff4a4eaaff4549a7ff4d50b0ff484aaaff4346a4ff484ca3" +
        "ff4346a5ff484aa7ff4245a4ff4546a4ff4346a6ff4245a4ff4649a8ff4548a8" +
        "ff4a4eaaff3e3f9aff3f429dff464aa9ff4546a3ff3f429aff5257b9ff4a4da8" +
        "ff43469fff4143a2ff4346a2ff484ab4ff494ca8ff494cabff4145a3ff4142a2" +
        "ff42439fff5055b8ff4c4eabff3f419aff3e3f9bff484aa6ff5156b4ff3d3e98" +
        "ff4649a1ff5053b4ff4546a1ff5055b3ff3d3f98ff4243a3ff4345a2ff4a4da9" +
        "ff484aa8ff4243a1ff4d50b1ff5960c5ff4a4daaff4a4dacff4548a5ff4548a5" +
        "ff484aa7ff494da9ff4c50b1ff4245a1ff4245a3ff3f419dff4548a6ff494db1" +
        "ff4649a7ff3e419cff4243a1ff4245a1ff4649a7ff5053b4ff5359bdff4c4eaf" +
        "ff484aa8ff4143a2ff4d51b3ff4c4daeff5157b7ff4245a2ff484aabff4548a6" +
        "ff3e419cff484ab1ff41459cff4245a2ff4546a3ff484aa8ff4245a3ff4548a5" +
        "ff3a3d95ff5053b1ff4346a5ff4a4eabff5157b4ff5156b4ff3f419dff494ca9" +
        "ff4c50acff4245a2ff4a4daaff4649aaff4a4eabff494cabff41439fff494cac" +
        "ff494ca9ff494ca9ff37398cff4548a7ff4a50a9ff4548a9ff4849a6ff3f42a1" +
        "ff4e52b4ff484cacff494aaaff4143a1ff4245a2ff4a4eb5ff464aa7ff4245a2" +
        "ff3e419bff4346a5ff5259b8ff4548a2ff4245a2ff4245a1ff4346a4ff484aa8" +
        "ff4348a4ff4549a6ff4245a1ff4c50b4ff494aa7ff4a4eaaff4548a3ff3d3f95" +
        "ff4c50b1ff4a4daaff4a4daaff4346a7ff3f42a1ff42439fff4649a8ff43489e" +
        "ff484aa5ff4e55b2ff4548a4ff4548a3ff5053b2ff4246a5ff42459fff4346a3" +
        "ff4c4eacff4348a7ff494ca7ff3b3e93ff3f419dff4649a5ff3f419bff484aae" +
        "ff4546a6ff484caeff494dabff4549a6ff484aa9ff3f429dff4e53b4ff4648a5" +
        "ff5259b8ff4d51afff4d51aeff4346a1ff4243a3ff4a4eaaff4549a6ff494daa" +
        "ff42459eff4a4dabff464aa2ff42439fff494dabff3e419cff4c51abff4d51af" +
        "ff494da9ff4649a8ff41429dff494da5ff5357bdff4649a7ff3f419aff4a4db0" +
        "ff4346a3ff484aabff494aa7ff4142a1ff4346a4ff4143a1ff4546a3ff42439e";

    private static final String BROWN_CONCRETE_POWDER =
        "ff714d2fff7a5134ff835939ff795132ff7d5336ff775032ff7e5536ff7f5637" +
        "ff8b603eff7c5234ff785133ff795133ff926543ff7c5333ff6c492dff724e2f" +
        "ff906341ff714d2fff765032ff825637ff724e32ff775133ff7d5336ff714c2f" +
        "ff7c5233ff7a5233ff9e6e49ff8c603eff7c5234ff8b5f3fff775032ff885d3a" +
        "ff734e30ff825636ff865a3aff744e30ff724c2fff744e30ff775132ff744e30" +
        "ff895d3dff765032ff895d3bff8a5e3bff865b3bff885b3dff744e30ff8a5e3b" +
        "ff895d3dff785133ff825739ff805636ff745032ff795132ff734e30ff785132" +
        "ff714e32ff7c5336ff795133ff785133ff835939ff7f5536ff785132ff6f4d30" +
        "ff795133ff734d2fff845739ff775032ff8b5f3dff8a5f3bff7a5233ff845939" +
        "ff84593aff745032ff7d5336ff855a3aff7e5336ff6e4a2fff7f5636ff714d2f" +
        "ff785133ff8c603fff6f4c2fff946541ff83593aff916441ff7e5334ff795133" +
        "ff734d2fff805636ff84593aff7f5739ff9c724dff865b3dff805637ff6c492c" +
        "ff795336ff7d5334ff785133ff8e6341ff744e32ff765032ff7a5134ff785133" +
        "ff7c5234ff775132ff7a5233ff805736ff775132ff7a5233ff885b3bff734e2f" +
        "ff885d3dff7c5234ff805637ff7e5336ff7f5537ff7c5234ff8b603dff8d623e" +
        "ff795233ff734d30ff9e744eff775132ff936543ff825739ff8d603eff7a5233" +
        "ff7f5637ff7e5536ff835737ff7c5233ff7d5334ff7e5536ff795133ff7e5336" +
        "ff775032ff845937ff6f4c30ff775132ff84593aff8a5e3eff895d3dff865b3b" +
        "ff6f4c30ff7c5234ff7a5133ff714d2fff775132ff8c5f3eff724c2fff775133" +
        "ff7c5234ff7e5536ff765032ff775132ff765032ff916442ff775132ff825737" +
        "ff795233ff7a5133ff644328ff805637ff7d5537ff845737ff734e2fff744e30" +
        "ff795132ff7d5334ff85593aff775132ff825739ff805534ff7f5637ff7f5537" +
        "ff82573aff8a5e3dff7d5336ff745032ff724c2fff8c603fff775132ff724d2f" +
        "ff765132ff8d623fff825739ff90633fff795234ff734d30ff6f4a2dff735032" +
        "ff845939ff724d2fff795234ff835737ff785132ff895d3dff825739ff765132" +
        "ff785134ff7d5334ff744e30ff795234ff7f5637ff7e5336ff83593bff795133" +
        "ff895d3dff7c5334ff7c5234ff765134ff765132ff745032ff795234ff805536" +
        "ff885d3aff865a3aff805637ff855a3aff765030ff795133ff7e5534ff805639" +
        "ff865b3aff7c5234ff7c5234ff865b3dff7c5234ff734e2fff785132ff825737" +
        "ff8c6041ff7d5536ff6c4a2dff825739ff775132ff7c5236ff724e30ff946843" +
        "ff7f5637ff8b5f3dff7c5336ff845a3bff8e603dff8c603fff83593aff8c603e" +
        "ff734e30ff7a5132ff7d5336ff765030ff825636ff835739ff765032ff765033";

    private static final String CYAN_CONCRETE_POWDER =
        "ff238492ff25989eff25939dff258b99ff25979fff259a9eff239099ff258b98" +
        "ff258c9aff258c9aff25929bff259ba2ff25939cff28a4aaff238b95ff258b98" +
        "ff258c9aff258d9aff25a3a5ff269fa5ff228490ff239299ff238995ff238895" +
        "ff25929fff26a2a7ff289aaaff25939cff25a2a4ff258d9aff25949dff30d0d0" +
        "ff25909cff28b4b3ff28a3a9ff23979cff258b99ff238998ff25919bff258d9a" +
        "ff258d9aff258d9aff258e9cff25939eff259da1ff238995ff238895ff26a1a8" +
        "ff239299ff238997ff258c99ff258c9aff238c98ff25999fff238997ff258c9a" +
        "ff1f7c88ff239299ff25959dff25979eff25919eff258e9cff258e9aff228390" +
        "ff25919cff23989eff258e9eff239299ff269aa3ff258e9eff2598a1ff26a5a8" +
        "ff238a97ff238694ff239a9eff25959fff259da1ff238b93ff2699a3ff238894" +
        "ff238895ff25a3a4ff238d98ff299eacff238a97ff25949fff258c9aff25929c" +
        "ff239099ff26a5a9ff25949dff238893ff23949aff238895ff259da1ff239398" +
        "ff21808dff258a99ff259e9fff258c9aff228390ff258e9bff258e9aff23939a" +
        "ff23929aff23949aff2699a2ff289aa8ff25919bff25989fff25919cff25979c" +
        "ff258b99ff23999dff269aa2ff25909aff25929bff23999cff25949eff28a6aa" +
        "ff25959dff238e99ff239b9fff2dc7c7ff25999fff258d9aff26a6abff26a1a6" +
        "ff25909bff25999fff2698a3ff269da4ff25939cff238995ff2598a1ff259ca2" +
        "ff23919aff2898a8ff239498ff259b9fff239099ff258c9aff25989fff258d9a" +
        "ff22828eff25919aff269ba2ff258898ff25939cff25909bff239a9dff23979c" +
        "ff25979dff259da1ff258c9aff258e9eff258c9aff25959eff259a9eff26a3a7" +
        "ff25999fff25949eff1f7c86ff2598a1ff238a94ff25929fff258998ff258d9a" +
        "ff26a3a8ff269aa3ff25939eff238b98ff25909aff28a9b1ff238d99ff26a5a6" +
        "ff239b9eff25939eff25919dff238a95ff26a3a5ff2fc8c8ff25909bff258b99" +
        "ff258d9aff25909bff259fa2ff28a3aaff25a2a2ff238a97ff29bab9ff218490" +
        "ff2594a1ff258d9aff238895ff2698a3ff25939cff239099ff258c9aff21808d" +
        "ff238893ff25919bff25a6a6ff259e9fff259ca3ff269ba3ff238593ff238998" +
        "ff258c9aff258e9cff239399ff229898ff238e99ff23959bff238c98ff26a2a9" +
        "ff26aaaaff26a9acff258e9cff259aa1ff25919cff25a3a4ff26a4a7ff258c99" +
        "ff26a4a6ff258a99ff238a98ff238c98ff258c9aff239ca1ff259a9eff25949e" +
        "ff238b95ff258c9aff228a91ff238d99ff25929dff238894ff23999cff25909b" +
        "ff23929aff258d9bff23979cff238c94ff29b4b5ff26a4a5ff228291ff2699a3" +
        "ff25999fff269ea6ff239c9fff25909bff25929cff259ba1ff238a98ff239298";

    private static final String DEEPSLATE =
        "ff797979ff646464ff515151ff515151ff515151ff515151ff3d3d43ff3d3d43" +
        "ff646464ff646464ff646464ff515151ff515151ff3d3d43ff515151ff515151" +
        "ff515151ff3d3d43ff2f2f37ff2f2f37ff3d3d43ff3d3d43ff3d3d43ff3d3d43" +
        "ff515151ff646464ff515151ff515151ff3d3d43ff3d3d43ff515151ff3d3d43" +
        "ff646464ff646464ff797979ff646464ff646464ff515151ff515151ff2f2f37" +
        "ff2f2f37ff2f2f37ff2f2f37ff3d3d43ff3d3d43ff2f2f37ff3d3d43ff3d3d43" +
        "ff3d3d43ff515151ff646464ff515151ff515151ff3d3d43ff3d3d43ff3d3d43" +
        "ff515151ff646464ff797979ff797979ff646464ff646464ff646464ff515151" +
        "ff2f2f37ff3d3d43ff515151ff515151ff3d3d43ff3d3d43ff3d3d43ff515151" +
        "ff646464ff646464ff646464ff797979ff646464ff646464ff515151ff3d3d43" +
        "ff797979ff797979ff515151ff646464ff515151ff515151ff2f2f37ff515151" +
        "ff646464ff646464ff515151ff646464ff515151ff515151ff515151ff646464" +
        "ff797979ff646464ff646464ff515151ff515151ff2f2f37ff3d3d43ff3d3d43" +
        "ff515151ff515151ff3d3d43ff3d3d43ff2f2f37ff2f2f37ff3d3d43ff515151" +
        "ff646464ff515151ff3d3d43ff3d3d43ff3d3d43ff3d3d43ff646464ff797979" +
        "ff646464ff646464ff515151ff3d3d43ff2f2f37ff3d3d43ff515151ff515151" +
        "ff515151ff3d3d43ff515151ff3d3d43ff3d3d43ff3d3d43ff515151ff797979" +
        "ff646464ff515151ff646464ff515151ff3d3d43ff3d3d43ff3d3d43ff515151" +
        "ff3d3d43ff3d3d43ff2f2f37ff2f2f37ff515151ff515151ff515151ff646464" +
        "ff515151ff515151ff3d3d43ff515151ff3d3d43ff2f2f37ff2f2f37ff3d3d43" +
        "ff646464ff515151ff646464ff515151ff3d3d43ff515151ff515151ff3d3d43" +
        "ff3d3d43ff2f2f37ff2f2f37ff3d3d43ff515151ff515151ff797979ff797979" +
        "ff646464ff646464ff515151ff3d3d43ff646464ff797979ff797979ff797979" +
        "ff515151ff3d3d43ff515151ff3d3d43ff3d3d43ff515151ff797979ff646464" +
        "ff646464ff515151ff3d3d43ff3d3d43ff515151ff797979ff646464ff646464" +
        "ff646464ff515151ff3d3d43ff3d3d43ff3d3d43ff515151ff646464ff646464" +
        "ff3d3d43ff3d3d43ff3d3d43ff2f2f37ff2f2f37ff515151ff646464ff646464" +
        "ff515151ff3d3d43ff2f2f37ff3d3d43ff2f2f37ff2f2f37ff3d3d43ff3d3d43" +
        "ff797979ff797979ff646464ff646464ff515151ff3d3d43ff3d3d43ff2f2f37" +
        "ff3d3d43ff797979ff646464ff515151ff515151ff646464ff646464ff797979" +
        "ff797979ff797979ff646464ff515151ff646464ff515151ff515151ff2f2f37" +
        "ff515151ff797979ff646464ff646464ff515151ff515151ff646464ff646464";

    private static final String DEEPSLATE_TOP =
        "ff646464ff5a5a5aff4b4b50ff4b4b50ff5a5a5aff5a5a5aff646464ff646464" +
        "ff646464ff646464ff747474ff4b4b50ff4b4b50ff3d3d43ff4b4b50ff646464" +
        "ff646464ff3d3d43ff3d3d43ff4b4b50ff4b4b50ff4b4b50ff5a5a5aff747474" +
        "ff646464ff747474ff747474ff3d3d43ff646464ff747474ff3d3d43ff5a5a5a" +
        "ff4b4b50ff5a5a5aff5a5a5aff646464ff747474ff3d3d43ff4b4b50ff4b4b50" +
        "ff4b4b50ff4b4b50ff3d3d43ff646464ff747474ff747474ff3d3d43ff3d3d43" +
        "ff3d3d43ff3d3d43ff4b4b50ff5a5a5aff646464ff3d3d43ff3d3d43ff5a5a5a" +
        "ff5a5a5aff646464ff646464ff646464ff646464ff747474ff3d3d43ff5a5a5a" +
        "ff747474ff3d3d43ff646464ff4b4b50ff3d3d43ff4b4b50ff5a5a5aff4b4b50" +
        "ff3d3d43ff3d3d43ff4b4b50ff5a5a5aff646464ff4b4b50ff646464ff646464" +
        "ff747474ff3d3d43ff646464ff646464ff646464ff5a5a5aff5a5a5aff5a5a5a" +
        "ff4b4b50ff4b4b50ff5a5a5aff4b4b50ff5a5a5aff646464ff646464ff747474" +
        "ff4b4b50ff3d3d43ff3d3d43ff3d3d43ff5a5a5aff4b4b50ff4b4b50ff5a5a5a" +
        "ff5a5a5aff4b4b50ff3d3d43ff3d3d43ff4b4b50ff4b4b50ff5a5a5aff5a5a5a" +
        "ff646464ff747474ff747474ff646464ff3d3d43ff4b4b50ff4b4b50ff4b4b50" +
        "ff646464ff646464ff646464ff747474ff747474ff3d3d43ff646464ff5a5a5a" +
        "ff5a5a5aff646464ff747474ff747474ff3d3d43ff646464ff5a5a5aff4b4b50" +
        "ff5a5a5aff5a5a5aff646464ff646464ff747474ff3d3d43ff5a5a5aff646464" +
        "ff646464ff747474ff747474ff5a5a5aff4b4b50ff5a5a5aff4b4b50ff4b4b50" +
        "ff4b4b50ff5a5a5aff5a5a5aff4b4b50ff3d3d43ff4b4b50ff4b4b50ff5a5a5a" +
        "ff646464ff646464ff747474ff4b4b50ff4b4b50ff5a5a5aff646464ff747474" +
        "ff3d3d43ff4b4b50ff646464ff5a5a5aff646464ff646464ff747474ff747474" +
        "ff4b4b50ff5a5a5aff3d3d43ff3d3d43ff646464ff646464ff646464ff747474" +
        "ff3d3d43ff3d3d43ff4b4b50ff646464ff5a5a5aff747474ff646464ff747474" +
        "ff3d3d43ff4b4b50ff5a5a5aff5a5a5aff4b4b50ff3d3d43ff3d3d43ff3d3d43" +
        "ff4b4b50ff4b4b50ff5a5a5aff5a5a5aff5a5a5aff4b4b50ff4b4b50ff3d3d43" +
        "ff4b4b50ff3d3d43ff5a5a5aff646464ff747474ff646464ff747474ff3d3d43" +
        "ff5a5a5aff646464ff747474ff646464ff747474ff3d3d43ff4b4b50ff5a5a5a" +
        "ff4b4b50ff5a5a5aff646464ff646464ff646464ff747474ff747474ff3d3d43" +
        "ff5a5a5aff5a5a5aff646464ff747474ff747474ff3d3d43ff646464ff646464" +
        "ff5a5a5aff5a5a5aff5a5a5aff4b4b50ff3d3d43ff3d3d43ff3d3d43ff4b4b50" +
        "ff5a5a5aff5a5a5aff4b4b50ff5a5a5aff5a5a5aff5a5a5aff4b4b50ff4b4b50";

    private static final String DIRT =
        "ffb9855cff966c4aff966c4aff79553aff79553affb9855cff966c4aff966c4a" +
        "ff79553aff79553aff593d29ff79553aff79553affb9855cff79553affb9855c" +
        "ff79553aff966c4aff593d29ff79553aff79553aff966c4aff878787ff593d29" +
        "ff79553affb9855cff966c4aff79553affb9855cff966c4aff593d29ff593d29" +
        "ffb9855cff79553aff79553aff593d29ffb9855cff79553aff79553aff79553a" +
        "ffb9855cff79553aff79553aff79553aff593d29ff593d29ffb9855cff79553a" +
        "ff966c4aff6c6c6cffb9855cff79553aff966c4aff593d29ff79553affb9855c" +
        "ff966c4aff966c4aff79553aff966c4aff79553affb9855cff966c4aff79553a" +
        "ff966c4aff79553aff966c4affb9855cff593d29ff966c4aff79553aff79553a" +
        "ff966c4aff593d29ff79553aff6c6c6cff79553aff966c4aff593d29ff79553a" +
        "ff79553aff593d29ff966c4aff966c4aff79553aff966c4aff593d29ff593d29" +
        "ff593d29ff79553aff79553aff593d29ff79553aff79553aff79553affb9855c" +
        "ffb9855cff79553aff79553aff79553aff878787ff79553aff79553affb9855c" +
        "ffb9855cff79553affb9855cffb9855cff79553aff966c4aff79553aff966c4a" +
        "ff79553aff79553affb9855cffb9855cff966c4aff966c4aff79553aff79553a" +
        "ff966c4aff593d29ff966c4aff966c4aff79553aff79553aff966c4aff966c4a" +
        "ff966c4aff79553aff79553aff966c4aff79553aff966c4aff79553aff593d29" +
        "ff79553aff966c4aff966c4aff79553aff79553aff79553aff593d29ff79553a" +
        "ff79553aff966c4aff593d29ff79553aff79553aff593d29ff593d29ff79553a" +
        "ff79553aff79553aff79553aff79553affb9855cffb9855cff79553aff966c4a" +
        "ff79553aff966c4aff79553affb9855cffb9855cff79553affb9855cff966c4a" +
        "ff593d29ffb9855cffb9855cff593d29ff966c4aff966c4aff878787ff79553a" +
        "ff966c4aff79553aff79553aff966c4aff966c4affb9855cff79553aff966c4a" +
        "ff6c6c6cff966c4aff966c4aff79553aff593d29ff966c4aff79553aff593d29" +
        "ff79553aff593d29ff966c4aff79553aff966c4aff966c4affb9855cff79553a" +
        "ff79553aff79553aff79553aff79553aff79553aff79553affb9855cffb9855c" +
        "ff79553aff966c4aff79553aff79553aff745844ff79553aff966c4aff966c4a" +
        "ff79553aff593d29ffb9855cff593d29ff79553affb9855cff966c4aff966c4a" +
        "ff966c4aff79553aff593d29ffb9855cff79553aff593d29ff79553aff593d29" +
        "ffb9855cffb9855cff79553aff966c4aff79553aff79553aff966c4aff966c4a" +
        "ff966c4aff79553affb9855cff966c4aff966c4aff79553aff878787ff79553a" +
        "ff966c4aff966c4aff79553aff79553aff966c4aff966c4aff79553aff593d29";

    private static final String DIRT_PATH_SIDE =
        "0000000000000000000000000000000000000000000000000000000000000000" +
        "0000000000000000000000000000000000000000000000000000000000000000" +
        "ffaa8d4aff998043ffaa8d4aff998043ff907540ff998043ff907540ff907540" +
        "ff998043ffaa8d4aff907540ff998043ff907540ff856b3aff907540ff998043" +
        "ffaa8d4aff856b3affaa8d4aff998043ffaa8d4aff593d29ff998043ff907540" +
        "ff998043ffaa8d4aff998043ff998043ff907540ff907540ffaa8d4aff998043" +
        "ff998043ff6c6c6cff998043ff856b3affaa8d4aff593d29ff998043ff593d29" +
        "ff998043ff856b3aff998043ff907540ff79553aff907540ffaa8d4aff79553a" +
        "ff966c4aff79553aff966c4affb9855cff907540ff966c4aff79553aff79553a" +
        "ff856b3aff593d29ff907540ff6c6c6cff79553aff966c4aff593d29ff79553a" +
        "ff79553aff593d29ff966c4aff966c4aff79553aff966c4aff593d29ff593d29" +
        "ff593d29ff79553aff79553aff593d29ff79553aff79553aff79553affb9855c" +
        "ffb9855cff79553aff79553aff79553aff878787ff79553aff79553affb9855c" +
        "ffb9855cff79553affb9855cffb9855cff79553aff966c4aff79553aff966c4a" +
        "ff79553aff79553affb9855cffb9855cff966c4aff966c4aff79553aff79553a" +
        "ff966c4aff593d29ff966c4aff966c4aff79553aff79553aff966c4aff966c4a" +
        "ff966c4aff79553aff79553aff966c4aff79553aff966c4aff79553aff593d29" +
        "ff79553aff966c4aff966c4aff79553aff79553aff79553aff593d29ff79553a" +
        "ff79553aff966c4aff593d29ff79553aff79553aff593d29ff593d29ff79553a" +
        "ff79553aff79553aff79553aff79553affb9855cffb9855cff79553aff966c4a" +
        "ff79553aff966c4aff79553affb9855cffb9855cff79553affb9855cff966c4a" +
        "ff593d29ffb9855cffb9855cff593d29ff966c4aff966c4aff878787ff79553a" +
        "ff966c4aff79553aff79553aff966c4aff966c4affb9855cff79553aff966c4a" +
        "ff6c6c6cff966c4aff966c4aff79553aff593d29ff966c4aff79553aff593d29" +
        "ff79553aff593d29ff966c4aff79553aff966c4aff966c4affb9855cff79553a" +
        "ff79553aff79553aff79553aff79553aff79553aff79553affb9855cffb9855c" +
        "ff79553aff966c4aff79553aff79553aff745844ff79553aff966c4aff966c4a" +
        "ff79553aff593d29ffb9855cff593d29ff79553affb9855cff966c4aff966c4a" +
        "ff966c4aff79553aff593d29ffb9855cff79553aff593d29ff79553aff593d29" +
        "ffb9855cffb9855cff79553aff966c4aff79553aff79553aff966c4aff966c4a" +
        "ff966c4aff79553affb9855cff966c4aff966c4aff79553aff878787ff79553a" +
        "ff966c4aff966c4aff79553aff79553aff966c4aff966c4aff79553aff593d29";

    private static final String DIRT_PATH_TOP =
        "ff998043ff998043ff907540ff907540ff998043ff998043ff907540ff907540" +
        "ff907540ff907540ff856b3aff907540ffaa8d4aff998043ff856b3aff907540" +
        "ff998043ff907540ff907540ff907540ffaa8d4aff907540ff907540ff998043" +
        "ff998043ff998043ff856b3aff907540ff907540ffaa8d4affaa8d4aff856b3a" +
        "ff907540ff907540ffaa8d4affaa8d4aff998043ff907540ff907540ff907540" +
        "ff856b3aff998043ff907540ffaa8d4aff907540ff907540ff907540ff856b3a" +
        "ff907540ff856b3aff856b3aff907540ff907540ff907540ffaa8d4aff998043" +
        "ff907540ff856b3aff907540ffaa8d4aff907540ff907540ff998043ff907540" +
        "ff907540ff998043ff907540ff907540ff998043ff998043ff856b3affaa8d4a" +
        "ff907540ff856b3aff998043ff907540ff998043ff856b3aff907540ff907540" +
        "ff907540ff998043ff998043ffaa8d4affaa8d4aff998043ff907540ffaa8d4a" +
        "ff907540ff907540ff907540ff856b3aff998043ff998043ff856b3aff907540" +
        "ff907540ff907540ff907540ff907540ff907540ff998043ff907540ff907540" +
        "ff856b3affaa8d4aff998043ff856b3aff907540ff907540ff856b3affaa8d4a" +
        "ff998043ff907540ff907540ff998043ff907540ff856b3aff856b3aff907540" +
        "ff907540ffaa8d4aff907540ff907540ff856b3aff907540ffaa8d4aff998043" +
        "ff998043ff907540ff856b3aff998043ff907540ff998043ff907540ff998043" +
        "ffaa8d4aff998043ff998043ff907540ff907540ff998043ffaa8d4aff907540" +
        "ff907540ff998043ff856b3affaa8d4aff998043ff998043ff907540ff907540" +
        "ff856b3aff856b3aff998043ff998043ff907540ff907540ff907540ff907540" +
        "ff998043ff998043ff998043ffaa8d4aff998043ff907540ff907540ff907540" +
        "ff907540ff907540ff907540ff998043ff907540ffaa8d4affaa8d4aff907540" +
        "ff907540ff998043ff907540ff998043ff907540ff998043ff998043ff907540" +
        "ffaa8d4aff998043ff907540ff907540ff907540ff998043ff907540ff907540" +
        "ff907540ffaa8d4aff998043ff907540ff907540ff907540ff998043ff998043" +
        "ff907540ffaa8d4affaa8d4aff998043ff907540ff907540ff907540ff998043" +
        "ff907540ffaa8d4aff907540ff907540ff907540ffaa8d4aff856b3aff856b3a" +
        "ff907540ff907540ff998043ff856b3aff856b3aff998043ff998043ff907540" +
        "ff907540ff998043ff907540ff907540ffaa8d4aff998043ff998043ff907540" +
        "ff907540ff998043ff856b3aff907540ff907540ff998043ff907540ff998043" +
        "ff907540ff907540ff907540ff998043ffaa8d4aff907540ff907540ff998043" +
        "ff907540ff907540ff907540ff907540ff998043ff907540ff907540ff907540";

    private static final String GRASS_BLOCK_SIDE =
        "ff74b44aff76b64cff73b349ff66a63cff66a63cff6faf45ff5f9f35ff6cac42" +
        "ff7ebe54ff76b64cff6aaa40ff67a73dff69a93fff61a137ff509026ff6dad43" +
        "ff75b54bff6cac42ff8ab95aff81b051ff83b253ff593d29ff68a83eff62a238" +
        "ff5f9f35ff93c263ff90bf60ff73b349ff61a137ff6cac42ff67a73dff6bab41" +
        "ff8dbc5dff593d29ff9ccb6cff64a43aff69a93fff593d29ff70b046ff593d29" +
        "ff74b44aff7fbf55ff92c162ff97c667ff593d29ff57972dff60a036ff593d29" +
        "ff593d29ff6c6c6cff593d29ff593d29ff71b147ff593d29ff593d29ff593d29" +
        "ff5f9f35ff593d29ff6dad43ff593d29ff79553aff593d29ff593d29ff79553a" +
        "ff966c4aff79553aff966c4affb9855cff593d29ff966c4aff79553aff79553a" +
        "ff593d29ff593d29ff593d29ff6c6c6cff79553aff966c4aff593d29ff79553a" +
        "ff79553aff593d29ff966c4aff966c4aff79553aff966c4aff593d29ff593d29" +
        "ff593d29ff79553aff79553aff593d29ff79553aff79553aff79553affb9855c" +
        "ffb9855cff79553aff79553aff79553aff878787ff79553aff79553affb9855c" +
        "ffb9855cff79553affb9855cffb9855cff79553aff966c4aff79553aff966c4a" +
        "ff79553aff79553affb9855cffb9855cff966c4aff966c4aff79553aff79553a" +
        "ff966c4aff593d29ff966c4aff966c4aff79553aff79553aff966c4aff966c4a" +
        "ff966c4aff79553aff79553aff966c4aff79553aff966c4aff79553aff593d29" +
        "ff79553aff966c4aff966c4aff79553aff79553aff79553aff593d29ff79553a" +
        "ff79553aff966c4aff593d29ff79553aff79553aff593d29ff593d29ff79553a" +
        "ff79553aff79553aff79553aff79553affb9855cffb9855cff79553aff966c4a" +
        "ff79553aff966c4aff79553affb9855cffb9855cff79553affb9855cff966c4a" +
        "ff593d29ffb9855cffb9855cff593d29ff966c4aff966c4aff878787ff79553a" +
        "ff966c4aff79553aff79553aff966c4aff966c4affb9855cff79553aff966c4a" +
        "ff6c6c6cff966c4aff966c4aff79553aff593d29ff966c4aff79553aff593d29" +
        "ff79553aff593d29ff966c4aff79553aff966c4aff966c4affb9855cff79553a" +
        "ff79553aff79553aff79553aff79553aff79553aff79553affb9855cffb9855c" +
        "ff79553aff966c4aff79553aff79553aff745844ff79553aff966c4aff966c4a" +
        "ff79553aff593d29ffb9855cff593d29ff79553affb9855cff966c4aff966c4a" +
        "ff966c4aff79553aff593d29ffb9855cff79553aff593d29ff79553aff593d29" +
        "ffb9855cffb9855cff79553aff966c4aff79553aff79553aff966c4aff966c4a" +
        "ff966c4aff79553affb9855cff966c4aff966c4aff79553aff878787ff79553a" +
        "ff966c4aff966c4aff79553aff79553aff966c4aff966c4aff79553aff593d29";

    private static final String GRASS_BLOCK_TOP =
        "ff949494ffc3c3c3ff939393ff868686ff868686ff8f8f8fff7f7f7fff8c8c8c" +
        "ff9e9e9eff969696ff8a8a8aff878787ff898989ffa7a7a7ff707070ff8d8d8d" +
        "ff959595ff8c8c8cffa0a0a0ff979797ff999999ff808080ff999999ff828282" +
        "ff7f7f7fffa9a9a9ffa6a6a6ff939393ffc3c3c3ff8c8c8cff878787ff8b8b8b" +
        "ffa3a3a3ff8d8d8dff7d7d7dff848484ff898989ff888888ff909090ff909090" +
        "ff949494ff9f9f9fffa8a8a8ffadadadff878787ff777777ff808080ffb6b6b6" +
        "ff767676ff858585ff8a8a8aff979797ff919191ff999999ff7d7d7dff929292" +
        "ff7f7f7fff838383ff8d8d8dff979797ff8f8f8fff8b8b8bffa7a7a7ff7d7d7d" +
        "ffa2a2a2ff838383ffadadadff838383ffacacacffaeaeaeff888888ffacacac" +
        "ffbababaff757575ff808080ffa7a7a7ffabababff7e7e7eff888888ff8d8d8d" +
        "ff999999ff989898ff787878ffa6a6a6ff818181ff969696ff959595ff9b9b9b" +
        "ff878787ff959595ff888888ff838383ff787878ff888888ff8e8e8eff949494" +
        "ff7e7e7eff9c9c9cffa5a5a5ff909090ff929292ff838383ff8a8a8aff7f7f7f" +
        "ff848484ff878787ffb5b5b5ffacacacff919191ffa7a7a7ff979797ff8f8f8f" +
        "ff8f8f8fff878787ff9d9d9dff8b8b8bff909090ff7c7c7cffa4a4a4ff848484" +
        "ff979797ffadadadff878787ff818181ff8f8f8fffa0a0a0ff7f7f7fffa9a9a9" +
        "ffa7a7a7ff9b9b9bffb6b6b6ffaaaaaaffacacacff818181ffadadadff9c9c9c" +
        "ffb3b3b3ffaaaaaaff828282ff929292ff858585ff939393ff9e9e9eff7e7e7e" +
        "ff979797ff828282ffb7b7b7ff8d8d8dff8c8c8cff989898ffaaaaaaff808080" +
        "ff878787ff919191ff929292ffa9a9a9ff8e8e8effb3b3b3ff8f8f8fff818181" +
        "ff9b9b9bff9e9e9eff979797ffa5a5a5ff828282ffb6b6b6ff8e8e8eff939393" +
        "ffc0c0c0ff797979ffa7a7a7ff838383ff8f8f8fff888888ff979797ff8f8f8f" +
        "ff848484ffadadadff868686ffa3a3a3ff979797ff848484ff929292ff898989" +
        "ff979797ff929292ff8c8c8cff7b7b7bffb3b3b3ff7f7f7fff7d7d7dff8a8a8a" +
        "ffbababaff979797ff818181ff8a8a8aff999999ff919191ff858585ff7a7a7a" +
        "ff8a8a8aff919191ff8a8a8aff7d7d7dff9f9f9fffa5a5a5ff797979ff979797" +
        "ff818181ffa8a8a8ff767676ff929292ff7d7d7dff999999ff818181ffc3c3c3" +
        "ffa1a1a1ff939393ffa8a8a8ff9b9b9bffa0a0a0ff888888ffa8a8a8ffaeaeae" +
        "ffa3a3a3ffa1a1a1ffc0c0c0ff7b7b7bffb3b3b3ffabababff949494ff939393" +
        "ff7e7e7eff959595ff959595ff878787ffa9a9a9ff9d9d9dff8b8b8bff797979" +
        "ff858585ff969696ff888888ff898989ffa4a4a4ff989898ff949494ff939393" +
        "ff8e8e8effbfbfbfff7c7c7cffa9a9a9ff848484ff909090ff888888ff9e9e9e";

    private static final String GRAY_CONCRETE_POWDER =
        "ff657377ff505257ff4c5053ff515357ff4d5153ff484c50ff46494dff4a4d51" +
        "ff535a5fff4d5255ff525a5eff494c50ff4d5155ff5d6569ff45484cff4e5155" +
        "ff494c51ff4c4e51ff4e5256ff515659ff43464aff46494cff484a4eff525a5e" +
        "ff4e5156ff4e5356ff555b5fff4c5053ff4c5053ff4c4e52ff484a4eff4c5055" +
        "ff4a4d51ff4d5053ff51565aff484a4dff51575bff4e5557ff4e5255ff4c5052" +
        "ff494c51ff4e5257ff4e5256ff4c5053ff494d51ff4d5356ff494c51ff505357" +
        "ff4d5256ff46494dff4d5053ff4a4e52ff4c5053ff4d5055ff4e5357ff4e5356" +
        "ff414348ff4a4d52ff494d51ff4d5055ff4a4e53ff575e62ff484a4eff46494d" +
        "ff4d5052ff4a4d51ff505557ff4a4d51ff515559ff4a4d52ff52575dff4e5357" +
        "ff505357ff4e5356ff4e5357ff575f63ff494c50ff4e5356ff4e5256ff515a5e" +
        "ff464a4dff52575bff484c50ff555a5fff4e5256ff565b60ff505359ff575f63" +
        "ff51565aff4d5155ff4c5052ff4a4d51ff4e5356ff515659ff494d51ff45484c" +
        "ff43464aff575e62ff45494dff505357ff424549ff51575aff4c4e52ff4c5153" +
        "ff494c50ff4a4d51ff4c5055ff575d62ff494c51ff4e5356ff4d5155ff4d5155" +
        "ff505257ff4a4d52ff505357ff51595bff505357ff505357ff4d5053ff5a6265" +
        "ff52575bff494d51ff51575dff484a4dff4d5055ff515559ff515357ff51565a" +
        "ff4d5155ff4c5053ff505357ff555a5eff565e60ff484a4eff4c5055ff4a4d51" +
        "ff4a4e52ff596064ff46494cff494c51ff4e5557ff4a4d52ff4d5255ff4c5053" +
        "ff4a5052ff4e5357ff4a4e52ff505559ff51575bff494c51ff494d51ff535d5f" +
        "ff4e5255ff494d51ff494d51ff515559ff4e5255ff4d5053ff4a4d51ff51575b" +
        "ff51565aff4c5053ff43484aff515357ff46494dff505357ff484a4eff4a4d52" +
        "ff4c4e52ff565d60ff4a4d51ff4d5255ff4d5053ff53595dff555d5fff535b5f" +
        "ff46494dff535a5eff494d51ff494c50ff4e5257ff51565aff4c5153ff484a4e" +
        "ff4a4d52ff4a4e52ff4d5156ff505257ff494c50ff464a4eff46494dff4c5153" +
        "ff4c5053ff4a5052ff4c5153ff4e5155ff51565aff51595bff4c4e52ff4a4e51" +
        "ff4c4e52ff4c5053ff4a4d50ff484a4eff4e5256ff4c5053ff45484cff494c50" +
        "ff4e5156ff4c5055ff505659ff43464aff535b5fff505357ff4c4e52ff51555a" +
        "ff51595bff4e5256ff51555aff4e5256ff4c4e52ff4d5155ff4a4d52ff484a4e" +
        "ff4c4d51ff4c5052ff4d5155ff464a4eff4c4e53ff4d5155ff4c5153ff4e5156" +
        "ff484c4eff4c4e52ff45484cff51575bff4e5256ff494c51ff484c50ff535b5f" +
        "ff4c4e53ff4d5052ff4c5055ff484c50ff575d60ff4a4d52ff4a4e51ff4d5053" +
        "ff505559ff51575bff4a4e52ff4c5053ff494c50ff4d5053ff52595dff525a5d";

    private static final String GREEN_CONCRETE_POWDER =
        "ff5f7726ff60782fff5e7232ff5d7130ff5f7234ff688026ff596b30ff637a29" +
        "ff5e7133ff5d6f32ff5d6f30ff657e29ff657e28ff657a34ff607828ff5e722f" +
        "ff6c8626ff62782bff5b6f32ff627730ff5f7625ff647d28ff627928ff6d8923" +
        "ff68802cff68802bff6e8630ff60772dff5d6f32ff6c8623ff5f7330ff698229" +
        "ff5f7332ff627832ff647934ff5e722fff5f732dff5b6e30ff698325ff5f762d" +
        "ff62792cff6c8628ff627730ff607632ff5e732dff5f7629ff6c8823ff688030" +
        "ff5b6f2cff5b6e30ff647c29ff688026ff596c2fff647d29ff5b6e2fff5a6d32" +
        "ff576d26ff5f772bff62792bff688028ff5f7333ff607432ff637c2bff637c23" +
        "ff5e7133ff5b6e2fff607730ff60782bff69822bff63792fff5f7234ff657c2d" +
        "ff5b6e2fff5b6f2cff5a6d2fff677f2cff63792bff5f7625ff677d2dff5f7628" +
        "ff5b6e2dff5e7230ff637a26ff6d8533ff627929ff607432ff5b6e30ff63792c" +
        "ff5b6f2fff627634ff5d7130ff5f7626ff5e722bff698323ff5e732dff596d2b" +
        "ff576c2bff657e2bff576b2fff5d6f32ff56692cff62792dff5d6f30ff5d712d" +
        "ff5e722fff5b6f2fff6b8428ff6d852fff5e7230ff698229ff5f742fff5b6e30" +
        "ff5b6f32ff5f742cff607630ff5d7130ff5e7132ff596b2fff607630ff718b2b" +
        "ff5d7130ff5a6d2fff637c29ff5e742cff5d7130ff5e7133ff657c33ff657e2c" +
        "ff688226ff657e29ff677e2dff5d7133ff5d6f32ff677f25ff62792fff6e8926" +
        "ff687f25ff677c37ff56682dff647c2bff5e732dff677f28ff5b6e30ff5a6d30" +
        "ff5d7228ff60742cff607730ff60762cff596c30ff5e7230ff5b712cff5e742c" +
        "ff688325ff5e7230ff677f28ff637732ff5d6f30ff5a6d33ff6c8525ff5f7333" +
        "ff688026ff688228ff505f2cff718c26ff57692dff607433ff5d712fff596c33" +
        "ff6f8929ff637733ff6c8628ff5f742cff5e722fff799729ff5e732dff5d7130" +
        "ff607729ff63792fff62782fff596c2dff698325ff5b6f2fff5f742fff5a6d2f" +
        "ff62792cff60772dff5d7130ff6b832fff637c28ff607729ff596b2fff53642d" +
        "ff657c2fff5e7230ff5d722cff657d2fff5f7430ff677e25ff60762fff56692b" +
        "ff678223ff5d6f32ff637a2bff698323ff647d2bff5d6f33ff688223ff6e8a23" +
        "ff60762fff698329ff5e7429ff52642bff5d722cff5b6e2fff5e732cff637833" +
        "ff6e8926ff627634ff63782fff688028ff5f7332ff5e722fff688029ff5e722f" +
        "ff5b6f33ff5e732dff677e26ff5b6f2dff637a2cff5e712fff6f8b25ff5f7332" +
        "ff627925ff5e7230ff657f22ff6b8525ff5f7333ff607728ff6b8623ff5e7430" +
        "ff5e732dff677f29ff698025ff596d2bff657939ff5d6f32ff5d7329ff647a2f" +
        "ff688325ff6e8829ff576b30ff5f7330ff6f8c25ff62792cff5f742cff637a25";

    private static final String LIGHT_BLUE_CONCRETE_POWDER =
        "ff4db7d1ff52c0dbff4ebcd9ff3da2ceff55c4deff3da3ceff50bcd5ff45aed1" +
        "ff63cce3ff4cb7d7ff56c3daff68cee3ff42abd2ff6cd9edff4cb6d0ff4db9d6" +
        "ff43aed3ff4ab6d4ff48b5d6ff5dcae2ff41a8c9ff43accfff49b4d1ff53bfd7" +
        "ff4ebeddff4cb9daff48bae2ff42abd1ff49b4d6ff4ab6d4ff5bc7deff41abd5" +
        "ff43afd4ff43b0d8ff49b9dfff45aed1ff4ebbd6ff48b4d3ff49b3d4ff4ebcd8" +
        "ff4cb7d6ff3da5d1ff5ac8e0ff46b4d8ff43acd2ff52bdd5ff42aaceff45b4dd" +
        "ff3fa7ccff42a9cfff4ab5d4ff48b3d4ff43accfff3ba3ceff50bbd5ff48b2d5" +
        "ff46a7c4ff3ea5cdff379acbff50bddaff3fa9d4ff3ea7d4ff4ebbd8ff3fa4c7" +
        "ff3a9dcfff3da2cdff48b5d9ff3b9fccff51c1ddff4dbddcff52c1dcff3faad4" +
        "ff4cb8d4ff45aeceff4ab5d3ff4ab7d9ff4ebcd7ff43abcbff45b1daff4ab4d0" +
        "ff399cc7ff49b4d5ff63c7dcff6cddf2ff43accfff45b1d8ff3ea5d0ff4ab6d7" +
        "ff46b0d2ff50c0dfff5ac5ddff49b1ceff45afcfff60c6dcff60c8dfff3ea4c7" +
        "ff52b7cfff52c1dbff48b1cfff5bc7deff53b8d0ff53c1dbff399ccbff5dc4db" +
        "ff4cb7d4ff3da2cdff4ebddcff43b4dfff59c4dcff52c2ddff45afd4ff4ab6d5" +
        "ff5ec8deff5dc5dcff50c0dcff56c3dbff42aad1ff56c0d8ff46b3d6ff59cde6" +
        "ff4ab5d6ff64c9dfff4db9d5ff48b3d2ff4ab5d6ff3fa9d3ff4ec1e1ff3fa9d4" +
        "ff3ba1cfff57c6deff48b6dbff51c0dcff51bfdbff48b3d2ff5ac9e2ff5fc9e0" +
        "ff3fa6ceff50c4e4ff50b7d0ff3da3ceff3fa5ceff41a9d1ff42abd2ff52c0d9" +
        "ff3fa4c7ff4ab4d4ff4dbad9ff45afd2ff5dc6dcff52c0daff56c0d8ff3fa5cd" +
        "ff46b0d3ff49b4d4ff3ea6cfff3fa9d6ff49b4d4ff49b7d7ff5bc5dcff3ea6d4" +
        "ff41a9d2ff46b3d5ff3b99bcff4cbadaff3da2c7ff42afd8ff4ab6d4ff3ba2ce" +
        "ff50c2dfff41acd8ff4ab8d9ff42abcfff59c4dcff5dd4ecff48b3d3ff3ea4ce" +
        "ff64c7ddff56c4deff3ea6d3ff399ac6ff46b0d4ff41a7ceff56c3dbff5bc5dc" +
        "ff41a8d0ff46b1d4ff4cb8d5ff4ec2e3ff48b3d2ff4db9d4ff49b3d2ff4ab0cb" +
        "ff55c6e1ff3ba2ceff53bfd7ff4dbdddff49b4d5ff3fa8ceff57c5deff42a6c7" +
        "ff399bc6ff5dc7dfff51bed8ff59c3d8ff45b1d5ff51c1dcff4cb7d3ff3fa6ce" +
        "ff50bcd8ff3fa9d4ff5ec4dbff41a3c3ff56c0d8ff3ba2cbff59c4daff3facd9" +
        "ff68cee4ff4cbbdfff45b2d8ff4ebbd8ff3ea7d2ff5ac4dbff50bfdcff4ab6d4" +
        "ff42aed4ff3ea4ceff48b4d3ff399bc7ff4cb8d7ff3ba1ccff49b3d4ff45b0d4" +
        "ff57bfd6ff53c1dbff48acc9ff3fa5ceff4ebedcff4ab4d1ff4ab5d2ff59c7df" +
        "ff45aed1ff399dcfff50bdd7ff42a9caff57cde8ff4cb7d7ff45abccff4ab9dc" +
        "ff4ebbd7ff3da8d7ff3fa7cdff49b4d5ff3fa8d1ff45afd2ff50bdd7ff3a9dc6";

    private static final String LIGHT_GRAY_CONCRETE_POWDER =
        "ff8a8a82ff999991ffacaca7ffa1a19cffa5a59eff9b9b94ff92928aff909086" +
        "ff9b9b94ff9c9c95ff95958eff9b9b94ff97978effb3b3abff8d8d86ff97978e" +
        "ff93938bffa1a29cff9a9a93ff9c9c94ff90908aff9b9b95ff909088ff93938d" +
        "ffa3a39cffa6a6a1ffaaaaa3ffa2a29dff979790ff919289ffa7a7a2ffa2a29c" +
        "ff9d9d97ff9b9b93ffb8b8b2ff979791ff93938bffa4a49eff999992ff9b9b94" +
        "ff9d9e97ff9e9f9aff9d9d97ffa8a8a3ff9b9b95ff92928bff909088ffb5b5af" +
        "ff989891ff92928aff97978eff93938cff909089ffaaaaa7ff92928aff92928b" +
        "ff888882ff95958effa1a19bff93938bff94948cffa1a29cff9e9e9aff9a9a94" +
        "ff9e9e99ff919189ff9e9e98ff9e9e99ff9a9a92ff989890ff9a9a93ffaeaea7" +
        "ff9f9f9aff8e8e86ff92928bff97978eff93938bff8c8c85ff999991ff8c8c84" +
        "ff919189ffa9a9a5ff9f9f9bffacaca4ff95978effacaca7ff9a9a92ff999992" +
        "ff9a9a93ffa1a199ffa5a59fff90908aff92928bff9a9a94ff9c9c98ff8c8c85" +
        "ff898983ffababa6ff91918bff999992ff93938dff9a9a93ff95958dff94958e" +
        "ff8e8e85ff93938bff9e9f98ff9e9e95ffa3a39fff9a9a92ffa1a19aff95958d" +
        "ff9c9c95ff9e9e9aff99998eff919289ffa2a29cff93938cff94948cffa3a39b" +
        "ff9b9b94ff92928aff93938bff989893ff9d9d97ffa6a69fffa2a29bff97978e" +
        "ff9a9a94ffacaca7ff9e9e98ff9c9c95ffa5a59eff92928bffa9a9a5ff9d9d97" +
        "ff9c9c97ffa7a79eff898980ff989890ff93938bff9d9d97ffa7a7a3ffa5a59f" +
        "ff8e8e88ffa8a8a4ffafafa8ffa1a19aff9d9e98ff919189ff909089ff989892" +
        "ff9c9c95ff9b9b94ff999992ffa3a39bff9c9c97ff9c9c95ff9d9e98ff97978d" +
        "ff999992ffa4a49eff808079ff99998eff8c8c85ff9d9d97ff9e9e99ff999992" +
        "ff9a9a92ffa8a8a2ff95958cff8d8d85ffa7a7a2ffb4b4acff9b9b95ff94948c" +
        "ff909089ff98988effa5a5a1ff8c8c85ffa5a5a1ff95958eff95958dff94948c" +
        "ff9d9d97ffa3a39fff9f9f99ff9d9d94ff9d9d98ff95958eff91918aff86867e" +
        "ffa4a59dff9c9c95ff92928bffababa7ff9a9a93ff94948eff9f9f9aff999993" +
        "ff979791ffa8a8a3ffa1a29cff91918aff999991ffa5a59eff9a9a94ff9d9d98" +
        "ff9e9e98ffa3a39cff919189ff8e8e89ff8e8e86ff919189ff9d9d97ff9b9b93" +
        "ff9f9f9affb0b0a8ff9d9d97ffb3b3aeff9e9e98ff989891ff9a9a92ff95958e" +
        "ffababa7ffa5a59fffbfbeb6ff92928bff92928aff979891ff9f9f99ff999992" +
        "ff9a9a94ffababa7ff8c8c85ff9e9e99ff9f9f9affa5a5a1ff8e8e88ffa1a29b" +
        "ff9d9d99ff97978eff93938bff94948effb0b0a8ff919188ff8c8c85ffa7a7a1" +
        "ff989890ff9f9f98ff8e9086ff999991ff999992ff9f9f9bff9a9a93ffa3a39e";

    private static final String LIME_CONCRETE_POWDER =
        "ff6fae26ff77ba29ff82c32bff7cbc28ff77bb29ff85c429ff7aba28ff71b328" +
        "ff7ebf29ff77b928ff7dbd28ff7cbc29ff7cbc28ff84c92dff7fbc28ff7fbf29" +
        "ff7cbd28ff79b928ff86c52bff7cc02bff7ab526ff79b728ff7ab928ff85c32b" +
        "ff88c72cff85c52bff85cc2dff8dc82dff84c329ff79b928ff89c52bff79bd29" +
        "ff76ba29ff85c62cff86ca2cff72b328ff7ebd28ff77b728ff78b928ff7cbc28" +
        "ff82c229ff83c32bff7cbe29ff8bc82cff78b928ff78b628ff71b228ff9bd633" +
        "ff71b228ff73b428ff80bf29ff76b828ff89c42cff7cbd29ff83bf29ff78b928" +
        "ff7cb426ff73b428ffa7d85aff7ebf29ff7ec12bff80c32bff73b628ff7fba26" +
        "ff9fd543ff85c229ff77bb2bff82c029ff7dc12cff90cd2dff89c72bff7cbe2b" +
        "ff71b228ff6eae28ff83c029ff7dc02bff91c932ff83bc28ff7ac02bff89c32d" +
        "ff76b428ff73b628ff74b528ff8dd22dff76b528ff7cbf29ff80c129ff82c12b" +
        "ff80bf29ff7dc22cff76b728ff6eac26ff84c12bff7cb928ff7cbc28ff76b226" +
        "ff71ae25ff7abc29ff71b228ff73b628ff6eab26ff86c52bff79ba28ff77b728" +
        "ff76b528ff7cba28ff82c42cff80c72dff90cb30ff7fc22bff77ba28ff82c129" +
        "ff7abb28ff7abb28ff79bc29ff79ba28ff83c229ff85c22bff79bb29ff85c82c" +
        "ff8cc72dff76b628ff7aba28ff79b928ff74b829ff7dbe29ff80c52cff77ba29" +
        "ff7fc12bff82c22bff7cc02bff80c22bff7ebf29ff82bf29ff84c52cff8bc72c" +
        "ff85c229ff86cc2dff77b326ff8eca2fff7fbe28ff85c429ff7ebe28ff74b628" +
        "ff7cb626ff80c029ff76ba29ff7dbd28ff76b728ff7cbc29ff7ab928ffb5df7a" +
        "ff79b928ff7dbc28ff79ba28ff79bd2bff7aba28ff77b929ff72b428ff7ec22c" +
        "ff7cbd29ff7dbe29ff73ab23ff77bb29ff82be28ff89c92cff80bf29ff82c129" +
        "ff80c42cff80c42cff77bb29ff83c029ff74b628ff86cc2dff7aba28ff80c029" +
        "ff74b428ff7ebf29ff84c42bff6dae28ff7abb28ff7aba28ff89c429ff7ab928" +
        "ff7dbe28ff82c229ff7aba28ff80c62dff89c42cff79b828ff83bf29ff6ba825" +
        "ff82c42cff76b728ff78b728ff7dc12cff72b629ff73b428ff7abc29ff73b026" +
        "ff7ab726ff7ebe28ff73b528ff7ebd28ff8ecb2dff8bc92bff82be28ff77b728" +
        "ff73b629ff86c62bff78b628ff77b126ff79b828ff79b928ff78b728ff8ccd2c" +
        "ff74b829ff7fc52cff7ec12bff76b929ff7abd29ff80bf29ff79bd29ff7fbe28" +
        "ff7abd29ff7cbc28ff8ec72fff8cc62fff78ba29ff84c229ff79ba28ff7ebe29" +
        "ff80bc26ff74b729ff7fb826ff80bf29ff7fc12bff6faf28ff6fb028ff83c32b" +
        "ff72b328ff7ebf29ff77b728ff6ca926ff8bcf2dff7abc28ff83be28ff88c72c" +
        "ff7abb28ff82c42cff78b728ff7dbe28ff7abb29ff91ca32ff74b528ff6faf26";

    private static final String MAGENTA_CONCRETE_POWDER =
        "ffb248aaffc352baffba48b1ffc65bbeffc759bfffc155b9ffb649aeffc153b8" +
        "ffc151b8ffc960c3ffc457bbffcd64c5ffc75dc0ffcd56c4ffbd55b6ffd473cf" +
        "ffc659beffb849b0ffbb49b2ffc453bbffaf45a7ffb74aafffb346abffb445ab" +
        "ffc452bbffd067caffce56c5ffb949b1ffc457bcffbc50b4ffbb4cb2ffbd49b4" +
        "ffc456bcffc756c0ffcf5ec8ffc155b7ffbe51b5ffc55bbcffba4cb1ffcb63c4" +
        "ffcb63c4ffbb49b2ffca5ec3ffc04db7ffbd4eb4ffbc51b3ffbb4eb2ffd15fc8" +
        "ffb549aeffc55dbeffb646aeffbf50b6ffb649aeffba49b1ffbc51b5ffbe4db4" +
        "ffb857b2ffb345abffb748afffc455bbffc351baffc351b9ffcf6bcaffbf5bb8" +
        "ffbe4db5ffc055b8ffc250b8ffbf51b5ffce60c5ffc452baffc757beffc756be" +
        "ffba4db1ffba50b2ffb446acffc85ac1ffb848afffb046a7ffc14db8ffb145a8" +
        "ffc45dbdffc659beffb346abffd862d0ffc75fbeffc04db7ffbf50b7ffc253ba" +
        "ffc760c1ffc24eb9ffb746aeffb048a9ffbb51b4ffb84db1ffb849b0ffb54dac" +
        "ffab43a4ffc75abeffb64aafffc255b9ffb049a8ffc85ec0ffd16eccffb84aaf" +
        "ffbf52b6ffc55bbcffbf4cb6ffdf73d8ffc75dbfffc04db7ffbd4eb4ffbf51b6" +
        "ffc357bbffd97dd8ffc453bbffc45abbffb94ab1ffc55ebeffca5ec3ffc752be" +
        "ffbe4eb5ffbc51b5ffc357baffb74aafffbb4cb2ffc95dc1ffd769d0ffc556be" +
        "ffbc4cb3ffdb7ad7ffd468cdffca5dc3ffd46eceffb546abffc452bbffb948b1" +
        "ffbc50b4ffdc6dd4ffaf45a7ffba4ab2ffc75fc0ffc052b7ffc659beffd574d1" +
        "ffbb55b4ffc255b8ffc95dc3ffbd50b4ffb646aeffc457bcffc156b8ffc45bbb" +
        "ffbb4cb3ffbb4cb2ffc65bbfffc14eb9ffc459bdffbb49b2ffbd51b5ffc351ba" +
        "ffc051b7ffd46eceffaa4aa4ffc04eb8ffb951b2ffc04cb6ffba4db2ffb746ae" +
        "ffce5fc6ffc756bfffcf65c8ffc760c0ffbb4ab2ffcb51c3ffc55bbcffc55abe" +
        "ffbc51b4ffc757beffca5dc2ffb64dafffbf50b6ffcb65c4ffbb4cb2ffc55bbc" +
        "ffc85dc0ffbd4db4ffbb4cb1ffd768d0ffbb50b4ffc259b9ffb649afffb14caa" +
        "ffd267cbffb949b0ffb648acffcc5bc4ffc252b9ffc45bbbffbb49b2ffa941a2" +
        "ffb54caeffc457bcffbc50b4ffb749aeffc455bbffc453bbffc25abaffc760c1" +
        "ffba49b2ffbb46b2ffc057b8ffa943a1ffbf55b8ffb74aaeffbe53b6ffc753bf" +
        "ffbb49b2ffce5dc6ffc351b9ffbe4eb5ffbf4eb6ffc55bbcffbf4cb5ffb648ae" +
        "ffc353bbffb746aeffb84aafffb245aaffc75bbfffc053b6ffc75ec1ffca5fc3" +
        "ffb246aaffb846afffb855b0ffc359bbffc14eb7ffb950b2ffb84aafffc455bb" +
        "ffc55bbcffc75abfffb749afffc05db9ffd867d0ffbf50b7ffc663c0ffc757c1" +
        "ffc457bbffd062c8ffb84cb1ffc85ec1ffd776d3ffc459bcffbd51b5ffb045a8";

    private static final String MYCELIUM_SIDE =
        "ff7b6d73ff7b6d73ff6a5d62ff5a5952ff6a656aff6a656aff736162ff83696a" +
        "ff5a5952ff6a5d62ff5a5952ff83696aff83696aff6a5d62ff736162ff6a656a" +
        "ff736162ff6a5d62ff6a5d62ff6a656aff7b6d73ff7b6d73ff736162ff736162" +
        "ff6a5d62ff736162ff6a656aff736162ff736162ff736162ff2b1d13ff2b1d13" +
        "ff5a5952ff6a656aff6a5d62ff7b6d73ff8b7173ff6a656aff6a656aff6a5d62" +
        "ff736162ff83696aff6a656aff3a291cff736162ff83696aff59402cff6a5d62" +
        "ff6a656aff5a5952ff5a5952ff6a5d62ff6a656aff6a5d62ff6a5d62ff6a5d62" +
        "ff736162ff483423ff3a291cff6a656aff736162ff6a5d62ff736162ff736162" +
        "ff7b6d73ff6a656aff483423ff59402cff83696aff483423ff3a291cff6a5d62" +
        "ff6a656aff2b1d13ff8b7173ff7b6d73ff6a656aff6a5d62ff2b1d13ff3a291c" +
        "ff3a291cff6a5d62ff6a5d62ff483423ff5a5952ff483423ff2b1d13ff6a5d62" +
        "ff5a5952ff83696aff6a5d62ff2b1d13ff3a291cff3a291cff3a291cff59402c" +
        "ff59402cff2b1d13ff6a5d62ff6a656aff736162ff7b6d73ff6a5d62ff59402c" +
        "ff59402cff3a291cff6a656aff59402cff79553aff483423ff3a291cff966c4a" +
        "ff59402cff3a291cff59402cff6a656aff7b6d73ff483423ff83696aff3a291c" +
        "ff483423ff593d29ff6a656aff483423ff79553aff3a291cff483423ff966c4a" +
        "ff966c4aff3a291cff79553aff966c4aff736162ff966c4aff3a291cff593d29" +
        "ff3a291cff6a5d62ff483423ff79553aff79553aff79553aff2b1d13ff79553a" +
        "ff79553aff966c4aff593d29ff79553aff6a5d62ff593d29ff593d29ff79553a" +
        "ff3a291cff79553aff79553aff79553affb9855cffb9855cff3a291cff966c4a" +
        "ff79553aff966c4aff79553affb9855cff59402cff79553affb9855cff966c4a" +
        "ff593d29ffb9855cffb9855cff593d29ff966c4aff966c4aff6a5d62ff79553a" +
        "ff966c4aff79553aff79553aff966c4aff966c4affb9855cff79553aff966c4a" +
        "ff6c6c6cff966c4aff966c4aff79553aff593d29ff966c4aff79553aff593d29" +
        "ff79553aff593d29ff966c4aff79553aff966c4aff966c4affb9855cff79553a" +
        "ff79553aff79553aff79553aff79553aff79553aff79553affb9855cffb9855c" +
        "ff79553aff966c4aff79553aff79553aff745844ff79553aff966c4aff966c4a" +
        "ff79553aff593d29ffb9855cff593d29ff79553affb9855cff966c4aff966c4a" +
        "ff966c4aff79553aff593d29ffb9855cff79553aff593d29ff79553aff593d29" +
        "ffb9855cffb9855cff79553aff966c4aff79553aff79553aff966c4aff966c4a" +
        "ff966c4aff79553affb9855cff966c4aff966c4aff79553aff878787ff79553a" +
        "ff966c4aff966c4aff79553aff79553aff966c4aff966c4aff79553aff593d29";

    private static final String MYCELIUM_TOP =
        "ff7b6d73ff7b6d73ff6a5d62ff5a5952ff6a656aff6a656aff736162ff83696a" +
        "ff5a5952ff6a5d62ff5a5952ff83696aff83696aff6a5d62ff736162ff6a656a" +
        "ff736162ff6a5d62ff6a5d62ff6a656aff7b6d73ff7b6d73ff736162ff736162" +
        "ff6a5d62ff736162ff6a656aff736162ff736162ff736162ff83696aff83696a" +
        "ff5a5952ff6a656aff6a5d62ff7b6d73ff8b7173ff6a656aff6a656aff6a5d62" +
        "ff736162ff83696aff6a656aff6a5d62ff736162ff83696aff736162ff6a5d62" +
        "ff6a656aff5a5952ff5a5952ff6a5d62ff6a656aff6a5d62ff6a5d62ff6a5d62" +
        "ff736162ff6a656aff7b6d73ff6a656aff736162ff6a5d62ff736162ff736162" +
        "ff7b6d73ff6a656aff6a5d62ff5a5952ff83696aff736162ff5a5952ff6a5d62" +
        "ff6a656aff7b6d73ff8b7173ff7b6d73ff6a656aff6a5d62ff6a656aff8b7173" +
        "ff6a656aff6a5d62ff6a5d62ff736162ff5a5952ff6a656aff736162ff6a5d62" +
        "ff5a5952ff83696aff6a5d62ff736162ff7b6d73ff6a656aff7b6d73ff7b6d73" +
        "ff6a5d62ff736162ff6a5d62ff6a656aff736162ff7b6d73ff6a5d62ff83696a" +
        "ff83696aff736162ff6a656aff7b6d73ff6a5d62ff6a656aff5a5952ff6a5d62" +
        "ff6a656aff6a5d62ff6a656aff6a656aff7b6d73ff736162ff83696aff736162" +
        "ff8b7173ff83696aff6a656aff8b7173ff6a656aff5a5952ff6a5d62ff736162" +
        "ff736162ff7b6d73ff6a656aff6a5d62ff736162ff6a5d62ff736162ff6a5d62" +
        "ff83696aff6a5d62ff6a656aff6a656aff7b6d73ff6a656aff5a5952ff6a5d62" +
        "ff6a5d62ff6a5d62ff6a5d62ff736162ff6a5d62ff6a656aff6a656aff7b6d73" +
        "ff736162ff5a5952ff6a5d62ff6a656aff6a5d62ff736162ff83696aff6a656a" +
        "ff736162ff6a5d62ff6a656aff8b7173ff7b6d73ff6a656aff7b6d73ff6a656a" +
        "ff6a5d62ff5a5952ff6a5d62ff5a5952ff736162ff6a656aff6a5d62ff6a5d62" +
        "ff6a656aff5a5952ff736162ff7b6d73ff6a656aff6a656aff6a5d62ff6a5d62" +
        "ff5a5952ff736162ff6a5d62ff736162ff83696aff6a5d62ff6a656aff736162" +
        "ff6a5d62ff736162ff83696aff83696aff736162ff736162ff6a5d62ff736162" +
        "ff6a656aff7b6d73ff6a656aff6a5d62ff736162ff83696aff6a656aff5a5952" +
        "ff6a5d62ff6a5d62ff736162ff6a5d62ff6a656aff7b6d73ff736162ff6a656a" +
        "ff7b6d73ff7b6d73ff7b6d73ff6a656aff736162ff736162ff736162ff6a5d62" +
        "ff6a5d62ff6a656aff5a5952ff5a5952ff736162ff6a656aff6a5d62ff736162" +
        "ff8b7173ff7b6d73ff6a656aff736162ff6a5d62ff6a656aff5a5952ff6a5d62" +
        "ff6a656aff7b6d73ff6a656aff6a5d62ff6a656aff5a5952ff6a5d62ff6a5d62" +
        "ff6a656aff7b6d73ff736162ff83696aff736162ff5a5952ff6a5d62ff736162";

    private static final String NETHERRACK =
        "ff511515ff572121ff511515ff501b1bff652828ff501b1bff723232ff723232" +
        "ff652828ff652828ff411616ff652828ff652828ff723232ff652828ff572121" +
        "ff572121ff652828ff652828ff572121ff501b1bff411616ff501b1bff652828" +
        "ff723232ff501b1bff511515ff411616ff652828ff652828ff501b1bff511515" +
        "ff723232ff652828ff723232ff652828ff572121ff411616ff511515ff501b1b" +
        "ff652828ff572121ff501b1bff511515ff652828ff501b1bff652828ff723232" +
        "ff854242ff723232ff652828ff723232ff652828ff501b1bff572121ff723232" +
        "ff652828ff723232ff723232ff572121ff511515ff652828ff501b1bff652828" +
        "ff723232ff854242ff723232ff652828ff501b1bff652828ff723232ff854242" +
        "ff854242ff652828ff723232ff723232ff501b1bff723232ff652828ff572121" +
        "ff511515ff723232ff652828ff572121ff511515ff723232ff854242ff854242" +
        "ff723232ff723232ff652828ff501b1bff652828ff652828ff723232ff652828" +
        "ff511515ff652828ff723232ff501b1bff411616ff723232ff652828ff723232" +
        "ff723232ff652828ff501b1bff511515ff652828ff723232ff854242ff723232" +
        "ff723232ff572121ff511515ff411616ff501b1bff652828ff723232ff652828" +
        "ff511515ff511515ff723232ff652828ff501b1bff652828ff854242ff854242" +
        "ff652828ff501b1bff652828ff723232ff652828ff501b1bff501b1bff511515" +
        "ff652828ff652828ff723232ff723232ff652828ff511515ff652828ff723232" +
        "ff501b1bff652828ff723232ff854242ff723232ff652828ff411616ff501b1b" +
        "ff652828ff723232ff652828ff723232ff723232ff511515ff511515ff501b1b" +
        "ff572121ff723232ff652828ff723232ff723232ff652828ff411616ff501b1b" +
        "ff723232ff854242ff723232ff723232ff501b1bff411616ff411616ff501b1b" +
        "ff652828ff501b1bff501b1bff723232ff652828ff501b1bff511515ff411616" +
        "ff501b1bff723232ff652828ff511515ff411616ff501b1bff572121ff501b1b" +
        "ff511515ff652828ff723232ff652828ff501b1bff723232ff652828ff511515" +
        "ff501b1bff501b1bff511515ff723232ff652828ff723232ff652828ff511515" +
        "ff572121ff723232ff652828ff723232ff723232ff854242ff723232ff511515" +
        "ff501b1bff501b1bff572121ff652828ff723232ff854242ff723232ff511515" +
        "ff501b1bff652828ff723232ff723232ff854242ff652828ff501b1bff572121" +
        "ff501b1bff411616ff652828ff723232ff652828ff723232ff652828ff652828" +
        "ff501b1bff501b1bff652828ff723232ff723232ff501b1bff652828ff723232" +
        "ff572121ff411616ff501b1bff652828ff723232ff723232ff652828ff652828";

    private static final String ORANGE_CONCRETE_POWDER =
        "ffe2892cffe47f19ffe27d16ffe68925ffe37e15ffde7913ffe48928ffe48422" +
        "ffe6851fffe17d18ffdd7913ffe68623ffe4821efff18e22ffd97818ffe68928" +
        "ffe4801cffde7a15ffea8e29ffe7841effd8791effe28323ffe28425ffdf7e1c" +
        "ffed9029ffeb8d29fff18b1bffde7913ffebb34dffdf7d19ffebae49ffea8923" +
        "ffe37e18ffef942dffef8e22ffe48423ffdf7d19ffe07f19ffeab34effe88c29" +
        "ffde7913ffe17912ffe88621ffe47f18ffe9902fffe59133ffdc7915ffec881b" +
        "ffdd7d1bffdd7915ffdc7812ffe88b28ffd8740fffe88a28ffe7af4cffe17d18" +
        "ffce6f16ffdf7e1bffdd7812ffe37e18ffe8841fffe7831effe27f1cffdd8328" +
        "ffed942fffe68d2dffe7841cffe38422ffee9530ffe58019ffe37e15ffeb8b26" +
        "ffda7612ffe08222ffe48928ffe58019ffe58525ffd57213ffea861fffe08426" +
        "ffe3892cffe27e19ffd87612fff79b2bffe79536ffe47f16ffe27e19ffea912d" +
        "ffe27f1cffea861fffdf7a15ffd47210ffdf7f1effdc7a18ffe99230ffd57315" +
        "ffdc8429ffe4801cffe28426ffe78a28ffd57416ffe78623ffde7a16ffdb7813" +
        "ffe58928ffe48525ffea8822ffeb8012ffea9734ffe47f16ffe4841fffdd7712" +
        "ffdf7a15ffe89130ffe8841fffdb740effe58422ffd8740fffea8a25ffe98215" +
        "ffe3821cffda7813ffdd7a16ffe18021ffdf7912ffe5831efff1942bffe37d13" +
        "ffe27e18ffe68521fff19c36ffe98622ffe4801bffdc7915ffea8822ffe5831e" +
        "ffdc7813ffed8312ffdf8529ffde7913ffdb760fffe88a26ffe58421ffe27f1b" +
        "ffda7c1fffe48423ffeb8d29ffe48422ffdf7d19ffe37f1bffdd7c19ffdc7a18" +
        "ffe17e19ffe4831fffebb44effe57e13ffe68926ffe17a12ffe38221ffe88621" +
        "ffe27f1bffe17c15ffc96c13ffe58219ffda791bffed8e28ffe2801effe4841f" +
        "ffed8b23ffe78018ffe47e16ffdc7915ffe99230fff69929ffe48826ffe17f1b" +
        "ffda7715ffe88521ffe98a25ffdb7918ffe88c29ffe48625ffe27f1cffe48423" +
        "ffe78825ffe78925ffeaab48fff3982cffe48928ffde7c19ffdf7f1fffdc8328" +
        "ffe8831bffe07c16ffdd7c19ffe68018ffe88b28ffe68d2dffeb912dffda7f23" +
        "ffe08529ffe68825ffe48523ffd87610ffe17912ffed922cffdc7918ffe9b14d" +
        "ffde7710ffe7831effdd7d1bffcd6d12ffe08021ffe18222ffe79132ffee8e25" +
        "ffea8d29ffeb881cffec8e29ffe07c15ffe47f19ffdf7e19ffec8e29ffe17f1b" +
        "ffe7851fffe78a28ffe28221ffde7d1cffe4821effde7a16ffdf7a15ffe17a13" +
        "ffe28629ffe98c29ffd87c1fffe07f1bffe6821cffe49334ffdf7e1effe37f19" +
        "ffe58826ffe47e18ffe9b14dffe08c2ffff08a19ffe4841fffde8022ffe78219" +
        "ffe58625ffeb881effdc7a18ffe07a15ffe78825ffe88b29ffde7c16ffd57312";

    private static final String PINK_CONCRETE_POWDER =
        "ffdf92acffe695b3ffe693b3ffe390aeffe590b0ffdd85a8ffe395b0ffe18dab" +
        "ffeba4bdffe594b1ffe494b1ffecc1d1ffdd85a8ffed94b8ffe3a4baffe393b0" +
        "ffe594b2ffe69bb5ffe490b0ffeeaac3ffe0a2b8ffdd8aa8ffe69fb7ffe499b2" +
        "ffeb9bbaffe692b2fff29dc0ffe89cb6ffe18bacffd87ea3ffe594b2ffeeb1c7" +
        "ffedaac2fff1cbdbffee9abbffd87ea2ffe292b0ffe8a2baffe491b0ffeaa6bd" +
        "ffeab8caffe695b3ffeea7c1ffeda2bdffe9bcceffd884a4ffe59ab4fff4b8ce" +
        "ffe295b0ffe79eb7ffdc84a6ffe492b0ffe395b1ffebacc4ffdc89a8ffe799b5" +
        "ffd892a9ffe18dabffe18cacffe89bb7ffe692b2ffeeabc3ffe595b2ffdc8ea8" +
        "ffeba3bcffe290acffeb9dbaffe290aeffea99b7ffeda5bfffe794b4ffefaec5" +
        "ffe6a3baffdb86a6ffe6a6bcffe794b3ffdc85a7ffdf97afffe185acffd986a5" +
        "ffe4a5bbffe593b1ffe5a3bafff7abc9ffe291aeffeeabc4ffebc4d4ffe89bb6" +
        "ffdb85a6fff1afc6ffeaa6beffce7699ffe5a1b8ffe6abc1ffe69ab5ffdd92ab" +
        "ffcd7799ffdc83a8ffdf8eaaffeaa9bfffd37f9fffde84a8ffdc85a7ffdf8ca9" +
        "ffdc85a7ffe08caaffefb4c9fff6b6ceffe18cabffeeabc3ffe99fb8ffdb82a6" +
        "ffdd86a8ffdf8aa9ffea9cb8ffe89fb8ffe79bb6ffd87fa3ffeda9c2fff4cddc" +
        "ffebb3c7ffe6a6bcffe08caaffd982a4ffe594b2ffe693b2fff3b2cbffe691b1" +
        "ffeba4bdffe088aafff0aac4ffe897b5ffecbdceffe291aeffefaec5ffedc5d4" +
        "ffdf8baafff7d1e0ffdd92abffe89bb6ffe08dacffdf89aaffe28daeffdf89a9" +
        "ffdf9cb3ffe69cb5ffe288aaffe597b3ffe08caaffe18babffe6a3baffdf8daa" +
        "ffe598b4ffdf88a9ffdf86a9ffefa5c1ffe8a1b9ffe48eafffe8a1b9ffeda2bd" +
        "ffe593b2ffea9eb9ffc97897ffeca2bcffde91abffe388aeffe8a6bcffe490af" +
        "ffe489aeffe891b3ffe48eafffe494b0ffeaabc2fff6a7c5ffe79eb7ffe595b1" +
        "ffe499b3ffeda5bfffedbfd0ffde8ea9ffebabc3ffe8a2baffebc4d3ffe292b0" +
        "ffdf89aaffe89eb8ffe597b2fff09ebeffe6a8beffe6aabfffdf8daaffdd9db3" +
        "ffe48bafffe594b1ffe08eabfff0afc6ffe28cacffe69eb7ffea9db8ffdc93aa" +
        "ffe29fb7ffeaa6beffe9a5bcffdf8daaffecbcceffeeacc4ffe192aeffe8afc3" +
        "ffdf85a9ffeeabc3ffda85a5ffd78ba5ffdc88a7ffe395b0ffe599b2fff1a5c1" +
        "ffe088aaffea93b6ffeebed0ffe695b3ffdd83a8ffd87fa3ffe895b5ffda82a5" +
        "ffeda9c1ffeaaac2ffe291aeffe49cb5ffdf85a8ffe8a5bbffdf88aaffe99cb8" +
        "ffe094aeffe28babffdd9fb4ffe69cb6ffeeb0c6ffe4abc0ffe6b2c5ffe899b5" +
        "ffdd88a8ffeaa1baffe393afffdc90a9ffed93b8ffdd85a8ffdf93acfff1c3d4" +
        "ffeaa3bbffefa2bfffe190acffebbeceffe799b5ffdc83a8ffe79eb7ffd580a1";

    private static final String PODZOL_SIDE =
        "ff4f351cff44311cff67431eff472c1bff68471eff67471eff462f1bff492e1c" +
        "ff472f1bff75481fff75481fffad6624ff63481eff545812ff42371cff4e3b1c" +
        "ff91683eff553e1cff7c541fff63411eff54361cff4e341cff54361cff4a2e1c" +
        "ff4e341cff4e341cff75481fff4f3a1cff503e1cff4e5412ff4d3e1cff48391b" +
        "ff543d1cff694a1fff694a1fff59421dff44311cff4d341cff59391cff442e1c" +
        "ff8b5720ff6f491fff4c3a1cff61491eff42371cff4d3e1cff505511ff462f1b" +
        "ff573b1cff503b1cffb9855cff58421dff46331bff64411eff79553aff65411e" +
        "ff966c4aff694a1fff4d3a1cff61491eff4d3e1cff48391bff966c4aff70471e" +
        "ff966c4aff79553aff966c4aff473c1bff5d491eff966c4aff533a1cff442e1c" +
        "ff442e1cff593d29ff90683cff6c6c6cff79553aff70471eff5e3d1dff79553a" +
        "ff79553aff593d29ff966c4aff966c4aff79553aff966c4aff593d29ff593d29" +
        "ff593d29ff79553aff79553aff593d29ff79553aff79553aff79553affb9855c" +
        "ffb9855cff79553aff79553aff79553aff878787ff79553aff79553affb9855c" +
        "ffb9855cff79553affb9855cffb9855cff79553aff966c4aff79553aff966c4a" +
        "ff79553aff79553affb9855cffb9855cff966c4aff966c4aff79553aff79553a" +
        "ff966c4aff593d29ff966c4aff966c4aff79553aff79553aff966c4aff966c4a" +
        "ff966c4aff79553aff79553aff966c4aff79553aff966c4aff79553aff593d29" +
        "ff79553aff966c4aff966c4aff79553aff79553aff79553aff593d29ff79553a" +
        "ff79553aff966c4aff593d29ff79553aff79553aff593d29ff593d29ff79553a" +
        "ff79553aff79553aff79553aff79553affb9855cffb9855cff79553aff966c4a" +
        "ff79553aff966c4aff79553affb9855cffb9855cff79553affb9855cff966c4a" +
        "ff593d29ffb9855cffb9855cff593d29ff966c4aff966c4aff878787ff79553a" +
        "ff966c4aff79553aff79553aff966c4aff966c4affb9855cff79553aff966c4a" +
        "ff6c6c6cff966c4aff966c4aff79553aff593d29ff966c4aff79553aff593d29" +
        "ff79553aff593d29ff966c4aff79553aff966c4aff966c4affb9855cff79553a" +
        "ff79553aff79553aff79553aff79553aff79553aff79553affb9855cffb9855c" +
        "ff79553aff966c4aff79553aff79553aff745844ff79553aff966c4aff966c4a" +
        "ff79553aff593d29ffb9855cff593d29ff79553affb9855cff966c4aff966c4a" +
        "ff966c4aff79553aff593d29ffb9855cff79553aff593d29ff79553aff593d29" +
        "ffb9855cffb9855cff79553aff966c4aff79553aff79553aff966c4aff966c4a" +
        "ff966c4aff79553affb9855cff966c4aff966c4aff79553aff878787ff79553a" +
        "ff966c4aff966c4aff79553aff79553aff966c4aff966c4aff79553aff593d29";

    private static final String PODZOL_TOP =
        "ff6a4418ff523c18ff523c18ff524c10ff523c18ff523c18ff523c18ff4a3018" +
        "ff4a3018ff6a4418ff6a4418ff523c18ff4a3018ff6a4418ff4a3018ff523c18" +
        "ff523c18ff523c18ff523c18ff8b5920ff523c18ff524c10ff4a3018ff4a3018" +
        "ff523c18ff6a4418ff523c18ff4a3018ff6a4418ff4a3018ff523c18ff6a4418" +
        "ff523c18ff6a4418ff523c18ff6a4418ff6a4418ff524c10ff523c18ff4a3018" +
        "ff6a4418ff523c18ff523c18ff524c10ff524c10ff523c18ffac6520ff523c18" +
        "ff523c18ff523c18ff6a4418ff6a4418ff524c10ff6a4418ff523c18ff6a4418" +
        "ff6a4418ff8b5920ff4a3018ff4a3018ff6a4418ff523c18ff8b5920ff4a3018" +
        "ff4a3018ff6a4418ff8b5920ff523c18ff523c18ff4a3018ff523c18ff6a4418" +
        "ffac6520ff6a4418ff523c18ff6a4418ff523c18ff4a3018ff4a3018ff6a4418" +
        "ff4a3018ff8b5920ff523c18ff4a3018ff4a3018ff523c18ff6a4418ff523c18" +
        "ff8b5920ff6a4418ff6a4418ff6a4418ff4a3018ff4a3018ff523c18ff6a4418" +
        "ff6a4418ff4a3018ff523c18ff6a4418ff6a4418ff6a4418ff524c10ff524c10" +
        "ff6a4418ff6a4418ff4a3018ff523c18ff6a4418ff523c18ff523c18ff523c18" +
        "ff523c18ff523c18ff6a4418ff6a4418ff523c18ff524c10ff523c18ff523c18" +
        "ff6a4418ff523c18ff4a3018ff4a3018ff523c18ff523c18ff523c18ff523c18" +
        "ff523c18ff6a4418ff4a3018ff523c18ff6a4418ff523c18ff523c18ff6a4418" +
        "ff4a3018ff4a3018ff523c18ff523c18ff4a3018ff6a4418ff6a4418ff6a4418" +
        "ff6a4418ff4a3018ff6a4418ff4a3018ff6a4418ff4a3018ff6a4418ff523c18" +
        "ff523c18ff6a4418ff4a3018ff8b5920ff524c10ff524c10ff523c18ff6a4418" +
        "ff8b5920ff8b5920ff6a4418ff6a4418ff524c10ff4a3018ff523c18ff6a4418" +
        "ff6a4418ff523c18ff6a4418ff523c18ff523c18ff524c10ff523c18ff523c18" +
        "ff523c18ff6a4418ff8b5920ff523c18ff4a3018ff4a3018ff4a3018ff523c18" +
        "ff6a4418ff6a4418ff523c18ff524c10ff4a3018ff523c18ff524c10ff6a4418" +
        "ff523c18ff523c18ff4a3018ff4a3018ff4a3018ff4a3018ff6a4418ffac6520" +
        "ff8b5920ff6a4418ff523c18ff524c10ff523c18ff523c18ff4a3018ff523c18" +
        "ff523c18ff4a3018ff4a3018ff4a3018ff523c18ff523c18ff8b5920ff524c10" +
        "ff523c18ff4a3018ff8b5920ff523c18ff8b5920ff6a4418ff523c18ff523c18" +
        "ff523c18ffac6520ff8b5920ff4a3018ff4a3018ff524c10ff6a4418ff523c18" +
        "ff4a3018ff523c18ff6a4418ff6a4418ffac6520ff523c18ff4a3018ff4a3018" +
        "ff8b5920ff523c18ff4a3018ff4a3018ff523c18ff523c18ff524c10ff523c18" +
        "ff523c18ff6a4418ff523c18ff4a3018ff4a3018ff523c18ff523c18ff6a4418";

    private static final String PURPLE_CONCRETE_POWDER =
        "ff7e36a9ff8437b3ff8a3ab7ff7e34aeff8236b2ff8437b2ff8036acff8036af" +
        "ff8036b1ff8437b2ff8b3db5ff7e34afff8839b3ff8d3bbfff7930a6ff7f34af" +
        "ff8237b0ff8539b2ff8a3ab7ff8437b5ff7832a4ff7832a8ff7932a8ff893ab3" +
        "ff8839b7ff8337b4ff8b3abfff8337b2ff8539b3ff7d33acff8337b1ff8437b5" +
        "ff8337b3ff8036b4ff8437b7ff7e33abff863ab2ff8034aeff8d3eb8ff8437b1" +
        "ff8036b0ff8a3ab7ff903ebbff8c3dbaff8337b1ff7932a8ff7a32a9ff8b3bbc" +
        "ff9343baff9342bbff7c33acff7c33acff7c33a9ff8439b2ff893bb3ff7e34af" +
        "ff6f2d9bff8d3db5ff7f34afff7a33aeff8337b4ff883ab6ff7d33acff7430a3" +
        "ff883ab5ff7930a9ff7f34b2ff8c3db4ff7e34b1ff8d3dbaff9a45c2ff8537b5" +
        "ff7a32a9ff8036acff7630a7ff8337b4ff8237b0ff7c33a6ff8c3bbbff8c3db4" +
        "ff8034acff9241bcff7a33a8ff8e3bc3ff7932a9ff883ab7ff8639b4ff8e3eb9" +
        "ff7930aaff9742c1ff7f36afff7a33a7ff7e34aaff883ab2ff8036afff7933a5" +
        "ff7330a1ff9945c1ff7933a8ff8036afff7932a5ff8b3ab7ff8439b1ff7830a8" +
        "ff7930aaff7830a9ff7e33b1ff8b3abdff8537b2ff7d33afff7932abff7f34af" +
        "ff762fa8ff863ab2ff9742bfff8539b2ff883ab4ff7832a8ff9542bfff8c3bbc" +
        "ff8036b0ff883ab2ff8e3eb6ff893ab3ff9946c1ff8d3bb8ff883abaff8539b5" +
        "ff8036b1ff8d3db8ff7e34b2ff8539b5ff7e36afff8337afff893ab7ff7d34ae" +
        "ff7d32abff8a3abdff7c34a7ff893ab4ff7a32aaff8639b4ff8236b0ff7f34af" +
        "ff8a3eb3ff8437b1ff883ab6ff903fbaff7c33acff8439b2ff8236aeff7c33aa" +
        "ff8036b0ff7d34aeff8036b0ff8a3ab9ff8036afff9442beff893bb4ff8639b6" +
        "ff8639b3ff7f36b0ff7d36a3ff7d33b0ff742fa3ff8939b7ff8d3db7ff7e34ae" +
        "ff8036b4ff8237b5ff8036b2ff7a32a9ff7d33abffa248cdff893ab3ff7e34ae" +
        "ff7f34abff9341bdff8a3bb8ff7830a6ff8e3eb8ff893ab3ff9a48c2ff8236af" +
        "ff7c33acff9441bcff8c3db7ff8537b9ff8436afff8e3fb7ff903fb7ff8037a8" +
        "ff8c3bbaff8839b4ff8537b0ff8637b6ff8839b4ff8436afff8539b4ff8339ab" +
        "ff722fa2ff7c33acff8337b0ff8236aeff7a33acff893bb7ff7c33aaff8e3eb6" +
        "ff7c33acff7c33afff893bb3ff722f9eff9545bcff913fb8ff7730a8ffa149c9" +
        "ff8d3bb8ff8839b8ff883ab6ff8237b1ff8237b2ff8539b1ff863ab6ff8c3db5" +
        "ff7f36b1ff7932abff8034aeff7a33a9ff8b3bb7ff893ab4ff8437b2ff903eba" +
        "ff7630a4ff913fbbff7f36a7ff8e3eb6ff8e3dbaff7a32a8ff8236aeff8036b1" +
        "ff883ab3ff8e3dbaff7d33abff742fa2ff903bc1ff7d33aeff8236acff9441c0" +
        "ff903fbaff8437b6ff8539b0ff8537b2ff8c3bb7ff8337b1ff7a32aaff7c33a7";

    private static final String RED_CONCRETE_POWDER =
        "ffa53730ffb23a36ffa93633ffa43332ffa63433ff9f3230ffa23330ffa63432" +
        "ffac3733ffb43d36ffa43332ffa13232ffac3733ffca4a3fffa43630ff9d3030" +
        "ffa43432ffa53432ffbb4139ffaf3a34ff98302fffa33330ffa53430ff9d302f" +
        "ffbc4237ffa53332ffba3d39ffb23a34ffb33b34ffae3933ffab3732ffa43332" +
        "ffa53433ffb53b36ffaa3634ffaa3732ff9f3230ffcc5b53ff9e3230ffa43332" +
        "ffb23a34ffa73433ffab3733ffa83633ffc8554dff9d322fffa93732ffac3734" +
        "ff982f2fffa13230ffa43332ffaa3632ffa43430ffac3733ffaa3732ffa13232" +
        "ff95302cffa93730ffb23b34ffa93633ffb53b36ffa43332ffa43332ff98302d" +
        "ffa93733ffb03a33ffb53b36ffa23330ffac3733ffaa3633ffa83633ffb23a34" +
        "ffa13330ffac3a33ffa13330ffaa3633ffa43332ffb03d36ffb83d37ff9c3230" +
        "ffa53630ffa93632ffa83732ffb63a37ffb03b34ffaa3633ffa53432ffa63432" +
        "ffa13230ffa73433ffa43332ff9a302fff9d322fffa13330ffac3933ff99302d" +
        "ff97302fffa53433ffaf3a34ffa63432ffa43630ffa63433ffa23232ff9f3230" +
        "ffa43430ffa13230ffb33b36ffae3634ffa53432ffaa3633ffb53d36ffa23330" +
        "ffa53432ffac3933ffac3733ffa43332ffa73432ffa93732ffb03a34ffb63b37" +
        "ffa43332ffa13230ffa43430ffa33430ffb43b36ffa53433ffac3734ffa63432" +
        "ffa93633ffb13936ffac3634ffa63432ffae3934ffa43430ffb53d36ffb53b36" +
        "ffaa3730ffb13937ffa33630ffa93632ffa73630ffa43332ffb53b36ffa53332" +
        "ff97302dffa33332ffa83633ffa93632ffa63432ffab3732ffa43430ffa13230" +
        "ffa63432ffa73432ffa63432ffb23934ffa23330ffba4139ffa23232ffaa3633" +
        "ffb33a34ffa73433ff912f2cffae3934ffa3362fffb03936ffaf3a33ffac3733" +
        "ffac3734ffa83633ffce5750ffa23330ffb63e37ffbf3f39ffa93730ffa23330" +
        "ff9a302fffae3934ffb33a36ffa13330ffab3632ffa53430ffa13230ffa53430" +
        "ffa63432ffab3732ffa43332ffb03936ffa53630ffa43430ffa13330ff952f2f" +
        "ffac3634ffa43332ffa93730ffaa3434ffa63432ffa33430ffab3633ff9b322f" +
        "ff9e322fffaf3a34ffa13232ff9e3230ffa83433ffa63432ff9d322fffa33330" +
        "ffa93633ffae3934ff982f2fff952f2dff9c302fffa13230ff9f3230ffbd3f39" +
        "ffa53433ffb53b37ffb93e37ffaf3a34ffa53433ffb63f37ffab3733ffa63430" +
        "ffa23332ffae3934ff9d3030ff9f3330ffb33a34ffa63430ffa63432ffa53433" +
        "ffa73730ffb93f39ffa2342fffa73630ffb23a34ffa33330ff9d3230ffa93633" +
        "ffa73630ffa53433ffa33330ff97302dffb33a39ffb73e37ff9b302fffa73433" +
        "ffa43332ffac3734ffa93732ff9f3232ffa13232ffaa3732ffab3932ffaa3932";

    private static final String RED_SAND =
        "ffd2752bffbf6721ffbf6721ffcb6e24ffbf6721ffb2601fffcb6e24ffbf6721" +
        "ffcb6e24ffbf6721ffb2601fffcb6e24ffbf6721ffcb6e24ffb2601fffcb6e24" +
        "ffb2601fffbf6721ffbf6721ffb2601fffbf6721ffb2601fffb2601fffcb6e24" +
        "ffac5712ffb2601fffd2752bffbf6721ffd97b30ffac5712ffbf6721ffd2752b" +
        "ffcb6e24ffbf6721ffac5712ffb2601fffb2601fffd2752bffbf6721ffbf6721" +
        "ffbf6721ffbf6721ffbf6721ffb2601fffbf6721ffb2601fffb2601fffbf6721" +
        "ffbf6721ffcb6e24ffcb6e24ffb2601fffbf6721ffb2601fffcb6e24ffb2601f" +
        "ffcb6e24ffcb6e24ffb2601fffcb6e24ffbf6721ffb2601fffbf6721ffbf6721" +
        "ffb2601fffbf6721ffd97b30ffbf6721ffb2601fffcb6e24ffbf6721ffcb6e24" +
        "ffbf6721ffcb6e24ffbf6721ffbf6721ffb2601fffbf6721ffbf6721ffcb6e24" +
        "ffbf6721ffb2601fffbf6721ffbf6721ffcb6e24ffcb6e24ffcb6e24ffbf6721" +
        "ffb2601fffbf6721ffcb6e24ffb2601fffbf6721ffbf6721ffb2601fffbf6721" +
        "ffb2601fffd2752bffbf6721ffb2601fffb2601fffd97b30ffb2601fffbf6721" +
        "ffb2601fffb2601fffac5712ffbf6721ffbf6721ffb2601fffbf6721ffb2601f" +
        "ffbf6721ffb2601fffbf6721ffbf6721ffb2601fffbf6721ffbf6721ffb2601f" +
        "ffbf6721ffb2601fffb2601fffb2601fffbf6721ffd2752bffbf6721ffb2601f" +
        "ffb2601fffbf6721ffcb6e24ffbf6721ffbf6721ffcb6e24ffb2601fffac5712" +
        "ffb2601fffcb6e24ffb2601fffbf6721ffcb6e24ffbf6721ffb2601fffbf6721" +
        "ffd2752bffbf6721ffb2601fffbf6721ffbf6721ffbf6721ffb2601fffb2601f" +
        "ffbf6721ffac5712ffbf6721ffb2601fffbf6721ffbf6721ffcb6e24ffb2601f" +
        "ffbf6721ffac5712ffbf6721ffcb6e24ffbf6721ffb2601fffbf6721ffcb6e24" +
        "ffbf6721ffbf6721ffcb6e24ffbf6721ffac5712ffb2601fffbf6721ffcb6e24" +
        "ffb2601fffb2601fffbf6721ffbf6721ffcb6e24ffbf6721ffb2601fffbf6721" +
        "ffcb6e24ffcb6e24ffb2601fffb2601fffb2601fffbf6721ffb2601fffbf6721" +
        "ffb2601fffbf6721ffb2601fffcb6e24ffb2601fffcb6e24ffbf6721ffb2601f" +
        "ffd2752bffd97b30ffcb6e24ffbf6721ffb2601fffb2601fffbf6721ffcb6e24" +
        "ffcb6e24ffbf6721ffcb6e24ffbf6721ffcb6e24ffac5712ffbf6721ffcb6e24" +
        "ffbf6721ffcb6e24ffbf6721ffcb6e24ffbf6721ffbf6721ffb2601fffbf6721" +
        "ffbf6721ffcb6e24ffbf6721ffcb6e24ffd97b30ffcb6e24ffcb6e24ffbf6721" +
        "ffcb6e24ffbf6721ffcb6e24ffbf6721ffac5712ffcb6e24ffbf6721ffbf6721" +
        "ffcb6e24ffbf6721ffcb6e24ffbf6721ffcb6e24ffcb6e24ffbf6721ffcb6e24" +
        "ffbf6721ffb2601fffbf6721ffbf6721ffcb6e24ffbf6721ffcb6e24ffbf6721";

    private static final String ROOTED_DIRT =
        "ffb9855cff966c4aff966c4aff79553aff593d29ffb9855cffad7d65ff966c4a" +
        "ff79553affbb9789ff593d29ff79553aff79553affb9855cff79553affb9855c" +
        "ff79553affad7d65ffbb9789ff79553aff79553aff966c4affad7d65ff593d29" +
        "ff79553affb9855cffbb9789ffad7d65ff905740ff905740ffad7d65ff593d29" +
        "ffb9855cff79553affad7d65ffad7d65ffb9855cffad7d65ffbb9789ff966c4a" +
        "ffb9855cff79553aff79553affbb9789ff79553aff593d29ff905740ffad7d65" +
        "ff966c4aff6c6c6cffb9855cffbb9789ffad7d65ff593d29ff905740ffbb9789" +
        "ff966c4aff966c4aff79553affbb9789ff79553affb9855cff966c4aff79553a" +
        "ff966c4aff79553aff966c4affad7d65ffbb9789ff966c4aff79553aff905740" +
        "ff966c4aff593d29ff79553aff6c6c6cffad7d65ff966c4aff593d29ff79553a" +
        "ff79553aff593d29ff966c4aff966c4affbb9789ff966c4aff593d29ff905740" +
        "ff593d29ff79553aff79553aff593d29ff79553aff79553aff79553affb9855c" +
        "ffbb9789ff79553aff79553aff79553affad7d65ff79553aff79553affb9855c" +
        "ffad7d65ff79553affb9855cffb9855cff79553aff966c4affad7d65ffbb9789" +
        "ff79553aff905740ffb9855cffb9855cffbb9789ff966c4aff79553aff79553a" +
        "ff966c4affad7d65ff966c4aff966c4affad7d65ff79553aff966c4affad7d65" +
        "ff966c4aff79553aff905740ffad7d65ff79553affbb9789ff79553aff593d29" +
        "ff79553aff966c4aff966c4affad7d65ff79553aff79553aff593d29ff79553a" +
        "ff79553aff966c4aff905740ff79553aff79553aff905740ff593d29ff79553a" +
        "ff966c4aff79553aff966c4aff905740ffad7d65ffb9855cff79553aff966c4a" +
        "ff79553aff966c4affbb9789ffb9855cffb9855cff79553aff905740ff966c4a" +
        "ffad7d65ff79553aff79553aff593d29ff966c4affad7d65ffad7d65ff79553a" +
        "ff966c4aff79553aff79553affad7d65ff966c4affb9855cff905740ffbb9789" +
        "ffad7d65ff905740ff966c4aff79553aff593d29ff966c4aff79553aff593d29" +
        "ff79553aff593d29ff966c4aff79553aff966c4aff966c4affbb9789ffad7d65" +
        "ff79553affad7d65ff79553aff79553affad7d65ff79553affb9855cffb9855c" +
        "ff79553affad7d65ff79553aff79553aff79553aff79553aff966c4affbb9789" +
        "ff79553aff593d29ff905740ff593d29ff79553affbb9789ff966c4aff966c4a" +
        "ff966c4affbb9789ff593d29ffb9855cff79553aff593d29ff79553affad7d65" +
        "ffb9855cffb9855cff79553aff966c4aff79553affad7d65ffbb9789ff966c4a" +
        "ff966c4aff79553affad7d65ff966c4aff966c4aff79553aff878787ff79553a" +
        "ffad7d65ff966c4aff79553aff79553aff966c4aff966c4affbb9789ff593d29";

    private static final String SAND =
        "ffe7e4bbffdacfa3ffdacfa3ffe3dbb0ffdacfa3ffd5c496ffe3dbb0ffdacfa3" +
        "ffe3dbb0ffdacfa3ffd5c496ffe3dbb0ffdacfa3ffe3dbb0ffd5c496ffe3dbb0" +
        "ffd5c496ffdacfa3ffdacfa3ffd5c496ffdacfa3ffd5c496ffd5c496ffe3dbb0" +
        "ffd1ba8affd5c496ffe7e4bbffdacfa3ffedebcbffd1ba8affdacfa3ffe7e4bb" +
        "ffe3dbb0ffdacfa3ffd1ba8affd5c496ffd5c496ffe7e4bbffdacfa3ffdacfa3" +
        "ffdacfa3ffdacfa3ffdacfa3ffd5c496ffdacfa3ffd5c496ffd5c496ffdacfa3" +
        "ffdacfa3ffe3dbb0ffe3dbb0ffd5c496ffdacfa3ffd5c496ffe3dbb0ffd5c496" +
        "ffe3dbb0ffe3dbb0ffd5c496ffe3dbb0ffdacfa3ffd5c496ffdacfa3ffdacfa3" +
        "ffd5c496ffdacfa3ffedebcbffdacfa3ffd5c496ffe3dbb0ffdacfa3ffe3dbb0" +
        "ffdacfa3ffe3dbb0ffdacfa3ffdacfa3ffd5c496ffdacfa3ffdacfa3ffe3dbb0" +
        "ffdacfa3ffd5c496ffdacfa3ffdacfa3ffe3dbb0ffe3dbb0ffe3dbb0ffdacfa3" +
        "ffd5c496ffdacfa3ffe3dbb0ffd5c496ffdacfa3ffdacfa3ffd5c496ffdacfa3" +
        "ffd5c496ffe7e4bbffdacfa3ffd5c496ffd5c496ffedebcbffd5c496ffdacfa3" +
        "ffd5c496ffd5c496ffd1ba8affdacfa3ffdacfa3ffd5c496ffdacfa3ffd5c496" +
        "ffdacfa3ffd5c496ffdacfa3ffdacfa3ffd5c496ffdacfa3ffdacfa3ffd5c496" +
        "ffdacfa3ffd5c496ffd5c496ffd5c496ffdacfa3ffe7e4bbffdacfa3ffd5c496" +
        "ffd5c496ffdacfa3ffe3dbb0ffdacfa3ffdacfa3ffe3dbb0ffd5c496ffd1ba8a" +
        "ffd5c496ffe3dbb0ffd5c496ffdacfa3ffe3dbb0ffdacfa3ffd5c496ffdacfa3" +
        "ffe7e4bbffdacfa3ffd5c496ffdacfa3ffdacfa3ffdacfa3ffd5c496ffd5c496" +
        "ffdacfa3ffd1ba8affdacfa3ffd5c496ffdacfa3ffdacfa3ffe3dbb0ffd5c496" +
        "ffdacfa3ffd1ba8affdacfa3ffe3dbb0ffdacfa3ffd5c496ffdacfa3ffe3dbb0" +
        "ffdacfa3ffdacfa3ffe3dbb0ffdacfa3ffd1ba8affd5c496ffdacfa3ffe3dbb0" +
        "ffd5c496ffd5c496ffdacfa3ffdacfa3ffe3dbb0ffdacfa3ffd5c496ffdacfa3" +
        "ffe3dbb0ffe3dbb0ffd5c496ffd5c496ffd5c496ffdacfa3ffd5c496ffdacfa3" +
        "ffd5c496ffdacfa3ffd5c496ffe3dbb0ffd5c496ffe3dbb0ffdacfa3ffd5c496" +
        "ffe7e4bbffedebcbffe3dbb0ffdacfa3ffd5c496ffd5c496ffdacfa3ffe3dbb0" +
        "ffe3dbb0ffdacfa3ffe3dbb0ffdacfa3ffe3dbb0ffd1ba8affdacfa3ffe3dbb0" +
        "ffdacfa3ffe3dbb0ffdacfa3ffe3dbb0ffdacfa3ffdacfa3ffd5c496ffdacfa3" +
        "ffdacfa3ffe3dbb0ffdacfa3ffe3dbb0ffedebcbffe3dbb0ffe3dbb0ffdacfa3" +
        "ffe3dbb0ffdacfa3ffe3dbb0ffdacfa3ffd1ba8affe3dbb0ffdacfa3ffdacfa3" +
        "ffe3dbb0ffdacfa3ffe3dbb0ffdacfa3ffe3dbb0ffe3dbb0ffdacfa3ffe3dbb0" +
        "ffdacfa3ffd5c496ffdacfa3ffdacfa3ffe3dbb0ffdacfa3ffe3dbb0ffdacfa3";

    private static final String SCULK =
        "ff0d1217ff0d1217ff0d1217ff0d1217ff111b21ff111b21ff111b21ff052a32" +
        "ff111b21ff052a32ff0d1217ff0d1217ff111b21ff111b21ff052a32ff0d1217" +
        "ff0d1217ff0d1217ff111b21ff052a32ff0d1217ff0d1217ff0d1217ff111b21" +
        "ff111b21ff111b21ff111b21ff0d1217ff052a32ff111b21ff111b21ff111b21" +
        "ff0d1217ff0d1217ff052a32ff194648ff052a32ff0d1217ff0d1217ff0d1217" +
        "ff111b21ff111b21ff0d1217ff052a32ff05625dff052a32ff111b21ff111b21" +
        "ff111b21ff111b21ff0d1217ff052a32ff111b21ff111b21ff0d1217ff0d1217" +
        "ff111b21ff052a32ff111b21ff0d1217ff052a32ff0d1217ff111b21ff052a32" +
        "ff111b21ff052a32ff111b21ff111b21ff111b21ff111b21ff111b21ff0d1217" +
        "ff0d1217ff111b21ff0d1217ff0d1217ff0d1217ff0d1217ff0d1217ff111b21" +
        "ff0d1217ff111b21ff111b21ff111b21ff111b21ff052a32ff034150ff111b21" +
        "ff0d1217ff052a32ff111b21ff111b21ff0d1217ff0d1217ff111b21ff0d1217" +
        "ff0d1217ff0d1217ff052a32ff111b21ff111b21ff111b21ff111b21ff0d1217" +
        "ff052a32ff05625dff052a32ff0d1217ff0d1217ff111b21ff034150ff111b21" +
        "ff0d1217ff0d1217ff052a32ff0d1217ff0d1217ff0d1217ff111b21ff0d1217" +
        "ff111b21ff052a32ff111b21ff0d1217ff0d1217ff111b21ff111b21ff111b21" +
        "ff111b21ff052a32ff05625dff052a32ff0d1217ff0d1217ff0d1217ff111b21" +
        "ff0d1217ff111b21ff0d1217ff0d1217ff111b21ff0d1217ff052a32ff0d1217" +
        "ff0d1217ff111b21ff052a32ff111b21ff111b21ff0d1217ff111b21ff052a32" +
        "ff111b21ff111b21ff111b21ff0d1217ff0d1217ff111b21ff111b21ff0d1217" +
        "ff0d1217ff111b21ff111b21ff052a32ff0d1217ff0d1217ff0d1217ff111b21" +
        "ff111b21ff0d1217ff111b21ff0d1217ff052a32ff0d1217ff0d1217ff111b21" +
        "ff111b21ff0d1217ff052a32ff194648ff052a32ff0d1217ff111b21ff111b21" +
        "ff111b21ff0d1217ff0d1217ff052a32ff034150ff052a32ff0d1217ff0d1217" +
        "ff111b21ff111b21ff0d1217ff052a32ff0d1217ff0d1217ff0d1217ff111b21" +
        "ff194648ff111b21ff0d1217ff0d1217ff052a32ff111b21ff111b21ff0d1217" +
        "ff111b21ff052a32ff111b21ff0d1217ff0d1217ff0d1217ff0d1217ff0d1217" +
        "ff111b21ff0d1217ff0d1217ff0d1217ff0d1217ff111b21ff0d1217ff0d1217" +
        "ff0d1217ff111b21ff0d1217ff0d1217ff0d1217ff111b21ff0d1217ff111b21" +
        "ff0d1217ff052a32ff0d1217ff0d1217ff0d1217ff0d1217ff052a32ff111b21" +
        "ff0d1217ff0d1217ff0d1217ff0d1217ff111b21ff034150ff111b21ff111b21" +
        "ff052a32ff05625dff052a32ff0d1217ff111b21ff052a32ff034150ff052a32" +
        "ff0d1217ff0d1217ff0d1217ff0d1217ff111b21ff111b21ff111b21ff052a32" +
        "ff111b21ff052a32ff0d1217ff0d1217ff111b21ff111b21ff052a32ff0d1217" +
        "ff0d1217ff0d1217ff111b21ff052a32ff0d1217ff0d1217ff0d1217ff111b21" +
        "ff111b21ff111b21ff111b21ff0d1217ff052a32ff111b21ff111b21ff111b21" +
        "ff0d1217ff0d1217ff052a32ff29dfebff052a32ff0d1217ff0d1217ff0d1217" +
        "ff111b21ff111b21ff0d1217ff052a32ff009295ff052a32ff111b21ff111b21" +
        "ff111b21ff111b21ff0d1217ff052a32ff111b21ff111b21ff0d1217ff0d1217" +
        "ff111b21ff052a32ff111b21ff0d1217ff052a32ff0d1217ff111b21ff052a32" +
        "ff111b21ff052a32ff111b21ff111b21ff111b21ff111b21ff111b21ff0d1217" +
        "ff0d1217ff111b21ff0d1217ff0d1217ff0d1217ff0d1217ff0d1217ff111b21" +
        "ff0d1217ff111b21ff111b21ff111b21ff111b21ff052a32ff034150ff111b21" +
        "ff0d1217ff052a32ff111b21ff111b21ff0d1217ff0d1217ff111b21ff0d1217" +
        "ff0d1217ff0d1217ff052a32ff111b21ff111b21ff111b21ff111b21ff0d1217" +
        "ff052a32ff05625dff052a32ff0d1217ff0d1217ff111b21ff034150ff111b21" +
        "ff0d1217ff0d1217ff034150ff0d1217ff0d1217ff0d1217ff111b21ff0d1217" +
        "ff111b21ff052a32ff111b21ff0d1217ff0d1217ff111b21ff111b21ff111b21" +
        "ff111b21ff034150ff29dfebff034150ff0d1217ff0d1217ff0d1217ff111b21" +
        "ff0d1217ff111b21ff0d1217ff0d1217ff111b21ff0d1217ff052a32ff0d1217" +
        "ff0d1217ff111b21ff034150ff052a32ff111b21ff0d1217ff111b21ff052a32" +
        "ff111b21ff111b21ff111b21ff0d1217ff0d1217ff111b21ff111b21ff0d1217" +
        "ff0d1217ff111b21ff111b21ff052a32ff0d1217ff0d1217ff0d1217ff111b21" +
        "ff111b21ff0d1217ff111b21ff0d1217ff052a32ff0d1217ff0d1217ff111b21" +
        "ff111b21ff0d1217ff052a32ff009295ff052a32ff0d1217ff111b21ff111b21" +
        "ff052a32ff0d1217ff0d1217ff052a32ff034150ff052a32ff0d1217ff0d1217" +
        "ff111b21ff111b21ff0d1217ff052a32ff0d1217ff0d1217ff0d1217ff052a32" +
        "ff009295ff052a32ff0d1217ff0d1217ff052a32ff111b21ff111b21ff0d1217" +
        "ff111b21ff052a32ff111b21ff0d1217ff0d1217ff0d1217ff0d1217ff0d1217" +
        "ff052a32ff0d1217ff0d1217ff0d1217ff0d1217ff111b21ff0d1217ff0d1217" +
        "ff0d1217ff111b21ff0d1217ff0d1217ff0d1217ff111b21ff0d1217ff111b21" +
        "ff0d1217ff052a32ff0d1217ff0d1217ff0d1217ff0d1217ff052a32ff111b21" +
        "ff0d1217ff0d1217ff0d1217ff0d1217ff111b21ff034150ff111b21ff111b21" +
        "ff052a32ff05625dff052a32ff0d1217ff111b21ff052a32ff034150ff052a32" +
        "ff0d1217ff0d1217ff0d1217ff0d1217ff111b21ff111b21ff111b21ff052a32" +
        "ff111b21ff052a32ff0d1217ff0d1217ff111b21ff111b21ff052a32ff0d1217" +
        "ff0d1217ff0d1217ff111b21ff052a32ff0d1217ff0d1217ff0d1217ff111b21" +
        "ff111b21ff111b21ff111b21ff0d1217ff052a32ff111b21ff111b21ff111b21" +
        "ff0d1217ff0d1217ff052a32ff009295ff052a32ff0d1217ff0d1217ff0d1217" +
        "ff111b21ff111b21ff0d1217ff052a32ff009295ff052a32ff111b21ff111b21" +
        "ff111b21ff111b21ff0d1217ff052a32ff111b21ff111b21ff0d1217ff0d1217" +
        "ff111b21ff052a32ff111b21ff0d1217ff052a32ff0d1217ff111b21ff052a32" +
        "ff111b21ff052a32ff111b21ff111b21ff111b21ff111b21ff111b21ff0d1217" +
        "ff0d1217ff111b21ff0d1217ff0d1217ff0d1217ff0d1217ff0d1217ff111b21" +
        "ff0d1217ff111b21ff111b21ff111b21ff111b21ff052a32ff034150ff111b21" +
        "ff0d1217ff052a32ff111b21ff111b21ff0d1217ff0d1217ff111b21ff0d1217" +
        "ff0d1217ff0d1217ff052a32ff111b21ff111b21ff111b21ff111b21ff0d1217" +
        "ff052a32ff29dfebff052a32ff0d1217ff0d1217ff111b21ff034150ff111b21" +
        "ff0d1217ff0d1217ff034150ff0d1217ff0d1217ff0d1217ff111b21ff0d1217" +
        "ff111b21ff052a32ff111b21ff0d1217ff0d1217ff111b21ff111b21ff111b21" +
        "ff111b21ff034150ff009295ff034150ff0d1217ff0d1217ff0d1217ff111b21" +
        "ff0d1217ff111b21ff0d1217ff0d1217ff111b21ff0d1217ff052a32ff0d1217" +
        "ff0d1217ff111b21ff034150ff052a32ff111b21ff0d1217ff111b21ff052a32" +
        "ff111b21ff111b21ff111b21ff0d1217ff0d1217ff111b21ff111b21ff0d1217" +
        "ff0d1217ff111b21ff111b21ff052a32ff0d1217ff0d1217ff0d1217ff111b21" +
        "ff111b21ff0d1217ff111b21ff0d1217ff052a32ff0d1217ff0d1217ff111b21" +
        "ff111b21ff0d1217ff052a32ff29dfebff052a32ff0d1217ff111b21ff111b21" +
        "ff111b21ff0d1217ff0d1217ff052a32ff009295ff052a32ff0d1217ff0d1217" +
        "ff111b21ff111b21ff0d1217ff052a32ff0d1217ff0d1217ff0d1217ff111b21" +
        "ff034150ff111b21ff0d1217ff0d1217ff052a32ff111b21ff111b21ff0d1217" +
        "ff111b21ff052a32ff111b21ff0d1217ff0d1217ff0d1217ff0d1217ff0d1217" +
        "ff111b21ff0d1217ff0d1217ff0d1217ff0d1217ff111b21ff0d1217ff0d1217" +
        "ff0d1217ff111b21ff0d1217ff0d1217ff0d1217ff111b21ff0d1217ff111b21" +
        "ff0d1217ff052a32ff0d1217ff0d1217ff0d1217ff0d1217ff052a32ff111b21" +
        "ff0d1217ff0d1217ff0d1217ff0d1217ff111b21ff034150ff111b21ff111b21" +
        "ff052a32ff009295ff052a32ff0d1217ff111b21ff052a32ff034150ff052a32" +
        "ff0d1217ff0d1217ff0d1217ff0d1217ff111b21ff111b21ff111b21ff052a32" +
        "ff111b21ff052a32ff0d1217ff0d1217ff111b21ff111b21ff052a32ff0d1217" +
        "ff0d1217ff0d1217ff111b21ff052a32ff0d1217ff0d1217ff0d1217ff111b21" +
        "ff111b21ff111b21ff111b21ff0d1217ff052a32ff111b21ff111b21ff111b21" +
        "ff0d1217ff0d1217ff052a32ff009295ff052a32ff0d1217ff0d1217ff0d1217" +
        "ff111b21ff111b21ff0d1217ff052a32ff009295ff052a32ff111b21ff111b21" +
        "ff111b21ff111b21ff0d1217ff052a32ff111b21ff111b21ff0d1217ff0d1217" +
        "ff111b21ff052a32ff111b21ff0d1217ff052a32ff0d1217ff111b21ff052a32" +
        "ff111b21ff052a32ff111b21ff111b21ff111b21ff111b21ff111b21ff0d1217" +
        "ff0d1217ff111b21ff0d1217ff0d1217ff0d1217ff0d1217ff0d1217ff111b21" +
        "ff0d1217ff111b21ff111b21ff111b21ff111b21ff052a32ff034150ff111b21" +
        "ff0d1217ff052a32ff111b21ff111b21ff0d1217ff0d1217ff111b21ff0d1217" +
        "ff0d1217ff0d1217ff052a32ff111b21ff111b21ff111b21ff111b21ff0d1217" +
        "ff052a32ff009295ff052a32ff0d1217ff0d1217ff111b21ff034150ff111b21" +
        "ff0d1217ff0d1217ff034150ff0d1217ff0d1217ff0d1217ff111b21ff0d1217" +
        "ff111b21ff052a32ff111b21ff0d1217ff0d1217ff111b21ff111b21ff111b21" +
        "ff111b21ff034150ff009295ff034150ff0d1217ff0d1217ff0d1217ff111b21" +
        "ff0d1217ff111b21ff0d1217ff0d1217ff111b21ff0d1217ff052a32ff0d1217" +
        "ff0d1217ff111b21ff034150ff052a32ff111b21ff0d1217ff111b21ff052a32" +
        "ff111b21ff111b21ff111b21ff0d1217ff0d1217ff111b21ff111b21ff0d1217" +
        "ff0d1217ff111b21ff111b21ff052a32ff0d1217ff0d1217ff0d1217ff111b21" +
        "ff111b21ff0d1217ff111b21ff0d1217ff034150ff0d1217ff0d1217ff111b21" +
        "ff111b21ff0d1217ff052a32ff009295ff052a32ff0d1217ff111b21ff111b21" +
        "ff111b21ff0d1217ff0d1217ff034150ff29dfebff034150ff0d1217ff0d1217" +
        "ff111b21ff111b21ff0d1217ff052a32ff0d1217ff0d1217ff0d1217ff111b21" +
        "ff052a32ff111b21ff0d1217ff0d1217ff034150ff111b21ff111b21ff0d1217" +
        "ff111b21ff052a32ff111b21ff0d1217ff0d1217ff0d1217ff0d1217ff0d1217" +
        "ff111b21ff0d1217ff0d1217ff0d1217ff0d1217ff111b21ff0d1217ff0d1217" +
        "ff0d1217ff111b21ff0d1217ff0d1217ff0d1217ff111b21ff0d1217ff111b21" +
        "ff0d1217ff052a32ff0d1217ff0d1217ff0d1217ff0d1217ff052a32ff111b21" +
        "ff0d1217ff0d1217ff0d1217ff0d1217ff111b21ff034150ff111b21ff111b21" +
        "ff052a32ff29dfebff052a32ff0d1217ff111b21ff052a32ff034150ff052a32";

    private static final String STONE =
        "ff8f8f8fff8f8f8fff8f8f8fff8f8f8fff7f7f7fff747474ff747474ff7f7f7f" +
        "ff747474ff686868ff747474ff747474ff7f7f7fff7f7f7fff7f7f7fff7f7f7f" +
        "ff7f7f7fff7f7f7fff747474ff7f7f7fff747474ff7f7f7fff7f7f7fff7f7f7f" +
        "ff7f7f7fff7f7f7fff7f7f7fff686868ff686868ff747474ff7f7f7fff747474" +
        "ff7f7f7fff747474ff686868ff686868ff747474ff747474ff747474ff686868" +
        "ff747474ff747474ff747474ff7f7f7fff7f7f7fff7f7f7fff7f7f7fff7f7f7f" +
        "ff7f7f7fff7f7f7fff8f8f8fff8f8f8fff7f7f7fff8f8f8fff7f7f7fff7f7f7f" +
        "ff8f8f8fff8f8f8fff7f7f7fff7f7f7fff7f7f7fff747474ff747474ff747474" +
        "ff747474ff7f7f7fff7f7f7fff747474ff747474ff747474ff7f7f7fff747474" +
        "ff7f7f7fff8f8f8fff8f8f8fff8f8f8fff8f8f8fff747474ff7f7f7fff7f7f7f" +
        "ff7f7f7fff8f8f8fff7f7f7fff7f7f7fff7f7f7fff7f7f7fff7f7f7fff7f7f7f" +
        "ff7f7f7fff747474ff747474ff686868ff747474ff686868ff747474ff8f8f8f" +
        "ff747474ff7f7f7fff7f7f7fff8f8f8fff8f8f8fff8f8f8fff747474ff8f8f8f" +
        "ff8f8f8fff747474ff747474ff747474ff7f7f7fff7f7f7fff7f7f7fff7f7f7f" +
        "ff747474ff747474ff686868ff686868ff747474ff686868ff747474ff747474" +
        "ff7f7f7fff7f7f7fff747474ff747474ff747474ff7f7f7fff7f7f7fff7f7f7f" +
        "ff8f8f8fff8f8f8fff8f8f8fff7f7f7fff8f8f8fff7f7f7fff7f7f7fff8f8f8f" +
        "ff8f8f8fff8f8f8fff7f7f7fff7f7f7fff7f7f7fff7f7f7fff7f7f7fff747474" +
        "ff7f7f7fff7f7f7fff8f8f8fff7f7f7fff7f7f7fff7f7f7fff8f8f8fff8f8f8f" +
        "ff8f8f8fff7f7f7fff7f7f7fff8f8f8fff8f8f8fff8f8f8fff8f8f8fff7f7f7f" +
        "ff686868ff7f7f7fff747474ff7f7f7fff747474ff747474ff686868ff686868" +
        "ff747474ff7f7f7fff747474ff7f7f7fff7f7f7fff747474ff747474ff747474" +
        "ff7f7f7fff7f7f7fff7f7f7fff7f7f7fff7f7f7fff8f8f8fff8f8f8fff7f7f7f" +
        "ff7f7f7fff7f7f7fff7f7f7fff7f7f7fff8f8f8fff8f8f8fff7f7f7fff8f8f8f" +
        "ff7f7f7fff747474ff747474ff747474ff7f7f7fff8f8f8fff7f7f7fff747474" +
        "ff747474ff7f7f7fff7f7f7fff686868ff686868ff747474ff686868ff747474" +
        "ff8f8f8fff8f8f8fff7f7f7fff747474ff747474ff7f7f7fff7f7f7fff747474" +
        "ff747474ff747474ff7f7f7fff7f7f7fff747474ff747474ff747474ff8f8f8f" +
        "ff7f7f7fff747474ff747474ff7f7f7fff7f7f7fff7f7f7fff7f7f7fff7f7f7f" +
        "ff7f7f7fff7f7f7fff7f7f7fff7f7f7fff8f8f8fff8f8f8fff8f8f8fff8f8f8f" +
        "ff7f7f7fff7f7f7fff7f7f7fff7f7f7fff7f7f7fff7f7f7fff747474ff747474" +
        "ff7f7f7fff8f8f8fff8f8f8fff7f7f7fff7f7f7fff747474ff7f7f7fff7f7f7f";

    private static final String WHITE_CONCRETE_POWDER =
        "ffe0e2e2ffe6e8e8ffe4e6e6ffdde0e0ffe2e4e4ffdfe0e1ffe0e2e2ffe8e9e9" +
        "ffdfe2e2ffdadddeffd9dcdcffd8dcddffeeeeeefff0f1f1ffd8dadbffe4e5e5" +
        "ffe7e8e8ffe1e2e3ffeaebebffeeefefffcbcecfffe9e9e9ffd4d8d8ffd6d9da" +
        "ffe2e4e5ffdcdfdffff9f9f9ffdcdfdfffecededffdfe0e1ffdcdfdffff2f2f2" +
        "fff3f3f3fff0f1f1fff3f4f5ffdfe1e1ffdfe0e1ffe8e8e9ffe3e4e5ffe2e4e4" +
        "ffe6e7e7ffe3e5e6ffe4e6e6fff1f1f2ffdbddddffe1e3e3ffe6e7e7ffe9ebec" +
        "ffd8d9daffe6e7e8ffd8dadaffe6e7e7ffe0e2e2ffdaddddffdfe1e1ffededee" +
        "ffcacdceffd9dcdcffeaeaeaffe0e2e2ffeeefefffe9ebebffe1e3e4ffd2d4d4" +
        "ffedededffdbdddeffeaececffe8e9e9ffe8eaeaffe1e4e4fff1f2f2ffe8eaea" +
        "ffdfe1e1ffe2e2e3ffe0e2e2fff2f3f3ffecededffe7e7e7fff0f1f1ffdbdcdd" +
        "ffd8dadaffe0e2e2ffdcdfdffff6f7f7ffdbdcddffe4e5e6ffe4e5e6ffdbddde" +
        "ffe0e2e2ffe1e3e4ffe7e8e8ffd8dadaffd6d8d9ffd3d6d7ffdaddddffe2e3e3" +
        "ffced2d2ffe6e7e7ffdcdedeffe7e9e9ffe1e2e2ffeaebebffd8dadbffe4e5e5" +
        "ffe2e4e4ffe1e2e3ffe2e5e6fff5f6f6ffe0e2e2ffe0e2e3ffeeefefffe7e8e8" +
        "ffdfe0e1ffdfe2e2ffe3e6e6ffd8dbdcffe9eaeaffe5e6e6fff2f3f3ffe6e8e9" +
        "ffe1e2e3ffdddfdfffdcdedfffd6d9daffdee0e0ffdfe1e1ffe7eaeafff2f2f2" +
        "ffe0e2e2ffeff0f0ffedeeeeffe5e7e8ffecededffdbdedeffe7e9e9ffdaddde" +
        "ffd8dadbfff6f7f8ffd6d8d8ffdee0e1ffe1e2e3ffe8eaeaffe2e4e4ffdfe0e1" +
        "ffd7d9d9ffdfe1e2ffebededffe6e7e7fff0f0f0ffdcdedfffe0e2e2ffd8dadb" +
        "ffe2e4e4ffdddfe0fff0f0f0ffebededffe8e9e9ffeaebecffe1e3e3ffe7e8e8" +
        "ffe2e4e4ffe2e4e4ffdadadaffe4e6e6ffd3d5d6ffe6e8e8ffdcdedfffe2e4e5" +
        "ffe3e6e6ffe3e6e6ffebebebffdcdfdfffe4e6e6fffdfdfdffe2e4e4ffdedfe0" +
        "ffd4d8d8ffe6e7e8ffe4e6e6ffe6e7e7ffdcdfdfffdfe2e2ffe9eaeaffdbdddd" +
        "ffdfe0e1ffe8eaeaffe8e9e9fff3f4f4ffd8dadbffe0e2e2ffe1e2e2ffd8dada" +
        "ffeceeeeffdfe1e2ffd6d8d9ffe7e9eaffe1e2e3ffdfe1e2ffdfe2e2ffcfd1d2" +
        "ffdadcdcffe2e4e4ffe0e2e2ffd8dbdcffe3e5e6ffebecedffd5d7d8ffdddfe0" +
        "ffdcdfe0ffe1e3e4ffd2d4d5ffcaceceffd9dbdcffe4e6e6ffdcdfdfffe6e8e9" +
        "ffe6e7e7ffe8eaeaffdfe2e2ffe2e4e4ffdfe2e2ffd8dadbffebededffeeeeee" +
        "ffdfe2e2ffe0e2e3ffdbdcddffe0e2e2ffdbdedfffd8dbdbffeaebebffe1e3e3" +
        "ffdbddddfff1f1f1ffd6d8d8ffdcdedfffe4e5e6ffd4d7d7ffd8dadaffe2e4e4" +
        "ffdfe1e1ffe1e3e4ffe1e3e3ffd4d7d7fff1f3f3ffe9eaeaffd4d6d6ffe4e6e6" +
        "ffdfe1e2ffe4e6e7ffd7dadaffdedfe0ffdfe1e2ffdcdfe0ffe6e7e8ffd3d5d5";

    private static final String YELLOW_CONCRETE_POWDER =
        "ffe2bc2fffebc632ffeed84effeacf3fffedc933ffe7c332ffe5c333ffe8c534" +
        "ffebca36ffe8bf2cffe9cc3dffeac836ffeacd3bfff4cf33ffe0b528ffeacb3a" +
        "ffe8c12cffe7c332ffeac532ffebc22cffddb62bffe4c130ffe4c333ffe6c637" +
        "fff0d03affecc42dfff8dc3fffedd850ffedd448ffeacf41ffe9c32fffebc42d" +
        "ffeccd3afff0cf39fff1cb2dffe4bb29ffe8c736ffe8cb3dffe8c432ffe6bd2b" +
        "fff0e6b0ffeccd3affecc52fffeece39ffecd54dffe3bf30ffe9d045fff4d63d" +
        "ffe6c93effe7c434ffe6bd2cffe8be2bffe6c637ffe9c32fffe6c739ffe8c12d" +
        "ffd6b42dffe6c232ffe6be2cffedd03effedc933ffedc832ffe7c02fffdfc239" +
        "ffedce3bffe8cc3effedc933ffebd551ffeece39ffeecd36ffecc630ffedc933" +
        "ffe3ba2bffe2b92bffe6c93afff3eab5ffe7c332ffdfbf34fff0cb32ffe3c234" +
        "ffe4c436ffebc937ffe3bb2cfff8d533ffe4c130ffecc62fffeed952ffedd549" +
        "ffe6c02fffeec82fffe8c22fffe1bf33ffe6c83bffe3bc2dffe7be2cffe0c237" +
        "ffdbb32bffeac42fffe3be2fffeacd3bffddb82dffe9c02dffe9cb3dffe3ba29" +
        "ffe4ba29ffead045fff0d543fff6d83dffe8c02dffeece3affe9c634ffe7c12f" +
        "ffe7be2cffe7c434ffecc832ffe7c332ffeacd3dffe3b929ffeed441fff3d43a" +
        "ffedd54cffe4bf2fffe7c534ffe6c333ffe8bd29ffeac22dfff2d137ffecc52f" +
        "ffe9c12cffeac22dfff2d541ffeecf3dffebcb37ffe4be2dffedc630ffecce3d" +
        "ffe6c332fff7da3effddb62cffeacc3affe6c02fffe9c533ffe8c02cffeacf3f" +
        "ffdeba2fffe7c02fffedc833ffeacc3dffe8c836ffe9c12cffe4be2dffe4bc2d" +
        "ffe6bd29ffe8c533ffe8c330ffeec830ffe8c634ffeac32dffe6bf2dfff0d441" +
        "ffe9c430ffedd243ffdac864fff0d94cffe0b72bfff2d742ffeacd3fffe8c22f" +
        "fff2d43efff0cc33ffeed341ffe8c83affe6be2bfff8dc3dffe9ce41ffe7c230" +
        "ffe5c73bfff0d84afff0db53ffe2c133ffe8bf2cffebd349ffe8c12dffe5bd2c" +
        "ffe8c12fffebd142ffebd54afff5d439ffe6c637ffe5c232ffe3bc2dffddbe36" +
        "ffefcc33ffe7bd29ffe3bc2bfff0cd34ffe9c32fffe5c232ffebca36ffdbb429" +
        "ffe2bd32ffebd142ffead045ffe3bd2dffeccc39fff0d94cffe3be2fffe6c433" +
        "ffebce3bffecc630ffe6cc41ffdabd39ffe4c032ffead149ffe6c434fff3d73f" +
        "ffedce3bfff1ce33ffefd542ffeed74effeed342ffe7c434ffecc52fffe6c230" +
        "ffecc936ffe8c634ffe9ce41ffe3bf30ffeac430ffe9ce42ffe9c634ffe9c12c" +
        "ffe0b82bffe9c12cffdfc643ffe6c030ffedc732ffe3bf30ffe3bf30ffeac42f" +
        "ffe6c433ffeac12cffe4bd2bffdfbd32fff9e44effebce3effe2c337ffefca32" +
        "ffeace3ffff0c930ffe4bf2fffe9c22fffedd74dffebd143ffe4bb29ffdfba2c";
}
