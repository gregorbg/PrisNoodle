package com.puzzletimer.categories;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import static com.puzzletimer.Internationalization.i18n;

public class CategoryProvider {
    private Category[] categories;
    private HashMap<UUID, Category> categoryMap;

    public CategoryProvider() {
        Category[] categories = new Category[]{
                new WcaCategory(UUID.fromString("64b9c16d-dc36-44b4-9605-c93933cdd311"), "222", i18n("category.2x2x2_cube"), new String[0], '2', '2', false),
                new WcaCategory(UUID.fromString("90dea358-e525-4b6c-8b2d-abfa61f02a9d"), "333", i18n("category.rubiks_cube"), new String[]{
                        "RUBIKS-CUBE-OPTIMAL-CROSS",
                        "RUBIKS-CUBE-OPTIMAL-X-CROSS"
                }, 'R', '3', false),
                new WcaCategory(UUID.fromString("3282c6bc-3a7b-4b16-aeae-45ae75b17e47"), "333", i18n("category.rubiks_cube_one_handed"), new String[]{
                        "RUBIKS-CUBE-OPTIMAL-CROSS",
                        "RUBIKS-CUBE-OPTIMAL-X-CROSS"
                }, 'O', 'O', false),
                new WcaCategory(UUID.fromString("953a7701-6235-4f9b-8dd4-fe32055cb652"), "333ni", i18n("category.rubiks_cube_blindfolded"), new String[]{
                        "RUBIKS-CUBE-BLD-ROTATION",
                        "RUBIKS-CUBE-CLASSIC-POCHMANN-CORNERS",
                        "RUBIKS-CUBE-CLASSIC-POCHMANN-EDGES",
                        "RUBIKS-CUBE-M2-EDGES"
                }, 'B', 'B', false, true, false),
                new WcaCategory(UUID.fromString("761088a1-64fc-47db-92ea-b6c3b812e6f3"), "333", i18n("category.rubiks_cube_with_feet"), new String[]{
                        "RUBIKS-CUBE-OPTIMAL-CROSS",
                        "RUBIKS-CUBE-OPTIMAL-X-CROSS"
                }, 'F', 'F', false),
                new WcaCategory(UUID.fromString("3577f24a-065b-4bcc-9ca3-3df011d07a5d"), "444", i18n("category.4x4x4_cube"), new String[0], '4', '4', false),
                new WcaCategory(UUID.fromString("587d884a-b996-4cd6-95bb-c3dafbfae193"), "444ni", i18n("category.4x4x4_cube_blindfolded"), new String[0], 'B', '4', true, true, false),
                new WcaCategory(UUID.fromString("e3894e40-fb85-497b-a592-c81703901a95"), "555", i18n("category.5x5x5_cube"), new String[0], '5', '5', false),
                new WcaCategory(UUID.fromString("0701c98c-a275-4e51-888c-59dc9de9de1a"), "555ni", i18n("category.5x5x5_cube_blindfolded"), new String[0], 'B', '5', true, true, false),
                new WcaCategory(UUID.fromString("86227762-6249-4417-840b-3c8ba7b0bd33"), "666", i18n("category.6x6x6_cube"), new String[0], '6', '6', false),
                new WcaCategory(UUID.fromString("b9375ece-5a31-4dc4-b58e-ecb8a638e102"), "777", i18n("category.7x7x7_cube"), new String[0], '7', '7', false),
                new WcaCategory(UUID.fromString("7f244648-0e14-44cd-8399-b41ccdb6d7db"), "clock", i18n("category.rubiks_clock"), new String[0], 'C', 'K', false),
                new WcaCategory(UUID.fromString("c50f60c8-99d2-48f4-8502-d110a0ef2fc9"), "minx", i18n("category.megaminx"), new String[0], 'M', 'M', false),
                new WcaCategory(UUID.fromString("6750cbfd-542d-42b7-9cf4-56265549dd88"), "pyram", i18n("category.pyraminx"), new String[0], 'P', 'P', false),
                new WcaCategory(UUID.fromString("748e6c09-cca5-412a-bd92-cc7febed9adf"), "sq1", i18n("category.square_1"), new String[]{
                        "SQUARE-1-OPTIMAL-CUBE-SHAPE"
                }, 'S', '1', false),
                new WcaCategory(UUID.fromString("7adb4689-1961-43e4-9063-c42f578fde34"), "skewb", i18n("category.skewb"), new String[0], 'S', 'W', false),
        };

        this.categories = categories;

        this.categoryMap = new HashMap<>();
        for (Category category : categories) this.categoryMap.put(category.getCategoryId(), category);
    }

    public Category get(UUID uuid) {
        return this.categoryMap.get(uuid);
    }

    public Category[] getAll() {
        return this.categories;
    }

    public void loadCustom(Category[] categories) {
        Category[] copyArray = Arrays.copyOf(this.categories, this.categories.length + categories.length);
        System.arraycopy(categories, 0, copyArray, this.categories.length, copyArray.length - this.categories.length);
        this.categories = copyArray;
    }

    public void add(Category category) {
        this.categoryMap.put(category.getCategoryId(), category);

        Category[] newArray = Arrays.copyOf(this.categories, this.categories.length + 1);
        newArray[this.categories.length] = category;
        this.categories = newArray;
    }

    public void remove(Category category) {
        this.categoryMap.remove(category.getCategoryId());

        this.categories = Arrays.copyOfRange(this.categories, 0, this.categories.length - 1);
    }
}