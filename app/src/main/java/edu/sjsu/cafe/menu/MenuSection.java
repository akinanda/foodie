package edu.sjsu.cafe.menu;

/**
 * Created by Akshay on 12/7/2016.
 */

public class MenuSection {
    String sectionName;
    String imagePath;

    public MenuSection(String menuSection, String resource_id) {
        this.sectionName = menuSection;
        this.imagePath = resource_id;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
