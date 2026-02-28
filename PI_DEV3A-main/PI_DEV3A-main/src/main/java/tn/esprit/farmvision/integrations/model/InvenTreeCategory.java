package tn.esprit.farmvision.integrations.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Modèle représentant une catégorie dans InvenTree
 */
public class InvenTreeCategory {
    private int id;
    private String name;
    private String description;
    private int parent;
    private String parentName;
    private int level;
    private String path;
    private List<InvenTreeCategory> children;
    private int partsCount;
    private boolean active;

    // Constructeurs
    public InvenTreeCategory() {
        this.children = new ArrayList<>();
    }

    public InvenTreeCategory(int id, String name) {
        this.id = id;
        this.name = name;
        this.children = new ArrayList<>();
        this.active = true;
    }

    // Getters et setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getParent() { return parent; }
    public void setParent(int parent) { this.parent = parent; }

    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public List<InvenTreeCategory> getChildren() { return children; }
    public void setChildren(List<InvenTreeCategory> children) { this.children = children; }

    public int getPartsCount() { return partsCount; }
    public void setPartsCount(int partsCount) { this.partsCount = partsCount; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public void addChild(InvenTreeCategory child) {
        children.add(child);
    }

    public String getFullPath() {
        if (path != null && !path.isEmpty()) {
            return path;
        }
        if (parentName != null && !parentName.isEmpty()) {
            return parentName + " > " + name;
        }
        return name;
    }

    @Override
    public String toString() {
        return getFullPath();
    }
}