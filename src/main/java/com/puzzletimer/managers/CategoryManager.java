package com.puzzletimer.managers;

import com.puzzletimer.categories.Category;
import com.puzzletimer.categories.CategoryProvider;

import java.util.ArrayList;

public class CategoryManager {
    public static class Listener {
        public void currentCategoryChanged(Category oldCategory, Category newCategory) {
        }

        public void categoriesUpdated(Category[] categories, Category currentCategory) {
        }

        public void categoryAdded(Category category) {
        }

        public void categoryRemoved(Category category) {
        }

        public void categoryUpdated(Category category) {
        }
    }

    private ArrayList<Listener> listeners;
    private CategoryProvider categoryProvider;
    private Category currentCategory;

    public CategoryManager(CategoryProvider categoryProvider, Category currentCategory) {
        this.listeners = new ArrayList<>();
        this.categoryProvider = categoryProvider;
        this.currentCategory = currentCategory;
    }

    public Category[] getCategories() {
        return this.categoryProvider.getAll();
    }

    public Category getCurrentCategory() {
        return this.currentCategory;
    }

    public void setCurrentCategory(Category category) {
        Category oldCategory = this.currentCategory;
        this.currentCategory = category;

        for (Listener listener : this.listeners) listener.currentCategoryChanged(oldCategory, this.currentCategory);

        notifyListeners();
    }

    public void addCategory(Category category) {
        this.categoryProvider.add(category);

        for (Listener listener : this.listeners) {
            listener.categoryAdded(category);
        }

        notifyListeners();
    }

    public void removeCategory(Category category) {
        this.categoryProvider.remove(category);

        for (Listener listener : this.listeners) {
            listener.categoryRemoved(category);
        }

        notifyListeners();
    }

    public void updateCategory(Category category) {
        for (int i = 0; i < this.categoryProvider.getAll().length; i++) {
            if (this.categoryProvider.getAll()[i].getCategoryId().equals(category.getCategoryId())) {
                this.categoryProvider.getAll()[i] = category;
                break;
            }
        }

        for (Listener listener : this.listeners) {
            listener.categoryUpdated(category);
        }

        notifyListeners();
    }

    public void notifyListeners() {
        Category[] categories = getCategories();
        for (Listener listener : this.listeners) listener.categoriesUpdated(categories, this.currentCategory);
    }

    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }
}